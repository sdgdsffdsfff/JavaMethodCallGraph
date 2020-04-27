package com.se.process;

import com.se.DAO.BuildConnection;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.DAO.MethodInvocationInViewDAO;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilteMethodInvocation {

    public static void main(String[] args) throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        MethodInvocationDAO methodInvocationDAO = new MethodInvocationDAO();
        MethodInvocationInViewDAO methodInvocationInViewDAO = new MethodInvocationInViewDAO();
        List<String> projectNameList = methodInvocationDAO.getAllProjectNameFromDB(conn);
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
        for(String projectName:projectNameList){
            System.out.println("正在进行方法调用匹配的项目为:" + projectName);
            List<MethodInvocation> methodInvocationList = methodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);
            for(MethodInvocation methodInvocation:methodInvocationList){
                MethodInfo callMethodInfo = methodInfoDAO.getMethodInfoByNameClassReturnParameters(projectName,methodInvocation.getCallClassName(),methodInvocation.getCallMethodName(),methodInvocation.getCallMethodReturnType(),methodInvocation.getCallMethodParameters(),conn);
                List<MethodInfo> calledMethodInfoList = methodInfoDAO.getMethodInfoByNameAndClass(projectName,methodInvocation.getCalledClassName(),methodInvocation.getCalledMethodName(),conn);
                if(callMethodInfo!=null && calledMethodInfoList.size()>0){
                    MethodInfo calledMethodInfo = calledMethodInfoList.get(0);
                    MethodInvocationInView methodInvocationInView = new MethodInvocationInView();
                    methodInvocationInView.setProjectName(projectName);
                    methodInvocationInView.setCallClassName(callMethodInfo.getClassName());
                    methodInvocationInView.setCallMethodName(callMethodInfo.getMethodName());
                    methodInvocationInView.setCallMethodID(callMethodInfo.getID());
                    methodInvocationInView.setCallMethodParameters(callMethodInfo.getMethodParameters());
                    methodInvocationInView.setCallMethodReturnType(callMethodInfo.getReturnType());
                    methodInvocationInView.setCalledClassName(calledMethodInfo.getClassName());
                    methodInvocationInView.setCalledMethodName(calledMethodInfo.getMethodName());
                    methodInvocationInView.setCalledMethodID(calledMethodInfo.getID());
                    methodInvocationInViewList.add(methodInvocationInView);
                }
            }
            methodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViewList,conn);
        }
    }
}
