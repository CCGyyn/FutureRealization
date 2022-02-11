package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.ReportContract;
import com.ccg.futurerealization.db.AccountCategoryManager;
import com.ccg.futurerealization.db.AccountCategoryManagerImpl;
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
 * @CreateDate: 22-2-11 下午3:23
 * @Version: 1.0
 */
public class ReportPresent extends ReportContract.Present {

    private AccountManager mAccountManager;

    private AccountCategoryManager mAccountCategoryManager;

    public ReportPresent(ReportContract.View view) {
        super(view);
        mAccountManager = AccountManagerImpl.getInstance();
        mAccountCategoryManager = AccountCategoryManagerImpl.getInstance();
    }

    /**
     * 根据yyyy-MM查找账单
     *
     * @param date
     */
    @Override
    public void queryAccountByMonth(String date) {
        Task.execute(emitter -> {
            List<Account> accounts = mAccountManager.queryAccountByDate(date);
            emitter.onNext(accounts);
            emitter.onComplete();
        }, new Observer<List<Account>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NonNull List<Account> accountList) {
                mView.loadAccountDataByMonth(accountList);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 查找对应集合的类型
     *
     * @param pid
     */
    @Override
    public void queryCategoryByPid(Long pid) {
        Task.execute(emitter -> {
            List<AccountCategory> accountCategories = mAccountCategoryManager.queryByPid(pid);
            emitter.onNext(accountCategories);
            emitter.onComplete();
        }, new Observer<List<AccountCategory>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NonNull List<AccountCategory> list) {
                mView.loadRootCategory(list);
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
