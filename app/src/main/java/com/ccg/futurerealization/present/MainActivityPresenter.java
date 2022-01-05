package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainActivityContract;
import com.ccg.futurerealization.db.DoSthManager;
import com.ccg.futurerealization.db.DoSthManagerImpl;
import com.ccg.futurerealization.utils.Task;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午7:04
 * @Version: 1.1
 * @update:更改架构 22-1-5
 */
public class MainActivityPresenter extends MainActivityContract.Presenter {

    private DoSthManager mDoSthManager;

    public MainActivityPresenter(MainActivityContract.View view) {
        super(view);
        mDoSthManager = DoSthManagerImpl.getInstance();
    }

    @Override
    public void queryDoSthData() {
        Task.execute(emitter -> {
            List<DoSth> list = mDoSthManager.queryAll();
            emitter.onNext(list);
            emitter.onComplete();
        },
                new Observer<List<DoSth>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDisposable(d);
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
