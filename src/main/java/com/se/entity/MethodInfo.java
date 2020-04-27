package com.se.entity;


public class MethodInfo {

    public MethodInfo(String projectName, Method method){
        this.projectName = projectName;
        this.className = method.getPackageAndClassName();
        this.methodName = method.getName();
        this.returnType = method.getReturnType().toString();
        this.qualifiedName = method.getQualifiedName();
        this.methodParameters = method.getParamTypeList().toString();
    }

    public MethodInfo(){

    }

    private String projectName;
    private String methodName;
    private String className;
    private String returnType;
    private String methodParameters;
    private String qualifiedName;
    private String ID;


    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }


    public String getMethodParameters() {
        return methodParameters;
    }

    public void setMethodParameters(String methodParameters) {
        this.methodParameters = methodParameters;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
