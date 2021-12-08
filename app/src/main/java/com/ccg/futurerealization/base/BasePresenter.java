package com.ccg.futurerealization.base;

/**
 * @author：cgaopeng on 2021/10/14 21:35
 */
public interface BasePresenter {

    /**
     * view销毁时调用,释放资源,解除绑定等,防止OOM
     */
    void onDestroy();
}
