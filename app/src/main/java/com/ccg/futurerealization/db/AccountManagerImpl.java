package com.ccg.futurerealization.db;

import com.ccg.futurerealization.bean.Account;

import org.litepal.LitePal;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午11:19
 * @Version: 1.0
 */
public class AccountManagerImpl implements AccountManager{

    private static AccountManager mAccountManager;

    private AccountManagerImpl() {}

    public static AccountManager getInstance() {
        if (mAccountManager == null) {
            synchronized (AccountManagerImpl.class) {
                if (mAccountManager == null) {
                    mAccountManager = new AccountManagerImpl();
                }
            }
        }
        return mAccountManager;
    }

    @Override
    public Integer deleteById(long id) {
        int delete = LitePal.delete(Account.class, id);
        return delete;
    }

    @Override
    public Integer updateInfo(Account info) {
        int update = info.update(info.getId());
        return update;
    }

    @Override
    public List<Account> queryAll() {
        List<Account> list = LitePal.findAll(Account.class);
        return list;
    }

    @Override
    public Account queryById(long id) {
        Account account = LitePal.find(Account.class, id);
        return account;
    }
}
