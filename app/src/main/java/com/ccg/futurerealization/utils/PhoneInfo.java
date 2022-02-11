package com.ccg.futurerealization.utils;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-11 下午4:30
 * @Version: 1.0
 */
public class PhoneInfo {

    /**
     * 获取系统SDK版本
     * @return
     */
    public static int getSDKVersionNumber() {
        int sdkVersion;
        try {
            sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            sdkVersion = 0;
        }
        return sdkVersion;
    }
}
