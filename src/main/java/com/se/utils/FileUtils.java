package com.se.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangyue on 2020/11/11 1:57 下午
 */
public class FileUtils {

    /**
     * 获得type文件夹下所有Java文件的路径
     * @param rootPath 根路径
     * @param type  目标文件夹，如controller、serviceImpl、dao等
     * @return  map<类路径,该类所属的层次，比如dao、controller>
     */
    public static Map<String,String> getJavaFilePath(String rootPath,String type){
        Map<String,String> map=new HashMap<>();
        File rootFile=new File(rootPath);
        if (rootFile.exists()&&rootFile.isDirectory()){
            File[] files=rootFile.listFiles();
            if (rootFile.getName().equals(type)){
                if (files!=null){
                    for (File file:files){
                        if (!file.isDirectory()){
                            if (file.getName().endsWith(".java")){
                                map.put(file.getAbsolutePath(),type);
                            }
                        }
                    }
                    return map;
                }
            }else {
                if (files!=null){
                    for (File file:files){
                        if (file.isDirectory()){
                            map=getJavaFilePath(file.getAbsolutePath(),type);
                            if (map!=null){
                                return map;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 由java路径List获取classname的map
     * @param map   <类路径,该类所属的层次，比如dao、controller>
     * @return  map<类名,该类所属的层次，比如dao、controller>
     */
    public static Map<String,String> getAllClassName(Map<String,String> map){
        Map<String,String> stringMap=new HashMap<>();
        if (map!=null){
            for (Map.Entry path:map.entrySet()){
                //获取 xxx.java
                String[] tempList= path.getKey().toString().split("/");
                //获取类名,分割xxx.java
                String javapath=tempList[tempList.length-1];
                String[] tempList1=javapath.split("\\.");
                stringMap.put(tempList1[0],path.getValue().toString());
            }
        }
        return stringMap;
    }

    public static void main(String[] args) {
        String projectPath = "/Users/zhangyue/Downloads/ssm2";
        System.out.println(getJavaFilePath(projectPath,"dao").size());
    }

}
