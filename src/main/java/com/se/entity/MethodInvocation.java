package com.se.entity;

public class MethodInvocation
{
    private String ProjectName;
    private String CallClassName;
    private String CalledClassName;
    private String CalledMethodName;
    private String CallMethodName;
    private String CallMethodParameters;
    private String CallMethodReturnType;

    public String getCallClassName() {
        return CallClassName;
    }

    public void setCallClassName(String callClassName) {
        CallClassName = callClassName;
    }

    public String getCalledClassName() {
        return CalledClassName.replaceAll("'","\'");
    }

    public void setCalledClassName(String calledClassName) {
        CalledClassName = calledClassName;
    }

    public String getProjectName() {
        return ProjectName;
    }

    public void setProjectName(String projectName) {
        ProjectName = projectName;
    }

    public String getCalledMethodName() {
        return CalledMethodName;
    }

    public void setCalledMethodName(String calledMethodName) {
        CalledMethodName = calledMethodName;
    }

    public String getCallMethodName() {
        return CallMethodName;
    }

    public void setCallMethodName(String callMethodName) {
        CallMethodName = callMethodName;
    }

    public String getCallMethodParameters() {
        return CallMethodParameters;
    }

    public void setCallMethodParameters(String callMethodParameters) {
        CallMethodParameters = callMethodParameters;
    }

    public String getCallMethodReturnType() {
        return CallMethodReturnType;
    }

    public void setCallMethodReturnType(String callMethodReturnType) {
        CallMethodReturnType = callMethodReturnType;
    }
}
