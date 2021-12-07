package com.ccg.futurerealization.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * @Description:tablayout+viewpager使用
 * @Author: cgaopeng
 * @CreateDate: 21-12-7 下午2:05
 * @Version: 1.0
 */
public class MsgPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;

    private List<String> mTitles;

    public MsgPageAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentList, List<String> titles) {
        super(fm);
        this.mFragmentList = fragmentList;
        this.mTitles = titles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
