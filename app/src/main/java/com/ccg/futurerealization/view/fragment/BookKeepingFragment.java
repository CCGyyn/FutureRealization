package com.ccg.futurerealization.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;
import com.ccg.futurerealization.present.BookKeepingPresenter;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Utils;
import com.ccg.futurerealization.view.widget.AccountTypeTextView;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
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

    private AccountTypeTextView mAccountTypeTextView;

    private Button mRemarkBtn;

    private TextView mDateText;

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

        mAccountTypeTextView = sendLayout.findViewById(R.id.account_type_view);
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
            account.setType(mAccountTypeTextView.getAccountType());
            account.setDate(Utils.stringConvertSqlDate(mDateText.getText().toString()));
            account.setTime(new Time(timeMillis));
            if (mSendCategory == null) {
                LogUtils.w("no category");
                return;
            }
            account.setCategory(mSendCategory);
            BigDecimal moneyDecimal = new BigDecimal(money)
                    .setScale(2);
            account.setAmount(moneyDecimal);
            String msg = account.getDate().toString() + ", " + mSendCategory.getCategory() + ":" + moneyDecimal;
            if (!"".equals(mRemarkMsg)) {
                account.setRemark(mRemarkMsg);
                msg += ", " + getContext().getResources().getString(R.string.remark_msg_btn) + ":"  + mRemarkMsg;
                mRemarkMsg = "";
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
        mDateText.setText(new Date(System.currentTimeMillis()).toString());
        mDateText.setOnClickListener(v -> {
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.datepick_dialog, null);
            MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                    .setView(linearLayout)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.dismiss();
                    });
            DatePicker datePicker = linearLayout.findViewById(R.id.date_picker);
            Calendar c = Calendar.getInstance();
            datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
                            mDateText.setText(Utils.stringConvertSqlDate(sb.toString()).toString());
                        }
                    });

            MaterialDialog dialog = builder.create();
            dialog.show();
            Resources resources = getContext().getResources();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.material_blue_700));
        });
        mViewPager = rootView.findViewById(R.id.view_list);
        mTabLayout = rootView.findViewById(R.id.tab_layout);
        mFragmentList = new ArrayList<>();
        mPresent.queryAccountCategory();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setAccountCategoryId(BookKeepingEvent bookKeepingEvent) {
        AccountCategory accountCategory = bookKeepingEvent.getAccountCategory();
        LogUtils.v("accountCategory = " + accountCategory.toString());
        setBookKeepTypeText(accountCategory);
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
    public void addAccountState(Boolean insert) {
        reply(insert ? SUCCESS_REPLY : FAILED_REPLY);
    }
}