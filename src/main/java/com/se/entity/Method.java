package com.se.entity;

//import japa.parser.ast.type.Type;
import com.github.javaparser.ast.type.Type;

import java.util.List;

public class Method {

    private String name;

    private String clazz;

    private String pkg;

    private Type returnType;

    private String returnTypeStr;

    private List<String> paramTypeList;

    private int beginLine;

    private int endLine;

    private String methodContent;

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

    private Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public String getReturnTypeStr() {
        if(this.getReturnType().getComment().isPresent()){
            this.returnTypeStr = this.getReturnType().toString().replace(this.getReturnType().getComment().get().toString(), "");
        } else {
            this.returnTypeStr = this.getReturnType().toString();
        }
        return returnTypeStr;
    }

    public List<String> getParamTypeList() {
        return paramTypeList;
    }

    public void setParamTypeList(List<String> paramTypeList) {
        this.paramTypeList = paramTypeList;
    }

    public String getQualifiedName() {
        StringBuffer buff = new StringBuffer();
        //if in default package.
        if(pkg != null) {
            buff.append(pkg).append(".");
        }
        buff.append(clazz).append(".").append(name);
        return buff.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String qualifiedName = this.getQualifiedName();
        result = prime * result + ( ( qualifiedName == null ) ? 0 : qualifiedName.hashCode() );
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Method other = (Method)obj;
        String qualifiedName = this.getQualifiedName();
        String otherQualifiedName = other.getQualifiedName();

        if ( qualifiedName == null ) {
            if ( otherQualifiedName != null )
                return false;
        }
        else if ( !qualifiedName.equals(otherQualifiedName) )
            return false;
        return true;
    }

    @Override
    public String toString(){
        StringBuffer callerMethodParameter = new StringBuffer();
        callerMethodParameter.append("(");
        for(String paramType : this.getParamTypeList()){
            callerMethodParameter.append(paramType).append(",");
        }
        callerMethodParameter.append(")");

        return this.getQualifiedName().concat(callerMethodParameter.toString());
    }

    public String getPackageAndClassName() {
        StringBuffer buff = new StringBuffer();
        //if in default package.
        if(pkg != null) {
            buff.append(pkg).append(".");
        }
        buff.append(clazz);
        return buff.toString();
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

    public String getMethodContent() {
        return methodContent;
    }

    public void setMethodContent(String methodContent) {
        this.methodContent = methodContent;
    }
}
