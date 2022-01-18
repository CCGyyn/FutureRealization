package com.ccg.futurerealization.event;

import com.ccg.futurerealization.bean.AccountCategory;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-12 上午10:15
 * @Version: 1.0
 */
public class BookKeepingEvent {

    private AccountCategory accountCategory;

    public BookKeepingEvent(AccountCategory accountCategory) {
        this.accountCategory = accountCategory;
    }

    public static BookKeepingEvent getInstance(AccountCategory accountCategory) {
        return new BookKeepingEvent(accountCategory);
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }
}
