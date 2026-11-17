package com.ccg.futurerealization.view.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.MsgAdapter;
import com.ccg.futurerealization.base.BaseFragment;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainFragmentContract;
import com.ccg.futurerealization.present.MainFragmentPresenter;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment implements MainFragmentContract.View {
    private final List<DoSth> goals = new ArrayList<>();
    private MainFragmentPresenter presenter;
    private MsgAdapter adapter;
    private SwipeRefreshLayout refresh;
    private TextView summary, progressText, empty;
    private ProgressBar progress;
    private View addButton;
    private final TextView[] filters = new TextView[3];
    private int filter;
    private boolean busy;
    private AlertDialog editor;

    public static MainFragment newInstance(List<DoSth> ignored) { return new MainFragment(); }
    @Override protected int getContentViewId() { return R.layout.main_fragment; }
    @Override protected void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        presenter = new MainFragmentPresenter(this);
        if (state != null) filter = state.getInt("goal_filter", 0);
    }
    @Override protected void onCreateViewInit(View root) {
        summary = root.findViewById(R.id.goal_summary);
        progressText = root.findViewById(R.id.goal_progress_text);
        progress = root.findViewById(R.id.goal_progress);
        empty = root.findViewById(R.id.goal_empty);
        addButton = root.findViewById(R.id.add_goal);
        addButton.setOnClickListener(v -> showEditor(null));
        refresh = root.findViewById(R.id.swip_refresh_layout);
        refresh.setColorSchemeResources(R.color.ledger_accent);
        refresh.setOnRefreshListener(() -> presenter.queryDoSthData());
        RecyclerView list = root.findViewById(R.id.msg_list);
        adapter = new MsgAdapter();
        adapter.setListen(new MsgAdapter.Listen() {
            @Override public void edit(DoSth goal) { showEditor(goal); }
            @Override public void toggle(DoSth goal) {
                DoSth changed = goal.clone();
                changed.setState(!Boolean.TRUE.equals(goal.getState()));
                setBusy(true);
                presenter.updateDoSth(changed);
            }
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        int[] ids = {R.id.goals_all, R.id.goals_pending, R.id.goals_done};
        for (int i = 0; i < filters.length; i++) {
            final int value = i;
            filters[i] = root.findViewById(ids[i]);
            filters[i].setOnClickListener(v -> { filter = value; render(); });
        }
        render();
    }
    @Override public void onResume() { super.onResume(); if (presenter != null && !busy) presenter.queryDoSthData(); }

    private void render() {
        if (adapter == null) return;
        int done = 0;
        for (DoSth goal : goals) if (Boolean.TRUE.equals(goal.getState())) done++;
        int percent = goals.isEmpty() ? 0 : (int) ((long) done * 100 / goals.size());
        summary.setText("进行中 " + (goals.size() - done) + "  ·  已完成 " + done);
        progressText.setText("已完成 " + done + " / " + goals.size() + "  ·  " + percent + "%");
        progress.setProgress(percent);
        List<DoSth> visible = new ArrayList<>();
        for (DoSth goal : goals) {
            boolean completed = Boolean.TRUE.equals(goal.getState());
            if (filter == 0 || filter == 1 && !completed || filter == 2 && completed) visible.add(goal);
        }
        visible.sort((a, b) -> {
            int order = Boolean.compare(Boolean.TRUE.equals(a.getState()), Boolean.TRUE.equals(b.getState()));
            return order != 0 ? order : Long.compare(b.getId(), a.getId());
        });
        adapter.setDoSthList(visible);
        filters[0].setText("全部 " + goals.size());
        filters[1].setText("进行中 " + (goals.size() - done));
        filters[2].setText("已完成 " + done);
        for (int i = 0; i < filters.length; i++) filters[i].setSelected(filter == i);
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText(filter == 2 ? "还没有已完成的目标" : filter == 1 ? "暂无进行中的目标" : "还没有目标\n点击“添加目标”开始记录");
    }

    private void showEditor(@Nullable DoSth original) {
        if (busy) return;
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        content.setPadding(pad, 0, pad, 0);
        EditText text = new EditText(requireContext());
        text.setId(R.id.goal_editor_text);
        text.setHint("想完成什么？");
        text.setMinLines(2); text.setMaxLines(5);
        text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        if (original != null) text.setText(original.getFuture_content());
        content.addView(text);
        Spinner type = new Spinner(requireContext());
        type.setId(R.id.goal_editor_type);
        type.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"普通目标", "长期目标", "短期目标"}));
        type.setSelection(original == null || original.getType() == null ? 0 : original.getType());
        content.addView(type, new LinearLayout.LayoutParams(-1, (int) (56 * getResources().getDisplayMetrics().density)));
        Switch completed = new Switch(requireContext());
        completed.setText("已完成");
        completed.setPadding(0, pad / 2, 0, pad / 2);
        completed.setChecked(original != null && Boolean.TRUE.equals(original.getState()));
        content.addView(completed);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setTitle(original == null ? "添加目标" : "编辑目标")
                .setView(content).setNegativeButton("取消", null).setPositiveButton("保存", null);
        if (original != null) builder.setNeutralButton("删除", (d, w) -> new AlertDialog.Builder(requireContext())
                .setTitle("删除这个目标？").setMessage(original.getFuture_content()).setNegativeButton("保留", null)
                .setPositiveButton("删除", (dialog, which) -> { setBusy(true); presenter.deleteDoSthById(original.getId()); }).show());
        editor = builder.create();
        editor.setOnShowListener(d -> editor.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = text.getText().toString().trim();
            if (value.isEmpty()) { text.setError("请输入目标内容"); return; }
            DoSth goal = original == null ? new DoSth() : original.clone();
            goal.setFuture_content(value); goal.setType(type.getSelectedItemPosition()); goal.setState(completed.isChecked());
            setBusy(true);
            if (original == null) presenter.addDoSth(goal); else presenter.updateDoSth(goal);
        }));
        editor.show();
    }
    private void setBusy(boolean value) {
        busy = value;
        addButton.setEnabled(!value);
        refresh.setEnabled(!value);
        adapter.setBusy(value);
        if (editor != null && editor.isShowing()) {
            editor.setCancelable(!value);
            editor.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!value);
            editor.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!value);
            if (editor.getButton(AlertDialog.BUTTON_NEUTRAL) != null) editor.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(!value);
        }
    }
    private void saved() {
        setBusy(false);
        if (editor != null) editor.dismiss();
        render();
    }
    @Override public void addMsgSuccess(DoSth goal) { goals.add(goal); saved(); }
    @Override public void refreshAllMsgItem(List<DoSth> values) { goals.clear(); goals.addAll(values); refresh.setRefreshing(false); render(); }
    @Override public void refreshMsgItem(DoSth changed) {
        for (int i = 0; i < goals.size(); i++) if (goals.get(i).getId() == changed.getId()) { goals.set(i, changed); break; }
        saved();
    }
    @Override public void deleteItem(long id) { goals.removeIf(goal -> goal.getId() == id); saved(); }
    @Override public void actionFailed() { setBusy(false); refresh.setRefreshing(false); Toast.makeText(requireContext(), "操作失败，内容已保留，请重试", Toast.LENGTH_LONG).show(); }
    @Override public void onSaveInstanceState(@NonNull Bundle state) { state.putInt("goal_filter", filter); super.onSaveInstanceState(state); }
    @Override public void onDestroyView() { if (editor != null) editor.dismiss(); presenter.destroy(); presenter = null; adapter = null; super.onDestroyView(); }
}
