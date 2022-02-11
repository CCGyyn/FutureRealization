package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.contract.CalendarContract;
import com.ccg.futurerealization.db.AccountManager;
import com.ccg.futurerealization.db.AccountManagerImpl;
import com.ccg.futurerealization.utils.Task;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-10 下午5:04
 * @Version: 1.0
 */
public class CalendarPresent extends CalendarContract.Present {

    private AccountManager mAccountManager;

    public CalendarPresent(CalendarContract.View view) {
        super(view);
        mAccountManager = AccountManagerImpl.getInstance();
    }

    /**
     * 根据日期查询账单
     *
     * @param date
     */
    @Override
    public void queryAccountByDate(String date) {
        Task.execute(emitter -> {
            List<Account> accountList = mAccountManager.queryAccountByDate(date);
            emitter.onNext(accountList);
            emitter.onComplete();
        }, new Observer<List<Account>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NonNull List<Account> accountList) {
                mView.addAccountDataByDay(accountList);
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
