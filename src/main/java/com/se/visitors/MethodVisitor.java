package com.se.visitors;

import com.github.javaparser.Range;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.DAO.ClassInfoDAO;
import com.se.container.MethodCallContainer;
import com.se.container.MethodInfoContainer;
import com.se.entity.Method;
import com.se.entity.MethodInfo;
import com.se.entity.Variable;
import com.se.utils.MethodUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class MethodVisitor extends VoidVisitorAdapter {

    private String projectName;
    private String pkg; //包名
    private String clazz;   //类名
    private String filePath;
    private List<String> classInfoList;
    private Map<String, String> importsWithoutAsterisk = new HashMap<>();
    private Map<String, String> importsWithAsterisk = new HashMap<>();
    private Map<String, Variable> fieldMap = new HashMap<>(); //方法所属的类中定义的字段
    private Map<String, Variable> methodVariableMap = new HashMap<>();    //调用者方法里面定义的变量
    private Map<String, Variable> calledMethodParamMap = new HashMap<>();   //被调用方法的参数

    private Connection conn;


    private String superClassWithPackageName;
    private List<String> impInterfaceWithPackageNames = new ArrayList<>();

    public MethodVisitor(String projectName,List<String> classInfoList){
        this.projectName = projectName;
        this.classInfoList = classInfoList;
    }

    public MethodVisitor(String projectName, List<String> classInfoList, String filePath, Connection conn){
        this.projectName = projectName;
        this.classInfoList = classInfoList;
        this.filePath = filePath;
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
     * import
     * example -> key:"List", value:"java.util.List"
     * @param importStmt
     * @param arg
     */
    @Override
    public void visit(ImportDeclaration importStmt, Object arg) {
        String importLine = importStmt.getName().toString();
        //todo:解决第三方或者用户创建的package，带"*"的import
        //判断import语句中是否包含星号
        if(importStmt.isAsterisk()){
            importLine = importLine.concat(".");
            importsWithAsterisk.put(importStmt.getName().toString(), importLine);
        } else {
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
        this.clazz = this.dollaryName(n);

        this.importsWithoutAsterisk.put(this.clazz, this.pkg.concat(".").concat(this.clazz));

        List<String> superClass = n.getExtendedTypes().stream()
                .map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toList());

        if(superClass.size() > 0){
            String packageNameOfSuperClass = importsWithoutAsterisk.get(superClass.get(0));
            if(packageNameOfSuperClass == null){

                for (Map.Entry<String, String> entry : importsWithAsterisk.entrySet()) {
                    String importStmt = entry.getValue();
                    String _superClassPackageName = importStmt.concat(".").concat(superClass.get(0));

                    if(classInfoList != null && classInfoList.contains(_superClassPackageName)){
                        this.superClassWithPackageName = _superClassPackageName;
                        break;
                    }
                }
            } else {
                this.superClassWithPackageName = packageNameOfSuperClass;
            }
        }


        List<String> implementedInterfaces = n.getImplementedTypes().stream()
                .map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toList());

        for(String implInterface : implementedInterfaces){
            String interfacePackageName = importsWithoutAsterisk.get(implInterface);
            if(interfacePackageName == null){
                for (Map.Entry<String, String> entry : importsWithAsterisk.entrySet()) {
                    String importStmt = entry.getValue();
                    String _interfacePackageName = importStmt.concat(".").concat(implInterface);

                    if(classInfoList != null && classInfoList.contains(_interfacePackageName)){
                        impInterfaceWithPackageNames.add(_interfacePackageName);
                        break;
                    }
                }
            } else {
                impInterfaceWithPackageNames.add(interfacePackageName);
            }

        }

        String fullClassName = this.pkg.concat(".").concat(this.clazz);

        if(this.superClassWithPackageName != null){
            try {
                ClassInfoDAO.updateSuperClass(this.superClassWithPackageName, fullClassName,this.projectName, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(this.impInterfaceWithPackageNames.size() > 0){
            try {
                ClassInfoDAO.updateImplInterfaces(this.impInterfaceWithPackageNames, fullClassName, this.projectName, conn);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        super.visit(n, arg);
    }

    private String dollaryName(TypeDeclaration<?> n) {
        if (n.isNestedType()) {
            return dollaryName((TypeDeclaration<?>) n.getParentNode().get()) + "$" + n.getNameAsString();
        }
        return n.getNameAsString();
    }


    /**
     * 方法所属的类中创建的字段的map: instanceVariableMap
     * fields created in the class scope of the caller method.
     * call this method for every field.
     * todo: 考虑静态变量
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
            fieldVar.setGenericType(fieldVar.getClazz().substring(fieldVar.getClazz().indexOf('<')));
            fieldVar.setClazz(fieldVar.getClazz().substring(0, fieldVar.getClazz().indexOf('<')));
        }
        fieldVar.setName(field.getVariables().get(0).getName().toString());
        //处理带"*"的import
        String importString = getClazzNameWithPackage(fieldVar.getClazz());
        //包名
        String varPkg;
        if(importString == null){
            if(MethodUtils.isJavaLang(fieldVar.getClazz())){
                varPkg = "java.lang";
            } else if(MethodUtils.isJavaBasicType(fieldVar.getClazz())){
                varPkg = "java.lang";
                fieldVar.setClazz(varPkg.concat(MethodUtils.basicToLange(fieldVar.getClazz())));
            } else {
                varPkg = this.pkg;
            }
        } else {
            varPkg = importString.substring(0, importString.lastIndexOf("."));
        }
        fieldVar.setPkg(varPkg);
        fieldMap.put(fieldVar.getName(), fieldVar);

        //TODO:hanlde overriding
        //TODO: handle factory implementation

        super.visit(field, arg);

    }

    /**
     * 调用者方法里面定义的变量: variableMap
     * todo: 目前是收集类中所有方法中的所有定义的变量，应该改成在处理一个方法时，只收集此方法内创建的变量
     * @param varExpr
     * @param arg
     */
    @Override
    public void visit(VariableDeclarationExpr varExpr, Object arg) {
        Variable var = new Variable();
        var.setClazz(varExpr.getCommonType().toString());
        if(var.getClazz().contains("<")) {
            var.setGenericType(var.getClazz().substring(var.getClazz().indexOf('<')));
            var.setClazz(var.getClazz().substring(0,var.getClazz().indexOf('<')));
        }
        var.setName(varExpr.getVariables().get(0).getName().toString());
        String importString = getClazzNameWithPackage(var.getClazz());
        String varPkg;
        if(importString == null) {
            //Default package;
            if(MethodUtils.isJavaLang(var.getClazz())) {
                varPkg = "java.lang";
            } else if(MethodUtils.isJavaBasicType(var.getClazz())){
                varPkg = "java.lang";
                var.setClazz(varPkg.concat(MethodUtils.basicToLange(var.getClazz())));
            } else {
                varPkg = this.pkg;
            }
        } else {
            varPkg = importString.substring(0, importString.lastIndexOf("."));
        }
        var.setPkg(varPkg);
        methodVariableMap.put(var.getName(), var);
        super.visit(varExpr, arg);
    }

    /**
     * get and save caller method info,
     * and examine every statement of the caller method to find out method call statement.
     * 获取调用者方法的信息（方法名、方法参数等）
     * todo: 处理多态
     * @param n
     * @param arg
     */
    @Override
    public void visit(MethodDeclaration n, Object arg) {
        super.visit(n, arg);
        //过滤长度小于四行的方法
        int lineCount =  n.getRange().get().getLineCount();
        if(lineCount<=4){
            return;
        }
        //create caller method info
        Method callerMethod = new Method();
        callerMethod.setName(n.getName().asString());
        callerMethod.setClazz(clazz);
        callerMethod.setPkg(pkg);
        callerMethod.setMethodContent(n.toString());
        callerMethod.setFilePath(this.filePath);
        Optional<BlockStmt> body = n.getBody();
        if(body.isPresent()){
            Optional<Range> range = n.getRange();
            if(range.isPresent()){
                Range r = range.get();
                callerMethod.setBeginLine(r.begin.line);
                callerMethod.setEndLine(r.end.line);
            }
        }

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

        MethodInfo methodInfo = new MethodInfo(projectName,callerMethod);
//        MethodInfoContainer.getContainer().addMethodInfo(methodInfo);
        MethodInfoContainer.getContainer().addMethodInfo(projectName, methodInfo);

        //add method scope variables to list
        Map<String, Variable> methodParams = new HashMap<>();
        List<Parameter> methodCallArgs =  n.getParameters(); //params of caller method
        if(methodCallArgs != null && !methodCallArgs.isEmpty()) {
            for(Parameter methodCallArg : methodCallArgs) {
                //get caller method param info
                Variable var = new Variable();
                var.setClazz(methodCallArg.getType().toString());
                if(var.getClazz().contains("<")) {
                    var.setGenericType(var.getClazz().substring(var.getClazz().indexOf('<')));
                    var.setClazz(var.getClazz().substring(0,var.getClazz().indexOf('<')));
                }

                var.setName(methodCallArg.getNameAsString());
                //import *
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
                }else {
                    pkg = pkg.substring(0,pkg.lastIndexOf('.'));
                }
                if(pkg.contains("."))
                    var.setPkg(pkg);
                else
                    var.setPkg(pkg);
                methodParams.put(var.getName(), var);
            }
        }
        //when method has body, not just method declaration
        if(n.getBody().isPresent()){
            this.visitStmt(n.getBody().get(), methodParams, callerMethod);
        }
    }



    /**
     * 方法调用
     * @param exp
     * @param methodParams
     * @param callerMethod
     */
    private void resolveMethodInvocation(Expression exp, Map<String, Variable> methodParams, Method callerMethod){

        MethodCallExpr mexpr = (MethodCallExpr)exp;
        if(mexpr.getScope().isPresent() && mexpr.getScope().get().isMethodCallExpr()) //处理方法调用链，递归实现
            this.resolveMethodInvocation(mexpr.getScope().get(), methodParams, callerMethod);
        Method calledMethod = new Method();
        calledMethod.setName(mexpr.getNameAsString());
        //get param type list of called method
        List<String> paramTypeList = getCalledMethodParamType(mexpr, methodParams, callerMethod);
        calledMethod.setParamTypeList(paramTypeList);
        String methodVarName = null;
        if(!mexpr.getScope().isPresent()){ //不是"var.method()"的形式，而是"method()"形式
            //Calling method within same class
            //在同一个类内调用方法，根据方法名查询方法，设置方法参数
        } else {
            methodVarName = mexpr.getScope().get().toString();
        }
        Variable methodVar = null;
        //find variable's info of called method
        //check caller method params -> variable map -> class field variable
        if(methodParams.containsKey(methodVarName)){
            methodVar = methodParams.get(methodVarName);
        } else if (methodVariableMap.containsKey(methodVarName)){
            methodVar = methodVariableMap.get(methodVarName);
        } else if(fieldMap.containsKey(methodVarName)){
            methodVar = fieldMap.get(methodVarName);
        } else if(mexpr.getScope().isPresent()){
            //静态方法调用
            methodVar = new Variable();
            methodVar.setClazz(mexpr.getScope().get().toString());
            methodVar.setStaticVar(true);
            String pkg = importsWithoutAsterisk.get(methodVarName);
            if(pkg == null){
                //没有星号的import匹配不上的话，就要根据带星号的import加上所有项目的类进行匹配
                //如果还是匹配不上，那么这个静态方法调用使用的是第三方类的方法并且导入第三方类的时候用了*号，
                //或者是调用类内的静态方法
            }
            if(pkg!=null){
                pkg = pkg.substring(0,pkg.lastIndexOf('.'));
            }
            methodVar.setPkg(pkg);
        } else {
            methodVar = new Variable();
            methodVar.setClazz(this.clazz);
            methodVar.setPkg(this.pkg);
        }
        //过滤掉JDK的方法，只存储JDK或者第三方的方法调用
        if((!MethodUtils.isJavaLang(methodVar.getID()))&&(!MethodUtils.isJDKMethod(methodVar.getID()))){
            if(methodVar.getClazz().equals("this"))
                calledMethod.setClazz(this.clazz);  //example: this.methodName()
            else
                calledMethod.setClazz(methodVar.getClazz());
            calledMethod.setPkg(methodVar.getPkg());
            MethodCallContainer.getContainer().addMethodCall(projectName, callerMethod, calledMethod);
            //System.err.println(calledMethod.getPackageAndClassName() + "..." + calledMethod.getPkg() + ".." + methodVar.getName());
        }

    }

    /**
     * 获取被调用方法参数的类型
     * @param mexpr
     * @param methodParams
     * @return
     */
    private List<String> getCalledMethodParamType(MethodCallExpr mexpr, Map<String,Variable> methodParams, Method callerMethod){

        List<Expression> params = mexpr.getArguments();

        List<String> paramTypeList = new ArrayList<>();

        List<Expression> paramNamrs = new ArrayList<>();

        if(params!=null && !params.isEmpty()){
            for(Expression e : params){
                if(e.isNameExpr()){
                    paramNamrs.add(e);
                } else if(e.isMethodCallExpr()){
                    //params of called method is still a method call 参数仍然为方法调用
                    paramNamrs.add(e);
                    this.resolveMethodInvocation(e, methodParams, callerMethod);
                }
            }
        }

        for(Expression pn : paramNamrs){
            String paramName = pn.isNameExpr() ? ((NameExpr) pn).getNameAsString() : ((MethodCallExpr) pn).getNameAsString();
            Variable paramVar;
            if(methodParams.containsKey(paramName)) {
                paramVar = methodParams.get(paramName);
            } else if(methodVariableMap.containsKey(paramName)) {
                paramVar = methodVariableMap.get(paramName);
            } else if(fieldMap.containsKey(paramName)) {
                paramVar = fieldMap.get(paramName);
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

            if(paramVar.getGenericType() != null && !paramVar.getGenericType().isEmpty())
                paramTypeList.add(paramVar.getClazz().concat(paramVar.getGenericType()));
            else
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

                if(classInfoList != null && classInfoList.contains(fullClassName))
                    return fullClassName;

                try{
                    Class clazz = Class.forName(fullClassName);
                    return fullClassName;
                } catch (ClassNotFoundException e){
                }catch (NoClassDefFoundError ex){
                    importString =  fullClassName;
                }
            }
        }
        return importString;
    }

    /**
     * 检查一个方法体/block中的每一条语句，判断是否包含方法调用
     * @param stmt
     * @param methodParams
     * @param callerMethod
     */

    private void visitStmt(Statement stmt, Map<String, Variable> methodParams, Method callerMethod){
        if(stmt.isExpressionStmt()){ //expression statement
            ExpressionStmt exprStmt = (ExpressionStmt) stmt;
            Expression exp = exprStmt.getExpression();
            this.getMethodCallInExpression(exp, methodParams, callerMethod);
        }

        else if (stmt.isForStmt()){
            ForStmt forStmt = (ForStmt) stmt;

            forStmt.getInitialization().forEach(item->this.getMethodCallInExpression(item, methodParams, callerMethod)); //初始化
            forStmt.getUpdate().forEach(item->this.getMethodCallInExpression(item, methodParams, callerMethod));    //更新

            if(forStmt.getCompare().isPresent())
                this.getMethodCallInCondtionExpr(forStmt.getCompare().get(), methodParams, callerMethod);   //比较

            //循环体
            if(forStmt.getBody() != null){
                this.visitStmt(forStmt.getBody(), methodParams, callerMethod);
            }

        } else if(stmt.isIfStmt()){
            IfStmt ifStmt = (IfStmt) stmt;

            //expression
            Expression conditionalExpr = ifStmt.getCondition();
            this.getMethodCallInCondtionExpr(conditionalExpr,methodParams, callerMethod);//find method call in condition expression

            //then statement
            //then statement is not empty
            if(ifStmt.getThenStmt() != null) {
                this.visitStmt(ifStmt.getThenStmt(), methodParams, callerMethod);
            }

            //else statement
            if(ifStmt.getElseStmt().isPresent()) {  //is a block
                this.visitStmt(ifStmt.getElseStmt().get(), methodParams, callerMethod);
            }

        } else if(stmt.isWhileStmt()){
            WhileStmt whileStmt = (WhileStmt) stmt;

            //expression
            Expression conditionalExpr = whileStmt.getCondition();
            this.getMethodCallInCondtionExpr(conditionalExpr,methodParams, callerMethod); //find method call in condition expression

            //then statement
            if(whileStmt.getBody() != null) {
                this.visitStmt(whileStmt.getBody(), methodParams, callerMethod);
            }

        } else if(stmt.isReturnStmt()){

            if(((ReturnStmt) stmt).getExpression().isPresent())
                this.getMethodCallInExpression(((ReturnStmt) stmt).getExpression().get(), methodParams, callerMethod);

        }  else if(stmt.isSwitchStmt()){
//            System.out.println("switch");
        } else if(stmt.isTryStmt()){
            //try block
            this.visitStmt(((TryStmt)stmt).getTryBlock(), methodParams, callerMethod);

            //catch clause
            List<CatchClause> catchClauseList = ((TryStmt)stmt).getCatchClauses();
            catchClauseList.forEach(item-> this.visitStmt(item.getBody(), methodParams, callerMethod));

            //finally block
            if(((TryStmt)stmt).getFinallyBlock().isPresent())
                this.visitStmt(((TryStmt)stmt).getFinallyBlock().get(), methodParams, callerMethod);

        } else if(stmt.isForEachStmt()){
            this.getMethodCallInExpression(((ForEachStmt)stmt).getVariable(), methodParams, callerMethod);
            this.getMethodCallInExpression(((ForEachStmt)stmt).getIterable(), methodParams, callerMethod);
            this.visitStmt(((ForEachStmt)stmt).getBody(), methodParams, callerMethod);

        } else if(stmt.isDoStmt()){
//            System.out.println("do");
        } else if(stmt.isBlockStmt()){
            //scan every statement to find out method call statements and caller method
            List<Statement> stmts = ((BlockStmt)stmt).getStatements();
            for(Statement tempStmt : stmts){
                this.visitStmt(tempStmt, methodParams, callerMethod);
            }
        }
    }

    /**
     * 提取条件表达式中的方法调用
     * @param expression
     */
    private void getMethodCallInCondtionExpr(Expression expression, Map<String, Variable> methodParams, Method callerMethod){
        //条件表达式可能是binary或者unary
        if(expression.isBinaryExpr()){
            BinaryExpr binaryExpr = (BinaryExpr) expression;
            this.getMethodCallInCondtionExpr(binaryExpr.getRight(), methodParams, callerMethod);
            this.getMethodCallInCondtionExpr(binaryExpr.getLeft(), methodParams, callerMethod);
        } else if (expression.isUnaryExpr()){
            UnaryExpr unaryExpr = (UnaryExpr) expression;
            if(unaryExpr.getExpression().isMethodCallExpr()){
                this.getMethodCallInCondtionExpr(unaryExpr.getExpression(), methodParams, callerMethod);
            }
        } else if (expression.isMethodCallExpr()){
            //处理method
            this.resolveMethodInvocation(expression, methodParams, callerMethod);
        }
    }

    /**
     * 提取表达式中的方法调用
     * @param exp
     * @param methodParams
     * @param callerMethod
     */
    private void getMethodCallInExpression(Expression exp, Map<String, Variable> methodParams, Method callerMethod){
        if(exp.isMethodCallExpr()){ //is a method call expression
            this.resolveMethodInvocation(exp, methodParams, callerMethod);
        } else if(exp.isVariableDeclarationExpr()){
            //is a variable declaration expression
            VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) exp;
            //ObjectCreationExpr
            //MethodCallExpr
            //CastExpr
            List<VariableDeclarator> vds = variableDeclarationExpr.getVariables();

            for(VariableDeclarator vd:vds){
                Expression expression1 = vd.getInitializer().isPresent() ? vd.getInitializer().get() : null;

                if(expression1!=null) {
                    this.getMethodCallInExpression(expression1,methodParams,callerMethod);
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
            //System.out.println("###############################" + exp);
        } else if(exp.isAssignExpr()){
            Expression expression = ((AssignExpr) exp).getValue();
            if(expression.isMethodCallExpr()){
                this.resolveMethodInvocation(expression,methodParams,callerMethod);
            }
        } else if(exp.isCastExpr())  {
            Expression expression = ((CastExpr) exp).getExpression();
            if(expression.isMethodCallExpr()){
                this.resolveMethodInvocation(expression, methodParams, callerMethod);
            }
        }else if(exp.isLambdaExpr()){
            //增加对于lambda表达式的解析
            Statement statement = ((LambdaExpr)exp).getBody();
            visitStmt(statement,methodParams,callerMethod);
        }
    }


}
