package com.se.process;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.*;
import com.se.config.DataConfig;
import com.se.container.ClassInfoContainer;
import com.se.container.MethodCallContainer;
import com.se.container.MethodInfoContainer;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;
import com.se.utils.FileHandler;
import com.se.utils.FileHelper;
import com.se.visitors.ClassVisitor;
import com.se.visitors.MethodVisitor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Process {

    private static String projectName;
    public static String getProjectNameFromProjectPath(String projectPath)
    {
        return new File(projectPath).getName();
    }
    public static List<String> newProjectNameList = new ArrayList<>();
    public static List<String> oldProjectNameList = new ArrayList<>();

    public static void main(String[] args) throws SQLException {
        long startTime = System.currentTimeMillis(); ///////

        //建立数据库连接
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        //分析源项目代码，抽取需要的信息
        getMethodInvocation(conn);


        long endTime1 = System.currentTimeMillis(); ///////


        //匹配方法调用关系
        filterMethodInvocation(conn);
        //根据配置信息决定时候需要统计调用次数和调用深度
        if(DataConfig.analyseInvocationCounts){
            CountInvocation.countInvocationCounts(conn);
            CountInvocation.countInvocationDept(conn);
        }


        long endTime2 = System.currentTimeMillis();  ///////


        System.out.println("程序运行时间1：" + (endTime1 - startTime) + "ms");  ///////
        System.out.println("程序运行时间2：" + (endTime2 - startTime) + "ms");  ///////
    }

    /**
     * 将类信息、方法信息、方法调用信息存储到数据库中
     * @param conn
     * @throws SQLException
     */
    public static void getMethodInvocation(Connection conn) throws SQLException {
        if(DataConfig.analyseSingleProject){
            System.out.println("对单个项目进行处理");
            projectName = getProjectNameFromProjectPath(DataConfig.sourceProjectPath);
            if(oldProjectNameList!=null && oldProjectNameList.contains(projectName)){
                System.out.println("数据库中已有该项目信息");
                return;
            }
            newProjectNameList.add(projectName);
            String f = DataConfig.sourceProjectPath;
            System.out.println("正在处理的项目为：" + f);
            for (String filePath : FileHelper.getSubFile(f, "java")) {
                File file = new File(filePath);
                //获取项目中所有类
                processClassInfo(file, conn);
            }

            //存储当前项目中的所有类
            ClassInfoDAO.saveClassInfoList(ClassInfoContainer.getContainer().getClassInfoList(), conn);
            //从获取该项目中的所有类
            List<String> classInfoList = ClassInfoDAO.getAllClassInfoList(projectName, conn);

            for (String filePath : FileHelper.getSubFile(f, "java")) {
                System.out.println("正在处理的文件为：" + filePath);
                File file = new File(filePath);
                //获取方法调用
                processMethodCallTree(file, classInfoList, conn);
            }

            //存储当前项目中的所有方法
            MethodInfoDAO.saveMethodInfoList(MethodInfoContainer.getContainer().getMethodInfoList(), conn);
            //存储当前项目中的所有方法调用
            MethodInvocationDAO.saveMethodInvocation(projectName, MethodCallContainer.getContainer().getMethodCalls(),conn);

            ClassInfoContainer.getContainer().clear();
            MethodInfoContainer.getContainer().clear();
        }else {
            System.out.println("对父目录中所有的项目进行处理");
            //获取数据库中已有的项目名列表
            oldProjectNameList = ClassInfoDAO.getAllProjectNameFromDB(conn);
            File dir = new File(DataConfig.sourceProjectParentPath);
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
                    File file = new File(filePath);
                    //存储类
                    processClassInfo(file, conn);
                }
                //存储当前项目中的所有类
                ClassInfoDAO.saveClassInfoList(ClassInfoContainer.getContainer().getClassInfoList(), conn);
                //从获取该项目中的所有类
                List<String> classInfoList = ClassInfoDAO.getAllClassInfoList(projectName, conn);

                for (String filePath : FileHelper.getSubFile(f, "java")) {
                    System.out.println("正在处理的文件为：" + filePath);
                    File file = new File(filePath);
                    //获取方法调用
                    processMethodCallTree(file, classInfoList, conn);
                }
                //存储当前项目中的所有方法
                MethodInfoDAO.saveMethodInfoList(MethodInfoContainer.getContainer().getMethodInfoList(), conn);
                //存储当前项目中的所有方法调用
                MethodInvocationDAO.saveMethodInvocation(projectName, MethodCallContainer.getContainer().getMethodCalls(),conn);

                ClassInfoContainer.getContainer().clear();
                MethodInfoContainer.getContainer().clear();
            }
        }
        System.out.println("数据处理完成...");
    }

    /**
     * 使用JavaParser获取项目中所有的类
     * @param file
     * @param conn
     */
    private static void processClassInfo(File file,Connection conn){
        ClassVisitor visitor = new ClassVisitor(projectName,file.getName(),conn);
        try{
            CompilationUnit cu = JavaParser.parse(file);
            visitor.visit(cu, null);
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    /**
     * 使用JavaParser获取方法调用
     * @param file
     * @param conn
     */
    private static void processMethodCallTree(File file, List<String> classInfoList, Connection conn){
        MethodVisitor visitor = new MethodVisitor(projectName,file.getName(), classInfoList, conn);
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
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
        for(String projectName : newProjectNameList){
            //数据库中已有的项目不进行检测
            if(oldProjectNameList != null && oldProjectNameList.contains(projectName)) continue;
            System.out.println("正在进行方法调用匹配的项目为:" + projectName);
            //根据项目名获取该项目中的所有方法调用
            List<MethodInvocation> methodInvocationList = methodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);
            for(MethodInvocation methodInvocation:methodInvocationList){
                MethodInfo callMethodInfo = MethodInfoDAO.getMethodInfoByNameClassReturnParameters(projectName,methodInvocation.getCallClassName(),methodInvocation.getCallMethodName(),methodInvocation.getCallMethodReturnType(),methodInvocation.getCallMethodParameters(),conn);
                List<MethodInfo> calledMethodInfoList = MethodInfoDAO.getMethodInfoByNameAndClass(projectName,methodInvocation.getCalledClassName(),methodInvocation.getCalledMethodName(),conn);
                if(calledMethodInfoList.size()==0) continue;
                String callClassID = ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName,callMethodInfo.getClassName(),conn);
                String calledClassID = ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName,calledMethodInfoList.get(0).getClassName(),conn);
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
            MethodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViewList,conn);
            MethodInvocationInViewDAO.updateIsRecursive(projectName,conn);
        }
    }

}