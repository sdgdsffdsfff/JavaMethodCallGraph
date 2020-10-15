package com.se.process;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.DAO.MethodInvocationInViewDAO;
import com.se.config.DataConfig;
import com.se.container.MethodCallContainer;
import com.se.container.MethodInfoContainer;
import com.se.entity.ClassInfo;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;
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
    private Map<Integer, String> deletedID2ClassNameMap; // key: ClassInfoID value: ClassName
    private Map<String, Integer> className2IDMap; // key: ClassName value: ClassInfoID

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
     * @return  list中存储 被调用方法 来自 被删除类 的 方法调用
     * @throws SQLException
     */
    public List<MethodInvocation> processDeletedFile(String projectName, List<String> deleteFilePaths, Connection conn) throws SQLException{
        //获取要被删除的ClassInfo(ID, className)
        long time0 = System.currentTimeMillis();

        List<Integer> deleteClassInfos = new ArrayList<>();
        List<String> deleteClassNames = new ArrayList<>();
//        deletedID2ClassNameMap = new HashMap<>();
        List<ClassInfo> allClassInfoList = ClassInfoDAO.getClassListByProjectName(projectName, conn);
        for(ClassInfo classInfo : allClassInfoList){
            if(deleteFilePaths.contains(classInfo.getFilePath())){
                deleteClassInfos.add(classInfo.getID());
                deleteClassNames.add(classInfo.getClassName());
//                deletedID2ClassNameMap.put(classInfo.getID(), classInfo.getClassName());
            }
        }
        allClassInfoList.clear();

        //获取要被删除的MethodInfo(ID)、MethodInvocationInfo(ID)和MethodInvocationInfoInView(ID)
        List<String> deleteMethodIDs = new ArrayList<>();
        List<MethodInfo> allMethodInfoList = MethodInfoDAO.getMethodInfoListByProjectName(projectName, conn);
        for(MethodInfo methodInfo : allMethodInfoList){
            if(deleteClassNames.contains(methodInfo.getClassName())){
                deleteMethodIDs.add(methodInfo.getID());
            }
        }
        allMethodInfoList.clear();


        List<String> deleteMethodInvocationIDs = new ArrayList<>();
        List<MethodInvocation> methodInvocationWithDeletedClass = new ArrayList<>(); // list中存储 被调用方法 来自 被删除类 的 方法调用
        List<MethodInvocation> allMethodInvocationList = MethodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);
        for(MethodInvocation methodInvocation : allMethodInvocationList){
            if(deleteClassNames.contains(methodInvocation.getCallClassName())){
                deleteMethodInvocationIDs.add(methodInvocation.getID());
            } else if (deleteClassNames.contains(methodInvocation.getCalledClassName())){
                methodInvocationWithDeletedClass.add(methodInvocation);
            }
        }
        allMethodInvocationList.clear();


        List<Integer> deleteMethodInvocationInViewIDs = new ArrayList<>();
        List<MethodInvocationInView> allMethodInvocationInViewList = MethodInvocationInViewDAO.getMethodInvocationInViewByProjectName(projectName,conn);

        for(MethodInvocationInView methodInvocationInView : allMethodInvocationInViewList){
            // 作为调用类或者被调用类都要删除
            if(deleteClassNames.contains(methodInvocationInView.getCallClassName()) || deleteClassNames.contains(methodInvocationInView.getCalledClassName())){
                deleteMethodInvocationInViewIDs.add(methodInvocationInView.getID());
            }
        }

        //根据list中的ID进行删除
        ClassInfoDAO.deleteClassInfoRecords(deleteClassInfos, conn);

        MethodInfoDAO.deleteMethodInfoRecords(deleteMethodIDs, conn);

        MethodInvocationDAO.deleteMethodInvocationInfoRecords(deleteMethodInvocationIDs, conn);

        // 更新calledClassFilePath
        MethodInvocationDAO.updateCalledClassFilePath(projectName, conn);

        MethodInvocationInViewDAO.deleteMethodInvocationInViewRecords(deleteMethodInvocationInViewIDs, conn);

        return methodInvocationWithDeletedClass;
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
    }


    /**
     * 处理修改的文件
     * @param projectName
     * @param modifiedFilePaths
     * @param conn
     * @throws SQLException
     */
    public void processModifiedFile(String projectName, List<String> modifiedFilePaths, Connection conn) throws SQLException {

        List<MethodInvocation> methodInvocationWithDeletedClass = processDeletedFile(projectName, modifiedFilePaths, conn);

        processAddedFile(projectName, modifiedFilePaths, conn);

        // 通过重新插入的方式更新methodinvocationinview表里的calledClassID和calledMethodID
        FilterMethodInvocation.doFilterPlus(projectName, methodInvocationWithDeletedClass, conn);
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
        MethodVisitor visitor = new MethodVisitor(projectName, classInfoList, file.getAbsolutePath(), this.connection);
        try{
            CompilationUnit cu = StaticJavaParser.parse(file);
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

            //匹配方法调用关系
//            FilterMethodInvocation.doFilterX(this.connection, projectNames, true);
            for(String currentProjectName : projectNames){
                List<MethodInvocation> methodInvocationList = MethodInvocationDAO.getMethodInvocationByProjectNameAndDate(projectName, this.connection);
                FilterMethodInvocation.doFilterPlus(currentProjectName, methodInvocationList, this.connection);
            }

            //根据配置信息决定是否需要统计调用次数和调用深度
            if(DataConfig.analyseInvocationCounts){
                CountInvocation.countInvokeCounts(projectNames,this.connection);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
