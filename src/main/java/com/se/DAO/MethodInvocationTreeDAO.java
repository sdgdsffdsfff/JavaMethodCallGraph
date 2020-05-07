package com.se.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MethodInvocationTreeDAO {
    public void insertIntoMethodInvocationTree(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO methodinvocationtree SELECT ID, projectName, callMethodName, calledMethodName, callClassName, calledClassName, callMethodParameters, callMethodReturnType, callMethodID, calledMethodID, 0 FROM methodinvocationtree";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.executeUpdate();
    }

    public void updateIsRecursive(Connection conn) throws SQLException {
        String sql = "UPDATE methodinvocationtree SET isRecursive = 1 WHERE callMethodID = calledMethodID";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.executeUpdate();
    }

}
