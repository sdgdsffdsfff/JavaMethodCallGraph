package com.se.DAO;

import com.se.entity.MethodInvocationInView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MethodInvocationInViewDAO {

    public void insertMethodInvocationInView(List<MethodInvocationInView> methodInvocationInViewList, Connection conn) throws SQLException {
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
}
