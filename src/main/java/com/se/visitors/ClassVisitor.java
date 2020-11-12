package com.se.visitors;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.entity.ClassInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassVisitor extends VoidVisitorAdapter {

    private String projectName;
    private String filePath;
    private String pkg; //包名
    private String clazz;   //类名
    private List<ClassInfo> classInfoList;
    private Map<String,String> fieldAndType;

    public ClassVisitor(String projectName, String filePath){
        this.projectName = projectName;
        this.filePath = filePath;
        this.classInfoList = new ArrayList<>();
        this.fieldAndType=new HashMap<>();
    }

    /**
     * package
     * example -> com.se.entity
     * @param pkgDec
     * @param arg
     */
    @Override
    public void visit(PackageDeclaration pkgDec, Object arg) {
        String pkgDecString = pkgDec.toString();
        String[] tokens = pkgDecString.split(" ");
        String pkgToken = tokens[tokens.length - 1].trim();
        this.pkg = pkgToken.substring(0, pkgToken.length() - 1);
        super.visit(pkgDec, arg);
    }

    /**
     * class name
     * example -> MethodVisitor
     * @param n
     * @param arg
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        this.clazz = this.dollaryName(n);
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(this.pkg +"."+ this.clazz);
        classInfo.setInterface(n.isInterface());
        classInfo.setProjectName(this.projectName);
        this.filePath = this.filePath.replace("\\","|");
        classInfo.setFilePath(this.filePath);
        this.classInfoList.add(classInfo);
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, Object arg) {
        this.fieldAndType.put(n.getVariable(0).toString(),n.getVariable(0).getTypeAsString());
        super.visit(n, arg);
    }

    private String dollaryName(TypeDeclaration<?> n) {
        if (n.isNestedType()) {
            return dollaryName((TypeDeclaration<?>) n.getParentNode().get()) + "$" + n.getNameAsString();
        }
        return n.getNameAsString();
    }
    public List<ClassInfo> getClassInfoList() {
        return this.classInfoList;
    }

    public void setClassInfoList(List<ClassInfo> classInfoList) {
        this.classInfoList = classInfoList;
    }

    public Map<String,String> getFieldMap() {
        return this.fieldAndType;
    }
}