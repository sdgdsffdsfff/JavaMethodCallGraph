package com.se.DAO;
import com.se.entity.ClassInfo;
import org.checkerframework.checker.units.qual.C;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInfoDAO {


    public static Map<Integer,String> getClassInfoByProjectName(String projectName, Connection conn) throws SQLException {
        String sql = "select ID,className from classinfo where projectName = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);
        ResultSet resultSet = pst.executeQuery();
        Map<Integer,String> idMap = new HashMap<>();
        while(resultSet.next()){
            idMap.put(resultSet.getInt("ID"),resultSet.getString("className"));
        }
        return idMap;
    }

    public static Map<String,Integer> getClassInfoMapByProjectName(String projectName, Connection conn) throws SQLException {
        String sql = "select ID,className from classinfo where projectName = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);
        ResultSet resultSet = pst.executeQuery();
        Map<String,Integer> idMap = new HashMap<>();
        while(resultSet.next()){
            idMap.put(resultSet.getString("className"),resultSet.getInt("ID"));
        }
        return idMap;
    }

    public static List<String> getAllClassInfoList(String projectName, Connection conn) throws SQLException {
        String sql = "select * from classinfo where projectName = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, projectName);
        ResultSet resultSet = pst.executeQuery();
        List<String> classInfoList = new ArrayList<>();
        while(resultSet.next()){
            classInfoList.add(resultSet.getString("className"));
        }
        return classInfoList;
    }

    /**
     * 存储一个项目内的所有类
     * @param classInfoList
     */
    public synchronized static void saveClassInfoList(List<ClassInfo> classInfoList, Connection conn) throws SQLException{
        String sql = "insert into classinfo (projectName,className,isInterface,filePath) values (?,?,?,?)";
        if(classInfoList != null && !classInfoList.isEmpty()){
            PreparedStatement pst = conn.prepareStatement(sql);
            for(ClassInfo classInfo : classInfoList){
                //过滤过长的方法名，过滤匿名函数，过滤链式调用
                if(classInfo == null||classInfo.getClassName().length()>100 || classInfo.getClassName().contains("{")||classInfo.getClassName().contains("}")||classInfo.getClassName().contains("(")
                        ||classInfo.getClassName().contains(")"))continue;
                pst.setString(1,classInfo.getProjectName());
                pst.setString(2,classInfo.getClassName());
                pst.setString(3,classInfo.getInterface().toString());
                pst.setString(4,classInfo.getFilePath());
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
        }
    }

    public static void updateInvokeCounts(List<List<Integer>> invokeInfoList, Connection conn) throws SQLException {
        String sql = "UPDATE classinfo SET invokedCounts = ?,invokeCounts = ? WHERE ID = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        for(List<Integer> list:invokeInfoList){
            pst.setInt(1,list.get(0));
            pst.setInt(2,list.get(1));
            pst.setInt(3,list.get(2));
            pst.addBatch();
        }
        pst.executeBatch();
        pst.clearBatch();
    }

    public static void updateDefaultInvokeDept(Connection conn) throws SQLException {
        String sql = "update classinfo set invocationDept = '0' where invocationDept is null";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.executeUpdate();
    }


    public static void updateInvocationDept(Map<String,Integer> invocationDeptMap, Connection conn) throws SQLException {
        String sql = "UPDATE classinfo SET invocationDept = ? WHERE className = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        for(String className:invocationDeptMap.keySet()){
            pst.setInt(1,invocationDeptMap.get(className));
            pst.setString(2,className);
            pst.addBatch();
        }
        pst.executeBatch();
        pst.clearBatch();
    }

    public static String getClassIDByProjectNameAndClassName(String projectName,String className,Connection conn) throws SQLException{
        String sql = "select ID from classinfo where projectName = ? and className = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);
        pst.setString(2,className);
        ResultSet resultSet = pst.executeQuery();
        if(resultSet.next()){
            return resultSet.getString("ID");
        }
        return null;
    }

    public static List<String> getAllProjectNameFromDB(Connection conn) throws SQLException {
        List<String> projectNameList = new ArrayList<>();
        String sql = "select distinct projectName from classinfo";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            projectNameList.add(resultSet.getString("projectName"));
        }
        return projectNameList;
    }

    public static List<ClassInfo> getClassInfoByFilePath(String filePath, Connection connection) throws SQLException {
        List<ClassInfo> classInfoList = new ArrayList<>();
        String sql = "select ID,className from classinfo where filePath = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,filePath);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            ClassInfo classInfo = new ClassInfo();
            classInfo.setID(resultSet.getInt("ID"));
            classInfo.setClassName(resultSet.getString("className"));
            classInfoList.add(classInfo);
        }
        return classInfoList;
    }

    public static ClassInfo getClassInfoByClassName(String className, Connection connection) throws SQLException {
        String sql = "select ID,invokeCounts,invokedCounts from classinfo where className = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,className);
        ResultSet resultSet = preparedStatement.executeQuery();
        ClassInfo classInfo = new ClassInfo();
        while(resultSet.next()){
            classInfo.setID(resultSet.getInt("ID"));
            classInfo.setClassName(className);
            classInfo.setInvokeCounts(resultSet.getInt("invokeCounts"));
            classInfo.setInvokedCounts(resultSet.getInt("invokedCounts"));
        }
        return classInfo;
    }
}