package com.ccg.futurerealization.contract;

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
    }

    interface Presenter extends BasePresenter {
        void addDoSth(DoSth doSth);

        void deleteAllDoSth();

        void queryDoSthData();
    }
}