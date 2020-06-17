package com.se.process;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.*;
import com.se.container.MethodCallContainer;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;
import com.se.utils.FileHandler;
import com.se.utils.FileHelper;
import com.se.visitors.MethodVisitor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.out;

public class Process {

    private static String sourceProjectPath = "C:\\Users\\Zero\\Desktop\\Pro";
    private static String projectName;
    public static String getProjectNameFromProjectPath(String projectPath)
    {
        return new File(projectPath).getName();
    }
    public static List<String> newProjectNameList = new ArrayList<>();
    public static List<String> oldProjectNameList = new ArrayList<>();

    public static void main(String[] args) throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        getMethodInvocation(conn);
        filterMethodInvocation(conn);
        filterMethodInvocationTree(conn);
    }

    /**
     * 将类信息、方法信息、方法调用信息存储到数据库中
     * @param conn
     * @throws SQLException
     */
    public static void getMethodInvocation(Connection conn) throws SQLException {
        //获取数据库中已有的项目名列表
        MethodInvocationDAO methodInvocationDAO = new MethodInvocationDAO();
        oldProjectNameList = methodInvocationDAO.getAllProjectNameFromDB(conn);

        File dir = new File(sourceProjectPath);
        LinkedList<String> folders = new LinkedList<>();
        FileHandler.getFolders(dir,folders);
        System.out.println("项目数为："+folders.size());
        for(String f:folders) {
            projectName = getProjectNameFromProjectPath(f);

            //数据库中已有的项目不进行检测
            if(oldProjectNameList!=null && oldProjectNameList.contains(projectName))
                continue;
            newProjectNameList.add(projectName);

            System.out.println("正在处理的项目为：" + f);
            for (String filePath : FileHelper.getSubFile(f, "java")) {
                out.println("正在处理的文件为：" + filePath);
                File file = new File(filePath);
                //获取方法调用
                processMethodCallTree(file,conn);
            }
            //存储方法调用
            methodInvocationDAO.saveMethodInvocation(projectName,MethodCallContainer.getContainer().getMethodCalls(),conn);
        }
        System.out.println("数据处理完成...");
    }

    /**
     * 使用JavaParser获取方法调用
     * @param file
     * @param conn
     */
    private static void processMethodCallTree(File file,Connection conn){
        MethodVisitor visitor = new MethodVisitor(projectName,file.getName(),conn);
        try{
            CompilationUnit cu = JavaParser.parse(file);
            visitor.visit(cu, null);
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    /**
     * 过滤方法调用
     * @param conn
     * @throws SQLException
     */
    public static void filterMethodInvocation(Connection conn) throws SQLException {
        MethodInvocationDAO methodInvocationDAO = new MethodInvocationDAO();
        MethodInvocationInViewDAO methodInvocationInViewDAO = new MethodInvocationInViewDAO();
        ClassInfoDAO classInfoDAO = new ClassInfoDAO();
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();

        for(String projectName : newProjectNameList){
            //数据库中已有的项目不进行检测
            if(oldProjectNameList != null && oldProjectNameList.contains(projectName)) continue;
            System.out.println("正在进行方法调用匹配的项目为:" + projectName);
            //根据项目名获取该项目中的所有方法调用
            List<MethodInvocation> methodInvocationList = methodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);

            for(MethodInvocation methodInvocation:methodInvocationList){

                MethodInfo callMethodInfo = methodInfoDAO.getMethodInfoByNameClassReturnParameters(projectName,methodInvocation.getCallClassName(),methodInvocation.getCallMethodName(),methodInvocation.getCallMethodReturnType(),methodInvocation.getCallMethodParameters(),conn);
                List<MethodInfo> calledMethodInfoList = methodInfoDAO.getMethodInfoByNameAndClass(projectName,methodInvocation.getCalledClassName(),methodInvocation.getCalledMethodName(),conn);
                if(calledMethodInfoList.size()==0) continue;
                String callClassID = classInfoDAO.getClassIDByProjectNameAndClassName(projectName,callMethodInfo.getClassName(),conn);
                String calledClassID = classInfoDAO.getClassIDByProjectNameAndClassName(projectName,calledMethodInfoList.get(0).getClassName(),conn);
                if(callClassID != null && calledClassID != null){
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
                    methodInvocationInView.setCallClassID(callClassID);
                    methodInvocationInView.setCalledClassID(calledClassID);
                    methodInvocationInViewList.add(methodInvocationInView);
                }
            }
            methodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViewList,conn);
        }
    }

    /**
     * 过滤方法调用树
     * @param conn
     * @throws SQLException
     */
    public static void filterMethodInvocationTree(Connection conn) throws SQLException {
        for(String projectName : newProjectNameList){
            //数据库中已有的项目不进行检测
            if(oldProjectNameList != null && oldProjectNameList.contains(projectName)) continue;

            MethodInvocationTreeDAO methodInvocationTreeDAO = new MethodInvocationTreeDAO();
            methodInvocationTreeDAO.insertIntoMethodInvocationTree(projectName, conn);
            methodInvocationTreeDAO.updateIsRecursive(projectName, conn);
        }

    }
}
