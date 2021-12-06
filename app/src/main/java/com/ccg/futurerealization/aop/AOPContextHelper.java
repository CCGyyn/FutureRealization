package com.ccg.futurerealization.aop;

import android.app.Activity;

/**
 * @Description:用于AOP获取实例
 * @Author: cgaopeng
 * @CreateDate: 21-12-6 下午4:25
 * @Version: 1.0
 */
public class AOPContextHelper {

    private static AOPContextHelper mInstance;

    private Activity mActivity;

    private AOPContextHelper() {

    }

    public static AOPContextHelper getInstance() {
        if (mInstance == null) {
            synchronized (AOPContextHelper.class) {
                if (mInstance == null) {
                    mInstance = new AOPContextHelper();
                }
            }
        }
        return mInstance;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }
}
