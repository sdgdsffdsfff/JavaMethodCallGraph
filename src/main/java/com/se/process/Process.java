package com.se.process;
import com.se.DAO.*;
import com.se.config.DataConfig;
import com.se.utils.FileHandler;
import com.se.utils.ListUtils;
import java.io.File;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Process {

    //线程数量
    private static int threadNum = 4;

    public static void main(String[] args){
        //建立数据库连接
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        LinkedList<String> folders = new LinkedList<>();
        File dir = new File(DataConfig.sourceProjectParentPath);
        FileHandler.getFolders(dir,folders);
        System.out.println("项目数为："+folders.size());
        List<List<String>> folderList = ListUtils.divideList(folders,threadNum);
        //分析源项目代码，抽取需要的信息
        for(int i = 0;i<threadNum;i++){
            cachedThreadPool.execute(new GetMethodInvocation(folderList.get(i),conn));
        }
        cachedThreadPool.shutdown();
    }

}