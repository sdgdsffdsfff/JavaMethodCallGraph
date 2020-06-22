package com.se.entity;


public class MethodInfo {

    public MethodInfo(String projectName, Method method){
        this.projectName = projectName;
        this.className = method.getPackageAndClassName();
        this.methodName = method.getName();
        this.returnType = method.getReturnTypeStr();
        this.qualifiedName = method.getQualifiedName();
        this.methodParameters = method.getParamTypeList().toString();
        this.beginLine = method.getBeginLine();
        this.endLine = method.getEndLine();
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
    private int beginLine;
    private int endLine;


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

    public int getBeginLine() {
        return beginLine;
    }

    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }
}
