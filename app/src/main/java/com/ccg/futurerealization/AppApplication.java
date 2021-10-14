package com.ccg.futurerealization;

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
    }
}
