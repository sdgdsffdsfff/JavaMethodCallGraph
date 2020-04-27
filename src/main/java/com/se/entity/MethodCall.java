package com.se.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodCall {
    private Method caller;
    private List<Method> called;


    public Method getCaller() {
        return caller;
    }

    public void setCaller(Method caller) {
        this.caller = caller;
    }

    public List<Method> getCalled() {
        return called;
    }

    public void setCalled(List<Method> called) {
        this.called = called;
    }

    public void addCalled(Method called) {
        if(called == null) {
            return;
        }

        if(this.called == null) {
            this.called = new ArrayList<Method>();
        }
        this.called.add(called);
    }

    public boolean containsCalled(Method called) {
        return this.called != null && this.called.contains(called);
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(" \"caller\": ");

        String callerJsonObject = String.format("{\n\t\"methodname\": \"%s\",\n\t\"paramtype\": \"%s\"\n  },\n", caller.getQualifiedName(), caller.getParamTypeList().toString());

        buff.append(callerJsonObject);

        buff.append(" \"callee\": [\n");

        if(called != null && !called.isEmpty()) {

            Iterator it = called.iterator();
            while(it.hasNext()){
                Method calledMethod = (Method) it.next();
                String calledJsonObject = String.format("\t{\"methodname\": \"%s\",\n\t\"paramtype\": \"%s\"}", calledMethod.getQualifiedName(), calledMethod.getParamTypeList());
                buff.append(calledJsonObject);
                if(it.hasNext())
                    buff.append(",");
                buff.append("\n");
            }
        }
        buff.append("  ]\n");
        return buff.toString();
    }

    public JSONObject toJSON(){

        JSONObject methodCallObj = new JSONObject();

        JSONObject callerObj = new JSONObject();
        //callerObj.put("name", caller.getName());
        callerObj.put("clazz", caller.getPackageAndClassName());
        JSONArray callerParamArray = JSONArray.parseArray(JSON.toJSONString(caller.getParamTypeList()));
        //callerObj.put("params", callerParamArray);

        JSONArray calledArray = new JSONArray();
        Iterator it = called.iterator();
        while(it.hasNext()){
            Method calledMethod = (Method) it.next();
            JSONObject calledObj = new JSONObject();
            //calledObj.put("name", calledMethod.getName());
            calledObj.put("clazz", calledMethod.getPackageAndClassName());
            //JSONArray calledParamArray = new JSONArray();
            //calledParamArray.addAll(calledMethod.getParamTypeList());
            JSONArray calledParamArray = JSONArray.parseArray(JSON.toJSONString(calledMethod.getParamTypeList()));
            //calledObj.put("params", calledParamArray);
            calledArray.add(calledObj);
        }
        methodCallObj.put("caller", callerObj);
        methodCallObj.put("called", calledArray);
        return methodCallObj;
    }
}
