package com.se.entity;

public class MethodInvocationInView {

    private String projectName;
    private String callClassName;
    private String calledClassName;
    private String calledMethodName;
    private String callMethodName;
    private String callMethodParameters;
    private String callMethodReturnType;
    private String callMethodID;
    private String calledMethodID;
    private String callClassID;
    private String calledClassID;


    public String getCallMethodID() {
        return callMethodID;
    }

    public void setCallMethodID(String callMethodID) {
        this.callMethodID = callMethodID;
    }

    public String getCalledMethodID() {
        return calledMethodID;
    }

    public void setCalledMethodID(String calledMethodID) {
        this.calledMethodID = calledMethodID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCallClassName() {
        return callClassName;
    }

    public void setCallClassName(String callClassName) {
        this.callClassName = callClassName;
    }

    public String getCalledClassName() {
        return calledClassName;
    }

    public void setCalledClassName(String calledClassName) {
        this.calledClassName = calledClassName;
    }

    public String getCalledMethodName() {
        return calledMethodName;
    }

    public void setCalledMethodName(String calledMethodName) {
        this.calledMethodName = calledMethodName;
    }

    public String getCallMethodName() {
        return callMethodName;
    }

    public String getQualifiedCallMethodName(){
        return callClassName + "." + callMethodName;
    }

    public String getQualifiedCalledMethodName(){
        return calledClassName + "." + calledMethodName;
    }

    public void setCallMethodName(String callMethodName) {
        this.callMethodName = callMethodName;
    }

    public String getCallMethodParameters() {
        return callMethodParameters;
    }

    public void setCallMethodParameters(String callMethodParameters) {
        this.callMethodParameters = callMethodParameters;
    }

    public String getCallMethodReturnType() {
        return callMethodReturnType;
    }

    public void setCallMethodReturnType(String callMethodReturnType) {
        this.callMethodReturnType = callMethodReturnType;
    }

    public String getCallClassID() {
        return callClassID;
    }

    public void setCallClassID(String callClassID) {
        this.callClassID = callClassID;
    }

    public String getCalledClassID() {
        return calledClassID;
    }

    public void setCalledClassID(String calledClassID) {
        this.calledClassID = calledClassID;
    }
}
