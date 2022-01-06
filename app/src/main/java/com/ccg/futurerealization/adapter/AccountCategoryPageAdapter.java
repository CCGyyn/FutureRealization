package com.ccg.futurerealization.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ccg.futurerealization.bean.AccountCategory;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-6 下午4:42
 * @Version: 1.0
 */
public class AccountCategoryPageAdapter extends FragmentPagerAdapter {

    private List<AccountCategory> mAccountCategoryTitles;
    private List<Fragment> mFragmentList;

    public AccountCategoryPageAdapter(@NonNull FragmentManager fm,
                                      List<AccountCategory> accountCategories,
                                      List<Fragment> fragmentList) {
        super(fm);
        this.mAccountCategoryTitles = accountCategories;
        this.mFragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mAccountCategoryTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String category = mAccountCategoryTitles.get(position).getCategory();
        return category;
    }
}
