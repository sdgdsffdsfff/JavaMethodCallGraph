package com.se.entity;

public class ClassInfo {
    private int ID;
    private String projectName;
    private String className;
    private Boolean isInterface;
    private String filePath;
    private int cloneId;
    private int invokedCounts;
    private int invokeCounts;
    private boolean asset;
    private String layer;
    private String superClass;
    private String interfaces;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean getInterface() {
        return isInterface;
    }

    public void setInterface(Boolean anInterface) {
        isInterface = anInterface;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getCloneId() {
        return cloneId;
    }

    public void setCloneId(int cloneId) {
        this.cloneId = cloneId;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getInvokedCounts() {
        return invokedCounts;
    }

    public void setInvokedCounts(int invokedCounts) {
        this.invokedCounts = invokedCounts;
    }

    public int getInvokeCounts() {
        return invokeCounts;
    }

    public void setInvokeCounts(int invokeCounts) {
        this.invokeCounts = invokeCounts;
    }

    public boolean isAsset() {
        return asset;
    }

    public void setAsset(boolean asset) {
        this.asset = asset;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }


    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String interfaces) {
        this.interfaces = interfaces;
    }
}
