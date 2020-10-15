package com.se.metrics.calculators;

import com.github.javaparser.ast.CompilationUnit;
import com.se.metrics.MetricCalculator;
import com.se.metrics.exceptions.MCCException;
import com.se.metrics.visitors.MCCVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MCCCalculator extends MetricCalculator {

    protected MCCVisitor visitor;

    // 当前方法的圈复杂度
    protected int currentMethodCyclomaticComplexity;
    // 记录每一个方法的圈复杂度
    protected Map<String, Integer> methodsCyclomaticComplexity;

    public MCCCalculator() {
        super();

        this.visitor = new MCCVisitor();
        this.methodsCyclomaticComplexity = new HashMap<>();
        this.currentMethodCyclomaticComplexity = 0;
    }

    @Override
    public void calculate(CompilationUnit cu) {
        this.visitor.visit(cu, this);

        this.metric = (double)this.getSumOfCyclomaticComplexity();
    }

    @Override
    public void reset() {
        // Reset general calculator variables
        super.reset();

        // Reset McCabe specific variables
        this.currentMethodCyclomaticComplexity = 0;
        this.methodsCyclomaticComplexity.clear();
    }

    public int getCurrentMethodCyclomaticComplexity() {
        return currentMethodCyclomaticComplexity;
    }

    public void setCurrentMethodCyclomaticComplexity(int currentMethodCyclomaticComplexity) {
        this.currentMethodCyclomaticComplexity = currentMethodCyclomaticComplexity;
    }

    public void startNewCyclomaticComplexity() {
        this.currentMethodCyclomaticComplexity = 1;
    }

    public void increaseCyclomaticComplexity()
    {
        this.currentMethodCyclomaticComplexity++;
    }

    public void increaseCyclomaticComplexity(int value)
    {
        this.currentMethodCyclomaticComplexity += value;
    }

    public void saveMethodCyclomaticComplexity(final String methodDeclaration) {
        this.methodsCyclomaticComplexity.put(methodDeclaration, this.currentMethodCyclomaticComplexity);
        this.currentMethodCyclomaticComplexity = 0;
    }

    public int getMethodCount()
    {
        return this.methodsCyclomaticComplexity.size();
    }

    public int getCyclomaticComplexity(String methodDeclaration) throws MCCException {
        for (String key : this.methodsCyclomaticComplexity.keySet())
            if (key.endsWith(methodDeclaration))
                return this.methodsCyclomaticComplexity.get(key);

        // 找不到这个method
        throw new MCCException("Method " + methodDeclaration + " not found");
    }

    public int getSumOfCyclomaticComplexity() {
        int v_G = 0;

        for(Entry<String, Integer> entry: this.methodsCyclomaticComplexity.entrySet())
            v_G += entry.getValue() /* -1 ???*/;

        return v_G;
    }

}
