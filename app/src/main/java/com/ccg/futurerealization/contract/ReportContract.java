package com.ccg.futurerealization.contract;

import com.ccg.futurerealization.base.BaseApiSubscriber;
import com.ccg.futurerealization.base.BasePresenter;
import com.ccg.futurerealization.base.BaseView;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-11 下午3:19
 * @Version: 1.0
 */
public interface ReportContract {

    interface View extends BaseView {

        /**
         * 根据月份查询数据
         * @param accountList
         */
        void loadAccountDataByMonth(List<Account> accountList);

        /**
         * 加载根目录
         * @param accountCategories
         */
        void loadRootCategory(List<AccountCategory> accountCategories);
    }

    abstract class Present extends BaseApiSubscriber implements BasePresenter {
        protected View mView;

        public Present(View view) {
            mView = view;
        }

        /**
         * 根据yyyy-MM查找账单
         * @param date
         */
        abstract public void queryAccountByMonth(String date);

        /**
         * 查找对应集合的类型
         * @param pid
         */
        abstract public void queryCategoryByPid(Long pid);

        @Override
        public void destroy() {
            unDisposable();
            mView = null;
        }
    }
}
