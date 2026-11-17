package com.ccg.futurerealization;

import android.app.Activity;
import android.app.Instrumentation;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.utils.AccountCategoryStore;
import com.ccg.futurerealization.view.activity.MainActivity;
import com.ccg.futurerealization.view.activity.ReportActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Callable;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FeatureUpgradeTest {
    private Instrumentation instrumentation;
    private Context context;
    private Activity activity;
    private static final String DB = "feature_upgrade_test";
    @Before public void setUp() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
        AccessibilityServiceInfo service = instrumentation.getUiAutomation().getServiceInfo();
        service.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        instrumentation.getUiAutomation().setServiceInfo(service);
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        database.addClassName(Account.class.getName()); database.addClassName(AccountCategory.class.getName()); database.addClassName(DoSth.class.getName());
        LitePal.use(database); LitePal.getDatabase();
    }
    @After public void tearDown() {
        if (activity != null) instrumentation.runOnMainSync(() -> activity.finish());
        instrumentation.waitForIdleSync();
        LitePal.deleteDatabase(DB); LitePal.useDefault();
    }
    private void start(Class<? extends Activity> type) {
        activity = instrumentation.startActivitySync(new Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
    private AccountCategory category(String rootName, String name, int type) {
        AccountCategory root = new AccountCategory(); root.setCategory(rootName); root.setType(type); root.setPid(0L); assertTrue(root.save());
        return AccountCategoryStore.add(root.getId(), name);
    }
    private void account(AccountCategory category, String date, int cents) {
        Account row = new Account(); row.setAccountCategory(category); row.setType(category.getType()); row.setAmount(cents); row.setDate(date); row.setRemark(""); assertTrue(row.save());
    }
    private String date(Calendar month) { return new SimpleDateFormat("yyyy-MM", Locale.ROOT).format(month.getTime()) + "-01"; }

    @Test public void historicalMonthsIncomeAndEmptyChartsRefreshWithoutLosingCents() throws Exception {
        Calendar current = Calendar.getInstance();
        Calendar previous = (Calendar) current.clone(); previous.add(Calendar.MONTH, -1);
        AccountCategory expense = category("食", "早餐", 1), income = category("收", "工资", 0);
        account(expense, date(current), 100);
        account(expense, date(previous), 50);
        account(income, date(previous), 345);
        start(ReportActivity.class);
        waitUntil(() -> text(R.id.report_expense).equals("¥1.00"));
        click(R.id.report_previous);
        waitUntil(() -> text(R.id.report_expense).equals("¥0.50") && text(R.id.report_income).equals("¥3.45"));
        click(R.id.report_income_tab);
        waitUntil(() -> chartDescription().contains("收入") && chartDescription().contains("1类"));
        click(R.id.report_previous);
        waitUntil(() -> text(R.id.report_status).equals("这个月暂无账单"));
        assertEquals("¥0.00", text(R.id.report_expense));
        assertTrue(chartDescription().contains("0类"));
        click(R.id.report_this_month);
        waitUntil(() -> text(R.id.report_expense).equals("¥1.00"));
        instrumentation.runOnMainSync(() -> { activity.findViewById(R.id.report_previous).performClick(); activity.findViewById(R.id.report_next).performClick(); });
        waitUntil(() -> text(R.id.report_expense).equals("¥1.00"));
    }

    @Test public void customCategoryCanBeAddedFromLedgerAndDuplicatesAreRejected() throws Exception {
        start(MainActivity.class);
        waitUntil(() -> text(R.id.book_keep_type).contains("早餐"));
        click(R.id.add_category_btn);
        setDialogText("category_editor_text", "下午茶");
        clickLabel("添加");
        waitUntil(() -> text(R.id.book_keep_type).contains("下午茶"));
        AccountCategory saved = LitePal.where("category = ?", "下午茶").findFirst(AccountCategory.class);
        assertNotNull(saved); assertEquals(Integer.valueOf(1), saved.getType());
        int count = LitePal.count(AccountCategory.class);
        try { AccountCategoryStore.add(saved.getPid(), " 下午茶 "); fail("must reject duplicate"); }
        catch (IllegalArgumentException expected) { }
        assertEquals(count, LitePal.count(AccountCategory.class));
        for (AccountCategory root : LitePal.where("pid = ?", "0").find(AccountCategory.class)) {
            if (root.getType() == 0) assertEquals(Integer.valueOf(0), AccountCategoryStore.add(root.getId(), "项目奖金").getType());
        }
        assertNull(activity.findViewById(context.getResources().getIdentifier("buddy_title", "id", context.getPackageName())));
    }

    @Test public void goalsCanBeCreatedCompletedAndRestoredToPending() throws Exception {
        start(MainActivity.class);
        waitUntil(() -> activity.findViewById(R.id.add_goal) != null);
        instrumentation.runOnMainSync(() -> ((ViewPager) activity.findViewById(R.id.view_list)).setCurrentItem(0, false));
        click(R.id.add_goal);
        setDialogText("goal_editor_text", "读完一本书");
        clickLabel("保存");
        waitUntil(() -> LitePal.count(DoSth.class) == 1 && text(R.id.goal_progress_text).contains("0 / 1"));
        instrumentation.runOnMainSync(() -> ((RecyclerView) activity.findViewById(R.id.msg_list)).getChildAt(0).findViewById(R.id.goal_check).performClick());
        waitUntil(() -> Boolean.TRUE.equals(LitePal.findAll(DoSth.class).get(0).getState()) && text(R.id.goal_progress_text).contains("100%"));
        click(R.id.goals_done);
        instrumentation.runOnMainSync(() -> ((RecyclerView) activity.findViewById(R.id.msg_list)).getChildAt(0).findViewById(R.id.goal_check).performClick());
        waitUntil(() -> !Boolean.TRUE.equals(LitePal.findAll(DoSth.class).get(0).getState()) && text(R.id.goal_progress_text).contains("0 / 1"));
        assertEquals("还没有已完成的目标", text(R.id.goal_empty));
        click(R.id.goals_pending);
        assertEquals("进行中 1", text(R.id.goals_pending));
    }

    private void click(int id) { instrumentation.runOnMainSync(() -> activity.findViewById(id).performClick()); }
    private String text(int id) {
        String[] result = {""};
        instrumentation.runOnMainSync(() -> { TextView view = activity.findViewById(id); if (view != null) result[0] = view.getText().toString(); });
        return result[0];
    }
    private String chartDescription() {
        String[] result = {""};
        instrumentation.runOnMainSync(() -> result[0] = String.valueOf(activity.findViewById(R.id.account_pie_chart).getContentDescription()));
        return result[0];
    }
    private void setDialogText(String id, String value) throws Exception {
        waitUntil(() -> {
            AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
            if (root == null) return false;
            for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByViewId(context.getPackageName() + ":id/" + id)) {
                Bundle arguments = new Bundle(); arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
                if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) return true;
            }
            return false;
        });
    }
    private void clickLabel(String label) throws Exception {
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
        long until = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < until) { if (condition.call()) return; Thread.sleep(80); }
        fail("Timed out waiting for feature UI");
    }
}
