package com.se.process;

import com.se.DAO.BuildConnection;
import com.se.DAO.MethodInvocationTreeDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class FilterMethodInvocationTree {
    public static void main(String[] args) throws SQLException {
        String projectName = "ProgramModelTest";
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        MethodInvocationTreeDAO methodInvocationTreeDAO = new MethodInvocationTreeDAO();
        methodInvocationTreeDAO.insertIntoMethodInvocationTree(projectName,conn);
        methodInvocationTreeDAO.updateIsRecursive(projectName,conn);
    }
}
