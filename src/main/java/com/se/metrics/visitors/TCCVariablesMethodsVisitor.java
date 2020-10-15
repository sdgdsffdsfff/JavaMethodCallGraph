package com.se.metrics.visitors;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.metrics.calculators.TCCCalculator;

import java.util.List;

public class TCCVariablesMethodsVisitor extends VoidVisitorAdapter<TCCCalculator> {
    @Override
    public void visit(FieldDeclaration fieldDeclaration, TCCCalculator tccCalculator) {
        List<VariableDeclarator> variables = fieldDeclaration.getVariables();

//        for(VariableDeclarator variable : variables)
//            System.out.println("[TCC] New variable member = " + fieldDeclaration.getType().toString() + " " + variable);

        tccCalculator.addMemberVariables(variables, fieldDeclaration.getCommonType());

        //super.visit(fieldDeclaration, tccCalculator);
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, TCCCalculator tccCalculator) {
        String methodDeclarationString = methodDeclaration.getDeclarationAsString(true, false, false);

        // 对于TCC和LCC，只考虑非private的方法
        if (methodDeclarationString.startsWith("private"))
            return;

        // 构造方法名，格式为 : "name type1 type2 ..."
        String methodName = methodDeclaration.getName().toString();
        for(Parameter parameter : methodDeclaration.getParameters())
            methodName += " " + parameter.getType();

//        System.out.println("[TCC] New visible method = " + methodName);

        tccCalculator.addVisibleMethod(methodName);

        // 不需要访问这个方法的方法体
        //super.visit(methodDeclaration, tccCalculator);
    }
}
