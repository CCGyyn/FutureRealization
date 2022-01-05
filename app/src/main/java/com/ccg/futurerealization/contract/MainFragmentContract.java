package com.ccg.futurerealization.contract;

import androidx.annotation.NonNull;

import com.ccg.futurerealization.base.BaseApiSubscriber;
import com.ccg.futurerealization.base.BasePresenter;
import com.ccg.futurerealization.base.BaseView;
import com.ccg.futurerealization.bean.DoSth;

import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午4:44
 * @Version: 1.0
 */
public interface MainFragmentContract {

    interface View extends BaseView {
        void addMsgSuccess(DoSth doSth);

        void refreshAllMsgItem(List<DoSth> list);

        void refreshMsgItem(DoSth doSth, int position);

        void deleteItem(int position);

        void actionFailed();
    }

    abstract class Presenter extends BaseApiSubscriber implements BasePresenter {

        protected View mView;

        abstract public void addDoSth(DoSth doSth);

        abstract public void deleteAllDoSth();

        abstract public void queryDoSthData();

        abstract public void updateDoSth(DoSth doSth, int position);

        abstract public void deleteDoSthById(@NonNull Long id, int position);

        @Override
        public void destroy() {
            unDisposable();
            mView = null;
        }
    }
}
