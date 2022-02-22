package com.ccg.futurerealization.view.activity;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.ReportContract;
import com.ccg.futurerealization.present.ReportPresent;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Utils;
import com.ccg.futurerealization.view.widget.DateTextView;
import com.lwb.piechart.PieChartView;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class ReportActivity extends BaseActivity implements ReportContract.View {

    private static final int MSG_LOAD_DATA = 100;

    private Boolean IS_FINISH_CATEGORY_LOAD = false;

    private DateTextView mDateText;
    private TextView mAccountText;

    private PieChartView mPieChartView;

    private ReportContract.Present mPresent;

    private Map<Long, AccountCategory> mRootCategoryMap;

    private LoadDataObservable mLoadDataObservable;

    /**
     * key="yyyy-MM"
     */
    private Map<String, List<Account>> mAccountMap;

    private Handler mHandler = new MyHandler(this);

    @Override
    public int getLayoutId() {
        return R.layout.activity_report;
    }

    @Override
    protected void initViews() {
        mDateText = findViewById(R.id.date_text);
        mAccountText = findViewById(R.id.account_text);
        mPieChartView = findViewById(R.id.account_pie_chart);
    }

    @Override
    protected void initData() {
        mPresent = new ReportPresent(this);
        mRootCategoryMap = new HashMap<>();
        mAccountMap = new HashMap<>();
        mPieChartView.setCell(5);            //设置环形图的间距
        mPieChartView.setInnerRadius(0.4f);  //设置环形图内环半径比例 0 - 1.0f
        mPresent.queryCategoryByPid(0L);
        mPresent.queryAccountByMonth(Utils.getYearMonthStrByDate(new Date()));
    }

    @Override
    protected void initAction() {
    }

    @Override
    protected void onDestroy() {
        if (null != mPresent) {
            mPresent.destroy();
            mPresent = null;
        }
        if (null != mLoadDataObservable) {
            mLoadDataObservable = null;
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    private void setPieChartView(String date) {
        List<Account> accounts = mAccountMap.get(Utils.getYearMonthStrByDate(date));
        if (accounts.size() > 0) {
            //收入
            Map<String, Integer> incomeMap = new HashMap<>();
            //支出
            Map<String, Integer> overMap = new HashMap<>();
            for (Account account:accounts
                 ) {
                AccountCategory ac = account.getAccountCategory();
                AccountCategory accountCategory = mRootCategoryMap.get(ac.getPid());
                String category = accountCategory.getCategory();
                Integer money;
                if (account.getType() == 0) {
                    money = incomeMap.getOrDefault(category, 0);
                    money += account.getAmount();
                    incomeMap.put(category, money);
                } else {
                    money = overMap.getOrDefault(category, 0);
                    money += account.getAmount();
                    overMap.put(category, money);
                }
            }
            for (String key:overMap.keySet()
                 ) {
                Integer money = overMap.get(key);
                int color = getRandomColor();
                mPieChartView.addItemType(new PieChartView.ItemType(key, money / 100, color));
            }
        }
    }

    /**
     * 随机颜色
     * @return
     */
    private int getRandomColor() {
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return color;
    }

    /**
     * 根据月份查询数据
     * @param accountList
     */
    @Override
    public void loadAccountDataByMonth(List<Account> accountList) {
        if (null == accountList || accountList.size() == 0) {
            return;
        }
        String date = accountList.get(0).getDate();
        mAccountMap.put(Utils.getYearMonthStrByDate(date), accountList);
        if (IS_FINISH_CATEGORY_LOAD) {
            setPieChartView(date);
            return;
        }
        if (null == mLoadDataObservable) {
            mLoadDataObservable = new LoadDataObservable();
        }
        LoadDataObserver observer = new LoadDataObserver(date);
        mLoadDataObservable.addObserver(observer);
    }

    /**
     * 加载根目录
     *
     * @param accountCategories
     */
    @Override
    public void loadRootCategory(List<AccountCategory> accountCategories) {
        for (AccountCategory ac:accountCategories
             ) {
            mRootCategoryMap.put(ac.getId(), ac);
        }
        IS_FINISH_CATEGORY_LOAD = true;
        if (null != mLoadDataObservable) {
            mLoadDataObservable.notifySetData();
        }
    }

    private class LoadDataObserver implements Observer {

        private String date;

        public LoadDataObserver(String date) {
            this.date = date;
        }

        @Override
        public void update(Observable o, Object arg) {
            LogUtils.v("update");
            setPieChartView(date);
        }
    }

    private class LoadDataObservable extends Observable {

        public void notifySetData() {
            LogUtils.v("notifyObservers");
            setChanged();
            notifyObservers();
            deleteObservers();
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<ReportActivity> mWeakReference;

        public MyHandler(ReportActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            ReportActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                default:
                    break;
            }
        }
    }
}