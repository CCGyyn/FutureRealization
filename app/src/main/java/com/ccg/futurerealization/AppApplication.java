package com.ccg.futurerealization;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccg.futurerealization.aop.AOPContextHelper;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * @author：cgaopeng on 2021/10/14 21:41
 */
public class AppApplication extends LitePalApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //不继承LitePalApplication的话则使用这个
        //LitePal.initialize(this);
        //任意地方调一次此方法即创建数据库
        LitePal.getDatabase();
        initActivityLifecycleCallbacks();
    }

    /**
     * 注册activity生命周期监听
     * @aurthor: cgaopeng
     */
    private void initActivityLifecycleCallbacks () {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                // activity create 的时候 告知当前是哪个activity
                AOPContextHelper.getInstance().setActivity(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }
}
