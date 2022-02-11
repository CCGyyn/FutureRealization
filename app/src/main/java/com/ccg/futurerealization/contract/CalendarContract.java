package com.ccg.futurerealization.contract;

import com.ccg.futurerealization.base.BaseApiSubscriber;
import com.ccg.futurerealization.base.BasePresenter;
import com.ccg.futurerealization.base.BaseView;
import com.ccg.futurerealization.bean.Account;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-10 下午4:48
 * @Version: 1.0
 */
public interface CalendarContract {

    interface View extends BaseView {
        /**
         * 添加账单数据
         * @param accountList
         */
        void addAccountDataByDay(List<Account> accountList);
    }

    abstract class Present extends BaseApiSubscriber implements BasePresenter {
        protected View mView;

        public Present(View view) {
            mView = view;
        }

        /**
         * 根据日期查询账单
         * @param date
         */
        abstract public void queryAccountByDate(String date);

        @Override
        public void destroy() {
            unDisposable();
            mView = null;
        }
    }
}
