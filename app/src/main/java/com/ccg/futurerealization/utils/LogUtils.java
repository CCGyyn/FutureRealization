package com.ccg.futurerealization.utils;

import android.util.Log;

/**
 * @author : cgaopeng
 * @description : 封装Log，打印出对应的log的函数
 */
public class LogUtils {
    public static final String TAG = "ccgD";

    private static boolean DEBUG = true;

    public static void d(Object s) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String fileName = stackTrace[3].getFileName();
        String methodName = stackTrace[3].getMethodName();
        s = "[" +fileName + "] " + "[" + methodName + "] " + s;
        Log.d(TAG, s + "");
    }

    public static void i(Object s) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String fileName = stackTrace[3].getFileName();
        String methodName = stackTrace[3].getMethodName();
        s = "[" +fileName + "] " + "[" + methodName + "] " + s;
        Log.i(TAG, s + "");
    }

    public static void w(Object s) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String fileName = stackTrace[3].getFileName();
        String methodName = stackTrace[3].getMethodName();
        s = "[" +fileName + "] " + "[" + methodName + "] " + s;
        Log.w(TAG, s + "");
    }

    public static void e(Object s) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String fileName = stackTrace[3].getFileName();
        String methodName = stackTrace[3].getMethodName();
        s = "[" +fileName + "] " + "[" + methodName + "] " + s;
        Log.e(TAG, s + "");
    }

}
