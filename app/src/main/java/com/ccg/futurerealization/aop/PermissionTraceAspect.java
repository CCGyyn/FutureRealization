package com.ccg.futurerealization.aop;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ccg.futurerealization.Constant;
import com.ccg.futurerealization.utils.LogUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-6 下午4:33
 * @Version: 1.0
 */
@Aspect
public class PermissionTraceAspect {

    private String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 切点
     * 要进入切面，都会进入这个方法
     * execution(注解名字 注解的作用域 *表示任意.java文件 *(..)不限方法 不限参数)
     */
    @Pointcut("execution(@com.ccg.futurerealization.aop.PermissionTrace * *(..))")
    public void methodAnnotatedWithPermissionTrace() {
        //这里面是不走进来的
    }

    /**
     * @Around 前后都运行 不知为啥只在方法前运行一次 方法完后没执行
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("methodAnnotatedWithPermissionTrace()")
    public Object joinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        LogUtils.d("joinPoint");
        Activity activity = AOPContextHelper.getInstance().getActivity();
        if (null != activity) {
            List<String> permissionsList = new ArrayList<>();
            for (String permission:PERMISSIONS
            ) {
                int checkSelfPermission = ContextCompat.checkSelfPermission(activity, permission);
                if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                }
            }
            if (permissionsList.size() > 0) {
                ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]), Constant.PERMISSION_REQUEST_CODE);
            } else {
                // 如果权限都有, 则通过joinPoint.proceed()正常调用PermissionTrace标记的方法
                return joinPoint.proceed();
            }
        }
        return null;
    }
}
