package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainFragmentContract;
import com.ccg.futurerealization.db.DoSthManager;
import com.ccg.futurerealization.db.DoSthManagerImpl;
import com.ccg.futurerealization.utils.LogUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午4:47
 * @Version: 1.0
 */
public class MainFragmentPresenter implements MainFragmentContract.Presenter {

    private MainFragmentContract.View mView;
    private DoSthManager mDoSthManager;

    public MainFragmentPresenter(MainFragmentContract.View view) {
        mView = view;
        mDoSthManager = DoSthManagerImpl.getInstance();
    }

    @Override
    public void addDoSth(DoSth doSth) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            LogUtils.d("ObservableOnSubscribe");
            Boolean ans = mDoSthManager.insert(doSth);
            emitter.onNext(ans);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        LogUtils.d("onSubscribe");
                    }

                    @Override
                    public void onNext(@NonNull Boolean aBoolean) {
                        LogUtils.d("onNext aBoolean=" + aBoolean);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LogUtils.d("onError");
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.d("onComplete " + doSth.toString());
                        mView.addMsgSuccess(doSth);
                    }
                });
    }

    @Override
    public void destroy() {
        mView = null;
    }
}
