package com.ccg.futurerealization.db;

import com.ccg.futurerealization.bean.AccountCategory;

import org.litepal.LitePal;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午11:05
 * @Version: 1.0
 */
public class AccountCategoryManagerImpl implements AccountCategoryManager {

    private static volatile AccountCategoryManager mAccountCategoryManager;

    private AccountCategoryManagerImpl() {

    }

    public static AccountCategoryManager getInstance() {
        if (mAccountCategoryManager == null) {
            synchronized (AccountCategoryManagerImpl.class) {
                if (mAccountCategoryManager == null) {
                    mAccountCategoryManager = new AccountCategoryManagerImpl();
                }
            }
        }
        return mAccountCategoryManager;
    }

    @Override
    public Integer deleteById(long id) {
        int delete = LitePal.delete(AccountCategory.class, id);
        return delete;
    }

    @Override
    public Integer updateInfo(AccountCategory info) {
        int update = info.update(info.getId());
        return update;
    }

    @Override
    public List<AccountCategory> queryAll() {
        List<AccountCategory> list = LitePal.findAll(AccountCategory.class);
        return list;
    }

    @Override
    public AccountCategory queryById(long id) {
        AccountCategory accountCategory = LitePal.find(AccountCategory.class, id);
        return accountCategory;
    }

    /**
     * 根据pid查找
     *
     * @param pid
     * @return
     */
    @Override
    public List<AccountCategory> queryByPid(Long pid) {
        List<AccountCategory> accountCategories = LitePal.where("pid = ?", String.valueOf(pid)).find(AccountCategory.class);
        return accountCategories;
    }
}
