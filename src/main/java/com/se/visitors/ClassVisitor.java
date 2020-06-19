package com.se.visitors;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.DAO.ClassInfoDAO;
import com.se.entity.ClassInfo;

import java.sql.Connection;
import java.sql.SQLException;

public class ClassVisitor extends VoidVisitorAdapter {
    private Connection conn;
    private String projectName;
    private String fileName;
    private String pkg; //包名
    private String clazz;   //类名

    public ClassVisitor(String projectName, String fileName, Connection conn){
        this.projectName = projectName;
        this.fileName = fileName;
        this.conn = conn;
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
        this.clazz = n.getName().asString().trim();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(this.pkg +"."+ this.clazz);
        classInfo.setInterface(n.isInterface());
        classInfo.setProjectName(this.projectName);
        classInfo.setFileName(this.fileName);
        ClassInfoDAO classInfoDAO = new ClassInfoDAO();
        try {
            classInfoDAO.InsertClassInfo(classInfo,conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.visit(n, arg);
    }
}