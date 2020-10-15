package com.se.metrics.visitors;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.metrics.calculators.WMCCalculator;

public class WMCVisitor extends VoidVisitorAdapter<WMCCalculator> {
    @Override
    public void visit(MethodDeclaration methodDeclaration, WMCCalculator wmcCalculator) {
        super.visit(methodDeclaration, wmcCalculator);

        // 方法数量
//        System.out.println("[WMC] New method = " + methodDeclaration.getName());
        wmcCalculator.increaseMethodCount();
    }

    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, WMCCalculator wmcCalculator) {
        super.visit(constructorDeclaration, wmcCalculator);

//        System.out.println("[WMC] New constructor = " + constructorDeclaration.getName());
        wmcCalculator.increaseMethodCount();
    }
}
