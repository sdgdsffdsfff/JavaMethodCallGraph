package com.se.utils;

import org.apache.commons.lang.time.StopWatch;

public class TimeUtil {

    private static StopWatch stopWatch = new StopWatch();

    public static void startTimer(){
        stopWatch.start();
    }

    public static String stopAndGetTime(){
        stopWatch.stop();
        String time = String.valueOf(stopWatch.getTime());
        stopWatch.reset();
        return time;
    }

}
