package com.ccg.futurerealization.contract;

import com.ccg.futurerealization.base.BaseApiSubscriber;
import com.ccg.futurerealization.base.BasePresenter;
import com.ccg.futurerealization.base.BaseView;
import com.ccg.futurerealization.bean.DoSth;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午7:03
 * @Version: 1.0
 */
public interface MainActivityContract {

    interface View extends BaseView {
        void loadData(List<DoSth> list);
    }

    abstract class Presenter extends BaseApiSubscriber implements BasePresenter {

        protected View mView;

        public Presenter(View view) {
            mView = view;
        }

        /**
         * 查找数据
         */
        abstract public void queryDoSthData();

        @Override
        public void destroy() {
            unDisposable();
            mView = null;
        }
    }
}
