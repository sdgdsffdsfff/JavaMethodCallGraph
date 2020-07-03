package com.se.container;

import com.alibaba.fastjson.JSONArray;
import com.se.entity.Method;
import com.se.entity.MethodCall;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class MethodCallContainer {
    private ConcurrentHashMap<String, MethodCall> methodCalls;
    private static MethodCallContainer container;

    private MethodCallContainer() {
        methodCalls = new ConcurrentHashMap<String, MethodCall>();
    }

    public synchronized static MethodCallContainer getContainer() {
        if(container == null) {
            container = new MethodCallContainer();
        }
        return container;
    }

    public ConcurrentHashMap<String, MethodCall> getMethodCalls()
    {
        return methodCalls;
    }

    public void addMethodCall(Method caller, Method called) {
        MethodCall methodCall = methodCalls.get(caller.getQualifiedName());
        if(methodCall != null) {
            if(!methodCall.containsCalled(called)) {
                methodCall.addCalled(called);
            }
        } else {
            methodCall = new MethodCall();
            methodCall.setCaller(caller);
            methodCall.addCalled(called);
            methodCalls.put(caller.getQualifiedName(), methodCall);
        }
    }

    public MethodCall getMethodCall(String caller) {
        return methodCalls.get(caller);
    }

    @Override
    public String toString() {
        JSONArray projectMethodCallArray = new JSONArray();

        if(methodCalls != null && !methodCalls.isEmpty()) {
            Collection<MethodCall> calls = methodCalls.values();

            for(MethodCall call : calls) {
                projectMethodCallArray.add(call.toJSON());
            }
        }
        return projectMethodCallArray.toString();
    }

    public String toJSON(){
        JSONArray projectMethodCallArray = new JSONArray();
        if(methodCalls != null && !methodCalls.isEmpty()) {
            Collection<MethodCall> calls = methodCalls.values();
            for(MethodCall call : calls) {
                projectMethodCallArray.add(call.toJSON());
            }
        }
        return projectMethodCallArray.toString();
    }
}
