package com.se.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.se.config.DataConfig;

public class BuildConnection {

    private String url = DataConfig.url;
    private String driver = DataConfig.driver;
    private String user = DataConfig.user;
    private String password = DataConfig.password;

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
