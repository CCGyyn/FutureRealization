package com.ccg.futurerealization.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @Description: fragment基类
 * @Author: cgaopeng
 * @CreateDate: 21-12-7 上午10:53
 * @Version: 1.0
 */
public abstract class BaseFragment extends Fragment {

    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getOptionsMenuId() != -1) {
            //指出fragment愿意添加item到选项菜单
            setHasOptionsMenu(true);
        }
        onCreateViewBefore(inflater, container, savedInstanceState);
        mView = inflater.inflate(getContentViewId(), container, false);
        onCreateViewInit(mView);
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (getOptionsMenuId() != -1) {
            inflater.inflate(getOptionsMenuId(), menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 当前页菜单资源
     *
     * @return
     */
    protected int getOptionsMenuId() {
        return -1;
    }

    /**
     * 非必须 一般可能就用来装present
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    protected abstract void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected abstract int getContentViewId();

    protected abstract void onCreateViewInit(View rootView);
}
