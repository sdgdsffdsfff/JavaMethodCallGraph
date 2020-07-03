package com.se.container;

import com.se.entity.MethodInfo;

import java.util.ArrayList;
import java.util.List;

public class MethodInfoContainer {
    private List<MethodInfo> methodInfoList;
    private static MethodInfoContainer container;

    private MethodInfoContainer(){ methodInfoList = new ArrayList<>(); }

    public synchronized static MethodInfoContainer getContainer(){
        if(container == null){
            container = new MethodInfoContainer();
        }
        return container;
    }

    public synchronized void addMethodInfo(MethodInfo methodInfo){
        methodInfoList.add(methodInfo);
    }

    public synchronized List<MethodInfo> getMethodInfoList(){
        return methodInfoList;
    }

    public synchronized void clear(){
        methodInfoList.clear();
    }
}
