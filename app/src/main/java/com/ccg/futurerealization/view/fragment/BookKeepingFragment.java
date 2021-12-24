package com.ccg.futurerealization.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.ChatAdapter;
import com.ccg.futurerealization.base.BaseFragment;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;

import java.sql.Date;
import java.sql.Time;

/**
 * @Description: 记账,相关聊天框用draw9patch制作
 * @Author: cgaopeng
 * @CreateDate: 21-12-20
 * @Version: 1.0
 */
public class BookKeepingFragment extends BaseFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView mRecyclerView;
    private Button mSendBtn;
    private EditText mSendText;
    private ChatAdapter mChatAdapter;

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

    }

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_book_keeping;
    }

    @Override
    protected void onCreateViewInit(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.chat_msg_list);
        mSendBtn = rootView.findViewById(R.id.send_layout).findViewById(R.id.send_btn);
        mSendText = rootView.findViewById(R.id.send_layout).findViewById(R.id.send_msg);
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
}