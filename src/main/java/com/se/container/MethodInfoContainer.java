package com.se.container;

import com.se.entity.MethodInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MethodInfoContainer {


    private ConcurrentHashMap<String, List<MethodInfo>> projectName2MethodInfoMap;
    private static MethodInfoContainer container;

    private MethodInfoContainer(){
        projectName2MethodInfoMap = new ConcurrentHashMap<>();
    }

    public synchronized static MethodInfoContainer getContainer(){
        if(container == null){
            container = new MethodInfoContainer();
        }
        return container;
    }

    public synchronized void addMethodInfo(String projectName, MethodInfo methodInfo){
        if(projectName2MethodInfoMap.containsKey(projectName)){
            projectName2MethodInfoMap.get(projectName).add(methodInfo);
        } else {
            List<MethodInfo> methodInfoList = new ArrayList<>();
            methodInfoList.add(methodInfo);
            projectName2MethodInfoMap.put(projectName, methodInfoList);
        }
    }

    public synchronized List<MethodInfo> getMethodInfoListByProjectName(String projectName){
        return projectName2MethodInfoMap.get(projectName);
    }

    public synchronized void clearMethodInfoListByProjectName(String projectName){
        projectName2MethodInfoMap.get(projectName).clear();
        projectName2MethodInfoMap.remove(projectName);
    }
}
