package com.ccg.futurerealization;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.recyclerview.widget.RecyclerView;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.utils.DefaultCategories;
import com.ccg.futurerealization.view.activity.MainActivity;
import com.ccg.futurerealization.view.activity.ReportActivity;
import com.ccg.futurerealization.view.activity.TransactionListActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DetailsUpgradeTest {
    private Instrumentation instrumentation;
    private Context context;
    private Activity activity;
    private final List<Activity> activities = new ArrayList<>();
    private static final String DB = "details_upgrade_test";
    @Before public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation(); context = instrumentation.getTargetContext();
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        database.addClassName(Account.class.getName()); database.addClassName(AccountCategory.class.getName()); database.addClassName(DoSth.class.getName());
        database.addClassName(com.ccg.futurerealization.bean.Note.class.getName());
        LitePal.use(database); LitePal.getDatabase();
    }
    @After public void tearDown() {
        instrumentation.runOnMainSync(() -> { for (int i = activities.size() - 1; i >= 0; i--) activities.get(i).finish(); });
        instrumentation.waitForIdleSync(); LitePal.deleteDatabase(DB); LitePal.useDefault();
    }
    private AccountCategory category(String name, long parent) {
        AccountCategory category = new AccountCategory(); category.setCategory(name); category.setType(1); category.setPid(parent); assertTrue(category.save()); return category;
    }
    private Account account(AccountCategory category, int cents, String time, String remark) {
        Account account = new Account(); account.setDate(new SimpleDateFormat("yyyy-MM", Locale.ROOT).format(new java.util.Date()) + "-05");
        account.setType(1); account.setAmount(cents); account.setAccountCategory(category); account.setTime(time); account.setRemark(remark); assertTrue(account.save()); return account;
    }
    private void start(Class<? extends Activity> type) {
        activity = instrumentation.startActivitySync(new Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); activities.add(activity);
    }
    @Test public void lunchAliasesMergeWithoutLosingAccountAmountsOrAssociations() throws Exception {
        AccountCategory food = category("食", 0), old = category("中餐", food.getId()), lunch = category("午餐", food.getId());
        account(old, 1250, "12:00:00", "旧中餐"); account(lunch, 2580, "12:30:00", "原午餐");
        context.getSharedPreferences("category_defaults", 0).edit().putInt("version", 2).commit();
        DefaultCategories.ensure(context);
        assertEquals(0, LitePal.where("category = ?", "中餐").count(AccountCategory.class));
        assertEquals(2, LitePal.count(Account.class));
        int total = 0;
        for (Account row : LitePal.findAll(Account.class, true)) { total += row.getAmount(); assertEquals(lunch.getId(), row.getAccountCategory().getId()); assertNotNull(row.getTime()); }
        assertEquals(3830, total);
    }
    @Test public void doubleTapCalendarDayCommitsWithoutPressingConfirm() throws Exception {
        start(MainActivity.class); waitUntil(() -> activity.findViewById(R.id.date_text) != null);
        String before = text(R.id.date_text);
        String selected = before.substring(0, 8) + (before.endsWith("05") ? "07" : "05");
        instrumentation.runOnMainSync(() -> activity.findViewById(R.id.date_text).performClick());
        waitUntil(() -> day(selected) != null);
        assertTrue(day(selected).performAction(AccessibilityNodeInfo.ACTION_CLICK));
        assertEquals(before, text(R.id.date_text)); // Single tap is only a pending selection.
        Thread.sleep(350);
        AccessibilityNodeInfo node = day(selected);
        assertNotNull(node);
        assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK));
        assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK));
        waitUntil(() -> selected.equals(text(R.id.date_text)));
        assertNull(day(selected));
    }
    private AccessibilityNodeInfo day(String date) {
        AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
        if (root != null) for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(date)) if (date.contentEquals(node.getContentDescription())) return node;
        return null;
    }
    @Test public void reportDrillsIntoBreakfastShowingRecordedAndImportedTimesWithExactAmounts() throws Exception {
        AccountCategory food = category("食", 0), breakfast = category("早餐", food.getId()), lunch = category("午餐", food.getId());
        account(breakfast, 1250, "07:42:30", "早餐一");
        account(breakfast, 600, null, "[XLS导入] 时间：08:12:00；账户：现金；币种：人民币");
        account(lunch, 2500, "12:20:00", "午餐");
        start(ReportActivity.class); waitUntil(() -> "¥43.50".equals(text(R.id.report_expense)));
        clickPart("食"); waitUntil(() -> text(R.id.report_path).startsWith("食"));
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(TransactionListActivity.class.getName(), null, false);
        clickPart("早餐");
        Activity records = monitor.waitForActivityWithTimeout(5000); instrumentation.removeMonitor(monitor);
        assertNotNull(records); activity = records; activities.add(records);
        waitUntil(() -> text(R.id.transaction_summary).contains("共 2 笔") && text(R.id.transaction_summary).contains("18.50"));
        instrumentation.runOnMainSync(() -> {
            RecyclerView list = activity.findViewById(R.id.transaction_list);
            assertEquals(2, list.getAdapter().getItemCount());
            assertTrue(((TextView) list.getChildAt(0).findViewById(R.id.transaction_time)).getText().toString().contains("08:12:00"));
            assertTrue(((TextView) list.getChildAt(0).findViewById(R.id.transaction_amount)).getText().toString().contains("6.00"));
            assertTrue(((TextView) list.getChildAt(1).findViewById(R.id.transaction_time)).getText().toString().contains("07:42:30"));
        });
    }
    private void clickPart(String name) {
        instrumentation.runOnMainSync(() -> {
            ViewGroup legend = activity.findViewById(R.id.report_legend);
            for (int i = 0; i < legend.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) legend.getChildAt(i);
                for (int j = 0; j < row.getChildCount(); j++) {
                    View child = row.getChildAt(j);
                    if (child instanceof TextView && name.contentEquals(((TextView) child).getText())) { row.performClick(); return; }
                }
            }
            fail("Missing report category " + name);
        });
    }
    private String text(int id) {
        String[] result = {""}; instrumentation.runOnMainSync(() -> { TextView view = activity.findViewById(id); if (view != null) result[0] = view.getText().toString(); }); return result[0];
    }
    private void waitUntil(Callable<Boolean> condition) throws Exception {
        long until = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < until) { if (condition.call()) return; Thread.sleep(80); }
        fail("Timed out waiting for details UI");
    }
}
