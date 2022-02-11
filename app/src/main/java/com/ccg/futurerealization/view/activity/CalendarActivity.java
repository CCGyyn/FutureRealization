package com.ccg.futurerealization.view.activity;


import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.CalendarAccountInfoAdapter;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.contract.CalendarContract;
import com.ccg.futurerealization.present.CalendarPresent;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Utils;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:日历显示账单
 * @Author: cgaopeng
 * @CreateDate: 22-2-10 下午3:32
 * @Version: 1.0
 */
public class CalendarActivity extends BaseActivity implements CalendarContract.View {

    private CalendarView mCalendarView;

    private TextView mAccountTitle;

    private TextView mAccountDate;

    private RecyclerView mRecyclerView;

    private CalendarAccountInfoAdapter mCalendarAccountInfoAdapter;

    private CalendarContract.Present mPresent;

    /**
     * yyyy-MM-dd分类
     */
    private Map<String, List<Account>> mAccountMap;

    @Override
    public int getLayoutId() {
        return R.layout.calendar_activity;
    }

    /**
     * 一般用于view初始化
     */
    @Override
    protected void initViews() {
        mCalendarView = findViewById(R.id.calendar_view);
        mAccountDate = findViewById(R.id.account_date);
        mAccountTitle = findViewById(R.id.account_title);
        mRecyclerView = findViewById(R.id.account_info);
    }

    @Override
    protected void initData() {
        mPresent = new CalendarPresent(this);
        mAccountMap = new HashMap<>();
        mCalendarAccountInfoAdapter = new CalendarAccountInfoAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(mCalendarAccountInfoAdapter);

        Date date = new Date();
        String yearMonthStrByDate = getDateString(date);
        mPresent.queryAccountByDate(yearMonthStrByDate);
    }

    /**
     * 多用于监听初始化
     */
    @Override
    protected void initAction() {
        mCalendarView.setOnMonthChangeListener(new CalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month) {
                LogUtils.v("year=" + year + ", month=" + month);
            }
        });
        mCalendarView.setOnCalendarSelectListener(new CalendarView.OnCalendarSelectListener() {
            @Override
            public void onCalendarOutOfRange(Calendar calendar) {
                LogUtils.v("year=" + calendar.getYear() + ", month=" + calendar.getMonth()
                        + ", day=" + calendar.getDay());
            }

            @Override
            public void onCalendarSelect(Calendar calendar, boolean isClick) {
                int year = calendar.getYear();
                int month = calendar.getMonth();
                int day = calendar.getDay();
                LogUtils.v("isClick=" + isClick +", year=" + year + ", month=" + month
                        + ", day=" + day);
                String date = concatCalendar(year, month, day);
                List<Account> accounts = mAccountMap.get(date);
                if (null != accounts) {
                    setAccountText(accounts, date);
                } else {
                    mPresent.queryAccountByDate(date);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (null != mPresent) {
            mPresent.destroy();
            mPresent = null;
        }
        super.onDestroy();
    }

    /**
     * 获取yyyy-MM-dd格式日期字符串
     * @param date
     * @return
     */
    private String getDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String str = formatter.format(date);
        return str;
    }

    /**
     * 返回string格式数据
     * @param year
     * @param month
     * @param day
     * @return
     */
    private String concatCalendar(int year, int month, int day) {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("-").append(month).append("-").append(day);
        return sb.toString();
    }

    private void setAccountText(List<Account> accountList, String date) {
        if (null == accountList || accountList.size() == 0 || date == null) {
            LogUtils.d("null");
            mAccountTitle.setText("");
            mAccountDate.setText("");
            mCalendarAccountInfoAdapter.clearData();
            return;
        }
        mAccountDate.setText(date);
        StringBuilder title = new StringBuilder();
        //收入
        BigDecimal income = new BigDecimal(0).setScale(2);
        //支出
        BigDecimal over = new BigDecimal(0).setScale(2);
        for (Account account:accountList
             ) {
            Integer amount = account.getAmount();
            BigDecimal money = Utils.convertIntegerToBigDecimal(amount);
            Integer type = account.getType();

            if (type == 0) {
                income = income.add(money);
            } else {
                over = over.add(money);
            }
        }
        title.append(getString(R.string.income_text)).append(":").append(income)
                .append("|").append(getString(R.string.over_text)).append(":").append(over);
        mAccountTitle.setText(title.toString());
        mCalendarAccountInfoAdapter.setAccountList(accountList);
    }

    /**
     * 添加账单数据
     *
     * @param accountList
     */
    @Override
    public void addAccountDataByDay(List<Account> accountList) {
        if (accountList.size() == 0) {
            setAccountText(accountList, null);
        }
        for (Account account:accountList
             ) {
            /**
             * 日期格式yyyy-MM-dd
             */
            String date = account.getDate();
            List<Account> accounts = mAccountMap.get(date);
            if (null == accounts) {
                accounts = new ArrayList<>();
            }
            accounts.add(account);
        }
        setAccountText(accountList, accountList.get(0).getDate());
    }
}
