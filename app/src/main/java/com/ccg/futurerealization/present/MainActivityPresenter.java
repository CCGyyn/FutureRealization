package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainActivityContract;
import com.ccg.futurerealization.db.DoSthManager;
import com.ccg.futurerealization.db.DoSthManagerImpl;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午7:04
 * @Version: 1.0
 */
public class MainActivityPresenter implements MainActivityContract.Presenter {

    private MainActivityContract.View mView;
    private DoSthManager mDoSthManager;

    public MainActivityPresenter(MainActivityContract.View view) {
        mView = view;
        mDoSthManager = DoSthManagerImpl.getInstance();
    }

    @Override
    public void destroy() {
        mView = null;
    }

    @Override
    public void queryDoSthData() {
        Observable.create((ObservableOnSubscribe<List<DoSth>>) emitter -> {
            List<DoSth> list = mDoSthManager.queryAll();
            emitter.onNext(list);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DoSth>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<DoSth> list) {
                        mView.loadData(list);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
