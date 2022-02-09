package com.ccg.futurerealization.db;

import com.ccg.futurerealization.base.BaseManager;
import com.ccg.futurerealization.bean.Account;

import java.sql.Date;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午11:18
 * @Version: 1.0
 */
public interface AccountManager extends BaseManager<Account> {

    List<Account> queryAccountByDate(Date date);
}
