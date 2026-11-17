package com.ccg.futurerealization.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.event.ChatMsgDeleteEvent;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMsgEntity> messages = new ArrayList<>();

    @Override public int getItemViewType(int position) { return messages.get(position).getChatType().getCode(); }

    @NonNull @Override public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == ChatType.SEND_MSG.getCode() ? R.layout.right_chat_msg_layout : R.layout.left_chat_msg_layout;
        return new ChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMsgEntity message = messages.get(position);
        holder.text.setText(message.getMsg());
        holder.text.setOnLongClickListener(null);
        if (message.getAccountId() != null) holder.text.setOnLongClickListener(v -> {
            EventBus.getDefault().post(ChatMsgDeleteEvent.getInstance(message));
            return true;
        });
    }

    @Override public int getItemCount() { return messages.size(); }

    public void setChatMsgs(List<ChatMsgEntity> list) {
        messages = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        ChatViewHolder(@NonNull View view) { super(view); text = view.findViewById(R.id.msg_text); }
    }
}
