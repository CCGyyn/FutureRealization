package com.ccg.futurerealization.present;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.utils.AccountCategoryStore;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.BookKeepingContract;
import com.ccg.futurerealization.db.AccountCategoryManager;
import com.ccg.futurerealization.db.AccountCategoryManagerImpl;
import com.ccg.futurerealization.db.AccountManager;
import com.ccg.futurerealization.db.AccountManagerImpl;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookKeepingPresenter extends BookKeepingContract.Present {
    private final AccountCategoryManager categoryManager = AccountCategoryManagerImpl.getInstance();
    private final AccountManager accountManager = AccountManagerImpl.getInstance();
    private int dayRequest, monthRequest, categoryRequest;

    public BookKeepingPresenter(BookKeepingContract.View view, Context context) { super(view, context); }

    private <T> void execute(Callable<T> work, Consumer<T> success, Consumer<Throwable> failure) {
        addDisposable(Single.fromCallable(work).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(success, failure));
    }

    @Override public void queryAccountCategory() {
        // Capture application context so a detached fragment cannot invalidate a running task.
        Context context = mContext.getApplicationContext();
        final int request = ++categoryRequest;
        execute(() -> {
            ensureCategories(context);
            return categoryManager.queryAll();
        }, categories -> { if (request == categoryRequest) mView.loadAccountCategoryData(categories); },
                e -> { if (request == categoryRequest) mView.showBookkeepingError("分类加载失败，请重新进入记账页"); });
    }

    @Override public void initAccountCategory() { queryAccountCategory(); }

    private void ensureCategories(Context context) throws Exception {
        if (!categoryManager.queryAll().isEmpty()) return;
        // Match LitePal's lock order for multi-operation transactions.
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = LitePal.getDatabase();
            db.beginTransaction();
            try {
                // Recheck under the write transaction: import and first-run initialization may overlap.
                if (categoryManager.queryAll().isEmpty()) {
                    try (InputStream input = context.getResources().openRawResource(R.raw.account_category)) {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(input, "UTF-8");
                        long parentId = 0;
                        int type = 1;
                        for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                            if (event != XmlPullParser.START_TAG) continue;
                            String tag = parser.getName();
                            if (!"type_1".equals(tag) && !"type_2".equals(tag)) continue;
                            String name = parser.getAttributeValue(null, "name");
                            if (name == null) continue;
                            if ("type_1".equals(tag)) type = "收".equals(name) ? 0 : 1;
                            AccountCategory category = new AccountCategory();
                            category.setCategory(name);
                            category.setType(type);
                            category.setPid("type_1".equals(tag) ? 0L : parentId);
                            if (!categoryManager.insert(category)) throw new IllegalStateException("分类初始化失败");
                            if ("type_1".equals(tag)) parentId = category.getId();
                        }
                    }
                }
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
    }

    @Override public void addAccount(Account account) {
        execute(() -> accountManager.insert(account), inserted -> mView.addAccountState(inserted, account),
                e -> mView.addAccountState(false, account));
    }

    @Override public void deleteAccount(Long id) {
        execute(() -> accountManager.deleteById(id), count -> mView.loadDeleteAccountState(count),
                e -> mView.loadDeleteAccountState(0));
    }

    @Override public void queryCurrentMonthAccount() {
        final int request = ++monthRequest;
        execute(() -> accountManager.queryAccountByDate(new SimpleDateFormat("yyyy-MM", Locale.ROOT).format(new Date())),
                accounts -> { if (request == monthRequest) mView.loadCurrentMonthAccountData(accounts); },
                e -> { if (request == monthRequest) mView.showBookkeepingError("本月账单加载失败，请稍后重试"); });
    }

    @Override public void queryDayAccounts(String date) {
        final int request = ++dayRequest;
        execute(() -> {
            List<Account> accounts = accountManager.queryAccountByDate(date);
            accounts.sort((a, b) -> Long.compare(a.getId(), b.getId()));
            return accounts;
        }, accounts -> { if (request == dayRequest) mView.loadDayAccounts(date, accounts); },
                e -> { if (request == dayRequest) mView.showBookkeepingError("当天账单加载失败，请稍后重试"); });
    }
    @Override public void addCategory(long parentId, String name) {
        execute(() -> AccountCategoryStore.add(parentId, name), category -> mView.categoryAdded(category, null),
                e -> mView.categoryAdded(null, e instanceof IllegalArgumentException ? e.getMessage() : "分类保存失败，请重试"));
    }

}
