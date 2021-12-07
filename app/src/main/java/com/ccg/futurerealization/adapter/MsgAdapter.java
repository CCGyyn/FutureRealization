package com.ccg.futurerealization.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.DoSth;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:用于显示DoSth的每一行信息
 * @Author: cgaopeng
 * @CreateDate: 21-12-7 上午11:14
 * @Version: 1.0
 */
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {

    private List<DoSth> mDoSthList = new ArrayList<>();

    public MsgAdapter() {
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
        holder.mTextView.setText(doSth.getFuture_content());
    }

    @Override
    public int getItemCount() {
        return mDoSthList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.msg_text);
        }
    }
}
