package com.ccg.futurerealization;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.ccg.futurerealization.backup.EmailBackup;
import com.ccg.futurerealization.backup.LedgerBackup;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.view.activity.BackupActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

@RunWith(AndroidJUnit4.class)
public class EmailBackupTest {
    private Context context;
    private Instrumentation instrumentation;
    private Activity activity;
    private static final String DB = "email_backup_test";
    @Before public void setUp() throws Exception {
        instrumentation = InstrumentationRegistry.getInstrumentation(); context = instrumentation.getTargetContext();
        toggleMailbox(false);
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        database.addClassName(Account.class.getName()); database.addClassName(AccountCategory.class.getName()); database.addClassName(DoSth.class.getName());
        database.addClassName(com.ccg.futurerealization.bean.Note.class.getName());
        LitePal.use(database); LitePal.getDatabase();
        AccountCategory root = new AccountCategory(); root.setCategory("食"); root.setPid(0L); root.setType(1); assertTrue(root.save());
        AccountCategory leaf = new AccountCategory(); leaf.setCategory("早餐"); leaf.setPid(root.getId()); leaf.setType(1); assertTrue(leaf.save());
        Account account = new Account(); account.setDate("2026-11-17"); account.setTime("07:42:30"); account.setAmount(1250); account.setType(1); account.setRemark("测试早餐"); account.setAccountCategory(leaf); assertTrue(account.save());
        DoSth goal = new DoSth(); goal.setFuture_content("测试目标"); goal.setState(true); goal.setType(1); assertTrue(goal.save());
    }
    @After public void tearDown() throws Exception {
        if (activity != null) { instrumentation.runOnMainSync(() -> activity.finish()); instrumentation.waitForIdleSync(); }
        toggleMailbox(false);
        LitePal.deleteDatabase(DB); LitePal.useDefault();
    }
    @Test public void attachmentContainsAllDataAndIntentGrantsOnlyReadAccess() throws Exception {
        EmailBackup.Prepared backup = EmailBackup.prepare(context);
        assertEquals("content", backup.uri.getScheme());
        assertEquals("application/json", context.getContentResolver().getType(backup.uri));
        byte[] bytes;
        try (InputStream input = context.getContentResolver().openInputStream(backup.uri); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int read; while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read); bytes = output.toByteArray();
        }
        LedgerBackup decoded = LedgerBackup.decode(bytes);
        assertEquals(1, decoded.data.accounts.size()); assertEquals(1, decoded.data.goals.size());
        assertEquals("07:42:30", decoded.data.accounts.get(0).time); assertTrue(decoded.data.goals.get(0).completed);
        Intent draft = EmailBackup.compose(context, backup, "example.mail");
        assertEquals(backup.uri, draft.getParcelableExtra(Intent.EXTRA_STREAM));
        assertEquals(backup.uri, draft.getClipData().getItemAt(0).getUri());
        assertEquals("example.mail", draft.getPackage());
        assertTrue((draft.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0);
        assertEquals(0, draft.getFlags() & Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        assertNull(draft.getStringArrayExtra(Intent.EXTRA_EMAIL));
        try { FileProvider.getUriForFile(context, context.getPackageName() + ".backupfiles", context.getDatabasePath("db_sth.db")); fail("Database must not be shareable"); }
        catch (IllegalArgumentException expected) { }
    }
    @Test public void emailButtonOpensDraftAndAnotherAppCanReadAttachmentWithoutSending() throws Exception {
        toggleMailbox(true);
        java.util.List<EmailBackup.Target> targets = EmailBackup.targets(context);
        EmailBackup.Target fake = null;
        for (EmailBackup.Target target : targets) if (target.packageName.equals(instrumentation.getContext().getPackageName())) fake = target;
        assertNotNull("Test mailbox must be visible", fake);
        final String label = fake.label;
        activity = instrumentation.startActivitySync(new Intent(context, BackupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.runOnMainSync(() -> activity.findViewById(R.id.email_backup).performClick());
        if (targets.size() > 1) waitUntil(() -> {
            AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
            if (root != null) for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(label)) {
                if (label.contentEquals(node.getText()) && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true;
            }
            return false;
        });
        waitUntil(() -> {
            AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
            return root != null && !root.findAccessibilityNodeInfosByText("附件读取成功").isEmpty();
        });
        AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
        assertFalse(root.findAccessibilityNodeInfosByText("账单数：1").isEmpty());
        assertFalse(root.findAccessibilityNodeInfosByText("目标数：1").isEmpty());
        assertFalse(root.findAccessibilityNodeInfosByText("仅只读授权：true").isEmpty());
        instrumentation.getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
        waitUntil(() -> status().contains("是否发送成功"));
        assertEquals(1, LitePal.count(Account.class)); assertEquals(1, LitePal.count(DoSth.class));
    }
    @Test public void missingMailboxShowsAnActionableMessage() throws Exception {
        assumeTrue(EmailBackup.targets(context).isEmpty());
        activity = instrumentation.startActivitySync(new Intent(context, BackupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        instrumentation.runOnMainSync(() -> activity.findViewById(R.id.email_backup).performClick());
        assertTrue(status().contains("未检测到"));
        assertEquals(1, LitePal.count(Account.class));
    }
    private void toggleMailbox(boolean enabled) throws Exception {
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        int[] result = {0};
        Intent control = new Intent().setComponent(new android.content.ComponentName(instrumentation.getContext().getPackageName(),
                "com.ccg.futurerealization.test.FakeEmailControlReceiver")).putExtra("enabled", enabled)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendOrderedBroadcast(control, null, new android.content.BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) { result[0] = getResultCode(); done.countDown(); }
        }, null, 0, null, null);
        assertTrue("Mailbox lifecycle control timed out", done.await(5, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(Activity.RESULT_OK, result[0]);
    }
    private String status() {
        String[] result = {""}; instrumentation.runOnMainSync(() -> result[0] = ((TextView) activity.findViewById(R.id.backup_status)).getText().toString()); return result[0];
    }
    private void waitUntil(Callable<Boolean> condition) throws Exception {
        long until = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < until) { if (condition.call()) return; Thread.sleep(80); }
        fail("Timed out waiting for email draft");
    }
}
