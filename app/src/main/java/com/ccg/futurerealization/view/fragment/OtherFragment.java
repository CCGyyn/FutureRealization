package com.ccg.futurerealization.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.base.BaseFragment;

/**
 * @Description: 其他界面
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 上午10:14
 * @Version: 1.0
 */
public class OtherFragment extends BaseFragment {
    @Override
    protected void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewId() {
        return R.layout.other_fragment;
    }

    @Override
    protected void onCreateViewInit(View rootView) {

    }
}
