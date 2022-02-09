package com.ccg.futurerealization.db;

import com.ccg.futurerealization.bean.Account;

import org.litepal.LitePal;

import java.sql.Date;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午11:19
 * @Version: 1.0
 * @update: 2022-02-09 修复无法查寻对应外键信息
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
        List<Account> list = LitePal.findAll(Account.class, true);
        return list;
    }

    @Override
    public Account queryById(long id) {
        Account account = LitePal.find(Account.class, id, true);
        return account;
    }

    @Override
    public List<Account> queryAccountByDate(Date date) {
        //根据对应的外键查找
        List<Account> accounts = LitePal.where("date = ?", date.toString()).find(Account.class, true);
        return accounts;
    }
}
