package com.se.process;

import com.se.DAO.BuildConnection;
import com.se.config.DataConfig;
import com.se.utils.FileHandler;
import com.se.utils.FileHelper;
import com.se.utils.ListUtils;
import com.se.utils.TimeUtil;

import java.io.File;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Process {

    //线程数量
    private static int threadNum = 1;

    public static void main(String[] args){
        Map<String, List<String>> allModifiedFileMap = new HashMap<>();
        //建立数据库连接
        BuildConnection buildConnection = new BuildConnection();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        if(!DataConfig.analyseSingleProject){
            LinkedList<String> folders = new LinkedList<>();
            File dir = new File(DataConfig.sourceProjectParentPath);
            FileHandler.getFolders(dir,folders);
            System.out.println("项目数为："+folders.size());
            List<List<String>> folderList = ListUtils.divideList(folders,threadNum);
            if(DataConfig.isAdditionalProcess)
                allModifiedFileMap = prepareModifiedFileMap(DataConfig.modifiedFilePath, folders);
            //分析源项目代码，抽取需要的信息
            for(int i = 0;i<threadNum;i++){
                Connection conn = buildConnection.buildConnect();
                if(DataConfig.isAdditionalProcess){
                    Map<String, List<String>> modifiedFileMap = new HashMap<>();
                    for(String projectRootPath : folderList.get(i)){
                        modifiedFileMap.put(projectRootPath, allModifiedFileMap.get(projectRootPath));
                    }
                    cachedThreadPool.execute(new GetMethodInvocationPlus(folderList.get(i), modifiedFileMap, conn));
                } else {
                    cachedThreadPool.execute(new GetMethodInvocation(folderList.get(i),conn));
                }
            }
            cachedThreadPool.shutdown();
        }else {
            System.out.println("对单个项目进行分析");
            LinkedList<String> folders = new LinkedList<>();
            folders.add(DataConfig.sourceProjectPath);
            Connection conn = buildConnection.buildConnect();
            if(DataConfig.isAdditionalProcess){
                allModifiedFileMap = prepareModifiedFileMap(DataConfig.modifiedFilePath, folders);
                cachedThreadPool.execute(new GetMethodInvocationPlus(folders, allModifiedFileMap, conn));
            } else {
                cachedThreadPool.execute(new GetMethodInvocation(folders,conn));
            }
            cachedThreadPool.shutdown();
        }
    }


    public static Map<String, List<String>> prepareModifiedFileMap(String txtFilePath, LinkedList<String> folders){
        //从txt文件中读取修改过的文件列表
        List<String> modifiedFileList = FileHelper.readFile(txtFilePath);
        //过滤非java文件和测试文件
        modifiedFileList = modifiedFileList.stream()
                .filter(f -> f.endsWith(".java") && !f.contains("\\test"))
                .collect(Collectors.toList());

        //将文件列表映射到项目根路径
        Map<String, List<String>> modifiedFileMap = new HashMap<>();
        for(String projectRootPath : folders){
            List<String> filePathInProject = modifiedFileList.stream()
                    .filter(filePath -> filePath.startsWith(projectRootPath))
                    .collect(Collectors.toList());
            modifiedFileMap.put(projectRootPath, filePathInProject);
        }

        //检查txt中是否有未被加入到map的路径
        List<String> filteredFilePaths = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : modifiedFileMap.entrySet()) {
            filteredFilePaths.addAll(entry.getValue());
        }
        modifiedFileList.removeAll(filteredFilePaths);
        if(modifiedFileList.size() > 0){
            System.out.println("无法处理以下新增文件，请检查文件路径是否正确：");
            modifiedFileList.forEach(System.out::println);
        }
        return modifiedFileMap;
    }

}