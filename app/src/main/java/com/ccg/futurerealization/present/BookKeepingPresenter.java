package com.ccg.futurerealization.present;

import android.content.Context;
import android.util.Xml;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.BookKeepingContract;
import com.ccg.futurerealization.db.AccountCategoryManager;
import com.ccg.futurerealization.db.AccountCategoryManagerImpl;
import com.ccg.futurerealization.db.AccountManager;
import com.ccg.futurerealization.db.AccountManagerImpl;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Task;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-5 上午11:37
 * @Version: 1.0
 */
public class BookKeepingPresenter extends BookKeepingContract.Present {

    private AccountCategoryManager mAccountCategoryManager;

    private AccountManager mAccountManager;

    public BookKeepingPresenter(BookKeepingContract.View view, Context context) {
        super(view, context);
        mAccountCategoryManager = AccountCategoryManagerImpl.getInstance();
        mAccountManager = AccountManagerImpl.getInstance();
    }

    @Override
    public void queryAccountCategory() {
        Task.execute(emitter -> {
                    List<AccountCategory> accountCategories = mAccountCategoryManager.queryAll();
                    emitter.onNext(accountCategories);
                    emitter.onComplete();
                },
                new Observer<List<AccountCategory>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(@NonNull List<AccountCategory> accountCategories) {
                        int size = accountCategories.size();
                        LogUtils.v("accountCategories size = " + size);
                        if (size == 0) {
                            initAccountCategory();
                            return;
                        }
                        mView.loadAccountCategoryData(accountCategories);
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
    public void initAccountCategory() {
        List<AccountCategory> titles = new ArrayList<>();
        Map<Long, List<AccountCategory>> map = new HashMap<>();
        Task.execute(emitter -> {
            InputStream fis = null;
            try {
                /**
                 * 一开始用FileInputStream一直为null
                 */
                fis = mContext.getResources().openRawResource(R.raw.account_category);
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(fis, "UTF-8");

                int mEventType = xmlPullParser.getEventType();
                Long pid = 0L;
                while (mEventType != XmlPullParser.END_DOCUMENT) {
                    if (mEventType == XmlPullParser.START_TAG) {
                        String startTag = xmlPullParser.getName();
                        String name = xmlPullParser.getAttributeValue(null, "name");
                        if ("type_1".equals(startTag)) {
                            if (null != name) {
                                pid = 0L;
                                AccountCategory accountCategory = new AccountCategory();
                                accountCategory.setCategory(name);
                                accountCategory.setPid(pid);
                                Boolean success = mAccountCategoryManager.insert(accountCategory);
                                if (success) {
                                    pid = accountCategory.getId();
                                    titles.add(accountCategory);
                                }
                                LogUtils.v(accountCategory.toString());
                            }
                        } else if ("type_2".equals(startTag)) {
                            if (null != name) {
                                AccountCategory accountCategory = new AccountCategory();
                                accountCategory.setCategory(name);
                                accountCategory.setPid(pid);
                                Boolean success = mAccountCategoryManager.insert(accountCategory);
                                if (success) {
                                    List<AccountCategory> list = map.getOrDefault(pid, new ArrayList<>());
                                    list.add(accountCategory);
                                    map.put(pid, list);
                                }
                                LogUtils.v(accountCategory.toString());
                            }
                        }
                    } /*else if (mEventType == XmlPullParser.END_TAG) {
                        String endTag = xmlPullParser.getName();
                    }*/
                    mEventType = xmlPullParser.next();
                }
            } catch (IOException e) {
                LogUtils.e("readResultFile:" + e);
            } finally {
                if (null != fis) {
                    try {
                        LogUtils.v("finally close fis");
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            emitter.onNext(titles);
            emitter.onComplete();
            },
                new Observer<List<AccountCategory>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDisposable(d);
                    }

                    @Override
                    public void onNext(@NonNull List<AccountCategory> list) {
                        LogUtils.v("loadAccountCategoryData");
                        //mView.loadAccountCategoryData(list);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        LogUtils.v("onComplete loadAccountCategoryData");
                        mView.loadAccountCategoryData(titles, map);
                    }
                });
    }

    @Override
    public void addAccount(Account account) {
        Task.execute(emmitter -> {
            Boolean insert = mAccountManager.insert(account);
            emmitter.onNext(insert);
            emmitter.onComplete();
        }, new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NonNull Boolean b) {
                mView.addAccountState(b);
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
