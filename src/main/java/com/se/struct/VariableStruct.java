package com.se.struct;

import java.util.LinkedList;
import java.util.List;

//VariableStruct2
public class VariableStruct {
    private String name;
    private String type;
    private String typePkg;
    private List<String> typeParams = new LinkedList<String>();
    //If variable type is an Interface, value type will be an implementation class of this interface.
    private String valueTypeQualified;
    private boolean staticVar;
    // should not be null if this is instance varible.
    private ClassOrInterfaceStruct parent;
    //Should not be null if this is delcared in side method.
    private MethodStruct parentMethod;
    private boolean arrayVar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypePkg() {
        return typePkg;
    }

    public void setTypePkg(String typePkg) {
        this.typePkg = typePkg;
    }

    public List<String> getTypeParams() {
        return typeParams;
    }

    public void setTypeParams(List<String> typeParams) {
        this.typeParams = typeParams;
    }

    public String getValueTypeQualified() {
        return valueTypeQualified;
    }

    public void setValueTypeQualified(String valueTypeQualified) {
        this.valueTypeQualified = valueTypeQualified;
    }

    public boolean isStaticVar() {
        return staticVar;
    }

    public void setStaticVar(boolean staticVar) {
        this.staticVar = staticVar;
    }

    public ClassOrInterfaceStruct getParent() {
        return parent;
    }

    public void setParent(ClassOrInterfaceStruct parent) {
        this.parent = parent;
    }

    public MethodStruct getParentMethod() {
        return parentMethod;
    }

    public void setParentMethod(MethodStruct parentMethod) {
        this.parentMethod = parentMethod;
    }

    public boolean isArrayVar() {
        return arrayVar;
    }

    public void setArrayVar(boolean arrayVar) {
        this.arrayVar = arrayVar;
    }

    public String getQualifiedNameWithoutVarName(){
        StringBuffer buff = new StringBuffer();

        if(this.typePkg != null) {
            //would be null for primitives like int,short etc..
            buff.append(this.typePkg).append(".");
        }
        if(this.type != null) {
            buff.append(this.type);
        }
        if(this.arrayVar) {
            buff.append("[]");
        }
        return buff.toString();
    }

    public String getQualifiedName() {
        StringBuffer buff = new StringBuffer();
        buff.append(getQualifiedNameWithoutVarName());

        if(this.name != null) {
            buff.append(".").append(this.name);
        }

        return buff.toString();
    }

    @Override
    public String toString()
    {
        return "VariableStruct2 [qname=" + getQualifiedName() + ", typeParams=" + typeParams + ", valueType=" + this.valueTypeQualified + "]";
    }

    //TODO: variable scope

}
