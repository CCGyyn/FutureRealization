package com.ccg.futurerealization.view.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ccg.futurerealization.Constant;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.MsgPageAdapter;
import com.ccg.futurerealization.aop.PermissionTrace;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainActivityContract;
import com.ccg.futurerealization.present.MainActivityPresenter;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.view.fragment.BookKeepingFragment;
import com.ccg.futurerealization.view.fragment.MainFragment;
import com.ccg.futurerealization.view.fragment.OtherFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

/**
 * @Description:主界面
 * @Author: cgaopeng
 * @CreateDate: 21-12-6 下午4:12
 * @Version: 1.0
 * @update 22-01-18 解决切换页面,呼出的键盘不消失问题
 */
public class MainActivity extends BaseActivity implements MainActivityContract.View {

    private final int CURRENT_ITEM = 1;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private List<String> mTitles;
    private MainActivityContract.Presenter mPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_list);
    }

    @Override
    protected void initData() {
        mPresenter = new MainActivityPresenter(this);
        mFragmentList = new ArrayList<>();
        mTitles = new ArrayList<>();
        String[] pageArray = getResources().getStringArray(R.array.page_tab_items);
        mTitles.addAll(Arrays.asList(pageArray));
        mPresenter.queryDoSthData();
    }

    @PermissionTrace
    @Override
    protected void initAction() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_deny_toast, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.destroy();
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public void loadData(List<DoSth> list) {
        MainFragment mainFragment = MainFragment.newInstance(list);
        OtherFragment otherFragment = new OtherFragment();
        BookKeepingFragment bookKeepingFragment = new BookKeepingFragment();
        mFragmentList.add(mainFragment);
        mFragmentList.add(bookKeepingFragment);
        mFragmentList.add(otherFragment);
        MsgPageAdapter msgPageAdapter = new MsgPageAdapter(getSupportFragmentManager(), mFragmentList,
                mTitles);
        mViewPager.setAdapter(msgPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        //设置默认位置
        mViewPager.setCurrentItem(CURRENT_ITEM);
        //解决切换页面时数据丢失 还有一种就是将FragmentPagerAdapter换为FragmentStatePagerAdapter
        mViewPager.setOffscreenPageLimit(2);
        //解决页面1呼出键盘，滑动到页面2键盘不显示的问题
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LogUtils.v("position = " + position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                LogUtils.v("state = " + state);
                //滑动开始 state = 1, 中间为2，结束为0
                if (state == 1) {
                    hideInput();
                }
            }
        });
    }

    /**
     * 隐藏键盘
     */
    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
