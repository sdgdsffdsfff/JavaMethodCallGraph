package com.se.metrics;

import com.github.javaparser.ast.CompilationUnit;

public abstract class MetricCalculator {

    protected double metric;

    protected MetricCalculator()
    {
        this.metric = 0.0;
    }

    public double getMetric()
    {
        return this.metric;
    }

    public void reset() {
        this.metric = 0.0;
    }

    public abstract void calculate(final CompilationUnit cu);

}
