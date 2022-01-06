package com.ccg.futurerealization.contract;

import com.ccg.futurerealization.base.BaseApiSubscriber;
import com.ccg.futurerealization.base.BasePresenter;
import com.ccg.futurerealization.base.BaseView;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-6 下午3:53
 * @Version: 1.0
 */
public interface AccountCategoryFragmentContract {

    interface View extends BaseView {

    }

    abstract class Present extends BaseApiSubscriber implements BasePresenter {

        protected View mView;

        public Present(View view) {
            mView = view;
        }

        @Override
        public void destroy() {
            unDisposable();
            mView = null;
        }
    }
}
