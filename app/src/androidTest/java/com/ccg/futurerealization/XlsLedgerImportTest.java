package com.ccg.futurerealization;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.ccg.futurerealization.backup.LedgerBackup;
import com.ccg.futurerealization.backup.LedgerBackupStore;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.importer.XlsLedgerReader;
import com.ccg.futurerealization.importer.XlsLedgerStore;
import com.ccg.futurerealization.utils.DefaultCategories;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import java.io.ByteArrayOutputStream;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class XlsLedgerImportTest {
    private Context context;
    private android.app.Activity activity;
    private static final String DB = "xls_import_test";
    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        database.addClassName(Account.class.getName()); database.addClassName(AccountCategory.class.getName()); database.addClassName(DoSth.class.getName());
        LitePal.use(database); LitePal.getDatabase();
    }
    @After public void tearDown() {
        if (activity != null) {
            android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
            instrumentation.runOnMainSync(() -> activity.finish()); instrumentation.waitForIdleSync();
        }
        LitePal.deleteDatabase(DB); LitePal.useDefault();
    }
    private XlsLedgerReader.Batch batch(int... amounts) throws Exception { return XlsLedgerReader.read(xls(amounts)); }
    private byte[] xls(int... amounts) throws Exception {
        String[] headers = {"记账日期", "记账时间", "记账类型", "使用账户", "账户类型", "一级分类", "二级分类", "记账金额", "记账货币", "备注"};
        try (HSSFWorkbook workbook = new HSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("支出记录"); Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            for (int i = 0; i < amounts.length; i++) {
                Row row = sheet.createRow(i + 1);
                String[] values = {"2026-11-17", "12:30:00", "支出", "现金", "现金账户", "餐饮测试", "'咖啡测试", "", "人民币", "测试备注"};
                for (int c = 0; c < values.length; c++) row.createCell(c).setCellValue(values[c]);
                row.getCell(7).setCellValue(amounts[i] / 100d);
            }
            workbook.write(output); return output.toByteArray();
        }
    }
    @Test public void previewIsReadOnlyAndRepeatedImportsAreIdempotentWhileKeepingSourceMultiplicity() throws Exception {
        XlsLedgerReader.Batch first = batch(1250, 1250);
        XlsLedgerStore.Plan preview = XlsLedgerStore.preview(first);
        assertEquals(2, preview.toImport); assertEquals(1, preview.newRoots); assertEquals(1, preview.newCategories);
        assertEquals(0, LitePal.count(AccountCategory.class)); assertEquals(0, LitePal.count(Account.class));
        XlsLedgerStore.Result result = XlsLedgerStore.importBatch(first);
        assertEquals(2, result.imported); assertEquals(2, LitePal.count(Account.class));
        assertEquals(0, XlsLedgerStore.importBatch(first).imported);
        XlsLedgerReader.Batch overlapping = batch(1250, 1250, 1250, 500);
        assertEquals(2, XlsLedgerStore.preview(overlapping).toImport);
        assertEquals(2, XlsLedgerStore.importBatch(overlapping).imported);
        assertEquals(4, LitePal.count(Account.class));
        List<Account> accounts = LitePal.findAll(Account.class, true);
        assertEquals("咖啡测试", accounts.get(0).getAccountCategory().getCategory());
        assertTrue(accounts.get(0).getRemark().contains("时间：12:30:00"));
        LedgerBackup backup = LedgerBackup.decode(LedgerBackupStore.exportLedger(context));
        assertEquals(4, backup.data.accounts.size());
    }
    @Test public void failedWriteRollsBackNewCategoriesAndAccountsWhilePreservingGoals() throws Exception {
        DoSth goal = new DoSth(); goal.setFuture_content("保留目标"); goal.setType(1); goal.setState(true); assertTrue(goal.save());
        LitePal.getDatabase().execSQL("CREATE TRIGGER reject_second_xls BEFORE INSERT ON account WHEN NEW.amount = 1251 BEGIN SELECT RAISE(ABORT, 'test import failure'); END");
        try { XlsLedgerStore.importBatch(batch(1250, 1251)); fail("must roll back"); } catch (RuntimeException expected) { }
        assertEquals(0, LitePal.count(Account.class)); assertEquals(0, LitePal.count(AccountCategory.class));
        assertEquals(1, LitePal.count(DoSth.class)); assertTrue(LitePal.findAll(DoSth.class).get(0).getState());
    }
    @Test public void selectedFilePreviewsWithoutWritingThenImportsAfterConfirmation() throws Exception {
        android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        java.io.File file = new java.io.File(context.getCacheDir(), "xls-ui-test.xls");
        try (java.io.FileOutputStream output = new java.io.FileOutputStream(file)) { output.write(xls(1250)); }
        activity = instrumentation.startActivitySync(new android.content.Intent(context,
                com.ccg.futurerealization.view.activity.XlsImportActivity.class)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
        java.lang.reflect.Method result = com.ccg.futurerealization.view.activity.XlsImportActivity.class
                .getDeclaredMethod("onActivityResult", int.class, int.class, android.content.Intent.class);
        result.setAccessible(true);
        instrumentation.runOnMainSync(() -> {
            try { result.invoke(activity, 201, android.app.Activity.RESULT_OK, new android.content.Intent().setData(android.net.Uri.fromFile(file))); }
            catch (Exception e) { throw new AssertionError(e); }
        });
        waitUntil(() -> statusText().contains("本次新增：1 笔"));
        assertEquals(0, LitePal.count(Account.class));
        instrumentation.runOnMainSync(() -> activity.findViewById(R.id.confirm_xls_import).performClick());
        waitUntil(() -> {
            android.view.accessibility.AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
            if (root == null) return false;
            for (android.view.accessibility.AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText("开始导入")) {
                if ("开始导入".contentEquals(node.getText()) && node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) return true;
            }
            return false;
        });
        waitUntil(() -> statusText().contains("导入完成"));
        assertEquals(1, LitePal.count(Account.class));
        assertEquals("咖啡测试", LitePal.findAll(Account.class, true).get(0).getAccountCategory().getCategory());
    }
    private String statusText() {
        String[] text = {""};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> text[0] = ((android.widget.TextView) activity.findViewById(R.id.xls_import_status)).getText().toString());
        return text[0];
    }
    private void waitUntil(java.util.concurrent.Callable<Boolean> condition) throws Exception {
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) { if (condition.call()) return; Thread.sleep(80); }
        fail("Timed out waiting for XLS import UI");
    }

    @Test public void upgradeAddsExportCategoriesWithoutChangingExistingLinksOrDuplicatingDefaults() throws Exception {
        context.getSharedPreferences("category_defaults", 0).edit().clear().commit();
        AccountCategory root = new AccountCategory(); root.setCategory("食"); root.setPid(0L); root.setType(1); assertTrue(root.save());
        AccountCategory custom = new AccountCategory(); custom.setCategory("自定义早餐"); custom.setPid(root.getId()); custom.setType(1); assertTrue(custom.save());
        DefaultCategories.ensure(context);
        int count = LitePal.count(AccountCategory.class);
        assertNotNull(LitePal.where("category = ?", "医药").findFirst(AccountCategory.class));
        assertNotNull(LitePal.where("category = ?", "午餐").findFirst(AccountCategory.class));
        assertNotNull(LitePal.where("category = ?", "AItoken").findFirst(AccountCategory.class));
        assertEquals(root.getId(), LitePal.find(AccountCategory.class, custom.getId()).getPid());
        DefaultCategories.ensure(context); assertEquals(count, LitePal.count(AccountCategory.class));
    }
}
