package com.ccg.futurerealization.contract;

import android.content.Context;

import com.ccg.futurerealization.base.BaseApiSubscriber;
import com.ccg.futurerealization.base.BasePresenter;
import com.ccg.futurerealization.base.BaseView;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-5 上午11:15
 * @Version: 1.0
 */
public interface BookKeepingContract {

    interface View extends BaseView {
        /**
         * 加载分类数据
         * @param list
         */
        void loadAccountCategoryData(List<AccountCategory> list);

        void loadAccountCategoryData(List<AccountCategory> titles, Map<Long, List<AccountCategory>> map);

        void addAccountState(Boolean insert);
    }

    abstract class Present extends BaseApiSubscriber implements BasePresenter {
        protected View mView;

        protected Context mContext;

        public Present(View view, Context context) {
            mView = view;
            mContext = context;
        }

        abstract public void queryAccountCategory();

        abstract public void initAccountCategory();

        abstract public void addAccount(Account account);

        @Override
        public void destroy() {
            unDisposable();
            mView = null;
            mContext = null;
        }
    }
}
