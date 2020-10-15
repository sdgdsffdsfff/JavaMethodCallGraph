package com.se.metrics.visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.metrics.calculators.ATFDCalculator;

import java.util.List;

public class ATFDVisitor extends VoidVisitorAdapter<ATFDCalculator>
{

    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, ATFDCalculator calculator) {
        // 如果是class
        if (!classOrInterfaceDeclaration.isInterface()) {
//            System.out.println("[ATFD] New class declaration = " + classOrInterfaceDeclaration.getName());

            calculator.addClassToRemove(classOrInterfaceDeclaration.getName().toString());
        }

        super.visit(classOrInterfaceDeclaration, calculator);
    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration, ATFDCalculator calculator) {
        List<VariableDeclarator> variables = fieldDeclaration.getVariables();

//        for(VariableDeclarator var : variables)
//            System.out.println("[ATFD] New variable member = " + fieldDeclaration.getType().toString() + " " + var.getId().getName());

        calculator.addMemberVariables(variables, fieldDeclaration.getCommonType());

        super.visit(fieldDeclaration, calculator);
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, ATFDCalculator calculator) {
//        System.out.println("[ATFD] New method = " + methodDeclaration.getName());

        if (!methodDeclaration.getParameters().isEmpty()) {
//            System.out.println("[ATFD] Saving parameter ");
            calculator.addLocalVariables(methodDeclaration.getParameters());
        }

        super.visit(methodDeclaration, calculator);

        // 清除当前方法的局部变量
        calculator.clearLocalVariables();
    }

    @Override
    public void visit(FieldAccessExpr fieldAccessExpr, ATFDCalculator calculator) {
//        System.out.println("[ATFD] New attribute accesses = " + fieldAccessExpr.toString());

        // getScope的值被视为变量名
        calculator.addExternalClassOfVariable(fieldAccessExpr.getScope().toString());

        super.visit(fieldAccessExpr, calculator);
    }

    @Override
    public void visit(VariableDeclarationExpr variableDeclarationExpr, ATFDCalculator calculator) {
//        System.out.println("[ATFD] New local variable = " + variableDeclarationExpr.toString());

        calculator.addLocalVariables(variableDeclarationExpr.getVariables(), variableDeclarationExpr.getCommonType());

        super.visit(variableDeclarationExpr, calculator);
    }

    @Override
    public void visit(MethodCallExpr methodCallExpr, ATFDCalculator calculator) {
        Expression scope = null;
        if(methodCallExpr.getScope().isPresent())
            scope = methodCallExpr.getScope().get();

        // scope是null或this，表示调用了成员方法，
        if (scope != null && !scope.toString().startsWith("this")) {
            String methodName = methodCallExpr.getName().toString().toUpperCase();

            // 对于setter和getter
            if (methodName.startsWith("GET") || methodName.startsWith("SET")) {
//                System.out.println("[ATFD] New call of an accessor = " + methodCallExpr.toString());
                calculator.addExternalClassOfVariable(scope.toString());
            }
        }

        super.visit(methodCallExpr, calculator);
    }
}
