package com.ccg.futurerealization.view.activity;

import android.content.pm.PackageManager;
import android.widget.Toast;

import com.ccg.futurerealization.Constant;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.MsgPageAdapter;
import com.ccg.futurerealization.aop.PermissionTrace;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainActivityContract;
import com.ccg.futurerealization.present.MainActivityPresenter;
import com.ccg.futurerealization.view.fragment.MainFragment;
import com.ccg.futurerealization.view.fragment.OtherFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

/**
 * @Description:主界面
 * @Author: cgaopeng
 * @CreateDate: 21-12-6 下午4:12
 * @Version: 1.0
 */
public class MainActivity extends BaseActivity implements MainActivityContract.View {


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
        mTitles.add(getResources().getText(R.string.main_page_tab).toString());
        mTitles.add(getResources().getText(R.string.other_page_tab).toString());
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
        mFragmentList.add(mainFragment);
        mFragmentList.add(otherFragment);
        MsgPageAdapter msgPageAdapter = new MsgPageAdapter(getSupportFragmentManager(), mFragmentList,
                mTitles);
        mViewPager.setAdapter(msgPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
