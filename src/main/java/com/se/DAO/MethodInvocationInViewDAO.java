package com.se.DAO;

import com.se.entity.MethodInvocationInView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MethodInvocationInViewDAO {

    public static void insertMethodInvocationInView(List<MethodInvocationInView> methodInvocationInViewList, Connection conn) throws SQLException {
        String sql = "insert into methodinvocationinview (projectName,callMethodName,calledMethodName,callClassName,calledClassName,callMethodParameters,callMethodReturnType,callMethodID,calledMethodID,callClassID,calledClassID) values(?,?,?,?,?,?,?,?,?,?,?)";
        if(methodInvocationInViewList != null && !methodInvocationInViewList.isEmpty()) {
            PreparedStatement pst = conn.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
            for(MethodInvocationInView methodInvocationInView : methodInvocationInViewList) {
                pst.setString(1,methodInvocationInView.getProjectName());
                pst.setString(2,methodInvocationInView.getCallMethodName());
                pst.setString(3,methodInvocationInView.getCalledMethodName());
                pst.setString(4,methodInvocationInView.getCallClassName());
                pst.setString(5,methodInvocationInView.getCalledClassName());
                pst.setString(6,methodInvocationInView.getCallMethodParameters());
                pst.setString(7,methodInvocationInView.getCallMethodReturnType());
                pst.setString(8,methodInvocationInView.getCallMethodID());
                pst.setString(9,methodInvocationInView.getCalledMethodID());
                pst.setString(10,methodInvocationInView.getCallClassID());
                pst.setString(11,methodInvocationInView.getCalledClassID());
                pst.addBatch();
            }
            pst.executeBatch();
        }
    }

    public static int selectCalledCountsByClassName(String className, Connection conn) throws SQLException {
        String sql = "select ID from methodinvocationinview where calledClassName = '" + className + "'";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet resultSet = pst.executeQuery();
        int count = 0;
        while(resultSet.next()){
            count++;
        }
        return count;
    }

    public static List<String> selectAllProjectName(Connection conn) throws SQLException {
        String sql = "select distinct projectName from methodinvocationinview";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet resultSet = pst.executeQuery();
        List<String> projectNameList = new ArrayList<>();
        while(resultSet.next()){
            projectNameList.add(resultSet.getString("projectName"));
        }
        return projectNameList;
    }

    public static List<MethodInvocationInView> getMethodInvocationInViewByProjectName(String projectName, Connection conn) throws SQLException {
        String sql = "select * from methodinvocationinview where projectName = '" + projectName + "'";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet resultSet = pst.executeQuery();
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
        while(resultSet.next()){
            MethodInvocationInView methodInvocationInView = new MethodInvocationInView();
            methodInvocationInView.setCallClassName(resultSet.getString("callClassName"));
            methodInvocationInView.setCalledClassName(resultSet.getString("calledClassName"));
            methodInvocationInView.setCallMethodName(resultSet.getString("callMethodName"));
            methodInvocationInView.setCalledMethodName(resultSet.getString("calledMethodName"));
            methodInvocationInView.setProjectName(projectName);
            methodInvocationInViewList.add(methodInvocationInView);
        }
        return methodInvocationInViewList;
    }

}
