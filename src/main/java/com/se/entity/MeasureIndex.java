package com.se.entity;

public class MeasureIndex {

    public MeasureIndex(int id, String filePath, int beginLine, int endLine,String sourcePath) {
        this.id = id;
        this.filePath = filePath;
        this.beginLine = beginLine;
        this.endLine = endLine;
        String tempStr  = filePath.replace(sourcePath,"");
        tempStr = tempStr.substring(1);
        this.projectName = tempStr.substring(0,tempStr.indexOf("\\"));
    }

    private int id;
    private String filePath;
    private int beginLine;
    private int endLine;
    private String projectName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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


    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
