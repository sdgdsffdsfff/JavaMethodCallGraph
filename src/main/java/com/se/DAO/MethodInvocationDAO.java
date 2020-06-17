package com.se.DAO;

import com.se.entity.Method;
import com.se.entity.MethodCall;
import com.se.entity.MethodInvocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MethodInvocationDAO {


    //todo:过滤掉JDK的方法调用，JDK的方法调用不入库
    public void saveMethodInvocation(String projectName,Map<String, MethodCall> methodCalls, Connection conn){
        String sql = null;
        PreparedStatement pst = null;
        MethodCall tempMethodCall = null;
        Method tempMethod = null;
        try{
            sql = "insert into methodinvocationinfo (projectName,callMethodName,calledMethodName,callClassName,calledClassName,callMethodParameters,callMethodReturnType) values(?,?,?,?,?,?,?)";
            if(methodCalls != null && !methodCalls.isEmpty()) {
                pst = conn.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                Collection<MethodCall> calls = methodCalls.values();
                for(MethodCall call : calls) {
                    if(call.getCalled() != null && !call.getCalled().isEmpty()){
                        for(Method calledMethod:call.getCalled()){
                            String calledClassName = calledMethod.getPackageAndClassName();
                            if(calledClassName.length()>100||calledClassName.length()<3)continue;
                            if(calledClassName.contains("{")||calledClassName.contains("}")||calledClassName.contains("(")
                                    ||calledClassName.contains(")"))continue;
                            pst.setString(1,projectName);
                            pst.setString(2,call.getCaller().getName());
                            pst.setString(3,calledMethod.getName());
                            pst.setString(4,call.getCaller().getPackageAndClassName());
                            pst.setString(5,calledMethod.getPackageAndClassName());
                            pst.setString(6,call.getCaller().getParamTypeList().toString());
                            pst.setString(7,call.getCaller().getReturnTypeStr());
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

    public List<String> getAllProjectNameFromDB(Connection conn) throws SQLException {
        List<String> projectNameList = new ArrayList<>();
        String sql = "select distinct projectName from methodinvocationinfo";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            projectNameList.add(resultSet.getString("projectName"));
        }
        return projectNameList;
    }

    public List<MethodInvocation> getMethodInvocationByProjectName(String projectName,Connection conn) throws SQLException {
        List<MethodInvocation> methodInvocationList = new ArrayList<>();
//        String sql = "select * from methodinvocationinfo where projectName = '" + projectName + "'";

        String sql = "select * from methodinvocationinfo where projectName = ?";

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

}
