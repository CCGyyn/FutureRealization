package com.ccg.futurerealization.base;

import com.ccg.futurerealization.utils.LogUtils;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @Description:统一解绑
 * @Author: cgaopeng
 * @CreateDate: 22-1-4 下午5:39
 * @Version: 1.0
 */
public abstract class BaseApiSubscriber {

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    protected void unDisposable() {
        if (mCompositeDisposable.size() > 0) {
            LogUtils.v("unDisposable");
            mCompositeDisposable.dispose();
        }
    }

}