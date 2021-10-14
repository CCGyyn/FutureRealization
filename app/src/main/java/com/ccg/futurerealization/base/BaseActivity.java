package com.ccg.futurerealization.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author：cgaopeng on 2021/10/14 21:27
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initViews();
        initData();
        initAction();
    }

    public abstract int getLayoutId();

    /**
     * 一般用于view初始化
     */
    protected abstract void initViews();

    protected abstract void initData();

    /**
     * 多用于监听初始化
     */
    protected abstract void initAction();
}
