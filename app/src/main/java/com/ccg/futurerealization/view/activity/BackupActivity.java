package com.ccg.futurerealization.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.backup.LedgerBackup;
import com.ccg.futurerealization.backup.LedgerBackupStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** User-selected document transfer; no storage permission or network is needed. */
public class BackupActivity extends AppCompatActivity {
    private static final int EXPORT = 101, IMPORT = 102;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private Button exportButton, importButton;
    private TextView status;
    private boolean busy;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_backup);
        setTitle("记账备份与换机");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        exportButton = findViewById(R.id.export_backup);
        importButton = findViewById(R.id.import_backup);
        status = findViewById(R.id.backup_status);
        String last = getPreferences(MODE_PRIVATE).getString("last_export", "");
        status.setText(last.isEmpty() ? "尚未导出备份。建议每周备份一次，换机前再备份一次。" : "上次成功导出：" + last);
        exportButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("application/json")
                    .putExtra(Intent.EXTRA_TITLE, "FutureRealization-ledger-" +
                            new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date()) + ".json");
            openPicker(intent, EXPORT);
        });
        importButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
            openPicker(intent, IMPORT);
        });
    }

    private void openPicker(Intent intent, int request) {
        try { startActivityForResult(intent, request); }
        catch (android.content.ActivityNotFoundException e) { status.setText("没有可用的系统文件选择器，请启用系统“文件”应用。"); }
    }

    @Override protected void onActivityResult(int request, int result, @Nullable Intent intent) {
        super.onActivityResult(request, result, intent);
        if (result != RESULT_OK || intent == null || intent.getData() == null || busy) return;
        Uri uri = intent.getData();
        if (request == EXPORT) exportTo(uri);
        else if (request == IMPORT) inspect(uri);
    }

    private void exportTo(Uri uri) {
        setBusy(true, "正在生成备份，请稍候…");
        worker.execute(() -> {
            try {
                byte[] bytes = LedgerBackupStore.exportLedger(getApplicationContext());
                LedgerBackup backup = LedgerBackup.decode(bytes);
                try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
                    if (output == null) throw new java.io.IOException("无法打开所选文件");
                    output.write(bytes);
                    output.flush();
                }
                getPreferences(MODE_PRIVATE).edit().putString("last_export", backup.exportedAt).commit();
                finishWork("备份导出成功：" + backup.data.accounts.size() + " 笔账单、" +
                        backup.data.categories.size() + " 个分类及搭子设置。\n请将文件另存到电脑、U盘或新手机，确认恢复成功前保留旧手机数据。");
            } catch (Exception e) {
                finishWork("导出失败：" + error(e) + "\n请重新导出，失败产生的文件不能用于恢复。");
            }
        });
    }

    private void inspect(Uri uri) {
        setBusy(true, "正在读取并校验备份…");
        worker.execute(() -> {
            try {
                byte[] bytes;
                try (InputStream input = getContentResolver().openInputStream(uri);
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    if (input == null) throw new java.io.IOException("无法读取所选文件");
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        if (output.size() + read > LedgerBackup.MAX_BYTES) throw new IllegalArgumentException("备份文件不能超过20MB");
                        output.write(buffer, 0, read);
                    }
                    bytes = output.toByteArray();
                }
                LedgerBackup backup = LedgerBackup.decode(bytes);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    setBusy(false, "备份校验通过，等待确认恢复。");
                    new AlertDialog.Builder(this).setTitle("恢复这份记账备份？")
                            .setMessage("备份时间：" + backup.exportedAt + "\n账单：" + backup.data.accounts.size() +
                                    " 笔\n分类：" + backup.data.categories.size() + " 个\n搭子：" + backup.data.buddyName +
                                    "\n\n仅允许恢复到没有账单的记账本，不会覆盖已有账单。此备份不包含心愿清单。")
                            .setNegativeButton("取消", (d, w) -> status.setText("已取消恢复，本地数据未改变。"))
                            .setPositiveButton("确认恢复", (d, w) -> restore(backup)).show();
                });
            } catch (Exception e) { finishWork("无法导入：" + error(e) + "\n本地账目未改变。"); }
        });
    }

    private void restore(LedgerBackup backup) {
        setBusy(true, "正在恢复，请勿关闭应用…");
        worker.execute(() -> {
            try {
                int count = LedgerBackupStore.restoreLedger(getApplicationContext(), backup);
                finishWork("恢复成功：" + count + " 笔账单。\n返回记账页即可查看；旧手机中的备份文件建议继续保留。");
            } catch (Exception e) { finishWork("恢复未完成：" + error(e)); }
        });
    }

    private static String error(Exception e) {
        return e instanceof IllegalArgumentException || e instanceof java.io.IOException ?
                (e.getMessage() == null ? "文件读写失败，请重试" : e.getMessage()) : "本地读写失败，请检查可用空间后重试";
    }

    private void finishWork(String text) {
        runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) setBusy(false, text); });
    }

    private void setBusy(boolean value, String text) {
        busy = value;
        exportButton.setEnabled(!value);
        importButton.setEnabled(!value);
        status.setText(text);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        if (busy) return;
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        worker.shutdown();
        super.onDestroy();
    }
}
