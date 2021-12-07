package com.ccg.futurerealization.view.activity;

import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.ccg.futurerealization.Constant;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.MsgPageAdapter;
import com.ccg.futurerealization.aop.PermissionTrace;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.view.fragment.MainFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:主界面
 * @Author: cgaopeng
 * @CreateDate: 21-12-6 下午4:12
 * @Version: 1.0
 */
public class MainActivity extends BaseActivity{


    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private List<Fragment> mFragmentList;
    private List<String> mTitles;

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
        mFragmentList = new ArrayList<>();
        //test
        List<DoSth> list = new ArrayList<>();
        for (long i = 0; i < 4; i++) {
            DoSth doSth = new DoSth();
            doSth.setId(i);
            doSth.setFuture_content("1223");
            doSth.setState(true);
            doSth.setType(2);
            list.add(doSth);
        }
        MainFragment mainFragment = MainFragment.newInstance(list);
        //test
        MainFragment mainFragment2 = MainFragment.newInstance(list);
        mFragmentList.add(mainFragment);
        mFragmentList.add(mainFragment2);
        //test
        mTitles = new ArrayList<>();
        mTitles.add("Main Page");
        mTitles.add("Other");

        MsgPageAdapter msgPageAdapter = new MsgPageAdapter(getSupportFragmentManager(), mFragmentList,
                mTitles);
        mViewPager.setAdapter(msgPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
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
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
    }
}
