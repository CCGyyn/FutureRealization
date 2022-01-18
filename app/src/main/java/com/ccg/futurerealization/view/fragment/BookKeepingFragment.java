package com.ccg.futurerealization.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.ccg.futurerealization.base.BaseFragment;
import com.ccg.futurerealization.base.EventBusFragment;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.BookKeepingContract;
import com.ccg.futurerealization.event.BookKeepingEvent;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;
import com.ccg.futurerealization.present.BookKeepingPresenter;
import com.ccg.futurerealization.utils.LogUtils;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private RecyclerView mRecyclerView;
    private Button mSendBtn;
    private EditText mSendText;
    private TextView mBookKeepTypeText;

    private ChatAdapter mChatAdapter;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private BookKeepingContract.Present mPresent;

    private List<Fragment> mFragmentList;

    private AccountCategory mSendCategory;

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

        mSendBtn.setOnClickListener(v -> {
            ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
            String msg = mSendText.getText().toString();
            if ("".equals(msg)) {
                return;
            }
            long timeMillis = System.currentTimeMillis();
            mSendText.setText("");
            chatMsgEntity.setMsg(msg);
            chatMsgEntity.setChatType(ChatType.SEND_MSG);
            chatMsgEntity.setDate(new Date(timeMillis));
            chatMsgEntity.setTime(new Time(timeMillis));

            addNewMsg(chatMsgEntity);
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
}