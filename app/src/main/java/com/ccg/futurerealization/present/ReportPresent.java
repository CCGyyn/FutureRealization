package com.ccg.futurerealization.present;

import com.ccg.futurerealization.contract.ReportContract;
import com.ccg.futurerealization.db.AccountCategoryManagerImpl;
import com.ccg.futurerealization.db.AccountManagerImpl;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReportPresent extends ReportContract.Present {
    private int request;
    public ReportPresent(ReportContract.View view) { super(view); }

    @Override public void queryAccountByMonth(String month) {
        int current = ++request;
        addDisposable(Single.fromCallable(() -> AccountManagerImpl.getInstance().queryAccountByDate(month))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(accounts -> { if (request == current) mView.loadAccountDataByMonth(month, accounts); },
                        e -> { if (request == current) mView.reportError("报表加载失败，请重新选择月份重试"); }));
    }

    @Override public void queryCategoryByPid(Long pid) {
        addDisposable(Single.fromCallable(() -> AccountCategoryManagerImpl.getInstance().queryByPid(pid))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(categories -> mView.loadRootCategory(categories),
                        e -> mView.reportError("分组加载失败，暂按具体分类展示")));
    }
}
