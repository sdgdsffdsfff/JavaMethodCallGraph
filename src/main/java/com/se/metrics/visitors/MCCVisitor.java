package com.se.metrics.visitors;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.se.metrics.calculators.MCCCalculator;
import java.util.List;

/**
 * McCabe Cyclomatic Complexity (MCC) Visitor
 * 圈复杂度
 */
public class MCCVisitor extends VoidVisitorAdapter<MCCCalculator> {

    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, MCCCalculator mCCCalculator) {
        // 当前构造方法的圈复杂度初值设置为1
        this.initializeMethodCyclomaticComplexity(constructorDeclaration.getThrownExceptions(),
                                                  mCCCalculator);

        // visit构造方法的方法体
        super.visit(constructorDeclaration, mCCCalculator);

        // 保存当前构造方法的圈复杂度
        try{
            this.validateMethodCyclomaticComplexity(constructorDeclaration.asMethodDeclaration(), mCCCalculator);
        }catch (Exception e){

        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, MCCCalculator mCCCalculator) {
        // 当前方法的圈复杂度初值设置为1
        this.initializeMethodCyclomaticComplexity(methodDeclaration.getThrownExceptions(), mCCCalculator);

        // visit方法体
        super.visit(methodDeclaration, mCCCalculator);

        // 保存当前方法的圈复杂度
        this.validateMethodCyclomaticComplexity(methodDeclaration, mCCCalculator);
    }

    @Override
    public void visit(IfStmt ifStmt, MCCCalculator mcCabeCalculator) {
//        System.out.println("[McCabe]    if statement --> Complexity + 1 ");

        // 对每一个if语句圈复杂度+1
        mcCabeCalculator.increaseCyclomaticComplexity();

        super.visit(ifStmt, mcCabeCalculator);
    }

    @Override
    public void visit(WhileStmt whileStmt, MCCCalculator mCCCalculator) {
//        System.out.println("[McCabe]    while statement --> Complexity + 1");

        // 对每一个while语句圈复杂度+1
        mCCCalculator.increaseCyclomaticComplexity();

        super.visit(whileStmt, mCCCalculator);
    }

    @Override
    public void visit(DoStmt doStmt, MCCCalculator mCCCalculator) {
//        System.out.println("[McCabe]    do...while statement --> Complexity + 1");

        // 对每一个do...while语句圈复杂度+1
        mCCCalculator.increaseCyclomaticComplexity();

        super.visit(doStmt, mCCCalculator);
    }

    @Override
    public void visit(ForStmt forStmt, MCCCalculator mCCCalculator) {
//        System.out.println("[McCabe]    for statement --> Complexity + 1");

        // 对每一个while语句圈复杂度+1
        mCCCalculator.increaseCyclomaticComplexity();

        super.visit(forStmt, mCCCalculator);
    }

    @Override
    public void visit(ForEachStmt foreachStmt, MCCCalculator mCCCalculator) {
//        System.out.println("[McCabe]    foreach statement --> Complexity + 1");

        // 对每一个foreach语句圈复杂度+1
        mCCCalculator.increaseCyclomaticComplexity();

        super.visit(foreachStmt, mCCCalculator);
    }

    @Override
    public void visit(SwitchStmt switchStmt, MCCCalculator mCCCalculator) {
//        System.out.println("[McCabe]    switch statement --> Complexity + 1 ");

        // 对每一个switch语句圈复杂度+1
        mCCCalculator.increaseCyclomaticComplexity();

        super.visit(switchStmt, mCCCalculator);
    }

    @Override
    public void visit(SwitchEntry switchEntryStmt, MCCCalculator mCCCalculator) {
        /*
         * Add one for each catch clause
         * 对switch case中的每一个"case"语句圈复杂度+1，但是不对"default"语句增加
         */
        if (switchEntryStmt.getLabels() != null) {
//            System.out.println("[McCabe]        case statement --> Complexity + 1 ");
            mCCCalculator.increaseCyclomaticComplexity();
        }

        super.visit(switchEntryStmt, mCCCalculator);
    }

    @Override
    public void visit(CatchClause catchClause, MCCCalculator mCCCalculator) {
//        System.out.println("[McCabe]    catch clause --> Complexity + 1 ");

        // 对每一个catch语句圈复杂度+1
        mCCCalculator.increaseCyclomaticComplexity();

        super.visit(catchClause, mCCCalculator);
    }

    @Override
    public void visit(BinaryExpr binaryExpr, MCCCalculator mcCabeCalculator) {
        Operator operator = binaryExpr.getOperator();
        if (operator == Operator.AND || operator == Operator.OR) {
//            System.out.println("[McCabe]        logical expression && or || --> Complexity + 1");

            // 对每一个逻辑表达式 && 或者 || 圈复杂度+1
            mcCabeCalculator.increaseCyclomaticComplexity();
        }

        super.visit(binaryExpr, mcCabeCalculator);
    }

    @Override
    public void visit(ThrowStmt throwStmt, MCCCalculator mcCabeCalculator) {
//        System.out.println("[McCabe]    throw statement --> Complexity + 1");

        // 对每一个throw语句圈复杂度+1
        mcCabeCalculator.increaseCyclomaticComplexity();

        super.visit(throwStmt, mcCabeCalculator);
    }

    private void initializeMethodCyclomaticComplexity(List<ReferenceType> throwsClauses, MCCCalculator mCCCalculator) {

        // 一个方法的圈复杂度初始值为1
        mCCCalculator.startNewCyclomaticComplexity();

        // 对每一个throws圈复杂度 + 1
        if (!throwsClauses.isEmpty()) {
            mCCCalculator.increaseCyclomaticComplexity(throwsClauses.size());

//            for (NameExpr nameExpr : throwsClauses)
//                System.out.println("[McCabe]    throws clause = " + nameExpr.getName() + " --> Complexity + 1");
        }
    }

    private void validateMethodCyclomaticComplexity(MethodDeclaration methodDeclaration, MCCCalculator mCCCalculator) {
        // 保存当前方法的圈复杂度
//        System.out.println("[McCabe] Final  Cyclomatic Complexity of method = " + mCCCalculator.getCurrentMethodCyclomaticComplexity());
        mCCCalculator.saveMethodCyclomaticComplexity(methodDeclaration.getDeclarationAsString(false, true));
    }
}
