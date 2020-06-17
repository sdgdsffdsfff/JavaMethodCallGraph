package com.se.entity;


import java.util.Objects;

//view object
public class GraphNode {

    private String id;

    private String name;

    private int category;

    private int symbolSize;

    private int value;

    private String qualifiedClassName;

    private int calledCounts;

    private int calledDept;

    public GraphNode() {
    }

    public GraphNode(String id, String name, int category, int symbolSize, int value) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.symbolSize = symbolSize;
        this.value = value;
    }

    public GraphNode(String id, String name, int category, int symbolSize) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.symbolSize = symbolSize;
    }

    public GraphNode(String id, String name, int category) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.calledDept = 0;
    }

    public GraphNode(String id, String name, String qualifiedClassName, int category, int symbolSize) {
        this.id = id;
        this.name = name;
        this.qualifiedClassName = qualifiedClassName;
        this.category = category;
        this.symbolSize = symbolSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getSymbolSize() {
        return symbolSize;
    }

    public void setSymbolSize(int symbolSize) {
        this.symbolSize = symbolSize;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphNode)) return false;
        GraphNode graphNode = (GraphNode) o;
        return Objects.equals(id, graphNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public void setQualifiedClassName(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public int getCalledCounts() {
        return calledCounts;
    }

    public void setCalledCounts(int calledCounts) {
        this.calledCounts = calledCounts;
    }

    public int getCalledDept() {
        return calledDept;
    }

    public void setCalledDept(int calledDept) {
        this.calledDept = calledDept;
    }
}
