package com.se.DAO;

import com.se.entity.Method;
import com.se.entity.MethodCall;
import com.se.entity.MethodInvocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MethodInvocationDAO {


    //todo:过滤掉JDK的方法调用，JDK的方法调用不入库
    public synchronized static void saveMethodInvocation(String projectName,Map<String, MethodCall> methodCalls, Connection conn){
        System.out.println("保存到数据库的项目名为：" + projectName);
        String sql = null;
        PreparedStatement pst = null;
        MethodCall tempMethodCall = null;
        Method tempMethod = null;
        Date currentDate = new Date();
        java.sql.Date currentDateInSql = new java.sql.Date(currentDate.getTime());
        try{
            sql = "insert into methodinvocationinfo (projectName,callMethodName,calledMethodName,callClassName,calledClassName,callMethodParameters,callMethodReturnType, create_time, update_time) values(?,?,?,?,?,?,?,?,?)";
            if(methodCalls != null && !methodCalls.isEmpty()) {
                pst = conn.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                Collection<MethodCall> calls = methodCalls.values();
                for(MethodCall call : calls) {
                    if(call.getCalled() != null && !call.getCalled().isEmpty()){
                        for(Method calledMethod:call.getCalled()){
                            String callClassName = call.getCaller().getPackageAndClassName();
                            String calledClassName = calledMethod.getPackageAndClassName();
                            if(calledClassName.length()>100||calledClassName.length()<3)continue;
                            if(calledClassName.contains("{")||calledClassName.contains("}")||calledClassName.contains("(")
                                    ||calledClassName.contains(")"))continue;
                            if(calledClassName.startsWith("java")||!calledClassName.contains(".")||!callClassName.substring(0,callClassName.indexOf(".")).equals(calledClassName.substring(0,calledClassName.indexOf(".")))){
                                continue;
                            }
                            pst.setString(1,projectName);
                            pst.setString(2,call.getCaller().getName());
                            pst.setString(3,calledMethod.getName());
                            pst.setString(4,call.getCaller().getPackageAndClassName());
                            pst.setString(5,calledMethod.getPackageAndClassName());
                            pst.setString(6,call.getCaller().getParamTypeList().toString());
                            pst.setString(7,call.getCaller().getReturnTypeStr());
                            pst.setDate(8, currentDateInSql);
                            pst.setDate(9, currentDateInSql);
                            pst.addBatch();

                            tempMethodCall = call;
                            tempMethod = calledMethod;
                        }
                        pst.executeBatch();
                    }
                }
            }
        } catch (SQLException e){
            System.out.println(sql);
            System.out.println(pst.toString());
            System.out.println(tempMethodCall.getCaller().getPackageAndClassName());
            System.out.println(tempMethod);
            e.printStackTrace();
        }

    }

    public static List<String> getAllProjectNameFromDB(Connection conn) throws SQLException {
        List<String> projectNameList = new ArrayList<>();
        String sql = "select distinct projectName from methodinvocationinfo where is_delete = 0";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            projectNameList.add(resultSet.getString("projectName"));
        }
        return projectNameList;
    }

    public static List<MethodInvocation> getMethodInvocationByProjectName(String projectName,Connection conn) throws SQLException {
        List<MethodInvocation> methodInvocationList = new ArrayList<>();
//        String sql = "select * from methodinvocationinfo where projectName = '" + projectName + "'";

        String sql = "select * from methodinvocationinfo where projectName = ? and is_delete = 0";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, projectName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            MethodInvocation methodInvocation = new MethodInvocation();
            methodInvocation.setProjectName(projectName);
            methodInvocation.setCallClassName(resultSet.getString("callClassName"));
            methodInvocation.setCalledClassName(resultSet.getString("calledClassName"));
            methodInvocation.setCallMethodName(resultSet.getString("callMethodName"));
            methodInvocation.setCalledMethodName(resultSet.getString("calledMethodName"));
            methodInvocation.setCallMethodReturnType(resultSet.getString("callMethodReturnType"));
            methodInvocation.setCallMethodParameters(resultSet.getString("callMethodParameters"));
            methodInvocationList.add(methodInvocation);
        }
        return methodInvocationList;
    }

    public static List<MethodInvocation> getMethodInvocationByProjectNameAndDate(String projectName,Connection conn) throws SQLException {
        List<MethodInvocation> methodInvocationList = new ArrayList<>();
        Date currentDate = new Date();
        java.sql.Date currentDateInSql = new java.sql.Date(currentDate.getTime());
        String sql = "select * from methodinvocationinfo where projectName = ? and create_time = ? and is_delete = 0 ";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, projectName);
        preparedStatement.setDate(2, currentDateInSql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            MethodInvocation methodInvocation = new MethodInvocation();
            methodInvocation.setProjectName(projectName);
            methodInvocation.setCallClassName(resultSet.getString("callClassName"));
            methodInvocation.setCalledClassName(resultSet.getString("calledClassName"));
            methodInvocation.setCallMethodName(resultSet.getString("callMethodName"));
            methodInvocation.setCalledMethodName(resultSet.getString("calledMethodName"));
            methodInvocation.setCallMethodReturnType(resultSet.getString("callMethodReturnType"));
            methodInvocation.setCallMethodParameters(resultSet.getString("callMethodParameters"));
            methodInvocationList.add(methodInvocation);
        }
        return methodInvocationList;
    }


    public static Set<String> getDistinctClassName(Connection connection) throws SQLException{
        Set<String> classNameSet = new HashSet<>();
        String sql = "select callClassName,calledClassName from methodinvocationinfo where is_delete = 0";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            classNameSet.add(resultSet.getString("callClassName"));
            classNameSet.add(resultSet.getString("calledClassName"));
        }
        return classNameSet;
    }

    public static List<String> getMethodInvocationIDsByClassName(String projectName, String callClassName, Connection conn) throws SQLException {
        List<String> methodInvocationIDList = new ArrayList<>();
//        String sql = "select * from methodinvocationinfo where projectName = '" + projectName + "'";
        String sql = "select * from methodinvocationinfo where projectName = ? and callClassName = ? and is_delete = 0";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, projectName);
        preparedStatement.setString(2, callClassName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            methodInvocationIDList.add(resultSet.getString("ID"));
        }
        return methodInvocationIDList;
    }


    public static void deleteMethodInvocationInfoRecords(List<String> deleteMethodInvocationIDs, Connection conn) throws SQLException{
        Date currentDate = new Date();
        java.sql.Date currentDateInSql = new java.sql.Date(currentDate.getTime());
        String mInvocInfoSQL = "update methodinvocationinfo set is_delete = 1, update_time = ? where ID = ?";

        PreparedStatement pst = conn.prepareStatement(mInvocInfoSQL);

        if(deleteMethodInvocationIDs != null){
            for(String methodInvocationID : deleteMethodInvocationIDs){
                pst.setDate(1, currentDateInSql);
                pst.setString(2, methodInvocationID);
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
//            conn.commit();
        }
    }
}
