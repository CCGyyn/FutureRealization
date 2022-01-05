package com.ccg.futurerealization;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccg.futurerealization.aop.AOPContextHelper;
import com.ccg.futurerealization.utils.LogUtils;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * @author：cgaopeng on 2021/10/14 21:41
 * @update 22-1-5 添加rxjava dispose时出错处理
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
        setRxJavaErrorHandler();
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

    /**
     * RxJava2的一个重要的设计理念是：不吃掉任何一个异常。产生的问题是，当RxJava2“downStream”取消订阅后，
     * “upStream”仍有可能抛出异常，这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，
     * 便会导致程序崩溃。
     */
    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                LogUtils.e(throwable.getMessage());
            }
        });
    }
}
