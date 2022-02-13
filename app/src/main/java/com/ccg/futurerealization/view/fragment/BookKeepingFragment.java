package com.ccg.futurerealization.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.AccountCategoryPageAdapter;
import com.ccg.futurerealization.adapter.ChatAdapter;
import com.ccg.futurerealization.base.EventBusFragment;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.BookKeepingContract;
import com.ccg.futurerealization.event.BookKeepingEvent;
import com.ccg.futurerealization.event.ChatMsgDeleteEvent;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;
import com.ccg.futurerealization.present.BookKeepingPresenter;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Utils;
import com.ccg.futurerealization.view.activity.CalendarActivity;
import com.ccg.futurerealization.view.activity.ReportActivity;
import com.ccg.futurerealization.view.widget.DateTextView;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mrapp.android.dialog.MaterialDialog;

/**
 * @Description: 记账,相关聊天框用draw9patch制作
 * @Author: cgaopeng
 * @CreateDate: 21-12-20
 * @Version: 1.0
 * @update 22-01-11 添加发送类型 添加EventBus订阅事件
 */
public class BookKeepingFragment extends EventBusFragment implements BookKeepingContract.View {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int SHOW_INPUT_MSG = 100;

    private static final int SHOW_INPUT_DELAY_TIMES = 2 * 100;

    private static final String SUCCESS_REPLY = "Success.";
    private static final String FAILED_REPLY = "Failed !!!";

    private RecyclerView mRecyclerView;
    private Button mSendBtn;
    private EditText mSendText;
    private TextView mBookKeepTypeText;

    private ChatAdapter mChatAdapter;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private Button mRemarkBtn;

    private DateTextView mDateText;
    /**
     * 当月收入和支出的总额
     */
    private TextView mAccountText;

    private ImageButton mCalendarButton;

    private ImageButton mReportButton;

    private BookKeepingContract.Present mPresent;

    private List<Fragment> mFragmentList;

    private AccountCategory mSendCategory;

    private String mRemarkMsg = "";

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_INPUT_MSG: {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                    /*InputMethodManager inputManager = (InputMethodManager) remarkEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(remarkEdit, 0);*/
                    break;
                }
                default:
                    break;
            }

        }
    };

    public static BookKeepingFragment newInstance(String param1, String param2) {
        BookKeepingFragment fragment = new BookKeepingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPresent = new BookKeepingPresenter(this, getContext());
    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_book_keeping;
    }

    @Override
    protected void onCreateViewInit(View rootView) {
        LinearLayout sendLayout = rootView.findViewById(R.id.send_layout);
        mRecyclerView = rootView.findViewById(R.id.chat_msg_list);
        mSendBtn = sendLayout.findViewById(R.id.send_btn);
        mSendText = sendLayout.findViewById(R.id.send_msg);
        mBookKeepTypeText = sendLayout.findViewById(R.id.book_keep_type);
        mChatAdapter = new ChatAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mChatAdapter);

        mRemarkBtn = sendLayout.findViewById(R.id.remark_msg);
        mRemarkBtn.setOnClickListener(v -> {
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.account_remark_dialog, null);
            EditText remarkEdit = linearLayout.findViewById(R.id.remark_edit_text);
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                    .setView(linearLayout)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        mRemarkMsg = remarkEdit.getText().toString();
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    });
            MaterialDialog dialog = builder.create();
            dialog.show();
            Resources resources = getContext().getResources();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.material_blue_700));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.material_blue_700));

            remarkEdit.setFocusable(true);
            remarkEdit.setFocusableInTouchMode(true);
            remarkEdit.requestFocus();

            Message msg = new Message();
            msg.what = SHOW_INPUT_MSG;
            mHandler.sendMessageDelayed(msg, SHOW_INPUT_DELAY_TIMES);
        });

        mSendBtn.setOnClickListener(v -> {
            String money = mSendText.getText().toString();
            if ("".equals(money)) {
                return;
            }
            long timeMillis = System.currentTimeMillis();
            mSendText.setText("");

            Account account = new Account();
            account.setType(mSendCategory.getType());
            account.setDate(mDateText.getText().toString());
            if (mSendCategory == null) {
                LogUtils.w("no category");
                return;
            }
            account.setAccountCategory(mSendCategory);
            BigDecimal moneyDecimal = new BigDecimal(money)
                    .setScale(2);

            account.setAmount(Utils.convertBigDecimalToInteger(moneyDecimal));
            String msg = account.getDate() + ", " + mSendCategory.getCategory() + ":" + moneyDecimal;
            if (!"".equals(mRemarkMsg)) {
                account.setRemark(mRemarkMsg);
                msg += ", " + getContext().getResources().getString(R.string.remark_msg_btn) + ":"  + mRemarkMsg;
                mRemarkMsg = "";
            } else {
                account.setRemark("");
            }

            if (isThisMonth(account.getDate())) {
                if (account.getType() == 0) {
                    addAccountText(moneyDecimal, new BigDecimal(0));
                } else {
                    addAccountText(new BigDecimal(0), moneyDecimal);
                }
            }

            ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
            chatMsgEntity.setMsg(msg);
            chatMsgEntity.setChatType(ChatType.SEND_MSG);
            chatMsgEntity.setDate(new Date(timeMillis));
            chatMsgEntity.setTime(new Time(timeMillis));

            addNewMsg(chatMsgEntity);
            mPresent.addAccount(account);
        });

        mDateText = rootView.findViewById(R.id.date_text);

        mAccountText = rootView.findViewById(R.id.account_text);

        mCalendarButton = rootView.findViewById(R.id.calendar_img_btn);
        mCalendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CalendarActivity.class);
            startActivity(intent);
        });

        mReportButton = rootView.findViewById(R.id.report_img_btn);
        mReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ReportActivity.class);
            startActivity(intent);
        });

        mViewPager = rootView.findViewById(R.id.view_list);
        mTabLayout = rootView.findViewById(R.id.tab_layout);
        mFragmentList = new ArrayList<>();
        mPresent.queryAccountCategory();
        mPresent.queryCurrentMonthAccount();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (null != mPresent) {
            mPresent.destroy();
            mPresent = null;
        }
        super.onDestroy();
    }

    /**
     * 添加新消息,并滑动到最后一行
     * @param chatMsgEntity
     */
    private void addNewMsg(ChatMsgEntity chatMsgEntity) {
        mChatAdapter.addNewMsg(chatMsgEntity);
        mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    private void reply(String msg) {
        ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
        long timeMillis = System.currentTimeMillis();
        chatMsgEntity.setMsg(msg);
        chatMsgEntity.setChatType(ChatType.RECEIVE_MSG);
        chatMsgEntity.setDate(new Date(timeMillis));
        chatMsgEntity.setTime(new Time(timeMillis));

        addNewMsg(chatMsgEntity);
    }

    /**
     *
     * @param titles
     * @param map
     */
    private void setAccountCategoryView(List<AccountCategory> titles,
                                        Map<Long, List<AccountCategory>> map) {
        for (AccountCategory ac:titles
             ) {
            Long pid = ac.getId();
            AccountCategoryFragment fragment = AccountCategoryFragment.newInstance(pid, map.get(pid));
            mFragmentList.add(fragment);
        }
        AccountCategoryPageAdapter accountCategoryPageAdapter = new AccountCategoryPageAdapter(
                getChildFragmentManager(), titles, mFragmentList);
        mViewPager.setAdapter(accountCategoryPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        if ("".equals(mBookKeepTypeText.getText().toString())) {
            AccountCategory accountCategory = map.get(titles.get(0).getId()).get(0);
            setBookKeepTypeText(accountCategory);
        }
    }

    /**
     * 内存中保存记账类型，方便直接获取
     * @param map 各种记账类型
     *
     */
    @Deprecated
    private void setAccountCategoryMap(Map<Long, List<AccountCategory>> map) {
        for (Map.Entry<Long, List<AccountCategory>> entry:map.entrySet()
             ) {
            List<AccountCategory> list = entry.getValue();
            for (AccountCategory accountCategory:list
                 ) {
                //mCategoryMap.put(accountCategory.getId(), accountCategory);
            }
        }
    }

    /**
     * 设置记账类型文本和id
     * @param accountCategory
     */
    private void setBookKeepTypeText(AccountCategory accountCategory) {
        mSendCategory = accountCategory;
        if (null != mSendCategory) {
            mBookKeepTypeText.setText(mSendCategory.getCategory());
        }
    }

    /**
     * 设置当月收入和支出显示
     * @param totalIncome   收入
     * @param totalOver     支出
     */
    private void setAccountText(BigDecimal totalIncome, BigDecimal totalOver) {
        if (null != mAccountText) {
            mAccountText.setText(concatAccountText(totalIncome, totalOver));
        }
    }

    /**
     * 发送数据增加相关数值
     * @param income
     * @param over
     */
    private void addAccountText(BigDecimal income, BigDecimal over) {
        if (null != mAccountText) {
            String s = mAccountText.getText().toString();
            if (null != s) {
                String[] accounts = s.split("/");
                if (accounts.length == 2) {
                    BigDecimal totalIncome = new BigDecimal(accounts[0]).add(income);
                    BigDecimal totalOver = new BigDecimal(accounts[1]).add(over);
                    mAccountText.setText(concatAccountText(totalIncome, totalOver));
                }
            }
        }
    }

    /**
     * 减少相关数值
     * @param income
     * @param over
     */
    private void subAccountText(BigDecimal income, BigDecimal over) {
        if (null != mAccountText) {
            String s = mAccountText.getText().toString();
            if (null != s) {
                String[] accounts = s.split("/");
                if (accounts.length == 2) {
                    BigDecimal totalIncome = new BigDecimal(accounts[0]).add(income);
                    BigDecimal totalOver = new BigDecimal(accounts[1]).add(over);
                    mAccountText.setText(concatAccountText(totalIncome, totalOver));
                }
            }
        }
    }

    /**
     * 拼接收入/支出 文本显示
     * @param totalIncome
     * @param totalOver
     * @return
     */
    private String concatAccountText(BigDecimal totalIncome, BigDecimal totalOver) {
        StringBuilder sb = new StringBuilder();
        sb.append(totalIncome).append("/").append(totalOver);
        return sb.toString();
    }

    /**
     * 日期是否在当前月份
     * @param date
     * @return
     */
    private boolean isThisMonth(String date) {
        java.util.Date month = new java.util.Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String str = formatter.format(month);
        if (str.equals(date.substring(0, 7))) {
            return true;
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setAccountCategoryId(BookKeepingEvent bookKeepingEvent) {
        AccountCategory accountCategory = bookKeepingEvent.getAccountCategory();
        LogUtils.v("accountCategory = " + accountCategory.toString());
        setBookKeepTypeText(accountCategory);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deleteAccountMsg(ChatMsgDeleteEvent chatMsgDeleteEvent) {
        Long accountId = chatMsgDeleteEvent.getChatMsgEntity().getAccountId();
        mPresent.deleteAccount(accountId);
    }

    /**
     *
     * @param list 全部
     */
    @Override
    public void loadAccountCategoryData(List<AccountCategory> list) {
        List<AccountCategory> titles = new ArrayList<>();
        Map<Long, List<AccountCategory>> map = new HashMap<>();
        for (AccountCategory ac:list
             ) {
            if (0 == ac.getPid()) {
                titles.add(ac);
                continue;
            }
            Long pid = ac.getPid();
            List<AccountCategory> acList = map.getOrDefault(pid, new ArrayList<>());
            acList.add(ac);
            map.put(pid, acList);
        }
        setAccountCategoryView(titles, map);
    }

    @Override
    public void loadAccountCategoryData(List<AccountCategory> titles,
                                        Map<Long, List<AccountCategory>> map) {
        setAccountCategoryView(titles, map);
    }

    @Override
    public void addAccountState(Boolean insert, Account account) {
        LogUtils.v(account.toString());
        Long accountId = account.getId();
        if (null != accountId) {
            mChatAdapter.setAccountIdForMsg(accountId);
        }
        reply(insert ? SUCCESS_REPLY : FAILED_REPLY);
    }

    /**
     * 加载当前月份账单数据
     *
     * @param accountList
     */
    @Override
    public void loadCurrentMonthAccountData(List<Account> accountList) {
        //收入
        BigDecimal totalIncome = BigDecimal.valueOf(0).setScale(2);
        //支出
        BigDecimal totalOver = BigDecimal.valueOf(0).setScale(2);
        for (Account account:accountList
             ) {
            BigDecimal money = Utils.convertIntegerToBigDecimal(account.getAmount());
            if (account.getType() == 0) {
                totalIncome = totalIncome.add(money);
            } else {
                // == 1
                totalOver = totalOver.add(money);
            }
        }
        setAccountText(totalIncome, totalOver);
    }

    /**
     * 加载删除信息状态
     *
     * @param i
     */
    @Override
    public void loadDeleteAccountState(Integer i) {
        LogUtils.v("delete state = " + i);
        if (i > 0) {
            //重新计算当月账单
            mPresent.queryCurrentMonthAccount();
            mChatAdapter.deleteMsgByPosition();
        }
    }
}