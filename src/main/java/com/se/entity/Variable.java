package com.se.entity;

public class Variable {
    private String name;

    private String clazz;

    private String pkg;

    private boolean staticVar;

    private String genericType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public boolean isStaticVar() {
        return staticVar;
    }

    public void setStaticVar(boolean staticVar) {
        this.staticVar = staticVar;
    }

    public String getQualifiedName() {
        return this.clazz + "." + this.name;
    }

    public void setGenericType(String genericType) {
        this.genericType = genericType;
    }

    public String getGenericType() {
        return genericType;
    }

    public String getID(){
        if(this.pkg!=null){
            return this.pkg + "." + this.clazz;
        }else {
            return this.clazz;
        }
    }
}
