package com.ccg.futurerealization.present;

import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainFragmentContract;
import com.ccg.futurerealization.db.DoSthManager;
import com.ccg.futurerealization.db.DoSthManagerImpl;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.concurrent.Callable;

public class MainFragmentPresenter extends MainFragmentContract.Presenter {
    private final DoSthManager manager = DoSthManagerImpl.getInstance();
    private int request;
    public MainFragmentPresenter(MainFragmentContract.View view) { mView = view; }
    private <T> void execute(Callable<T> work, Consumer<T> next) {
        addDisposable(Single.fromCallable(work).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(next, e -> mView.actionFailed()));
    }
    @Override public void addDoSth(DoSth goal) {
        ++request;
        execute(() -> manager.insert(goal), saved -> { if (saved) mView.addMsgSuccess(goal); else mView.actionFailed(); });
    }
    @Override public void deleteAllDoSth() {
        execute(() -> manager.deleteAll(), count -> queryDoSthData());
    }
    @Override public void queryDoSthData() {
        int current = ++request;
        execute(() -> manager.queryAll(), goals -> { if (current == request) mView.refreshAllMsgItem(goals); });
    }
    @Override public void updateDoSth(DoSth goal) {
        ++request; // Discard any snapshot requested before this mutation.
        execute(() -> manager.updateInfo(goal), count -> { if (count > 0) mView.refreshMsgItem(goal); else mView.actionFailed(); });
    }
    @Override public void deleteDoSthById(Long id) {
        ++request;
        execute(() -> manager.deleteById(id), count -> { if (count > 0) mView.deleteItem(id); else mView.actionFailed(); });
    }
}
