package com.ccg.futurerealization.utils;

import android.util.Log;

import com.ccg.futurerealization.BuildConfig;
import com.ccg.futurerealization.Constant;

/**
 * @author : cgaopeng
 * @description : 封装Log，打印出对应的log的函数
 *
 * @update: cgaopeng 21-12-17 添加统一log控制
 */
public class LogUtils {
    public static final String TAG = "ccgD";

    private static final boolean DEBUG_D = Constant.DEBUG_ALL || BuildConfig.DEBUG;
    private static final boolean DEBUG_V = Constant.DEBUG_ALL;

    public static void v(Object s) {
        if (!DEBUG_V) {
            return;
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String fileName = stackTrace[3].getFileName();
        String methodName = stackTrace[3].getMethodName();
        s = "[" +fileName + "] " + "[" + methodName + "] " + s;
        Log.v(TAG, s + "");
    }

    public static void d(Object s) {
        if (!DEBUG_D) {
            return;
        }
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
