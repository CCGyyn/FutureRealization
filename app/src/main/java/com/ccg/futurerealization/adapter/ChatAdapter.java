package com.ccg.futurerealization.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.event.ChatMsgDeleteEvent;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-20 下午3:15
 * @Version: 1.0
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHoder> {

    private static final Integer[] mMsgLayout = new Integer[] {
            // ChatType.RECEIVE_MSG
            R.layout.left_chat_msg_layout,
            // ChatType.SEND_MSG
            R.layout.right_chat_msg_layout
    };

    int x;
    int y;
    private PopupWindow mPopupWindow;

    private int mRemovePosition = -1;

    private List<ChatMsgEntity> mChatMsgs = new ArrayList<>();


    @Override
    public int getItemViewType(int position) {
        final ChatMsgEntity chatMsgEntity = mChatMsgs.get(position);
        return chatMsgEntity.getChatType().getCode();
    }

    @NonNull
    @Override
    public ChatViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mMsgLayout[viewType], parent, false);
        return new ChatViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHoder holder, int position) {
        final ChatMsgEntity chatMsgEntity = mChatMsgs.get(position);
        holder.setText(chatMsgEntity.getMsg());
        if (chatMsgEntity.getChatType().getCode() == 1) {
            holder.msgText.setOnLongClickListener(v -> {
                showPopupWindow(v, position);
                return false;
            });
            holder.msgText.setOnTouchListener((v, event) -> {
                x = (int) event.getRawX();
                y = (int) event.getRawY();
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mChatMsgs.size();
    }

    public void setChatMsgs(List<ChatMsgEntity> chatMsgEntities) {
        mChatMsgs = chatMsgEntities;
        notifyDataSetChanged();
    }

    public void setAccountIdForMsg(Long id) {
        mChatMsgs.get(mChatMsgs.size() - 1).setAccountId(id);
    }

    public void deleteMsgByPosition() {
        if (mRemovePosition >= 0 && mRemovePosition <= mChatMsgs.size()) {
            mChatMsgs.remove(mRemovePosition);
            notifyDataSetChanged();
            mRemovePosition = -1;
        }
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

    private View getPopupWindowContentView(View view) {
        // 一个自定义的布局，作为显示的内容
        int layoutId = R.layout.popup_menu_links;   // 布局ID
        View contentView = LayoutInflater.from(view.getContext()).inflate(layoutId, null);
        return contentView;
    }

    private void showPopupWindow(View anchorView) {
        View contentView = getPopupWindowContentView(anchorView);
        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        // 设置好参数之后再show
        int windowPos[] = PopupWindowUtil.calculatePopWindowPos(anchorView, contentView, contentView.findViewById(R.id.pop_up_window_view), x, y);
        mPopupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);
    }

    private void showPopupWindow(View view, int position) {
        View contentView = getPopupWindowContentView(view);
        TextView deletePopUpMenu = contentView.findViewById(R.id.pop_up_delete_menu);
        deletePopUpMenu.setOnClickListener(v -> {
            if (position <= mChatMsgs.size()) {
                EventBus.getDefault().post(ChatMsgDeleteEvent.getInstance(mChatMsgs.get(position)));
                mRemovePosition = position;
            }
            mPopupWindow.dismiss();
        });
        mPopupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        int windowPos[] = PopupWindowUtil.calculatePopWindowPos(view, contentView, contentView.findViewById(R.id.pop_up_window_view), x, y);
        mPopupWindow.showAtLocation(view, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);
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

    static class PopupWindowUtil {
        public static int[] calculatePopWindowPos(View anchorView
                , View popView, View itemTestView
                , int anchorX, int anchroY) {
            final int[] windowPos = new int[2];
            final int screenHeight = getScreenHeight(anchorView.getContext());
            final int screenWidth = getScreenWidth(anchorView.getContext());
            // 测量popView 弹出框
            popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int padding = popView.getPaddingTop();
            int popHeight = popView.getMeasuredHeight()  - padding*2;
            int popWidth = popView.getMeasuredWidth()    - padding*2;
            int itemHeight = itemTestView.getHeight() + padding;
            final boolean showUp = screenHeight + itemHeight - anchroY < popHeight;
            final boolean showLeft = screenWidth - anchorX < popWidth;

            if (showUp) {
                windowPos[1] = anchroY - popHeight - padding;
            } else {
                windowPos[1] = anchroY - padding;
            }
            if (showLeft) {
                windowPos[0] = anchorX - popWidth - padding;
            } else {
                windowPos[0] = anchorX - padding;
            }
            return windowPos;
        }

        public static int getScreenHeight(Context context) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }
        /**
         * 获取屏幕宽度(px)
         */
        public static int getScreenWidth(Context context) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }
    }
}
