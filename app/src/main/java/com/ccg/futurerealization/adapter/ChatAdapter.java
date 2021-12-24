package com.ccg.futurerealization.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;
import com.ccg.futurerealization.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-20 下午3:15
 * @Version: 1.0
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHoder> {

    private List<ChatMsgEntity> mChatMsgs = new ArrayList<>();


    @Override
    public int getItemViewType(int position) {
        final ChatMsgEntity chatMsgEntity = mChatMsgs.get(position);
        return chatMsgEntity.getChatType().getCode();
    }

    @NonNull
    @Override
    public ChatViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatType.SEND_MSG.getCode()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_chat_msg_layout, parent, false);
        } else {
            // ChatType.RECEIVE_MSG
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_chat_msg_layout, parent, false);
        }
        return new ChatViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHoder holder, int position) {
        final ChatMsgEntity chatMsgEntity = mChatMsgs.get(position);
        holder.setText(chatMsgEntity.getMsg());
    }

    @Override
    public int getItemCount() {
        return mChatMsgs.size();
    }

    public void setChatMsgs(List<ChatMsgEntity> chatMsgEntities) {
        mChatMsgs = chatMsgEntities;
        notifyDataSetChanged();
    }

    /**
     * 新增消息
     * @param chatMsgEntity
     */
    public void addNewMsg(ChatMsgEntity chatMsgEntity) {
        LogUtils.v(chatMsgEntity.toString());
        mChatMsgs.add(chatMsgEntity);
        notifyItemChanged(mChatMsgs.size() - 1);
    }

    public class ChatViewHoder extends RecyclerView.ViewHolder {

        private TextView msgText;

        public ChatViewHoder(@NonNull View itemView) {
            super(itemView);
            msgText = itemView.findViewById(R.id.msg_text);
        }

        private void setText(String msg) {
            msgText.setText(msg);
        }
    }

}
