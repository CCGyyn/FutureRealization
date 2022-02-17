package com.ccg.futurerealization.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static java.util.Date stringConvertDate(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = null;
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static java.sql.Date stringConvertSqlDate(String time) {
        java.util.Date date = stringConvertDate(time);
        return new java.sql.Date(date.getTime());
    }

    /**
     * 转换到数据库中存储
     * @param money
     * @return
     */
    public static Integer convertBigDecimalToInteger(BigDecimal money) {
        return money.multiply(new BigDecimal(100)).intValue();
    }

    /**
     * 从数据库中取出转换
     * @param money
     * @return
     */
    public static BigDecimal convertIntegerToBigDecimal(Integer money) {
        return new BigDecimal(money).divide(new BigDecimal(100)).setScale(2);
    }

    /**
     * 获取yyyy-MM格式日期字符串
     * @param date
     * @return
     */
    public static String getYearMonthStrByDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String month = formatter.format(date);
        return month;
    }

    public static String getYearMonthStrByDate(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date date = null;
        try {
            date = formatter.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return "";
        }
        return getYearMonthStrByDate(date);
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;

    }
}
