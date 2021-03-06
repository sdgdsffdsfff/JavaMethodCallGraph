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
        this.methodContent = method.getMethodContent();
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
    private boolean asset;
    private int cloneGroupId;
    private String methodContent;
    private int isSameProjectClone;
    private String filePath;


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

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isAsset() {
        return asset;
    }

    public void setAsset(boolean asset) {
        this.asset = asset;
    }

    public int getCloneGroupId() {
        return cloneGroupId;
    }

    public void setCloneGroupId(int cloneGroupId) {
        this.cloneGroupId = cloneGroupId;
    }

    public String getMethodContent() {
        return methodContent;
    }

    public void setMethodContent(String methodContent) {
        this.methodContent = methodContent;
    }

    public int getIsSameProjectClone() {
        return isSameProjectClone;
    }

    public void setIsSameProjectClone(int isSameProjectClone) {
        this.isSameProjectClone = isSameProjectClone;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
