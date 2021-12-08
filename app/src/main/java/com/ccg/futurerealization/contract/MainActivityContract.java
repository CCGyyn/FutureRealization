package com.ccg.futurerealization.contract;

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

    interface Presenter extends BasePresenter {
        /**
         * 查找数据
         */
        void queryDoSthData();
    }
}
