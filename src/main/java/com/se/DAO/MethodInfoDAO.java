package com.se.DAO;

import com.se.entity.MethodInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MethodInfoDAO {

    public static void InsertMethodInfo(MethodInfo methodInfo, Connection conn) throws SQLException {
        String sql = "insert into methodinfo (projectName,className,methodName,returnType,methodParameters,qualifiedName) values (?,?,?,?,?,?)";
        if(methodInfo!=null){
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1,methodInfo.getProjectName());
            pst.setString(2,methodInfo.getClassName());
            pst.setString(3,methodInfo.getMethodName());
            pst.setString(4,methodInfo.getReturnType());
            pst.setString(5,methodInfo.getMethodParameters());
            pst.setString(6,methodInfo.getQualifiedName());
            pst.executeUpdate();
        }
    }

    public static void saveMethodInfoList(List<MethodInfo> methodInfoList, Connection conn) throws SQLException{
        String sql = "insert into methodinfo (projectName,className,methodName,returnType,methodParameters,qualifiedName) values (?,?,?,?,?,?)";
        if(methodInfoList != null && !methodInfoList.isEmpty()){
            PreparedStatement pst = conn.prepareStatement(sql);
            for(MethodInfo methodInfo : methodInfoList){
                pst.setString(1,methodInfo.getProjectName());
                pst.setString(2,methodInfo.getClassName());
                pst.setString(3,methodInfo.getMethodName());
                pst.setString(4,methodInfo.getReturnType());
                pst.setString(5,methodInfo.getMethodParameters());
                pst.setString(6,methodInfo.getQualifiedName());
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();

        }
    }

    public static List<MethodInfo> getMethodInfoByNameAndClass(String projectName,String className,String methodName,Connection conn){
//        String sql = "select * from methodinfo where projectName = '" + projectName +"'and className = '" + className + "'and methodName = '" + methodName + "'";

        String sql = "select * from methodinfo where projectName = ? and className = ? and methodName = ?";

        List<MethodInfo> methodInfoList = new ArrayList<>();
        ResultSet resultSet = null;
        try{
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1,projectName);
            pst.setString(2,className);
            pst.setString(3,methodName);


            resultSet = pst.executeQuery();
            while(resultSet.next()){
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setProjectName(projectName);
                methodInfo.setClassName(className);
                methodInfo.setMethodName(methodName);
                methodInfo.setQualifiedName(resultSet.getString("qualifiedName"));
                methodInfo.setReturnType(resultSet.getString("returnType"));
                methodInfo.setMethodParameters(resultSet.getString("methodParameters"));
                methodInfo.setID(resultSet.getString("ID"));
                methodInfoList.add(methodInfo);
            }
        }catch (Exception e){
            System.out.println(sql);
            e.printStackTrace();
        }
        return methodInfoList;
    }

    public static MethodInfo getMethodInfoByNameClassReturnParameters(String projectName,String className,String methodName,String returnType,String methodParameters,Connection conn) throws SQLException {
//        String sql = "select * from methodinfo where projectName = '" + projectName +"'and className = '" + className + "'and methodName = '" + methodName + "'and returnType = '" + returnType + "'and methodParameters = '" + methodParameters +"'";

        String sql = "select * from methodinfo where projectName = ? and className = ? and methodName = ? and returnType = ? and methodParameters = ?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);
        pst.setString(2,className);
        pst.setString(3,methodName);
        pst.setString(4,returnType);
        pst.setString(5,methodParameters);

        ResultSet resultSet = pst.executeQuery();
        while(resultSet.next()){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setProjectName(projectName);
            methodInfo.setClassName(className);
            methodInfo.setMethodName(methodName);
            methodInfo.setQualifiedName(resultSet.getString("qualifiedName"));
            methodInfo.setReturnType(resultSet.getString("returnType"));
            methodInfo.setMethodParameters(resultSet.getString("methodParameters"));
            methodInfo.setID(resultSet.getString("ID"));
            return methodInfo;
        }
        return null;
    }
}
