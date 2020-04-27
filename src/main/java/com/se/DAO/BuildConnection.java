package com.se.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BuildConnection {

    private String url = "jdbc:mysql://localhost:3306/methodinvocation?serverTimezone=UTC&useSSL=false";
    private String driver = "com.mysql.jdbc.Driver";
    private String user = "root";
    private String password = "15927029790";

    public Connection buildConnect(){
        Connection conn = null;
        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        }catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
