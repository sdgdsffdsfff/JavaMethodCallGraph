package com.se.utils;



public class GodClassUtils {

    //使用WMC, TCC, ATFD这三个指标进行判断一个类是否为God Class
    private final int veryHigh = 47;
    private final int few = 5;
    private final double calRate =  0.33;
    //类的圈复杂度
    private double WMC;
    //类中通过访问相同属性而发生连接的方法对的个数
    private double TCC;
    //被检测类所访问的外部类属性的个数
    private double ATFD;


    public double getWMC() {
        return WMC;
    }

    public void setWMC(double WMC) {
        this.WMC = WMC;
    }

    public double getTCC() {
        return TCC;
    }

    public void setTCC(double TCC) {
        this.TCC = TCC;
    }

    public double getATFD() {
        return ATFD;
    }

    public void setATFD(double ATFD) {
        this.ATFD = ATFD;
    }

}
