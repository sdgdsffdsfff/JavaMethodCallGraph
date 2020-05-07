package com.se.process;

import com.se.DAO.BuildConnection;
import com.se.DAO.MethodInvocationTreeDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class FilterMethodInvocationTree {
    public static void main(String[] args) throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        MethodInvocationTreeDAO methodInvocationTreeDAO = new MethodInvocationTreeDAO();
        methodInvocationTreeDAO.insertIntoMethodInvocationTree(conn);
        methodInvocationTreeDAO.updateIsRecursive(conn);
    }
}
