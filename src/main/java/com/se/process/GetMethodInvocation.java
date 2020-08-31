package com.se.process;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.config.DataConfig;
import com.se.container.MethodCallContainer;
import com.se.container.MethodInfoContainer;
import com.se.entity.ClassInfo;
import com.se.utils.FileHelper;
import com.se.visitors.ClassVisitor;
import com.se.visitors.LayerVisitor;
import com.se.visitors.MethodVisitor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetMethodInvocation implements Runnable {

    private String projectName;

    private static String getProjectNameFromProjectPath(String projectPath)
    {
        return new File(projectPath).getName();
    }
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
        for(String f:folders) {
            int fileSize = 0;
            projectName = getProjectNameFromProjectPath(f);
            System.out.println("正在处理的项目为：" + f);
            List<ClassInfo> classInfosContainer = new ArrayList<>();
            for (String filePath : FileHelper.getSubFile(f, "java")) {
                if(filePath.contains("\\test")){
                    continue;
                }
                File file = new File(filePath);
                //存储类
                processClassInfo(file, classInfosContainer);
            }
            //存储当前项目中的所有类
            ClassInfoDAO.saveClassInfoList(classInfosContainer, conn);
            //从获取该项目中的所有类
            List<String> classInfoList = ClassInfoDAO.getAllClassInfoList(projectName, conn);
            for (String filePath : FileHelper.getSubFile(f, "java")) {
                if(filePath.contains("\\test")){
                    continue;
                }
                //System.out.println("正在处理的文件为：" + filePath);
                fileSize++;
                File file = new File(filePath);
                //获取方法调用
                processMethodCallTree(file, classInfoList);
            }
            //存储当前项目中的所有方法
            MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
            methodInfoDAO.saveMethodInfoList(MethodInfoContainer.getContainer().getMethodInfoListByProjectName(projectName), conn);
            //存储当前项目中的所有方法调用
            MethodInvocationDAO.saveMethodInvocation(projectName, MethodCallContainer.getContainer().getMethodCallsByProjectName(projectName),conn);
            // 更新calledClassFilePath
            MethodInvocationDAO.updateCalledClassFilePath(projectName, conn);
            MethodInfoContainer.getContainer().clearMethodInfoListByProjectName(projectName);
            MethodCallContainer.getContainer().clearMethodCallByProjectName(projectName);
            System.out.println("项目中的文件数为：" + fileSize);
        }
        System.out.println("数据处理完成...");
    }

    /**
     * 使用JavaParser获取项目中所有的类
     * @param file
     */
    private void processClassInfo(File file,List<ClassInfo> classInfos){
        ClassVisitor visitor = new ClassVisitor(projectName,file.getPath());
        try{
            CompilationUnit cu = StaticJavaParser.parse(file);
            visitor.visit(cu, null);
            if(DataConfig.isLayerProcess){
                List<ClassInfo>  classInfoList = visitor.getClassInfoList();
                for(ClassInfo classInfo : classInfoList){
                    classInfo.setLayer(LayerVisitor.splitLayer(cu));
                }
            }
        }catch (Exception ex){
            //ex.printStackTrace();
        }
        classInfos.addAll(visitor.getClassInfoList());
    }

    /**
     * 使用JavaParser获取方法调用
     * @param file
     */
    private void processMethodCallTree(File file, List<String> classInfoList){
//        MethodVisitor visitor = new MethodVisitor(projectName, classInfoList);
        MethodVisitor visitor = new MethodVisitor(projectName, classInfoList, file.getAbsolutePath());
        try{
            CompilationUnit cu = StaticJavaParser.parse(file);
            visitor.visit(cu, null);
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            List<String> oldProjectNameList = ClassInfoDAO.getAllProjectNameFromDB(connection);
            List<String> newProjectNameList = new ArrayList<>();
            List<String> newFolders = new ArrayList<>();
            //过滤数据库中已有项目
            for(String folder:folders){
                String name = getProjectNameFromProjectPath(folder);
                if(!oldProjectNameList.contains(name)){
                    newFolders.add(folder);
                    newProjectNameList.add(name);
                }
            }
            this.folders = newFolders;
            getMethodInvocation(newProjectNameList,connection);
            //匹配方法调用关系
            FilterMethodInvocation.doFilter(this.connection,newProjectNameList);
            //根据配置信息决定是否需要统计调用次数和调用深度
            if(DataConfig.analyseInvocationCounts){
                CountInvocation.countInvokeCounts(newProjectNameList,this.connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}