package com.se.DAO;

import com.se.entity.ClassInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClassInfoDAO {

    public void InsertClassInfo(ClassInfo classInfo, Connection conn) throws SQLException {
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

    public String getClassIDByProjectNameAndClassName(String projectName,String className,Connection conn) throws SQLException{
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
