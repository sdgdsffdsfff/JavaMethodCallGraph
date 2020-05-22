package com.se.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MethodInvocationTreeDAO {
    public void insertIntoMethodInvocationTree(String projectName, Connection conn) throws SQLException {
//        String sql = "INSERT IGNORE INTO methodinvocationtree SELECT ID, projectName, callMethodName, calledMethodName, callClassName, calledClassName, callMethodParameters, callMethodReturnType, callMethodID, calledMethodID, 0 FROM methodinvocationinview";

//        String newSql = "INSERT INTO methodinvocationtree(ID,projectName,callClassName,calledClassName,callMethodName,callMethodParameters, callMethodReturnType, callMethodID, calledMethodID) " +
//                "SELECT ID,projectName,callClassName,calledClassName,callMethodName,callMethodParameters, callMethodReturnType, callMethodID, calledMethodID " +
//                "FROM methodinvocationinview " +
//                "where projectName = '" +  projectName + "'";

        String sql = "INSERT INTO methodinvocationtree(ID,projectName,callClassName,calledClassName,callMethodName,callMethodParameters, callMethodReturnType, callMethodID, calledMethodID) " +
                "SELECT ID,projectName,callClassName,calledClassName,callMethodName,callMethodParameters, callMethodReturnType, callMethodID, calledMethodID " +
                "FROM methodinvocationinview " +
                "where projectName = ?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);
        pst.executeUpdate();
    }

    public void updateIsRecursive(String projectName, Connection conn) throws SQLException {
//        String sql = "UPDATE methodinvocationtree SET isRecursive = 1 WHERE callMethodID = calledMethodID AND projectName = '" + projectName + "'";
        String sql = "UPDATE methodinvocationtree SET isRecursive = 1 WHERE callMethodID = calledMethodID AND projectName = ?";

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);

        pst.executeUpdate();
    }

}
