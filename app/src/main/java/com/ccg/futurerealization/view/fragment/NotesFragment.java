package com.ccg.futurerealization.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.Note;
import com.ccg.futurerealization.view.activity.NoteEditorActivity;
import org.litepal.LitePal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Independent local-notes tab with a searchable two-column card layout. */
public class NotesFragment extends Fragment {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final List<Note> notes = new ArrayList<>(), visible = new ArrayList<>();
    private RecyclerView list;
    private EditText search;
    private TextView status, empty;
    private NoteAdapter adapter;
    private boolean deleting;
    private int request;
    private String query = "";

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }
    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        super.onViewCreated(root, state);
        list = root.findViewById(R.id.notes_list); search = root.findViewById(R.id.notes_search);
        status = root.findViewById(R.id.notes_status); empty = root.findViewById(R.id.notes_empty);
        adapter = new NoteAdapter();
        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setAdapter(adapter);
        if (state != null) query = state.getString("query", "");
        search.setText(query);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { query = s.toString(); render(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        root.findViewById(R.id.add_note).setOnClickListener(v -> open(0));
        reload();
    }
    @Override public void onResume() { super.onResume(); reload(); }
    @Override public void setUserVisibleHint(boolean value) { super.setUserVisibleHint(value); if (value && getView() != null) reload(); }
    private void open(long id) { if (!deleting) startActivity(new Intent(requireContext(), NoteEditorActivity.class).putExtra("noteId", id)); }
    private void reload() {
        if (!isAdded() || getView() == null) return;
        final View root = getView(); final android.app.Activity activity = requireActivity(); final int serial = ++request;
        worker.execute(() -> {
            try {
                List<Note> rows = LitePal.order("updatedat desc, id desc").find(Note.class);
                activity.runOnUiThread(() -> {
                    if (!isAdded() || getView() != root || serial != request) return;
                    notes.clear(); notes.addAll(rows); render();
                });
            } catch (Exception e) { activity.runOnUiThread(() -> { if (getView() == root) { status.setText("读取失败，点击重试"); status.setOnClickListener(v -> reload()); } }); }
        });
    }
    private void render() {
        if (getView() == null || adapter == null) return;
        String needle = query.trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (Note note : notes) if (needle.isEmpty() || (note.getTitle() + "\n" + note.getContent()).toLowerCase(Locale.ROOT).contains(needle)) visible.add(note);
        status.setText(needle.isEmpty() ? notes.size() + " 篇笔记" : "找到 " + visible.size() + " 篇笔记");
        status.setOnClickListener(null);
        empty.setText(needle.isEmpty() ? "记录此刻的想法\n点击右下角 ＋ 新建笔记" : "没有找到相关笔记");
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }
    private void delete(Note note) {
        if (deleting) return;
        new AlertDialog.Builder(requireContext()).setTitle("删除这篇笔记？").setMessage(note.getTitle().isEmpty() ? "未命名笔记" : note.getTitle()).setNegativeButton("保留", null).setPositiveButton("删除", (d, w) -> {
            if (deleting) return;
            deleting = true;
            android.app.Activity activity = requireActivity();
            worker.execute(() -> {
                boolean success;
                try { success = LitePal.delete(Note.class, note.getId()) == 1; } catch (Exception e) { success = false; }
                final boolean ok = success;
                activity.runOnUiThread(() -> { deleting = false; if (!isAdded() || getView() == null) return; if (ok) reload(); else Toast.makeText(requireContext(), "删除失败，请重试", Toast.LENGTH_SHORT).show(); });
            });
        }).show();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) { state.putString("query", query); super.onSaveInstanceState(state); }
    @Override public void onDestroyView() { request++; list.setAdapter(null); adapter = null; list = null; search = null; status = null; empty = null; super.onDestroyView(); }
    @Override public void onDestroy() { worker.shutdown(); super.onDestroy(); }

    private class NoteAdapter extends RecyclerView.Adapter<NoteHolder> {
        @NonNull @Override public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) { return new NoteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false)); }
        @Override public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
            Note note = visible.get(position);
            holder.title.setText(note.getTitle().isEmpty() ? "未命名笔记" : note.getTitle());
            holder.body.setText(note.getContent());
            holder.date.setText(new SimpleDateFormat("M月d日 HH:mm", Locale.CHINA).format(new Date(note.getUpdatedAt())));
            holder.itemView.setOnClickListener(v -> open(note.getId()));
            holder.itemView.setOnLongClickListener(v -> { delete(note); return true; });
        }
        @Override public int getItemCount() { return visible.size(); }
    }
    private static class NoteHolder extends RecyclerView.ViewHolder {
        final TextView title, body, date;
        NoteHolder(View item) { super(item); title = item.findViewById(R.id.note_card_title); body = item.findViewById(R.id.note_card_body); date = item.findViewById(R.id.note_card_date); }
    }
}
