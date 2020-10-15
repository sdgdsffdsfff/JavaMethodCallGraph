package com.se.godclass;

import com.github.javaparser.ast.CompilationUnit;
import com.se.metrics.calculators.ATFDCalculator;
import com.se.metrics.calculators.MCCCalculator;
import com.se.metrics.calculators.TCCCalculator;
import com.se.metrics.calculators.WMCCalculator;

public class GodClassCalculator {

    public static double DEFAULT_ATFD_LIMIT;
    public static double DEFAULT_WMC_LIMIT;
    public static double DEFAULT_MCC_LIMIT;
    public static double DEFAULT_TCC_LIMIT;

    static {
        DEFAULT_ATFD_LIMIT = 4;
        DEFAULT_WMC_LIMIT  = 15;
        DEFAULT_MCC_LIMIT  = 47;
        DEFAULT_TCC_LIMIT  = 1.0/3.0;

//        DEFAULT_ATFD_LIMIT = 4;
//        DEFAULT_WMC_LIMIT  = 15;
//        DEFAULT_MCC_LIMIT  = 150;
//        DEFAULT_TCC_LIMIT  = 1.0/4.0;
    }

    protected WMCCalculator wmcCalculator;
    protected MCCCalculator mccCalculator;
    protected TCCCalculator tccCalculator;
    protected ATFDCalculator atfdCalculator;

    protected double wmcLimit;
    protected double mccLimit;
    protected double tccLimit;
    protected double atfdLimit;

    public GodClassCalculator(double WMCLimit, double MCCLimit, double TCCLimit, double ATFDLimit) {
        this.wmcCalculator  = new WMCCalculator();
        this.mccCalculator  = new MCCCalculator();
        this.tccCalculator  = new TCCCalculator();
        this.atfdCalculator = new ATFDCalculator();

        this.wmcLimit  = WMCLimit;
        this.mccLimit  = MCCLimit;
        this.tccLimit  = TCCLimit;
        this.atfdLimit = ATFDLimit;
    }

    public void calculate(final CompilationUnit cu) {

        this.wmcCalculator.reset();
        this.wmcCalculator.calculate(cu);

        this.mccCalculator.reset();
        this.mccCalculator.calculate(cu);

        this.tccCalculator.reset();
        this.tccCalculator.calculate(cu);

        this.atfdCalculator.reset();
        this.atfdCalculator.calculate(cu);
    }

    public boolean isGodClass() {
        return this.getATFD() > this.atfdLimit &&
               this.getWMC() >= this.wmcLimit  &&
               this.getMCC() >= this.mccLimit  &&
               this.getTCC() <  this.tccLimit;
    }

    public double getWMC()
    {
        return this.wmcCalculator.getMetric();
    }

    public WMCCalculator getWMCCalculator()
    {
        return this.wmcCalculator;
    }

    public double getWMCLimit()
    {
        return this.wmcLimit;
    }

    public void setWMCLimit(double wmcLimit)
    {
        this.wmcLimit = wmcLimit;
    }

    public double getMCC()
    {
        return this.mccCalculator.getMetric();
    }

    public MCCCalculator getMCCCalculator()
    {
        return this.mccCalculator;
    }

    public double getMCCLimit()
    {
        return this.mccLimit;
    }

    public void setMCCLimit(double mccLimit)
    {
        this.mccLimit = mccLimit;
    }

    public double getTCC()
    {
        return this.tccCalculator.getMetric();
    }

    public TCCCalculator getTCCCalculator()
    {
        return this.tccCalculator;
    }

    public double getTCCLimit()
    {
        return this.tccLimit;
    }

    public void setTCCLimit(double tccLimit)
    {
        this.tccLimit = tccLimit;
    }

    public double getATFD() {
        return this.atfdCalculator.getMetric();
    }

    public ATFDCalculator getATFDCalculator()
    {
        return this.atfdCalculator;
    }

    public double getATFDLimit()
    {
        return this.atfdLimit;
    }

    public void setATFDLimit(double atfdLimit)
    {
        this.atfdLimit = atfdLimit;
    }
}
