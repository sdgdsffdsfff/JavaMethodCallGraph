package com.se.DAO;

import com.se.entity.MethodInvocationInView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MethodInvocationInViewDAO {

    public static void insertMethodInvocationInView(List<MethodInvocationInView> methodInvocationInViewList, Connection conn) throws SQLException {
        String sql = "insert into methodinvocationinview (projectName,callMethodName,calledMethodName,callClassName,calledClassName,callMethodParameters,callMethodReturnType,callMethodID,calledMethodID,callClassID,calledClassID, create_time, update_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Date currentDate = new Date();
        java.sql.Date currentDateInSql = new java.sql.Date(currentDate.getTime());
        if(methodInvocationInViewList != null && !methodInvocationInViewList.isEmpty()) {
            PreparedStatement pst = conn.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
            for(MethodInvocationInView methodInvocationInView : methodInvocationInViewList) {
                if(methodInvocationInView.getCallClassID() == null||methodInvocationInView.getCallClassID().equals("null")||methodInvocationInView.getCalledClassID() == null||methodInvocationInView.getCalledClassID().equals("null"))continue;
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
                pst.setDate(12, currentDateInSql);
                pst.setDate(13, currentDateInSql);
                pst.addBatch();
            }
            pst.executeBatch();
        }
    }

    public static int selectCalledCountsByClassName(String className, Connection conn) throws SQLException {
        String sql = "select ID from methodinvocationinview where calledClassName = '" + className + "' and callClassName != calledClassName";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet resultSet = pst.executeQuery();
        int count = 0;
        while(resultSet.next()){
            count++;
        }
        return count;
    }

    public static int selectCallCountsByClassName(String className, Connection conn) throws SQLException {
        String sql = "select ID from methodinvocationinview where callClassName = '" + className + "' and callClassName != calledClassName";
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


    public static List<MethodInvocationInView> getInfoByProjectName(String projectName, Connection conn) throws SQLException {
        String sql = "select callClassName,calledClassName from methodinvocationinview where projectName = '" + projectName + "'";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet resultSet = pst.executeQuery();
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
        while(resultSet.next()){
            MethodInvocationInView methodInvocationInView = new MethodInvocationInView();
            methodInvocationInView.setCallClassName(resultSet.getString("callClassName"));
            methodInvocationInView.setCalledClassName(resultSet.getString("calledClassName"));
            methodInvocationInView.setProjectName(projectName);
            methodInvocationInViewList.add(methodInvocationInView);
        }
        return methodInvocationInViewList;
    }

    public static List<MethodInvocationInView> getInvokeInfoByProjectName(String projectName, Connection conn) throws SQLException {
        String sql = "select callClassName,calledClassName,callMethodName,calledMethodName from methodinvocationinview where projectName = '" + projectName + "'";
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


    public static void updateIsRecursive(String projectName, Connection conn) throws SQLException {
        String sql = "UPDATE methodinvocationinview SET isRecursive = 1 WHERE callMethodID = calledMethodID AND projectName = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1,projectName);
        pst.executeUpdate();
    }

}
