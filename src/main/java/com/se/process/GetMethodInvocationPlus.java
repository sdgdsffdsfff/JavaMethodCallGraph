package com.se.process;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.DAO.MethodInvocationInViewDAO;
import com.se.config.DataConfig;
import com.se.container.MethodCallContainer;
import com.se.container.MethodInfoContainer;
import com.se.entity.*;
import com.se.visitors.ClassVisitor;
import com.se.visitors.MethodVisitor;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetMethodInvocationPlus implements Runnable {

    private String projectName;
    private Connection connection;
    private List<String> projectFolders;    //用于增量扫描的项目根目录List
    private Map<String, List<String>> modifiedFileMap;  //项目中修改的文件路径

    /**
     *
     * @param projectFolders 用于增量扫描的项目根目录List
     * @param modifiedFileMap key:项目根目录 value:修改的文件路径List
     * @param connection
     */
    public GetMethodInvocationPlus(List<String> projectFolders, Map<String, List<String>> modifiedFileMap, Connection connection){
        this.projectFolders = projectFolders;
        this.modifiedFileMap = modifiedFileMap;
        this.connection = connection;
    }

    private static String getProjectNameFromProjectPath(String projectPath) {
        return new File(projectPath).getName();
    }

    /**
     * 处理删除的文件
     * @param projectName
     * @param deleteFilePaths
     * @param conn
     * @throws SQLException
     */
    public void processDeletedFile(String projectName, List<String> deleteFilePaths, Connection conn) throws SQLException{
        //获取要被删除的ClassInfo(ID, className)
        List<ClassInfo> deleteClassInfos = new ArrayList<>();
        for(String deleteFile : deleteFilePaths){
            ClassInfo classInfo = ClassInfoDAO.getClassInfoByFilePath(projectName, deleteFile, conn);
            deleteClassInfos.add(classInfo);
        }

        //获取要被删除的MethodInfo(ID)、MethodInvocationInfo(ID)和MethodInvocationInfoInView(ID)
        List<String> deleteMethodIDs = new ArrayList<>();
        List<String> deleteMethodInvocationIDs = new ArrayList<>();
        List<String> deleteMethodInvocationInViewIDs = new ArrayList<>();
        for(ClassInfo classInfo : deleteClassInfos){
            List<String> methodIDs = MethodInfoDAO.getMethodIDInClass(projectName, classInfo.getClassName(), conn);
            deleteMethodIDs.addAll(methodIDs);

            List<String> methodInvocationIDs = MethodInvocationDAO.getMethodInvocationIDsByClassName(projectName, classInfo.getClassName(), conn);
            deleteMethodInvocationIDs.addAll(methodInvocationIDs);

            List<String> methodInvocationInViewIDs = MethodInvocationInViewDAO.getMethodInvocationInViewByCallClassID(projectName, String.valueOf(classInfo.getID()), conn);
            deleteMethodInvocationInViewIDs.addAll(methodInvocationInViewIDs);
        }

        //根据list中的ID进行删除
        ClassInfoDAO.deleteClassInfoRecords(deleteClassInfos, conn);
        MethodInfoDAO.deleteMethodInfoRecords(deleteMethodIDs, conn);
        MethodInvocationDAO.deleteMethodInvocationInfoRecords(deleteMethodInvocationIDs, conn);
        MethodInvocationInViewDAO.deleteMethodInvocationInViewRecords(deleteMethodInvocationInViewIDs, conn);
    }

    /**
     * 处理新增的文件
     * @param projectName
     * @param addedFilePaths
     * @param conn
     * @throws SQLException
     */
    public void processAddedFile(String projectName, List<String> addedFilePaths, Connection conn) throws SQLException{
        List<ClassInfo> addedClassInfosContainer = new ArrayList<>();
        for(String filePath : addedFilePaths){
            File file = new File(filePath);
            //获取新增文件的class info
            if(file.exists())
                processClassInfo(file, addedClassInfosContainer);
        }
        //存储新增文件中的类
        ClassInfoDAO.saveClassInfoList(addedClassInfosContainer, conn);
        //从db中获取该项中的所有类
        List<String> classInfoList = ClassInfoDAO.getAllClassInfoList(projectName, conn);
        for(String filePath : addedFilePaths){
            File file = new File(filePath);
            //获取新增文件的method info
            if(file.exists())
                processMethodCallTree(file, classInfoList);
        }

        //存储当前项目中的所有方法
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        methodInfoDAO.saveMethodInfoList(MethodInfoContainer.getContainer().getMethodInfoListByProjectName(projectName), conn);
        //存储当前项目中的所有方法调用
        MethodInvocationDAO.saveMethodInvocation(projectName, MethodCallContainer.getContainer().getMethodCallsByProjectName(projectName),conn);

        // 向methodInvocationInView表中插入数据
        Map<String, MethodCall> methodCallMap = MethodCallContainer.getContainer().getMethodCallsByProjectName(projectName);
        if(methodCallMap == null) return;
        List<MethodInvocationInView> methodInvocationInViews = new ArrayList<>();
        for (Map.Entry<String, MethodCall> entry : methodCallMap.entrySet()) {
            Method callMethod = entry.getValue().getCaller();
            List<Method> calledMethods = entry.getValue().getCalled();
            for(Method calledMethod : calledMethods){

                MethodInfo callMethodInfo = MethodInfoDAO.getMethodInfoByQualifiedName(projectName, callMethod.getQualifiedName(), conn);
                MethodInfo calledMethodInfo = MethodInfoDAO.getMethodInfoByQualifiedName(projectName, calledMethod.getQualifiedName(), conn);
                if(callMethodInfo == null || calledMethodInfo == null) continue;

                String callClassID = ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName, callMethodInfo.getClassName(), conn);
                String calledClassID = ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName, calledMethodInfo.getClassName(), conn);
                if(callClassID == null || calledClassID == null) continue;

                MethodInvocationInView m = new MethodInvocationInView();
                m.setProjectName(projectName);
                m.setCallClassName(callMethod.getPackageAndClassName());
                m.setCallMethodName(callMethod.getName());
                m.setCallMethodID(callMethodInfo.getID());
                m.setCallMethodParameters(callMethod.getParamTypeList().toString());
                m.setCallMethodReturnType(callMethod.getReturnTypeStr());
                m.setCalledClassName(calledMethodInfo.getClassName());
                m.setCalledMethodName(calledMethodInfo.getMethodName());
                m.setCalledMethodID(calledMethodInfo.getID());
                m.setCallClassID(callClassID);
                m.setCalledClassID(calledClassID);
                methodInvocationInViews.add(m);
            }
        }
        MethodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViews,conn);
        MethodInvocationInViewDAO.updateIsRecursive(projectName,conn);

        MethodInfoContainer.getContainer().clearMethodInfoListByProjectName(projectName);
        MethodCallContainer.getContainer().clearMethodCallByProjectName(projectName);
    }

    /**
     * 处理修改的文件
     * @param projectName
     * @param modifiedFilePaths
     * @param conn
     * @throws SQLException
     */
    public void processModifiedFile(String projectName, List<String> modifiedFilePaths, Connection conn) throws SQLException {
        processDeletedFile(projectName, modifiedFilePaths, conn);
        processAddedFile(projectName, modifiedFilePaths, conn);
        MethodInvocationInViewDAO.updateCalledClassID(projectName, conn);
        MethodInvocationInViewDAO.updateCalledMethodID(projectName, conn);
        System.out.println("数据处理完成...");
    }

    /**
     * 使用JavaParser获取项目中所有的类
     * @param file
     */
    private void processClassInfo(File file,List<ClassInfo> classInfos){
        ClassVisitor visitor = new ClassVisitor(projectName,file.getPath());
        try{
            CompilationUnit cu = JavaParser.parse(file);
            visitor.visit(cu, null);
        }catch (Exception ex){
            //ex.printStackTrace();
        }
        classInfos.add(visitor.getClassInfo());
    }

    /**
     * 使用JavaParser获取方法调用
     * @param file
     */
    private void processMethodCallTree(File file, List<String> classInfoList){
        MethodVisitor visitor = new MethodVisitor(projectName, classInfoList);
        try{
            CompilationUnit cu = JavaParser.parse(file);
            visitor.visit(cu, null);
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            //对所有修改文件进行方法调用处理
            for (Map.Entry<String, List<String>> entry : modifiedFileMap.entrySet()) {
                String projectRootPath = entry.getKey();
                List<String> modifiedFilePaths = this.modifiedFileMap.get(projectRootPath);
                projectName = getProjectNameFromProjectPath(projectRootPath);

                if(modifiedFilePaths == null || modifiedFilePaths.isEmpty()) continue;
                processModifiedFile(projectName, modifiedFilePaths, this.connection);
            }

            //将项目根目录list转化为项目名list
            List<String> projectNames = projectFolders.stream()
                    .map(GetMethodInvocationPlus::getProjectNameFromProjectPath)
                    .collect(Collectors.toList());

            //根据配置信息决定是否需要统计调用次数和调用深度
            if(DataConfig.analyseInvocationCounts){
                CountInvocation.countInvokeCounts(projectNames,this.connection);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
