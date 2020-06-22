package com.se.DAO;

import com.se.entity.ClassAsset;
import com.se.entity.ClassInfo;

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
    public static void saveClassInfoList(List<ClassInfo> classInfoList, Connection conn) throws SQLException{
        String sql = "insert into classinfo (projectName,className,isInterface,fileName) values (?,?,?,?)";
        if(classInfoList != null && !classInfoList.isEmpty()){
            PreparedStatement pst = conn.prepareStatement(sql);
            for(ClassInfo classInfo : classInfoList){
                //过滤过长的方法名，过滤匿名函数，过滤链式调用
                if(classInfo.getClassName().length()>100 || classInfo.getClassName().contains("{")||classInfo.getClassName().contains("}")||classInfo.getClassName().contains("(")
                        ||classInfo.getClassName().contains(")"))return;
                pst.setString(1,classInfo.getProjectName());
                pst.setString(2,classInfo.getClassName());
                pst.setString(3,classInfo.getInterface().toString());
                pst.setString(4,classInfo.getFileName());
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

    public static void updateInvocationDept(String className, int invocationDept, Connection conn) throws SQLException {
        String sql = "UPDATE classinfo SET invocationDept = ? WHERE className = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1,invocationDept);
        pst.setString(2,className);
        pst.executeUpdate();
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

    public static List<ClassAsset> selectClassInfoByFilePath(String filePath, Connection conn) throws SQLException {
        String selectSQL = "select projectName,className,content from classinfo where filename = '" + filePath +"'";
        PreparedStatement stmt = conn.prepareStatement(selectSQL);
        ResultSet rs = stmt.executeQuery(selectSQL);
        if(rs!=null){
            List<ClassAsset> classAssetList = new ArrayList<>();
            while(rs.next()){
                ClassAsset classAsset = new ClassAsset();
                classAsset.setProjectName(rs.getString("projectname"));
                classAsset.setFilePath(filePath);
                classAsset.setContent(rs.getString("content"));
                classAssetList.add(classAsset);
            }
            return classAssetList;
        }
        return null;
    }

}