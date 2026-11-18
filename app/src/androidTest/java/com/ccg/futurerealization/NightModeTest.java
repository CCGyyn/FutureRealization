package com.ccg.futurerealization;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.ColorUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager.widget.ViewPager;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.bean.Note;
import com.ccg.futurerealization.event.BookKeepingEvent;
import com.ccg.futurerealization.view.activity.MainActivity;
import com.ccg.futurerealization.view.activity.NoteEditorActivity;
import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import java.util.concurrent.Callable;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NightModeTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private Activity activity;
    private int originalMode;
    private static final String DB = "night_mode_test";
    @Before public void setup() {
        originalMode = AppCompatDelegate.getDefaultNightMode();
        instrumentation.runOnMainSync(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        for (Class<?> type : new Class<?>[]{Account.class, AccountCategory.class, DoSth.class, Note.class}) database.addClassName(type.getName());
        LitePal.use(database); LitePal.getDatabase();
    }
    @After public void cleanup() {
        if (activity != null) instrumentation.runOnMainSync(() -> activity.finish());
        instrumentation.waitForIdleSync();
        instrumentation.runOnMainSync(() -> AppCompatDelegate.setDefaultNightMode(originalMode));
        LitePal.deleteDatabase(DB); LitePal.useDefault();
    }
    private void start(Class<? extends Activity> type) {
        activity = instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(), type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
    private void mode(int value) {
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(activity.getClass().getName(), null, false);
        instrumentation.runOnMainSync(() -> AppCompatDelegate.setDefaultNightMode(value));
        Activity replacement = instrumentation.waitForMonitorWithTimeout(monitor, 8000);
        instrumentation.removeMonitor(monitor); assertNotNull("Activity did not recreate", replacement);
        activity = replacement; instrumentation.waitForIdleSync();
    }
    @Test public void noteDraftSurvivesNightAndDaySwitchWithReadableColorsAndSystemBars() {
        start(NoteEditorActivity.class);
        instrumentation.runOnMainSync(() -> {
            ((EditText) activity.findViewById(R.id.note_title)).setText("未保存的笔记");
            ((EditText) activity.findViewById(R.id.note_content)).setText("测量数据 123.45\n第二行");
        });
        mode(AppCompatDelegate.MODE_NIGHT_YES);
        instrumentation.runOnMainSync(() -> {
            assertEquals(Configuration.UI_MODE_NIGHT_YES, activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
            assertEquals("未保存的笔记", ((EditText) activity.findViewById(R.id.note_title)).getText().toString());
            assertEquals("测量数据 123.45\n第二行", ((EditText) activity.findViewById(R.id.note_content)).getText().toString());
            View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            int background = ((ColorDrawable) root.getBackground()).getColor();
            assertTrue(ColorUtils.calculateLuminance(background) < 0.1);
            assertTrue(ColorUtils.calculateContrast(((TextView) activity.findViewById(R.id.note_content)).getCurrentTextColor(), background) >= 4.5);
            assertEquals(0, activity.getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            for (int[] pair : new int[][]{{R.color.ledger_text,R.color.ledger_surface}, {R.color.ledger_muted,R.color.ledger_surface}, {R.color.ledger_on_accent,R.color.ledger_accent}, {R.color.notes_body,R.color.ledger_surface}, {R.color.goal_long_text,R.color.goal_long_surface}, {R.color.goal_short_text,R.color.goal_short_surface}}) {
                assertTrue("Night color contrast below 4.5", ColorUtils.calculateContrast(activity.getResources().getColor(pair[0]), activity.getResources().getColor(pair[1])) >= 4.5);
            }
        });
        mode(AppCompatDelegate.MODE_NIGHT_NO);
        instrumentation.runOnMainSync(() -> {
            assertEquals("未保存的笔记", ((EditText) activity.findViewById(R.id.note_title)).getText().toString());
            assertTrue((activity.getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0);
        });
        assertEquals(0, LitePal.count(Note.class));
    }
    @Test public void switchingModeRetainsLedgerAmountCategoryAndSelectedBottomTab() throws Exception {
        start(MainActivity.class);
        waitUntil(() -> activity.findViewById(R.id.book_keep_type) != null && text(R.id.book_keep_type).contains("早餐"));
        AccountCategory lunch = LitePal.where("category = ?", "午餐").findFirst(AccountCategory.class); assertNotNull(lunch);
        instrumentation.runOnMainSync(() -> {
            EventBus.getDefault().post(BookKeepingEvent.getInstance(lunch));
            ((EditText) activity.findViewById(R.id.send_msg)).setText("12.50");
            ((ViewPager) activity.findViewById(R.id.view_list)).setCurrentItem(2, false);
        });
        mode(AppCompatDelegate.MODE_NIGHT_YES);
        waitUntil(() -> text(R.id.book_keep_type).contains("午餐"));
        instrumentation.runOnMainSync(() -> {
            assertEquals(2, ((ViewPager) activity.findViewById(R.id.view_list)).getCurrentItem());
            assertEquals("12.50", ((EditText) activity.findViewById(R.id.send_msg)).getText().toString());
        });
    }
    private String text(int id) {
        String[] value = {""}; instrumentation.runOnMainSync(() -> { TextView view = activity.findViewById(id); if (view != null) value[0] = view.getText().toString(); }); return value[0];
    }
    private void waitUntil(Callable<Boolean> condition) throws Exception {
        long end = System.currentTimeMillis() + 8000;
        while (System.currentTimeMillis() < end) { if (condition.call()) return; Thread.sleep(80); }
        fail("Timed out waiting for night-mode state");
    }
}
