package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainFragmentContract;
import com.ccg.futurerealization.db.DoSthManager;
import com.ccg.futurerealization.db.DoSthManagerImpl;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Task;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午4:47
 * @Version: 1.1
 * @update:更改架构 22-1-5
 */
public class MainFragmentPresenter extends MainFragmentContract.Presenter {

    private DoSthManager mDoSthManager;

    public MainFragmentPresenter(MainFragmentContract.View view) {
        mView = view;
        mDoSthManager = DoSthManagerImpl.getInstance();
    }

    @Override
    public void addDoSth(DoSth doSth) {
        Task.execute(emitter -> {
            LogUtils.d("ObservableOnSubscribe + " + doSth.toString());
            Boolean ans = mDoSthManager.insert(doSth);
            emitter.onNext(ans);
            emitter.onComplete();
        }, new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                LogUtils.d("onNext aBoolean=" + aBoolean);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                LogUtils.v("onError");
            }

            @Override
            public void onComplete() {
                LogUtils.d("onComplete " + doSth.toString());
                mView.addMsgSuccess(doSth);
            }
        });
    }

    @Override
    public void deleteAllDoSth() {
        Task.execute(emitter -> {
            Integer i = mDoSthManager.deleteAll();
            emitter.onNext(i);
            emitter.onComplete();
        },
                new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(@NonNull Integer integer) {
                        if (integer > 0) {
                            queryDoSthData();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LogUtils.v("onError");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
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
                        mView.refreshAllMsgItem(list);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void updateDoSth(DoSth doSth, int position) {
        Task.execute(emitter -> {
            Integer i = mDoSthManager.updateInfo(doSth);
            emitter.onNext(i);
            emitter.onComplete();
        },
                new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(@NonNull Integer i) {
                        LogUtils.d("onNext " + i + ", sth=" + doSth.toString());
                        if (i > 0) {
                            mView.refreshMsgItem(doSth, position);
                        } else {
                            mView.actionFailed();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void deleteDoSthById(@androidx.annotation.NonNull Long id, int position) {
        Task.execute(emitter -> {
            Integer i = mDoSthManager.deleteById(id);
            emitter.onNext(i);
            emitter.onComplete();
        },
                new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Integer i) {
                        LogUtils.d("onNext " + i);
                        if (i > 0) {
                            mView.deleteItem(position);
                        } else {
                            mView.actionFailed();
                        }
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
