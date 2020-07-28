package com.se.DAO;
import com.se.entity.ClassInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    public static List<ClassInfo> getClassListByProjectName(String projectName,Connection connection) throws SQLException{
        String sql = "select className,filePath from classinfo where projectName = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1,projectName);
        ResultSet resultSet = pst.executeQuery();
        List<ClassInfo> classInfoList = new ArrayList<>();
        while(resultSet.next()){
            ClassInfo classInfo = new ClassInfo();
            classInfo.setProjectName(projectName);
            classInfo.setClassName(resultSet.getString("className"));
            classInfo.setFilePath(resultSet.getString("filePath"));
            classInfoList.add(classInfo);
        }
        return classInfoList;
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
        String sql = "insert into classinfo (projectName,className,isInterface,filePath, create_time, update_time) values (?,?,?,?,?,?)";
        Date currentDate = new Date();
        java.sql.Date currentDateInSql = new java.sql.Date(currentDate.getTime());
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
                pst.setDate(5, currentDateInSql);
                pst.setDate(6, currentDateInSql);
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

    public static ClassInfo getClassInfoByFilePath(String projectName, String filePath, Connection connection) throws SQLException {
        List<ClassInfo> classInfoList = new ArrayList<>();
        String sql = "select ID,className from classinfo where projectName = ? and filePath = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,projectName);
        preparedStatement.setString(2,filePath);
        ResultSet resultSet = preparedStatement.executeQuery();
        ClassInfo classInfo = new ClassInfo();
        while(resultSet.next()){
            classInfo.setID(resultSet.getInt("ID"));
            classInfo.setClassName(resultSet.getString("className"));
            classInfoList.add(classInfo);
        }
        return classInfo;
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


    public static Map<String,List<Integer>> getAllClassInvokeInfo(Connection connection) throws SQLException {
        String sql = "select className,invokeCounts,invokedCounts from classinfo";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        Map<String,List<Integer>> classInvokeMap = new HashMap<>();
        while(resultSet.next()){
            List<Integer> integerList = new ArrayList<>();
            integerList.add(resultSet.getInt("invokeCounts"));
            integerList.add(resultSet.getInt("invokedCounts"));
            classInvokeMap.put(resultSet.getString("className"),integerList);
        }
        return classInvokeMap;
    }


    public static List<Integer> getDiscardClassId(Connection connection) throws SQLException {
        String sql = "select ID from classinfo where invocationDept = 0 and invokedCounts = 0 and invokeCounts = 0";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Integer> discardClassIDList = new ArrayList<>();
        while(resultSet.next()){
            discardClassIDList.add(resultSet.getInt("ID"));
        }
        return discardClassIDList;
    }

    public static double getAvgInvokeCounts(Connection connection) throws SQLException {
        String sql = "select AVG(classinfo.invokeCounts) as avgInvokeCounts from classinfo where invokeCounts!=0";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        double avgInvokeCounts = 0;
        while(resultSet.next()){
            avgInvokeCounts = resultSet.getDouble("avgInvokeCounts");
        }
        return avgInvokeCounts;
    }

    public static double getAvgInvokedCounts(Connection connection) throws SQLException {
        String sql = "select AVG(classinfo.invokedCounts) as avgInvokedCounts from classinfo where invokedCounts!=0";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        double avgInvokedCounts = 0;
        while(resultSet.next()){
            avgInvokedCounts = resultSet.getDouble("avgInvokedCounts");
        }
        return avgInvokedCounts;
    }

    public static List<Integer> getUniversalClassId(int avgInvokeCounts,int avgInvokedCounts,Connection connection) throws SQLException {
        String sql = "select ID from classinfo where invokedCounts > ? and invokeCounts > ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1,avgInvokeCounts);
        preparedStatement.setInt(2,avgInvokedCounts);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Integer> universalClassIDList = new ArrayList<>();
        while(resultSet.next()){
            universalClassIDList.add(resultSet.getInt("ID"));
        }
        return universalClassIDList;
    }

    public static List<String> getFilePathListByProjectName(String projectName, Connection connection) throws SQLException{
        String sql = "select distinct filePath from classinfo where projectName = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1,projectName);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<String> filePathList = new ArrayList<>();
        while(resultSet.next()){
            filePathList.add(resultSet.getString("filePath"));
        }
        return filePathList;

    }

    public static void deleteClassInfoRecords(List<ClassInfo> deleteClassInfos, Connection conn) throws SQLException{
//        conn.setAutoCommit(false);
        String cInfoSQL = "delete from classinfo where ID = ?";

        PreparedStatement pst = conn.prepareStatement(cInfoSQL);

        if(deleteClassInfos != null){
            for(ClassInfo classInfo : deleteClassInfos){
                pst.setInt(1, classInfo.getID());
                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
//            conn.commit();
        }

    }
}