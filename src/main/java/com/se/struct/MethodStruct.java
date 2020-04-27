package com.se.struct;

import com.se.utils.PrettyPrintingMap;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//MethodStruct2
public class MethodStruct {
    private String name;
    private String clazz;
    private String pkg;
    private String returnType;
    private Map<String, VariableStruct> callArgs = new LinkedHashMap<>();
    private Map<String, VariableStruct> vars = new LinkedHashMap<>();
    private ClassOrInterfaceStruct parent;
    private List<MethodCallStruct> calledMethods = new LinkedList<>();

    //exception classes
    private List<String> exceptions;
    private boolean abstractMethod;
    private boolean staticMethod;

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

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public Map<String, VariableStruct> getCallArgs() {
        return callArgs;
    }

    public void setCallArgs(Map<String, VariableStruct> callArgs) {
        this.callArgs = callArgs;
    }

    public Map<String, VariableStruct> getVars() {
        return vars;
    }

    public void setVars(Map<String, VariableStruct> vars) {
        this.vars = vars;
    }

    public ClassOrInterfaceStruct getParent() {
        return parent;
    }

    public void setParent(ClassOrInterfaceStruct parent) {
        this.parent = parent;
    }

    public List<MethodCallStruct> getCalledMethods() {
        return calledMethods;
    }

    public void setCalledMethods(List<MethodCallStruct> calledMethods) {
        this.calledMethods = calledMethods;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    public boolean isAbstractMethod() {
        return abstractMethod;
    }

    public void setAbstractMethod(boolean abstractMethod) {
        this.abstractMethod = abstractMethod;
    }

    public boolean isStaticMethod() {
        return staticMethod;
    }

    public void setStaticMethod(boolean staticMethod) {
        this.staticMethod = staticMethod;
    }

    public String getQualifiedNameWithoutArgs(){
        StringBuffer buff = new StringBuffer();
        buff.append(this.pkg).append(".")
                .append(this.clazz).append(".")
                .append(this.name);
        return buff.toString();
    }

    public String getArgsAsCSSep(){
        if(this.callArgs != null && !this.callArgs.isEmpty()) {
            StringBuffer buff = new StringBuffer();

            int i = 1;
            buff.append("<");
            for(VariableStruct var : this.callArgs.values()) {
                buff.append(var.getQualifiedNameWithoutVarName());

                if(i < this.callArgs.size()) {
                    buff.append(",");
                }
                i++;
            }
            buff.append(">");
            return buff.toString();
        }
        return "";
    }

    public String getQualifiedNameWithArgs() {
        StringBuffer buff = new StringBuffer();
        buff.append(getQualifiedNameWithoutArgs())
                .append(getArgsAsCSSep());
        return buff.toString();
    }

    @Override
    public String toString()
    {
        return "MethodStruct [qname=" + getQualifiedNameWithArgs() + ", returnType=" + returnType
                + ", callArgs=" + new PrettyPrintingMap<String, VariableStruct>(callArgs) + ", calledMethods=" + calledMethods + "]";
    }



}
