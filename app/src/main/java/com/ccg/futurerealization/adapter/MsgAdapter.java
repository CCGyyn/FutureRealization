package com.ccg.futurerealization.adapter;

import android.graphics.Paint;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.DoSth;
import java.util.ArrayList;
import java.util.List;

/** Goal cards. Actions use immutable row identity rather than captured list positions. */
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.Holder> {
    private final List<DoSth> goals = new ArrayList<>();
    private Listen listener;
    private boolean busy;
    public interface Listen { void edit(DoSth goal); void toggle(DoSth goal); void abandon(DoSth goal); }
    public void setListen(Listen value) { listener = value; }
    public void setBusy(boolean value) { busy = value; notifyDataSetChanged(); }
    public void setDoSthList(List<? extends DoSth> values) { goals.clear(); goals.addAll(values); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_list, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        DoSth goal = goals.get(position);
        boolean abandoned = goal.isAbandoned();
        boolean done = !abandoned && Boolean.TRUE.equals(goal.getState());
        holder.itemView.setBackgroundResource(abandoned ? R.drawable.goal_abandoned_card : R.drawable.ledger_card);
        holder.abandonedIcon.setVisibility(abandoned ? View.VISIBLE : View.GONE);
        holder.check.setVisibility(abandoned ? View.GONE : View.VISIBLE);
        holder.title.setText(goal.getFuture_content());
        holder.title.setPaintFlags(done ? holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : holder.title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        holder.title.setTextColor(holder.itemView.getResources().getColor(done || abandoned ? R.color.ledger_muted : R.color.ledger_text));
        int type = goal.getType() == null ? 0 : goal.getType();
        holder.type.setText(type == 1 ? "长期目标" : type == 2 ? "短期目标" : "普通目标");
        holder.status.setText(abandoned ? "⊘ 已放弃" : done ? "已完成" : "进行中");
        int textColor = holder.itemView.getResources().getColor(type == 1 ? R.color.goal_long_text : type == 2 ? R.color.goal_short_text : R.color.goal_normal_text);
        int surfaceColor = holder.itemView.getResources().getColor(type == 1 ? R.color.goal_long_surface : type == 2 ? R.color.goal_short_surface : R.color.goal_normal_surface);
        int markerColor = holder.itemView.getResources().getColor(type == 1 ? R.color.goal_long_marker : type == 2 ? R.color.goal_short_marker : R.color.goal_normal_marker);
        GradientDrawable badge = new GradientDrawable();
        badge.setColor(surfaceColor);
        badge.setCornerRadius(8 * holder.itemView.getResources().getDisplayMetrics().density);
        holder.type.setBackground(badge);
        holder.type.setTextColor(textColor);
        holder.marker.setBackgroundColor(markerColor);
        holder.marker.setAlpha(abandoned ? 0.3f : done ? 0.55f : 1f);
        holder.status.setTextColor(holder.itemView.getResources().getColor(abandoned ? R.color.goal_normal_text : R.color.ledger_muted));
        holder.check.setButtonTintList(ColorStateList.valueOf(textColor));
        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(done);
        holder.check.setEnabled(!busy && !abandoned);
        holder.check.setContentDescription((abandoned ? "已放弃目标：" : done ? "恢复目标：" : "完成目标：") + goal.getFuture_content());
        holder.check.setOnClickListener(v -> {
            holder.check.setChecked(done); // Wait for the database before reflecting the new state.
            if (!busy && listener != null) listener.toggle(goal);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (busy || listener == null) return true;
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext()).setTitle(goal.getFuture_content())
                    .setItems(new String[]{abandoned ? "恢复进行中" : "标记放弃", "编辑目标"}, (dialog, which) -> {
                        if (which == 0) listener.abandon(goal); else listener.edit(goal);
                    }).show();
            return true;
        });
        holder.itemView.setOnClickListener(v -> { if (!busy && listener != null) listener.edit(goal); });
    }
    @Override public int getItemCount() { return goals.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, type, status;
        final View marker, abandonedIcon;
        final CheckBox check;
        Holder(View view) {
            super(view);
            title = view.findViewById(R.id.msg_text);
            type = view.findViewById(R.id.goal_type);
            status = view.findViewById(R.id.goal_status);
            marker = view.findViewById(R.id.goal_type_marker);
            abandonedIcon = view.findViewById(R.id.goal_abandoned_icon);
            check = view.findViewById(R.id.goal_check);
        }
    }
}
