package com.ccg.futurerealization.view.activity;

import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ccg.futurerealization.Constant;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.aop.PermissionTrace;
import com.ccg.futurerealization.base.BaseActivity;

/**
 * @Description:主界面
 * @Author: cgaopeng
 * @CreateDate: 21-12-6 下午4:12
 * @Version: 1.0
 */
public class MainActivity extends BaseActivity{


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initData() {

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
