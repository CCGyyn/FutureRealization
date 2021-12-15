package com.ccg.futurerealization.utils;

import android.widget.TextView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.aop.AOPContextHelper;
import com.hjq.xtoast.XToast;
import com.hjq.xtoast.draggable.MovingDraggable;

/**
 * @Description: Toast    XToast 悬浮窗框架：https://github.com/getActivity/XToast
 * @Author: cgaopeng
 * @CreateDate: 21-12-15 下午2:19
 * @Version: 1.0
 */
public class ToastUtils {

    private static final int TIME = 1 * 1000;

    public static void success(String text) {
        new XToast<>(AOPContextHelper.getInstance().getActivity())
                .setDuration(TIME)
                .setContentView(R.layout.window_hint)
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setImageDrawable(android.R.id.icon, R.mipmap.ic_dialog_tip_finish)
                .setText(android.R.id.message, text)
                .show();
    }

    public static void success(int resourceId) {
        new XToast<>(AOPContextHelper.getInstance().getActivity())
                .setDuration(TIME)
                .setContentView(R.layout.window_hint)
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setImageDrawable(android.R.id.icon, R.mipmap.ic_dialog_tip_finish)
                .setText(android.R.id.message, resourceId)
                .show();
    }

    public static void warn(String text) {
        new XToast<>(AOPContextHelper.getInstance().getActivity())
                .setDuration(TIME)
                .setContentView(R.layout.window_hint)
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setImageDrawable(android.R.id.icon, R.mipmap.ic_dialog_tip_warning)
                .setText(android.R.id.message, text)
                .show();
    }

    public static void warn(int resourceId) {
        new XToast<>(AOPContextHelper.getInstance().getActivity())
                .setDuration(TIME)
                .setContentView(R.layout.window_hint)
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setImageDrawable(android.R.id.icon, R.mipmap.ic_dialog_tip_warning)
                .setText(android.R.id.message, resourceId)
                .show();
    }

    public static void error(String text) {
        new XToast<>(AOPContextHelper.getInstance().getActivity())
                .setContentView(R.layout.window_hint)
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setImageDrawable(android.R.id.icon, R.mipmap.ic_dialog_tip_error)
                .setText(android.R.id.message, text)
                // 设置成可拖拽的
                .setDraggable(new MovingDraggable())
                .setOnClickListener(android.R.id.message, new XToast.OnClickListener<TextView>() {

                    @Override
                    public void onClick(XToast<?> toast, TextView view) {
                        toast.cancel();
                    }
                })
                .show();
    }

    public static void error(int resourceId) {
        new XToast<>(AOPContextHelper.getInstance().getActivity())
                .setContentView(R.layout.window_hint)
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setImageDrawable(android.R.id.icon, R.mipmap.ic_dialog_tip_error)
                .setText(android.R.id.message, resourceId)
                // 设置成可拖拽的
                .setDraggable(new MovingDraggable())
                .setOnClickListener(android.R.id.message, new XToast.OnClickListener<TextView>() {

                    @Override
                    public void onClick(XToast<?> toast, TextView view) {
                        toast.cancel();
                    }
                })
                .show();
    }

}
