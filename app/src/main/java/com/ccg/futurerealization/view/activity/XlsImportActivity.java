package com.ccg.futurerealization.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.importer.XlsLedgerReader;
import com.ccg.futurerealization.importer.XlsLedgerStore;
import com.ccg.futurerealization.utils.BookkeepingInput;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** System document selection, local preview, then explicit append-only import. */
public class XlsImportActivity extends AppCompatActivity {
    private static final int PICK_XLS = 201;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private Button choose, confirm;
    private TextView status;
    private boolean busy;
    private XlsLedgerReader.Batch pending;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_xls_import);
        setTitle("导入 XLS 账单");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        choose = findViewById(R.id.choose_xls);
        confirm = findViewById(R.id.confirm_xls_import);
        status = findViewById(R.id.xls_import_status);
        choose.setOnClickListener(v -> {
            try { startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), PICK_XLS); }
            catch (android.content.ActivityNotFoundException e) { status.setText("请先启用系统“文件”应用，再选择账单文件。"); }
        });
        confirm.setOnClickListener(v -> {
            if (busy || pending == null) return;
            new AlertDialog.Builder(this).setTitle("确认追加导入？")
                    .setMessage("将追加账目，并自动补齐缺少的分类。已有账目和目标会保留，重复导入的完整记录会跳过。")
                    .setNegativeButton("取消", null).setPositiveButton("开始导入", (d, w) -> importPending()).show();
        });
    }

    @Override protected void onActivityResult(int request, int result, @Nullable Intent intent) {
        super.onActivityResult(request, result, intent);
        if (request != PICK_XLS || result != RESULT_OK || intent == null || intent.getData() == null || busy) return;
        inspect(intent.getData());
    }

    private void inspect(Uri uri) {
        pending = null;
        confirm.setVisibility(View.GONE);
        setBusy(true, "正在读取 XLS 并核对账目、分类…");
        worker.execute(() -> {
            try {
                byte[] bytes;
                try (InputStream input = getContentResolver().openInputStream(uri); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    if (input == null) throw new java.io.IOException("无法读取所选文件");
                    byte[] buffer = new byte[8192]; int read;
                    while ((read = input.read(buffer)) != -1) {
                        if (output.size() + read > XlsLedgerReader.MAX_BYTES) throw new IllegalArgumentException("文件不能超过20MB");
                        output.write(buffer, 0, read);
                    }
                    bytes = output.toByteArray();
                }
                XlsLedgerReader.Batch batch = XlsLedgerReader.read(bytes);
                XlsLedgerStore.Plan plan = XlsLedgerStore.preview(batch);
                String preview = preview(batch, plan);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    pending = batch;
                    setBusy(false, preview);
                    confirm.setVisibility(View.VISIBLE);
                    confirm.setEnabled(plan.toImport > 0);
                    confirm.setText(plan.toImport > 0 ? "确认导入 " + plan.toImport + " 笔账目" : "没有需要新增的账目");
                });
            } catch (Exception e) { finishWork("读取失败：" + error(e) + "\n本地数据未改变。", false); }
        });
    }

    private static String preview(XlsLedgerReader.Batch batch, XlsLedgerStore.Plan plan) {
        StringBuilder text = new StringBuilder("文件核对结果\n\n期间：").append(batch.firstDate).append(" ～ ").append(batch.lastDate)
                .append("\n账目：").append(batch.entries.size()).append(" 笔（收入 ").append(batch.incomeCount).append(" / 支出 ").append(batch.expenseCount).append("）")
                .append("\n文件收入：¥").append(BookkeepingInput.format(batch.incomeCents)).append("\n文件支出：¥").append(BookkeepingInput.format(batch.expenseCents))
                .append("\n\n本次新增：").append(plan.toImport).append(" 笔\n跳过已有：").append(plan.skipped).append(" 笔")
                .append("\n新增收入：¥").append(BookkeepingInput.format(plan.incomeCents)).append("\n新增支出：¥").append(BookkeepingInput.format(plan.expenseCents))
                .append("\n新增分组：").append(plan.newRoots).append(" 个\n新增分类：").append(plan.newCategories).append(" 个");
        if (batch.transfers > 0) text.append("\n转账记录：").append(batch.transfers).append(" 笔（不计入收支，不导入）");
        if (!plan.categoryNames.isEmpty()) {
            text.append("\n\n将自动添加的分类：");
            for (int i = 0; i < Math.min(plan.categoryNames.size(), 40); i++) text.append("\n").append(plan.categoryNames.get(i));
            if (plan.categoryNames.size() > 40) text.append("\n…其余 ").append(plan.categoryNames.size() - 40).append(" 个也会自动添加");
        }
        text.append("\n\n请核对后再确认。原备注、记账时间和账户信息会保存到每笔账目的备注中。");
        return text.toString();
    }

    private void importPending() {
        XlsLedgerReader.Batch batch = pending;
        if (batch == null || busy) return;
        setBusy(true, "正在导入 " + batch.entries.size() + " 笔账目，请勿关闭应用…");
        worker.execute(() -> {
            try {
                XlsLedgerStore.Result result = XlsLedgerStore.importBatch(batch);
                finishWork("导入完成\n\n新增账目：" + result.imported + " 笔\n跳过已有：" + result.skipped +
                        " 笔\n新增分组：" + result.rootsAdded + " 个\n新增分类：" + result.categoriesAdded +
                        " 个\n\n新增收入：¥" + BookkeepingInput.format(result.incomeCents) + "\n新增支出：¥" + BookkeepingInput.format(result.expenseCents) +
                        "\n\n可返回记账页或报表查看历史数据。", true);
            } catch (Exception e) { finishWork("导入未完成：" + error(e) + "\n本次新增的账目和分类已回滚，可以重试。", false); }
        });
    }
    private static String error(Exception e) {
        if (e instanceof IllegalArgumentException) return e.getMessage() == null ? "文件格式不受支持" : e.getMessage();
        if (e instanceof java.io.IOException) return "文件读取失败，请确认文件可用，且为原始 .xls 导出文件";
        return "无法处理此文件，请确认格式或检查手机可用空间";
    }
    private void finishWork(String message, boolean success) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            setBusy(false, message);
            if (success) { pending = null; confirm.setVisibility(View.GONE); }
        });
    }
    private void setBusy(boolean value, String message) { busy = value; choose.setEnabled(!value); confirm.setEnabled(!value); status.setText(message); }
    @Override public void onBackPressed() { if (!busy) super.onBackPressed(); }
    @Override public boolean onOptionsItemSelected(MenuItem item) { if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; } return super.onOptionsItemSelected(item); }
    @Override protected void onDestroy() { worker.shutdown(); super.onDestroy(); }
}
