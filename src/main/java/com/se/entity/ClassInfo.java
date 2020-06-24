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
}
