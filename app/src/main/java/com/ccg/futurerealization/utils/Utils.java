package com.ccg.futurerealization.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-13 下午4:39
 * @Version: 1.0
 */
public class Utils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    /**
     * 当API最小版本小于17时使用当前方法生成ID
     *      当API版本大于17时使用 View.generateViewId()方法生成ID
     * @return
     */
    @Deprecated
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random= new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
