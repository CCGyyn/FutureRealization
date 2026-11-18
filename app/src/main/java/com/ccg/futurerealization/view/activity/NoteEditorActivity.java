package com.ccg.futurerealization.view.activity;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.Note;
import org.litepal.LitePal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Full-page plain-text editor. Back and Done both save before leaving. */
public class NoteEditorActivity extends AppCompatActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private EditText title, content;
    private TextView metadata;
    private View save, delete, back;
    private long id, updatedAt;
    private String originalTitle = "", originalContent = "";
    private boolean busy, ready;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_note_editor);
        title = findViewById(R.id.note_title); content = findViewById(R.id.note_content); metadata = findViewById(R.id.note_metadata);
        save = findViewById(R.id.note_save); delete = findViewById(R.id.note_delete); back = findViewById(R.id.note_back);
        id = state == null ? getIntent().getLongExtra("noteId", 0) : state.getLong("noteId");
        save.setOnClickListener(v -> saveAndClose(false)); back.setOnClickListener(v -> saveAndClose(true)); delete.setOnClickListener(v -> confirmDelete());
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateMetadata(); }
            @Override public void afterTextChanged(Editable s) { }
        };
        title.addTextChangedListener(watcher); content.addTextChangedListener(watcher);
        if (state != null && state.getBoolean("ready")) {
            originalTitle = state.getString("originalTitle", ""); originalContent = state.getString("originalContent", ""); updatedAt = state.getLong("updatedAt");
            title.setText(state.getString("title", "")); content.setText(state.getString("content", "")); ready = true; setBusy(false);
        } else if (id == 0) { ready = true; updatedAt = System.currentTimeMillis(); setBusy(false); updateMetadata(); }
        else load();
    }
    private void load() {
        setBusy(true); metadata.setText("正在读取…");
        worker.execute(() -> {
            try {
                Note note = LitePal.find(Note.class, id);
                if (note == null) throw new IllegalStateException();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    originalTitle = note.getTitle(); originalContent = note.getContent(); updatedAt = note.getUpdatedAt();
                    title.setText(originalTitle); content.setText(originalContent); ready = true; setBusy(false); updateMetadata();
                });
            } catch (Exception e) { runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) new AlertDialog.Builder(this).setTitle("无法读取笔记").setMessage("请重试，原笔记未改变。").setCancelable(false).setNegativeButton("返回", (d, w) -> finish()).setPositiveButton("重试", (d, w) -> load()).show(); }); }
        });
    }
    private void updateMetadata() {
        if (metadata == null || title == null || content == null) return;
        metadata.setText(new SimpleDateFormat("M月d日 HH:mm", Locale.CHINA).format(new Date(updatedAt == 0 ? System.currentTimeMillis() : updatedAt)) + "  |  " + (title.length() + content.length()) + " 字");
    }
    private void saveAndClose(boolean fromBack) {
        if (busy || !ready) return;
        final String name = title.getText().toString().trim(), body = content.getText().toString();
        if (body.trim().isEmpty()) {
            if (id == 0 && name.isEmpty() && fromBack) { finish(); return; }
            content.setError("请输入笔记内容"); content.requestFocus(); return;
        }
        if (id != 0 && name.equals(originalTitle) && body.equals(originalContent)) { finish(); return; }
        setBusy(true);
        worker.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                if (id == 0) {
                    Note note = new Note(); note.setTitle(name); note.setContent(body); note.setUpdatedAt(now);
                    if (!note.save()) throw new IllegalStateException();
                } else {
                    ContentValues values = new ContentValues(); values.put("title", name); values.put("content", body); values.put("updatedat", now);
                    if (LitePal.update(Note.class, values, id) != 1) throw new IllegalStateException();
                }
                runOnUiThread(() -> { if (!isDestroyed()) { setResult(RESULT_OK); finish(); } });
            } catch (Exception e) { fail("保存失败，内容已保留，请重试"); }
        });
    }
    private void confirmDelete() {
        if (busy || !ready || id == 0) return;
        new AlertDialog.Builder(this).setTitle("删除这篇笔记？").setMessage(title.getText().toString().isEmpty() ? "未命名笔记" : title.getText().toString())
                .setNegativeButton("保留", null).setPositiveButton("删除", (dialog, which) -> {
                    if (busy) return;
                    setBusy(true);
                    worker.execute(() -> {
                        try { if (LitePal.delete(Note.class, id) != 1) throw new IllegalStateException(); runOnUiThread(() -> { if (!isDestroyed()) { setResult(RESULT_OK); finish(); } }); }
                        catch (Exception e) { fail("删除失败，请重试"); }
                    });
                }).show();
    }
    private void setBusy(boolean value) {
        busy = value; title.setEnabled(!value); content.setEnabled(!value); save.setEnabled(!value); back.setEnabled(!value); delete.setEnabled(!value);
        save.setAlpha(value ? 0.4f : 1f); delete.setVisibility(id == 0 ? View.GONE : View.VISIBLE);
    }
    private void fail(String message) { runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) { setBusy(false); Toast.makeText(this, message, Toast.LENGTH_LONG).show(); } }); }
    @Override public void onBackPressed() { saveAndClose(true); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putLong("noteId", id); state.putBoolean("ready", ready); state.putLong("updatedAt", updatedAt);
        state.putString("originalTitle", originalTitle); state.putString("originalContent", originalContent);
        state.putString("title", title.getText().toString()); state.putString("content", content.getText().toString()); super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() { worker.shutdown(); super.onDestroy(); }
}
