package com.se.metrics.calculators;

import com.github.javaparser.ast.CompilationUnit;
import com.se.metrics.MetricCalculator;
import com.se.metrics.visitors.WMCVisitor;

public class WMCCalculator extends MetricCalculator
{

    protected WMCVisitor visitor;

    public WMCCalculator() {
        super();
        this.visitor = new WMCVisitor();
    }

    @Override
    public void calculate(CompilationUnit cu) {
        this.visitor.visit(cu, this);
    }

    public int getMethodCount()
    {
        return (int)this.metric;
    }

    public void setMethodCount(int methodCount)
    {
        this.metric = (double)methodCount;
    }

    public void increaseMethodCount() {
        this.metric++;
    }
}
