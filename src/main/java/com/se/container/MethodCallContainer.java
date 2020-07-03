package com.se.container;

import com.se.entity.Method;
import com.se.entity.MethodCall;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MethodCallContainer {
    private ConcurrentHashMap<String, Map<String, MethodCall>> projectName2MethodCallMap;
    private static MethodCallContainer container;

    private MethodCallContainer() {
        projectName2MethodCallMap = new ConcurrentHashMap<>();
    }

    public synchronized static MethodCallContainer getContainer() {
        if(container == null) {
            container = new MethodCallContainer();
        }
        return container;
    }

    public synchronized Map<String, MethodCall> getMethodCallsByProjectName(String projectName)
    {
        return projectName2MethodCallMap.get(projectName);
    }

    public synchronized void addMethodCall(String projectName, Method caller, Method called) {
        if(projectName2MethodCallMap.containsKey(projectName)){
            Map<String,MethodCall> methodCallMap = projectName2MethodCallMap.get(projectName);
            MethodCall methodCall = methodCallMap.get(caller.getQualifiedName());
            if(methodCall != null) {
                if(!methodCall.containsCalled(called)) {
                    methodCall.addCalled(called);
                    methodCallMap.put(caller.getQualifiedName(), methodCall);
                }
            } else {
                methodCall = new MethodCall();
                methodCall.setCaller(caller);
                methodCall.addCalled(called);
                methodCallMap.put(caller.getQualifiedName(), methodCall);
            }
            projectName2MethodCallMap.put(projectName,methodCallMap);
        }else {
            Map<String,MethodCall> methodCallMap = new HashMap<>();
            MethodCall methodCall = new MethodCall();
            methodCall.setCaller(caller);
            methodCall.addCalled(called);
            methodCallMap.put(caller.getQualifiedName(), methodCall);
            projectName2MethodCallMap.put(projectName,methodCallMap);
        }
    }

    public synchronized MethodCall getMethodCall(String projectName, String caller) {
        if(projectName2MethodCallMap.containsKey(projectName)){
            return projectName2MethodCallMap.get(projectName).get(caller);
        }else {
            return null;
        }
    }

    public synchronized void clearMethodCallByProjectName(String projectName){
        projectName2MethodCallMap.get(projectName).clear();
        projectName2MethodCallMap.remove(projectName);
    }
}
