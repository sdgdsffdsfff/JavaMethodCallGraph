package com.se.struct;

import com.se.entity.ClassType;
import com.se.utils.PrettyPrintingMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//ClassOrInterfaceStruct2
public class ClassOrInterfaceStruct {
    private String name;

    private String pkg;

    private Map<String, MethodStruct> methods = new LinkedHashMap<>();

    //instance variables and static variables.
    private Map<String, VariableStruct> variables = new LinkedHashMap<>();

    //Applicable only for class
    private Map<String, String> interfacesImplemented = new LinkedHashMap<>();

    //In case of Interface this can be more than one. In case of class however only one.
    private Map<String, String> superClasses = new LinkedHashMap<>();

    //Format<classname, fullyqualified name of import> ex: <String,java.lang.String>
    private Map<String, String> imports = new LinkedHashMap<>();

    //For Generics. TODO
    private Map<String, String> typeParams = new LinkedHashMap<>();

    //In case of inner/anonymous class.
    private String parent;

    private ClassType type;

    private boolean abstractClazz;

    private boolean staticClazz;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public Map<String, MethodStruct> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, MethodStruct> methods) {
        this.methods = methods;
    }

    public Map<String, VariableStruct> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, VariableStruct> variables) {
        this.variables = variables;
    }

    public Map<String, String> getInterfacesImplemented() {
        return interfacesImplemented;
    }

    public void setInterfacesImplemented(Map<String, String> interfacesImplemented) {
        this.interfacesImplemented = interfacesImplemented;
    }

    public Map<String, String> getSuperClasses() {
        return superClasses;
    }

    public void setSuperClasses(Map<String, String> superClasses) {
        this.superClasses = superClasses;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public void setImports(Map<String, String> imports) {
        this.imports = imports;
    }

    public Map<String, String> getTypeParams() {
        return typeParams;
    }

    public void setTypeParams(Map<String, String> typeParams) {
        this.typeParams = typeParams;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ClassType getType() {
        return type;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    public boolean isAbstractClazz() {
        return abstractClazz;
    }

    public void setAbstractClazz(boolean abstractClazz) {
        this.abstractClazz = abstractClazz;
    }

    public boolean isStaticClazz() {
        return staticClazz;
    }

    public void setStaticClazz(boolean staticClazz) {
        this.staticClazz = staticClazz;
    }

    public String getQualifiedName() {

        return this.pkg + "." + this.name;
    }

    public List<MethodStruct> getMatchingMethods(String methodQnameWithoutArgs) {
        List<MethodStruct> m_list = new LinkedList<MethodStruct>();

        if(this.getMethods() != null && !this.getMethods().isEmpty()) {
            for(MethodStruct m_struct : this.getMethods().values()) {
                if(m_struct.getQualifiedNameWithoutArgs().equals(methodQnameWithoutArgs)) {
                    m_list.add(m_struct);
                }
            }
        }
        return m_list;
    }

    @Override
    public String toString() {
        return "ClassOrInterfaceStruct [qname=" + this.getQualifiedName() + ", methods=" + new PrettyPrintingMap<String, MethodStruct>(methods) + ", variables=" + variables.toString() + ", interfacesImplemented=" + interfacesImplemented + ", superClasses=" + superClasses + ", imports=" + imports + "]";
    }
}
