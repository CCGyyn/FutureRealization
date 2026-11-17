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
        database.addClassName(com.ccg.futurerealization.bean.Note.class.getName());
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

    @Test public void goalCanBeAbandonedFilteredAndRestored() throws Exception {
        DoSth goal = new DoSth(); goal.setFuture_content("旧计划"); goal.setType(1); goal.setState(false); assertTrue(goal.save());
        start(MainActivity.class);
        waitUntil(() -> activity.findViewById(R.id.add_goal) != null);
        instrumentation.runOnMainSync(() -> ((ViewPager) activity.findViewById(R.id.view_list)).setCurrentItem(0, false));
        waitUntil(() -> text(R.id.goals_pending).equals("进行中 1"));
        instrumentation.runOnMainSync(() -> ((RecyclerView) activity.findViewById(R.id.msg_list)).getChildAt(0).performLongClick());
        clickLabel("标记放弃");
        waitUntil(() -> LitePal.find(DoSth.class, goal.getId()).isAbandoned() && text(R.id.goals_abandoned).equals("已放弃 1"));
        assertEquals("进行中 0", text(R.id.goals_pending));
        assertTrue(text(R.id.goal_progress_text).contains("0 / 0"));
        click(R.id.goals_abandoned);
        instrumentation.runOnMainSync(() -> ((RecyclerView) activity.findViewById(R.id.msg_list)).getChildAt(0).performLongClick());
        clickLabel("恢复进行中");
        waitUntil(() -> !LitePal.find(DoSth.class, goal.getId()).isAbandoned() && text(R.id.goals_pending).equals("进行中 1"));
        assertFalse(LitePal.find(DoSth.class, goal.getId()).getState());
        assertEquals("暂无已放弃的目标", text(R.id.goal_empty));
    }

    @Test public void engelCoefficientUsesFoodExpensesAndFollowsSelectedMonth() throws Exception {
        Calendar current = Calendar.getInstance(), previous = (Calendar) current.clone(); previous.add(Calendar.MONTH, -1);
        AccountCategory food = category("食", "早餐", 1), travel = category("行", "公交", 1), income = category("收", "工资", 0);
        account(food, date(current), 5000); account(travel, date(current), 20000); account(income, date(current), 100000);
        account(food, date(previous), 700); account(travel, date(previous), 700);
        start(ReportActivity.class);
        waitUntil(() -> text(R.id.report_engel).equals("最富裕"));
        click(R.id.report_income_tab); assertEquals("最富裕", text(R.id.report_engel));
        click(R.id.report_previous); waitUntil(() -> text(R.id.report_engel).equals("温饱"));
        click(R.id.report_previous); waitUntil(() -> text(R.id.report_engel).equals("暂无支出"));
    }

    @Test public void notesTabCanCreateSearchEditWithBackSaveAndDelete() throws Exception {
        start(MainActivity.class);
        waitUntil(() -> activity.findViewById(R.id.notes_list) != null);
        instrumentation.runOnMainSync(() -> {
            com.google.android.material.tabs.TabLayout tabs = activity.findViewById(R.id.tab_layout);
            assertEquals(4, tabs.getTabCount()); assertEquals("笔记", tabs.getTabAt(2).getText()); assertEquals("备份", tabs.getTabAt(3).getText());
            ((ViewPager) activity.findViewById(R.id.view_list)).setCurrentItem(2, false);
        });
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(com.ccg.futurerealization.view.activity.NoteEditorActivity.class.getName(), null, false);
        click(R.id.add_note);
        Activity editor = instrumentation.waitForMonitorWithTimeout(monitor, 5000); instrumentation.removeMonitor(monitor); assertNotNull(editor);
        setDialogText("note_title", "测量数据"); setDialogText("note_content", "体重 65.2\n步数 8000"); clickLabel("完成");
        waitUntil(() -> LitePal.count(com.ccg.futurerealization.bean.Note.class) == 1 && text(R.id.notes_status).equals("1 篇笔记"));
        setDialogText("notes_search", "不存在的内容"); waitUntil(() -> text(R.id.notes_status).equals("找到 0 篇笔记"));
        assertEquals("没有找到相关笔记", text(R.id.notes_empty));
        setDialogText("notes_search", "8000"); waitUntil(() -> text(R.id.notes_status).equals("找到 1 篇笔记"));
        setDialogText("notes_search", "");
        monitor = instrumentation.addMonitor(com.ccg.futurerealization.view.activity.NoteEditorActivity.class.getName(), null, false);
        instrumentation.runOnMainSync(() -> ((RecyclerView) activity.findViewById(R.id.notes_list)).getChildAt(0).performClick());
        Activity editing = instrumentation.waitForMonitorWithTimeout(monitor, 5000); instrumentation.removeMonitor(monitor); assertNotNull(editing);
        setDialogText("note_content", "体重 64.8"); instrumentation.runOnMainSync(() -> editing.onBackPressed());
        waitUntil(() -> editing.isFinishing() && LitePal.findAll(com.ccg.futurerealization.bean.Note.class).get(0).getContent().equals("体重 64.8"));
        instrumentation.runOnMainSync(() -> activity.finish());
        start(MainActivity.class);
        waitUntil(() -> text(R.id.notes_status).equals("1 篇笔记"));
        instrumentation.runOnMainSync(() -> ((ViewPager) activity.findViewById(R.id.view_list)).setCurrentItem(2, false));
        instrumentation.runOnMainSync(() -> ((RecyclerView) activity.findViewById(R.id.notes_list)).getChildAt(0).performLongClick());
        clickLabel("删除");
        waitUntil(() -> LitePal.count(com.ccg.futurerealization.bean.Note.class) == 0 && text(R.id.notes_status).equals("0 篇笔记"));
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
