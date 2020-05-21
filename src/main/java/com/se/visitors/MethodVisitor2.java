package com.se.visitors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.container.MethodCallContainer;
import com.se.entity.ClassInfo;
import com.se.entity.Method;
import com.se.entity.MethodInfo;
import com.se.entity.Variable;
import com.se.utils.MethodUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * 使用Google版本的JavaParser（旧版本）
 */
public class MethodVisitor2 extends VoidVisitorAdapter {


    public MethodVisitor2(String projectName, String fileName, Connection conn){
        this.projectName = projectName;
        this.fileName = fileName;
        this.conn = conn;
    }

    private Connection conn;
    private String projectName;
    private String fileName;
    private String pkg; //包名
    private String clazz;   //类名
    private Map<String, String> importsWithoutAsterisk = new HashMap<>();
    private Map<String, String> importsWithAsterisk = new HashMap<>();
    private Map<String, Variable> instanceVariableMap = new HashMap<>(); //方法所属的类中定义的字段
    private Map<String, Variable> variableMap = new HashMap<>();    //调用者方法里面定义的变量
    private Map<String, Variable> calledMethodParamMap = new HashMap<>();   //被调用方法的参数

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
     * import
     * example -> key:"List", value:"java.util.List"
     * @param importStmt
     * @param arg
     */
    @Override
    public void visit(ImportDeclaration importStmt, Object arg) {
        String importLine = importStmt.getName().toString();
        //todo:解决带"*"的import
        //判断import语句中是否包含星号
        if(importStmt.isAsterisk()){
            importLine = importLine.concat(".");
            importsWithAsterisk.put(importStmt.getName().toString(), importLine);
        } else {
//            importsWithoutAsterisk.put(importStmt.getName().toString(), importLine);
            String importStr = importStmt.getName().getIdentifier();
            importsWithoutAsterisk.put(importStr, importLine);
        }
        super.visit(importStmt, arg);
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

//    @Override
//    public void visit(TypeDeclarationStmt n, Object arg) {
//        super.visit(n, arg);
//    }

    /**
     * 方法所属的类中创建的字段的map
     * fields created in the class scope of the caller method.
     * call this method for every field.
     * todo: 有没有考虑静态变量
     * @param field
     * @param arg
     */
    public void visit(FieldDeclaration field, Object arg){
        Variable fieldVar = new Variable();
        //类名
        fieldVar.setClazz(field.getCommonType().toString());

        //范型类，类名不包含"<类名>"
        //example: List<String> -> List
        if(fieldVar.getClazz().contains("<")){
            fieldVar.setClazz(fieldVar.getClazz().substring(0, fieldVar.getClazz().indexOf('<')));
        }

        //todo:为什么是get(0),一行定义了多个变量？ example: String x, y = "aaa";
        fieldVar.setName(field.getVariables().get(0).getName().toString());

        //todo:处理带"*"的import
        String importString = getClazzNameWithPackage(fieldVar.getClazz());

        //包名
        String varPkg;
        if(importString == null){
            if(MethodUtils.isJavaLang(fieldVar.getClazz())){
                varPkg = "java.lang";
            } else if(MethodUtils.isJavaBasicType(fieldVar.getClazz())){
                varPkg = "java.lang";
                fieldVar.setClazz(MethodUtils.basicToLange(fieldVar.getClazz()));
            } else {
                varPkg = this.pkg;
            }
        } else {
            varPkg = importString.substring(0, importString.lastIndexOf("."));
        }
        fieldVar.setPkg(varPkg);

        instanceVariableMap.put(fieldVar.getName(), fieldVar);

        //TODO:hanlde overriding
        //TODO: handle factory implementation

        super.visit(field, arg);

    }

    /**
     * 调用者方法里面定义的变量
     * @param varExpr
     * @param arg
     */
    @Override
    public void visit(VariableDeclarationExpr varExpr, Object arg) {
        Variable var = new Variable();
        var.setClazz(varExpr.getCommonType().toString());
        if(var.getClazz().contains("<"))
        {
            var.setClazz(var.getClazz().substring(0,var.getClazz().indexOf('<')));
        }
        var.setName(varExpr.getVariables().get(0).getName().toString());

        //String importString = importsWithoutAsterisk.get(var.getClazz());
        String importString = getClazzNameWithPackage(var.getClazz());
        String varPkg;
        if(importString == null) {
            //Default package;
            if(MethodUtils.isJavaLang(var.getClazz()))
            {
                varPkg = "java.lang";
            }else if(MethodUtils.isJavaBasicType(var.getClazz())){
                varPkg = "java.lang";
                var.setClazz(MethodUtils.basicToLange(var.getClazz()));
            }
            else{
                varPkg = this.pkg;
            }
        } else {
            varPkg = importString.substring(0, importString.lastIndexOf("."));
        }
        var.setPkg(varPkg);
        //System.out.println("Var name " + var.getName() + " Type " + var.getClazz() + " pkg " + var.getPkg());
        variableMap.put(var.getName(), var);
        super.visit(varExpr, arg);
    }

    /**
     * get and save caller method info,
     * and examine every statement of the caller method to find out method call statement.
     * todo: 处理多态
     * @param n
     * @param arg
     */
    @Override
    public void visit(MethodDeclaration n, Object arg) {
        super.visit(n, arg);
        //set caller method info
        Method callerMethod = new Method();
        callerMethod.setName(n.getName().asString());
        callerMethod.setClazz(clazz);
        callerMethod.setPkg(pkg);

        //collect and add param type to list
        List<Parameter> paramList = n.getParameters();
        List<String> paramTypeList = new ArrayList<>(); //only collect the param type
        if(paramList!= null && !paramList.isEmpty()){
            for(Parameter parameter : paramList){
                paramTypeList.add(parameter.getType().toString());
            }
        }
        callerMethod.setParamTypeList(paramTypeList);
        callerMethod.setReturnType(n.getType());
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        MethodInfo methodInfo = new MethodInfo(projectName,callerMethod);
        try {
            methodInfoDAO.InsertMethodInfo(methodInfo,conn);
        } catch (SQLException e) {
//            System.out.println(methodInfo.getID());
//            System.out.println(methodInfo.getMethodName());
//            System.out.println(methodInfo.getReturnType());
            e.printStackTrace();
        }


        //add method scope variables to list
        Map<String, Variable> methodParams = new HashMap<String, Variable>();
        List<Parameter> methodCallArgs =  n.getParameters(); //params of caller method
        if(methodCallArgs != null && !methodCallArgs.isEmpty()) {
            for(Parameter methodCallArg : methodCallArgs) {
                //get caller method param info
                Variable var = new Variable();
                var.setClazz(methodCallArg.getType().toString());
                if(var.getClazz().contains("<")) {
                    var.setClazz(var.getClazz().substring(0,var.getClazz().indexOf('<')));
                }
                var.setName(methodCallArg.getNameAsString());
                //todo：import *
                //String pkg = importsWithoutAsterisk.get(var.getClazz());
                String pkg = getClazzNameWithPackage(var.getClazz());

                if(pkg == null) {
                    //Defaulty package;
                    if(MethodUtils.isJavaLang(var.getClazz())) {
                        pkg = "java.lang";
                    } else if (MethodUtils.isJavaBasicType(var.getClazz())){
                        pkg = "java.lang";
                        var.setClazz(MethodUtils.basicToLange(var.getClazz()));
                    } else {
                        pkg = this.pkg;
                    }
                } else if(pkg.contains("."))
                    var.setPkg(pkg.substring(0, pkg.lastIndexOf(".")));
                else
                    var.setPkg(pkg);
                methodParams.put(var.getName(), var);
            }
        }

        //when method has body, not just method declaration
        if(n.getBody() != null){
            List<Statement> stmts = n.getBody().get().getStatements();
            if(stmts == null) //when method has empty body
                return;
            //scan every statement to find out method call statements and caller method
            for(Statement stmt : stmts){
                if(stmt.isExpressionStmt()){ //expression statement
                    ExpressionStmt exprStmt = (ExpressionStmt) stmt;
                    Expression exp = exprStmt.getExpression();
                    if(exp.isMethodCallExpr()){ //is a method call expression
                        this.resolveMethodInvocation(exp, methodParams, callerMethod);
                    } else if(exp.isVariableDeclarationExpr()){
                        //is a variable declaration expression
                        VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr)exp;
                        List<VariableDeclarator> vds = variableDeclarationExpr.getVariables();
                        for(VariableDeclarator vd:vds){
                            Expression expression1 = vd.getInitializer().isPresent() ? vd.getInitializer().get() : null;
                            if(expression1 != null && expression1.isMethodCallExpr()) {
                                this.resolveMethodInvocation(expression1,methodParams,callerMethod);
                            }
                        }
                    } else if(exp.isObjectCreationExpr()){
                        List<Expression> arguments = ((ObjectCreationExpr) exp).getArguments();
                        for(Expression expression:arguments){
                            if(expression.isMethodCallExpr()){
                                this.resolveMethodInvocation(expression,methodParams,callerMethod);
                            }
                        }
                    } else if(exp.isBinaryExpr()){
                        Expression left = ((BinaryExpr) exp).getLeft();
                        if(left.isMethodCallExpr()){
                            this.resolveMethodInvocation(left,methodParams,callerMethod);
                        }
                        Expression right = ((BinaryExpr) exp).getRight();
                        if(left.isMethodCallExpr()){
                            this.resolveMethodInvocation(right,methodParams,callerMethod);
                        }
                    } else if(exp.isFieldAccessExpr()){
                        System.out.println("###############################" + exp);
                    } else if(exp.isAssignExpr()){
                        Expression expression = ((AssignExpr) exp).getValue();
                        if(expression.isMethodCallExpr()){
                            this.resolveMethodInvocation(expression,methodParams,callerMethod);
                        }
                    }
                }
            }
        }
    }

    private void resolveMethodInvocation(Expression exp, Map<String, Variable> methodParams, Method callerMethod){
        MethodCallExpr mexpr = (MethodCallExpr)exp;

        Method calledMethod = new Method();
        calledMethod.setName(mexpr.getNameAsString());

        //get param type list of called method
        List<String> paramTypeList = getCalledMethodParamType(mexpr, methodParams);
        calledMethod.setParamTypeList(paramTypeList);

        String methodVarName = null;
        if(!mexpr.getScope().isPresent()){
            //Calling method within same class
        } else {
            methodVarName = mexpr.getScope().get().toString();
        }
        Variable methodVar = null;

        //find variable's info of called method
        //check caller method params -> variable map -> class field variable
        if(methodParams.containsKey(methodVarName)){
            methodVar = methodParams.get(methodVarName);
        } else if (variableMap.containsKey(methodVarName)){
            methodVar = variableMap.get(methodVarName);
        } else if(instanceVariableMap.containsKey(methodVarName)){
            methodVar = instanceVariableMap.get(methodVarName);
        } else if(mexpr.getScope().isPresent()){
            methodVar = new Variable();
            methodVar.setClazz(mexpr.getScope().get().toString());
            methodVar.setStaticVar(true);
        } else {
            methodVar = new Variable();
            methodVar.setClazz(this.clazz);
            methodVar.setPkg(this.pkg);
        }

        if((!MethodUtils.isJavaLang(methodVar.getID()))&&(!MethodUtils.isJDKMethod(methodVar.getID()))){
            calledMethod.setClazz(methodVar.getClazz());
            calledMethod.setPkg(methodVar.getPkg());
            MethodCallContainer.getContainer().addMethodCall(callerMethod, calledMethod);
        }
    }

    /**
     * 获取被调用方法参数的类型
     * @param mexpr
     * @param methodParams
     * @return
     */
    private List<String> getCalledMethodParamType(MethodCallExpr mexpr, Map<String,Variable> methodParams){
        List<Expression> params = mexpr.getArguments();
        List<NameExpr> paramNamrs = new ArrayList<>();
        List<String> paramTypeList = new ArrayList<>();

        if(params!=null && !params.isEmpty()){
            for(Expression e : params){
                if(e instanceof NameExpr){
                    //System.out.println(((NameExpr)e).getName());
                    paramNamrs.add((NameExpr)e);
                } else if(e instanceof  MethodCallExpr){
                    this.getCalledMethodParamType((MethodCallExpr)e, methodParams);
                }
            }
        }

        for(NameExpr pn : paramNamrs){
            String paramName = pn.getNameAsString();
            Variable paramVar;
            if(methodParams.containsKey(paramName)) {
                paramVar = methodParams.get(paramName);
            } else if(variableMap.containsKey(paramName)) {
                paramVar = variableMap.get(paramName);
            } else if(instanceVariableMap.containsKey(paramName)) {
                paramVar = instanceVariableMap.get(paramName);
            } else if(mexpr.getScope().isPresent()) {
                //Static method calls. in static method scope contains static call details.
                paramVar= new Variable();
                paramVar.setClazz(mexpr.getScope().get().toString());
                paramVar.setStaticVar(true);
            } else {
                //calling method within itself so no scope.
                paramVar= new Variable();
                paramVar.setClazz(this.clazz);
                paramVar.setPkg(this.pkg);
            }
            calledMethodParamMap.put(paramName, paramVar);
            paramTypeList.add(paramVar.getClazz());
        }

        return paramTypeList;
    }

    /**
     * 根据类名从import语句中获取带包名的类名
     * @param clazzName
     * @return
     */
    private String getClazzNameWithPackage(String clazzName){
        String importString = null;
        if(importsWithoutAsterisk.get(clazzName) != null)
            importString = importsWithoutAsterisk.get(clazzName);
        else{
            Iterator<Map.Entry<String, String>> it = importsWithAsterisk.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, String> entry = it.next();
                String importStmt = entry.getValue();

                String fullClassName = importStmt.concat(clazzName);

                Class clazz;

                try{
                    clazz = Class.forName(fullClassName);
                    //System.out.println(clazz.getName());
                    return fullClassName;
                } catch (ClassNotFoundException e){
                    //System.out.println("not this "+fullClassName);
                }

            }
        }
        return importString;
    }
}
