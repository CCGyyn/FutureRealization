package com.ccg.futurerealization;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ccg.futurerealization.backup.LedgerBackup;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.view.activity.BackupActivity;
import com.ccg.futurerealization.view.activity.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/** Exercises the real UI callbacks against an isolated database. */
@RunWith(AndroidJUnit4.class)
public class BookkeepingFlowTest {
    private Instrumentation instrumentation;
    private Context context;
    private Activity activity;
    private String name;
    private int style;
    private static final String DB = "ledger_ui_test";

    @Before public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
        name = context.getSharedPreferences("bookkeeping_buddy", 0).getString("name", "小伴");
        style = context.getSharedPreferences("bookkeeping_buddy", 0).getInt("style", 0);
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        database.addClassName(Account.class.getName());
        database.addClassName(AccountCategory.class.getName());
        database.addClassName(DoSth.class.getName());
        database.addClassName(com.ccg.futurerealization.bean.Note.class.getName());
        LitePal.use(database);
        LitePal.getDatabase();
    }

    @After public void tearDown() {
        if (activity != null) instrumentation.runOnMainSync(() -> activity.finish());
        instrumentation.waitForIdleSync();
        LitePal.deleteDatabase(DB);
        LitePal.useDefault();
        context.getSharedPreferences("bookkeeping_buddy", 0).edit().putString("name", name).putInt("style", style).commit();
    }

    @Test public void numericKeyboardEntrySavesSelectedCategoryAndCents() throws Exception {
        // Existing legacy startup permission prompt is unrelated to the offline ledger.
        instrumentation.getUiAutomation().grantRuntimePermission(context.getPackageName(), "android.permission.READ_EXTERNAL_STORAGE");
        instrumentation.getUiAutomation().grantRuntimePermission(context.getPackageName(), "android.permission.WRITE_EXTERNAL_STORAGE");
        activity = instrumentation.startActivitySync(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        waitUntil(() -> LitePal.count(AccountCategory.class) > 0 && uiText(R.id.book_keep_type).contains("早餐"));
        instrumentation.runOnMainSync(() -> {
            EditText amount = activity.findViewById(R.id.send_msg);
            assertEquals(android.text.InputType.TYPE_CLASS_NUMBER,
                    amount.getInputType() & android.text.InputType.TYPE_MASK_CLASS);
            assertTrue((amount.getInputType() & android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL) != 0);
            amount.setText("25.50");
            activity.findViewById(R.id.send_btn).performClick();
        });
        waitUntil(() -> LitePal.count(Account.class) == 1 && uiText(R.id.month_expense).equals("¥25.50"));
        Account account = LitePal.findAll(Account.class, true).get(0);
        assertEquals(Integer.valueOf(2550), account.getAmount());
        assertEquals("早餐", account.getAccountCategory().getCategory());
        assertEquals("", account.getRemark());
    }

    @Test public void selectedBackupFileIsValidatedConfirmedAndRestored() throws Exception {
        LedgerBackup.Data data = new LedgerBackup.Data();
        LedgerBackup.Category parent = new LedgerBackup.Category();
        parent.id = 100; parent.name = "食"; parent.type = 1;
        LedgerBackup.Category child = new LedgerBackup.Category();
        child.id = 300; child.parentId = 100; child.name = "中餐"; child.type = 1;
        data.categories.add(parent); data.categories.add(child);
        LedgerBackup.Entry entry = new LedgerBackup.Entry();
        entry.date = "2026-11-17"; entry.type = 1; entry.amountCents = 2550; entry.categoryId = 300;
        data.accounts.add(entry);
        File file = new File(context.getCacheDir(), "ledger-flow-test.json");
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(LedgerBackup.encode(data, "2026-11-17 12:00:00"));
        }
        activity = instrumentation.startActivitySync(new Intent(context, BackupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        // Deliver a selected local document to the same callback used by the system picker.
        Method result = BackupActivity.class.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
        result.setAccessible(true);
        instrumentation.runOnMainSync(() -> {
            try { result.invoke(activity, 102, Activity.RESULT_OK, new Intent().setData(Uri.fromFile(file))); }
            catch (Exception e) { throw new AssertionError(e); }
        });
        clickDialog("确认恢复");
        waitUntil(() -> uiText(R.id.backup_status).contains("恢复成功"));
        assertEquals(1, LitePal.count(Account.class));
        assertEquals("午餐", LitePal.findAll(Account.class, true).get(0).getAccountCategory().getCategory());
    }

    private String uiText(int id) {
        String[] result = {""};
        instrumentation.runOnMainSync(() -> {
            TextView view = activity.findViewById(id);
            if (view != null) result[0] = view.getText().toString();
        });
        return result[0];
    }

    private void clickDialog(String label) throws Exception {
        waitUntil(() -> {
            AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
            if (root == null) return false;
            for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(label)) {
                if (label.contentEquals(node.getText()) && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
            }
            return false;
        });
    }

    private void waitUntil(Callable<Boolean> condition) throws Exception {
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) {
            if (condition.call()) return;
            Thread.sleep(80);
        }
        fail("Timed out waiting for ledger UI");
    }
}
