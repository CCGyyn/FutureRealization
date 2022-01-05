package com.ccg.futurerealization.utils;


import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * @Description:统一使用rxjava
 * @Author: cgaopeng
 * @CreateDate: 22-1-4 下午3:32
 * @Version: 1.0
 */
public class Task {

    public static <T> void execute(ObservableOnSubscribe<T> observableOnSubscribe, Observer<T> observer) {
        Observable.create(observableOnSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
