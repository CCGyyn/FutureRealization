package com.ccg.futurerealization.view.activity;

import android.widget.TextView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.ReportContract;
import com.ccg.futurerealization.present.ReportPresent;
import com.ccg.futurerealization.utils.Utils;
import com.ccg.futurerealization.view.widget.DateTextView;
import com.lwb.piechart.PieChartView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends BaseActivity implements ReportContract.View {

    private DateTextView mDateText;
    private TextView mAccountText;

    private PieChartView mPieChartView;

    private ReportContract.Present mPresent;

    private Map<Long, AccountCategory> mRootCategoryMap;

    /**
     * key="yyyy-MM"
     */
    private Map<String, List<Account>> mAccountMap;

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
        super.onDestroy();
    }

    private void setPieChartView(String date) {
        List<Account> accounts = mAccountMap.get(Utils.getYearMonthStrByDate(date));
        if (accounts.size() > 0) {
            //收入
            Map<String, Integer> incomeMap = new HashMap<>();
            //支出
            Map<String, Integer> overMap = new HashMap<>();
            Integer totalIncome = 0;
            Integer totalOver = 0;
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
            for (String key:incomeMap.keySet()
                 ) {
                Integer money = incomeMap.get(key);
                //mPieChartView.addItemType(new PieChartView.ItemType(key + ":" + , ));
            }
        }
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
    }
}