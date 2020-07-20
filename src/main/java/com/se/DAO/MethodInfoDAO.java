package com.se.DAO;

import com.se.entity.MethodInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodInfoDAO {

    public void saveMethodInfoList(List<MethodInfo> methodInfoList, Connection conn) throws SQLException{
        String sql = "insert into methodinfo (projectName,className,methodName,returnType,methodParameters,qualifiedName,methodContent,beginLine,endLine) values (?,?,?,?,?,?,?,?,?)";
        if(methodInfoList != null && !methodInfoList.isEmpty()){
            PreparedStatement pst = conn.prepareStatement(sql);
            for(MethodInfo methodInfo : methodInfoList){
                pst.setString(1,methodInfo.getProjectName());
                pst.setString(2,methodInfo.getClassName());
                pst.setString(3,methodInfo.getMethodName());
                pst.setString(4,methodInfo.getReturnType());
                pst.setString(5,methodInfo.getMethodParameters());
                pst.setString(6,methodInfo.getQualifiedName());
                pst.setString(7,methodInfo.getMethodContent());
                pst.setInt(8,methodInfo.getBeginLine());
                pst.setInt(9,methodInfo.getEndLine());
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
        }
    }

    public List<MethodInfo> getMethodInfoByNameAndClass(String projectName,String className,String methodName,Connection conn){
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

    public List<MethodInfo> getMethodIdListByClassName(String className,Connection connection) throws SQLException {
        String sql = "select ID,beginLine,endLine from methodinfo where className = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1,className);
        List<MethodInfo> methodInfos = new ArrayList<>();
        ResultSet resultSet = pst.executeQuery();
        while(resultSet.next()){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setID(resultSet.getString("ID"));
            methodInfo.setBeginLine(resultSet.getInt("beginLine"));
            methodInfo.setEndLine(resultSet.getInt("endLine"));
            methodInfos.add(methodInfo);
        }
        return methodInfos;
    }


    public List<MethodInfo> getMethodIdListByProjectName(String projectName,Connection connection) throws SQLException {
        String sql = "select ID,beginLine,endLine,className from methodinfo where projectName = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1,projectName);
        List<MethodInfo> methodInfos = new ArrayList<>();
        ResultSet resultSet = pst.executeQuery();
        while(resultSet.next()){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setID(resultSet.getString("ID"));
            methodInfo.setBeginLine(resultSet.getInt("beginLine"));
            methodInfo.setEndLine(resultSet.getInt("endLine"));
            methodInfo.setClassName(resultSet.getString("className"));
            methodInfos.add(methodInfo);
        }
        return methodInfos;
    }

    public void updateCloneId(Map<Integer,Integer> cloneIdMap, Connection connection) throws SQLException {
        String sql = "UPDATE methodinfo SET cloneId = ? where ID = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        for(Integer id:cloneIdMap.keySet()){
            pst.setInt(1,cloneIdMap.get(id));
            pst.setInt(2,id);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.clearBatch();
    }

    public MethodInfo getMethodInfoByNameClassReturnParameters(String projectName,String className,String methodName,String returnType,String methodParameters,Connection conn) throws SQLException {
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

    public MethodInfo getMethodInfoByCloneId(int cloneId, Connection connection) throws SQLException {
        String sql = "select ID,className,projectName from methodinfo where cloneId = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1,cloneId);
        ResultSet resultSet = preparedStatement.executeQuery();
        MethodInfo methodInfo = new MethodInfo();
        while(resultSet.next()){
            methodInfo.setID(String.valueOf(resultSet.getInt("ID")));
            methodInfo.setClassName(resultSet.getString("className"));
            methodInfo.setProjectName(resultSet.getString("projectName"));
        }
        return methodInfo;
    }

    public Map<Integer,MethodInfo> getAllMethodInfo(Connection connection) throws SQLException {
        String sql = "select ID,className,projectName,cloneId from methodinfo";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        Map<Integer,MethodInfo> methodInfoMap = new HashMap<>();
        while(resultSet.next()){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setID(String.valueOf(resultSet.getInt("ID")));
            methodInfo.setClassName(resultSet.getString("className"));
            methodInfo.setProjectName(resultSet.getString("projectName"));
            methodInfoMap.put(resultSet.getInt("cloneId"),methodInfo);
        }
        return methodInfoMap;
    }

    public List<MethodInfo> getMethodInfoListByProjectName(String projectName, Connection connection) throws SQLException {
        String sql = "select ID,methodName,className,qualifiedName from methodinfo where projectName = ?";
        List<MethodInfo> methodInfoList = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,projectName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setID(String.valueOf(resultSet.getInt("ID")));
            methodInfo.setClassName(resultSet.getString("className"));
            methodInfo.setMethodName(resultSet.getString("methodName"));
            methodInfo.setQualifiedName(resultSet.getString("qualifiedName"));
            methodInfoList.add(methodInfo);
        }
        return methodInfoList;
    }

    public void updateAsset(List<MethodInfo> methodInfoList, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        String sql = "UPDATE methodinfo SET asset = ?,cloneGroupId = ?,isSameProjectClone = ? WHERE ID = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        for(MethodInfo methodInfo:methodInfoList){
            if(methodInfo.isAsset())
                pst.setInt(1,1);
            else
                pst.setInt(1,0);
            pst.setInt(2,methodInfo.getCloneGroupId());
            pst.setInt(3,methodInfo.getIsSameProjectClone());
            pst.setString(4,methodInfo.getID());
            pst.addBatch();
        }
        pst.executeBatch();
        conn.commit();
        conn.setAutoCommit(true);
    }
}
