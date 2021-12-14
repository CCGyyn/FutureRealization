package com.ccg.futurerealization.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.db.DoSthManager;
import com.ccg.futurerealization.db.DoSthManagerImpl;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.view.activity.MsgShowDialogActivity;
import com.ccg.futurerealization.view.widget.RadioGroupButton;

import java.util.ArrayList;
import java.util.List;

import de.mrapp.android.dialog.MaterialDialog;

/**
 * @Description:用于显示DoSth的每一行信息
 * @Author: cgaopeng
 * @CreateDate: 21-12-7 上午11:14
 * @Version: 1.0
 *
 * @update:cgaopeng 21-12-13 添加每行点击可以更改数据
 * @update:cgaopeng 21-12-14 更新修改数据方法，添加不同类型的计划表示
 */
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {

    private List<DoSth> mDoSthList = new ArrayList<>();

    private DoSthManager mDoSthManager;

    public MsgAdapter() {
        mDoSthManager = DoSthManagerImpl.getInstance();
    }

    public void setDoSthList(List<? extends DoSth> doSthList) {
        mDoSthList.clear();
        mDoSthList.addAll(doSthList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_list, parent, false);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        final DoSth doSth = mDoSthList.get(position);
        int num = position + 1;
        String text = num + "." + doSth.getFuture_content();
        TextView msgTextView = holder.msgText;
        msgTextView.setText(text);
        //已实现添加删除线
        if (doSth.getState()) {
            msgTextView.setPaintFlags(msgTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        msgTextView.setOnClickListener(v -> {
           showMsgDialog(v.getContext(), doSth, position);
        });
        switch (doSth.getType()) {
            case 1:
                // long term
                msgTextView.setBackgroundColor(msgTextView.getContext().getResources().getColor(R.color.long_term_color));
                break;
            case 2:
                // short term
                msgTextView.setBackgroundColor(msgTextView.getContext().getResources().getColor(R.color.short_term_color));
                break;
            default:
                msgTextView.setBackgroundColor(msgTextView.getContext().getResources().getColor(R.color.white));
        }
    }

    /**
     * 更新某行数据
     * @param doSth
     * @param position
     */
    private void updateMsgItem(DoSth doSth, int position) {
        mDoSthList.remove(position);
        mDoSthList.add(position, doSth);
        notifyItemChanged(position);
    }

    /**
     * 移除某一行
     * 但是移除后位置不会更新
     * 4,5,6 移除5后, 剩下的是4,6 后面不会更改位置
     * 主要用于前面不加序号的
     * @param position
     */
    @Deprecated
    private void removeMsgItem(int position) {
        mDoSthList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     *
     * @param context
     * @param doSth
     * @param position 用于刷新数据的位置
     */
    private void showMsgDialog(Context context, DoSth doSth, int position) {
        DoSth doSthC = doSth.clone();
        LinearLayout linearLayout = (LinearLayout) View.inflate(context, R.layout.update_dosth_dialog, null);
        RadioGroupButton rgType = linearLayout.findViewById(R.id.rg_item_group_type);
        RadioGroupButton rgState = linearLayout.findViewById(R.id.rg_item_group_state);
        EditText content = linearLayout.findViewById(R.id.future_content);

        rgType.setDefualtSelected(doSthC.getType());
        rgState.setDefualtSelected(doSthC.getState() ? 1 : 0);
        content.setText(doSthC.getFuture_content());

        rgType.setOnGroupBtnClickListener(code -> {
            doSthC.setType(Integer.valueOf(code));
        });
        rgState.setOnGroupBtnClickListener(code -> {
            doSthC.setState(Integer.parseInt(code) == 1);
        });

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .setTitle(context.getString(R.string.update_msg_alerdialog_title))
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    doSthC.setFuture_content(content.getText().toString());
                    updateMsgItem(doSthC, position);
                    mDoSthManager.updateInfo(doSthC);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.update_dialog_delete_btn, (dialog, which) -> {
                    mDoSthManager.deleteById(doSthC.getId());
                    mDoSthList.remove(position);
                    notifyDataSetChanged();
                    dialog.dismiss();
                });
        MaterialDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Resources resources = context.getResources();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.material_blue_700));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.material_blue_700));
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(resources.getColor(R.color.material_blue_700));
    }

    @Override
    public int getItemCount() {
        return mDoSthList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {

        private TextView msgText;

        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = (TextView) itemView.findViewById(R.id.msg_text);
        }
    }
}
