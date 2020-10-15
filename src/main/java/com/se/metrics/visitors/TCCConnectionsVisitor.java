package com.se.metrics.visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.metrics.calculators.TCCCalculator;
import com.se.metrics.exceptions.TCCException;

import java.util.List;

public class TCCConnectionsVisitor extends VoidVisitorAdapter<TCCCalculator> {
    @Override
    public void visit(MethodDeclaration methodDeclaration, TCCCalculator calculator) {
        String methodDeclarationString = methodDeclaration.getDeclarationAsString(true, false, false);

        // 不需要访问private方法
        if (methodDeclarationString.startsWith("private"))
            return;

        // 方法名的表示格式为 : "name type1 type2 ..."
        String methodPrototype = methodDeclaration.getName().toString();
        for(Parameter parameter : methodDeclaration.getParameters())
            methodPrototype += " " + parameter.getType();

        try {
            // 设置当前访问的方法名
            calculator.setCurrentMethod(methodPrototype);

            // 将方法的参数加入到局部变量list中
            if (!methodDeclaration.getParameters().isEmpty()) {
//                System.out.println("[TCC] Saving parameter");
                calculator.addLocalVariables(methodDeclaration.getParameters());
            }

            // 访问方法体
            super.visit(methodDeclaration, calculator);
        } catch (TCCException ex) {
//            System.out.println("[TCC] Error with current method : " + ex.getMessage());
        }
    }

    @Override
    public void visit(VariableDeclarationExpr variableDeclarationExpr, TCCCalculator calculator) {
        // 保存当前方法中创建的局部变量
        calculator.addLocalVariables(variableDeclarationExpr.getVariables(), variableDeclarationExpr.getCommonType());

        super.visit(variableDeclarationExpr, calculator);
    }

    @Override
    public void visit(MethodCallExpr methodCallExpr, TCCCalculator calculator) {

        /* 方法调用表达式的作用域可能是：
         * 1) null 或者 this: 对当前类的方法成员进行调用
         * 2) 当前类的成员变量： 当前方法直接访问成员变量
         * 3) 方法内创建的局部变量/对象： do nothing
         * 所有情况中，都要检查成员变量是否被作为参数传递
         */
        Expression scope = null;
        if(methodCallExpr.getScope().isPresent())
            scope = methodCallExpr.getScope().get();

        // 1) 被调用者为null或者调用了成员方法
        if (scope == null || scope.toString().equals("this"))
            this.memberMethodCalled(methodCallExpr, calculator);
        // 2) 被调用者为成员变量
        else if (calculator.containsMemberVariable(scope.toString()))
            this.memberVariableAccessed(scope.toString(), calculator);

        /* 检查被调用方法的参数是否为成员变量 */
        List<Expression> args = methodCallExpr.getArguments();
        if (args != null && !args.isEmpty()) {
            for(Expression arg : args) {
                String argString = arg.toString();

                // 移除 "this."
                if(argString.startsWith("this"))
                    argString = argString.substring(argString.lastIndexOf(".") +1);

                // 判断参数是否为成员变量
                if (calculator.isMemberVariable(argString))
                    this.memberVariableAccessed(argString, calculator);
            }
        }

        super.visit(methodCallExpr, calculator);
    }

    @Override
    public void visit(AssignExpr assignExpr, TCCCalculator calculator) {
        // 等号左侧的表达式
        String expression = assignExpr.getTarget().toString();

        // 移除 "this."
        if(expression.startsWith("this"))
            expression = expression.substring(expression.lastIndexOf(".") +1);

        // 判断等号左侧的表达式是否为成员变量
        if (calculator.isMemberVariable(expression))
            this.memberVariableAccessed(expression, calculator);


        // 等号右侧的表达式
        expression = assignExpr.getValue().toString();

        // 移除 "this."
        if(expression.startsWith("this"))
            expression = expression.substring(expression.lastIndexOf(".") +1);

        // 判断等号右侧的表达式是否为成员变量
        if (calculator.isMemberVariable(expression))
            this.memberVariableAccessed(expression, calculator);

        super.visit(assignExpr, calculator);
    }

    private void memberMethodCalled(MethodCallExpr methodCallExpr, TCCCalculator calculator) {
        // 当前方法调用了成员方法
        String calledMethodPrototype = methodCallExpr.getName().toString();

        try {
            // 获取获取被调用方法的参数类型
            List<Expression> args = methodCallExpr.getArguments();
            if (args != null && !args.isEmpty())
                for(Expression arg : args)
                    calledMethodPrototype += " " + calculator.getTypeOfVariable(arg.toString());

//            System.out.println("[TCC] method \"" + calculator.getCurrentMethod() + "\" invoke method \"" + calledMethodPrototype + "\"");

            calculator.addMemberMethodCall(calledMethodPrototype);
        }
        catch (TCCException ex) {
//            System.out.println("[TCC] Error in the construction of the prototype" + " of the method member " + methodCallExpr.getName() + " : " + ex.getMessage());
        }
    }

    private void memberVariableAccessed(String variable, TCCCalculator calculator) {
        try {
//            System.out.println("[TCC] method \"" + calculator.getCurrentMethod() + "\" access the variable \"" + variable + "\"");

            calculator.addMemberVariableAccess(variable);
        }
        catch (TCCException ex) {
//            System.out.println("[TCC] Error filling in a method" + " accessed a member variable : " + ex.getMessage());
        }
    }
}
