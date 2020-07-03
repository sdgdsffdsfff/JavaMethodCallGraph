package com.se.process;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.config.DataConfig;
import com.se.container.ClassInfoContainer;
import com.se.container.MethodCallContainer;
import com.se.container.MethodInfoContainer;
import com.se.utils.FileHelper;
import com.se.utils.ListUtils;
import com.se.visitors.ClassVisitor;
import com.se.visitors.MethodVisitor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GetMethodInvocation implements Runnable {

    private static String projectName;
    private static String getProjectNameFromProjectPath(String projectPath)
    {
        return new File(projectPath).getName();
    }
    private static List<String> newProjectNameList = new ArrayList<>();
    private static List<String> oldProjectNameList = new ArrayList<>();
    private Connection connection;
    private List<String> folders;

    public GetMethodInvocation(List<String> folders, Connection connection){
        this.folders = folders;
        this.connection = connection;
    }

    /**
     * 将类信息、方法信息、方法调用信息存储到数据库中
     * @param conn
     * @throws SQLException
     */
    public void getMethodInvocation(List<String> projectNameList,Connection conn) throws SQLException {
        System.out.println("线程处理的项目数为：" + projectNameList.size());
        //获取数据库中已有的项目名列表
        oldProjectNameList = ClassInfoDAO.getAllProjectNameFromDB(conn);
        for(String f:folders) {
            projectName = getProjectNameFromProjectPath(f);
            //数据库中已有的项目不进行检测
//            if(oldProjectNameList!=null && oldProjectNameList.contains(projectName))
//                continue;
            newProjectNameList.add(projectName);
            System.out.println("正在处理的项目为：" + f);
            for (String filePath : FileHelper.getSubFile(f, "java")) {
                File file = new File(filePath);
                //存储类
                processClassInfo(file, conn);
            }
            //存储当前项目中的所有类
            ClassInfoDAO.saveClassInfoList(new ArrayList<>(ClassInfoContainer.getContainer().getClassInfoList()), conn);
            //从获取该项目中的所有类
            List<String> classInfoList = ClassInfoDAO.getAllClassInfoList(projectName, conn);

            for (String filePath : FileHelper.getSubFile(f, "java")) {
                if(filePath.contains("\\test")){
                    continue;
                }
                System.out.println("正在处理的文件为：" + filePath);
                File file = new File(filePath);
                //获取方法调用
                processMethodCallTree(file, classInfoList, conn);
            }
            //存储当前项目中的所有方法
            MethodInfoDAO.saveMethodInfoList(new ArrayList<>(MethodInfoContainer.getContainer().getMethodInfoList()), conn);
            //存储当前项目中的所有方法调用
            MethodInvocationDAO.saveMethodInvocation(projectName, new HashMap<>(MethodCallContainer.getContainer().getMethodCalls()),conn);
            ClassInfoContainer.getContainer().clear();
            MethodInfoContainer.getContainer().clear();
        }
        System.out.println("数据处理完成...");
    }

    /**
     * 使用JavaParser获取项目中所有的类
     * @param file
     * @param conn
     */
    private void processClassInfo(File file,Connection conn){
        ClassVisitor visitor = new ClassVisitor(projectName,file.getPath(),conn);
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
    private void processMethodCallTree(File file, List<String> classInfoList, Connection conn){
        MethodVisitor visitor = new MethodVisitor(projectName,file.getName(), classInfoList, conn);
        try{
            CompilationUnit cu = JavaParser.parse(file);
            visitor.visit(cu, null);
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            getMethodInvocation(folders,connection);
            //匹配方法调用关系
            //newProjectNameList = ClassInfoDAO.getAllProjectNameFromDB(conn);
            //将projectNameList等分为多份，同时用多个线程并行处理
            FilterMethodInvocation.filterMethodInvocation(this.connection,newProjectNameList);
            //根据配置信息决定是否需要统计调用次数和调用深度
            if(DataConfig.analyseInvocationCounts){
                CountInvocation.countInvokeCounts(this.connection);
                CountInvocation.countInvocationDept(this.connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
