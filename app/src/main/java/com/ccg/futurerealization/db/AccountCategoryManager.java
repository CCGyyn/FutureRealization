package com.ccg.futurerealization.db;

import com.ccg.futurerealization.base.BaseManager;
import com.ccg.futurerealization.bean.AccountCategory;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午11:03
 * @Version: 1.0
 */
public interface AccountCategoryManager extends BaseManager<AccountCategory> {

    /**
     * 根据pid查找
     * @param pid
     * @return
     */
    List<AccountCategory> queryByPid(Long pid);
}
