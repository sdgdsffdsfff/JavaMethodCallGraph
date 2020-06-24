package com.se.utils;

public class CalculateUtil {

    private static double rateThreshold = 5;

    public static boolean CalCouplingRate(int invokeCounts, int invokedCounts){
        double invoke,invoked;
        if(invokeCounts == 0){
            invoke = 0.1;
        }else {
            invoke = invokeCounts;
        }
        if(invokedCounts == 0){
            invoked = 0.1;
        }else {
            invoked = invokedCounts;
        }
        double rate = invoked/invoke;
        return rate > 5;
    }

}
