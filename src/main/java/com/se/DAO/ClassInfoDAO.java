package com.se.DAO;

import com.se.entity.ClassInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ClassInfoDAO {


    public static Map<Integer,String> getAllClassInfo(Connection conn) throws SQLException {
        String sql = "select * from classinfo";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet resultSet = pst.executeQuery();
        Map<Integer,String> idMap = new HashMap<>();
        while(resultSet.next()){
            idMap.put(resultSet.getInt("ID"),resultSet.getString("className"));
        }
        return idMap;
    }

    public static void InsertClassInfo(ClassInfo classInfo, Connection conn) throws SQLException {
        //过滤过长的方法名，过滤匿名函数，过滤链式调用
        if(classInfo.getClassName().length()>100)return;
        if(classInfo.getClassName().contains("{")||classInfo.getClassName().contains("}")||classInfo.getClassName().contains("(")
                ||classInfo.getClassName().contains(")"))return;
        String sql = "insert into classinfo (projectName,className,isInterface,fileName) values (?,?,?,?)";
        if(classInfo!=null){
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1,classInfo.getProjectName());
            pst.setString(2,classInfo.getClassName());
            pst.setString(3,classInfo.getInterface().toString());
            pst.setString(4,classInfo.getFileName());
            pst.executeUpdate();
        }
    }

    public static void updateInvocationCounts(int id, int invocationCounts, Connection conn) throws SQLException {
        String sql = "UPDATE classinfo SET invocationCounts = ? WHERE ID = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1,invocationCounts);
        pst.setInt(2,id);
        pst.executeUpdate();
    }

    public static void updateInvocationDept(String className, int invocationDept, Connection conn) throws SQLException {
        String sql = "UPDATE classinfo SET invocationDept = ? WHERE className = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1,invocationDept);
        pst.setString(2,className);
        pst.executeUpdate();
    }

    public static String getClassIDByProjectNameAndClassName(String projectName,String className,Connection conn) throws SQLException{
//        String sql = "select ID from classinfo where projectName = '" + projectName +"'and className = '" + className +"'";
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
}
