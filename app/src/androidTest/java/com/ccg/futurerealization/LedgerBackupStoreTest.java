package com.ccg.futurerealization;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ccg.futurerealization.backup.LedgerBackup;
import com.ccg.futurerealization.backup.LedgerBackupStore;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.util.List;
import static org.junit.Assert.*;

/** Uses a separate test DB; never clears the personal ledger. */
@RunWith(AndroidJUnit4.class)
public class LedgerBackupStoreTest {
    private Context context;
    private String buddyName;
    private int buddyStyle;
    private static final String DB = "ledger_migration_test";

    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        android.content.SharedPreferences prefs = context.getSharedPreferences("bookkeeping_buddy", Context.MODE_PRIVATE);
        buddyName = prefs.getString("name", "小伴");
        buddyStyle = prefs.getInt("style", 0);
        LitePal.deleteDatabase(DB);
        LitePalDB database = new LitePalDB(DB, 1);
        database.addClassName(Account.class.getName());
        database.addClassName(com.ccg.futurerealization.bean.DoSth.class.getName());
        database.addClassName(AccountCategory.class.getName());
        LitePal.use(database);
        LitePal.getDatabase();
    }

    @After public void tearDown() {
        LitePal.deleteDatabase(DB);
        LitePal.useDefault();
        context.getSharedPreferences("bookkeeping_buddy", Context.MODE_PRIVATE).edit()
                .putString("name", buddyName).putInt("style", buddyStyle).commit();
    }

    private LedgerBackup fixture() {
        LedgerBackup.Data data = new LedgerBackup.Data();
        LedgerBackup.Category parent = new LedgerBackup.Category();
        parent.id = 400; parent.type = 1; parent.name = "食";
        LedgerBackup.Category child = new LedgerBackup.Category();
        child.id = 900; child.parentId = 400; child.type = 1; child.name = "中餐";
        data.categories.add(parent); data.categories.add(child);
        for (int i = 0; i < 2; i++) {
            LedgerBackup.Entry row = new LedgerBackup.Entry();
            row.date = "2026-11-17"; row.type = 1; row.amountCents = 1250 + i;
            row.categoryId = 900; row.remark = "测试午餐";
            data.accounts.add(row);
        }
        return LedgerBackup.decode(LedgerBackup.encode(data, "2026-11-17 12:00:00"));
    }

    @Test public void restoresRelationsAndRejectsSecondImportWithoutDuplicating() {
        LedgerBackup backup = fixture();
        assertEquals(2, LedgerBackupStore.restoreLedger(context, backup));
        List<Account> accounts = LitePal.findAll(Account.class, true);
        assertEquals(2, accounts.size());
        assertEquals("中餐", accounts.get(0).getAccountCategory().getCategory());
        assertEquals(Integer.valueOf(1250), accounts.get(0).getAmount());
        assertNotEquals(Long.valueOf(900), accounts.get(0).getAccountCategory().getId());
        LedgerBackup exported = LedgerBackup.decode(LedgerBackupStore.exportLedger(context));
        assertEquals(2, exported.data.accounts.size());
        try { LedgerBackupStore.restoreLedger(context, backup); fail("must reject nonempty ledger"); }
        catch (IllegalArgumentException expected) { }
        assertEquals(2, LitePal.count(Account.class));
    }

    @Test public void reusesExistingDefaultCategoriesWithDifferentIds() {
        AccountCategory parent = new AccountCategory();
        parent.setCategory("食"); parent.setType(1); parent.setPid(0L); assertTrue(parent.save());
        AccountCategory child = new AccountCategory();
        child.setCategory("中餐"); child.setType(1); child.setPid(parent.getId()); assertTrue(child.save());
        assertEquals(2, LedgerBackupStore.restoreLedger(context, fixture()));
        assertEquals(2, LitePal.count(AccountCategory.class));
        assertEquals(child.getId(), LitePal.findAll(Account.class, true).get(0).getAccountCategory().getId());
    }

    @Test public void categorySurvivesParcelWithAndWithoutNullableFields() {
        for (boolean nullable : new boolean[]{false, true}) {
            AccountCategory category = new AccountCategory();
            category.setCategory("中餐");
            if (!nullable) { category.setId(900L); category.setPid(400L); category.setType(1); }
            android.os.Parcel parcel = android.os.Parcel.obtain();
            try {
                category.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                AccountCategory copy = AccountCategory.CREATOR.createFromParcel(parcel);
                assertEquals(category.getId(), copy.getId());
                assertEquals(category.getPid(), copy.getPid());
                assertEquals(category.getType(), copy.getType());
                assertEquals(category.getCategory(), copy.getCategory());
            } finally { parcel.recycle(); }
        }
    }

    @Test public void goalCompletionAndCustomCategorySurviveFullBackup() {
        LedgerBackup backup = fixture();
        backup.data.goals = new java.util.ArrayList<>();
        LedgerBackup.Goal goal = new LedgerBackup.Goal();
        goal.content = "读完一本书"; goal.completed = true; goal.type = 2;
        backup.data.goals.add(goal);
        assertEquals(2, LedgerBackupStore.restoreLedger(context, backup));
        com.ccg.futurerealization.bean.DoSth saved = LitePal.findAll(com.ccg.futurerealization.bean.DoSth.class).get(0);
        assertTrue(saved.getState());
        assertEquals("读完一本书", saved.getFuture_content());
        LedgerBackup exported = LedgerBackup.decode(LedgerBackupStore.exportLedger(context));
        assertEquals(2, exported.version);
        assertEquals(1, exported.data.goals.size());
        assertTrue(exported.data.goals.get(0).completed);
    }

    @Test public void goalWriteFailureRollsBackTheWholeRestore() {
        LedgerBackup backup = fixture();
        backup.data.goals = new java.util.ArrayList<>();
        LedgerBackup.Goal goal = new LedgerBackup.Goal();
        goal.content = "abort"; goal.completed = false; goal.type = 0; backup.data.goals.add(goal);
        LitePal.getDatabase().execSQL("CREATE TRIGGER abort_goal BEFORE INSERT ON dosth BEGIN SELECT RAISE(ABORT, 'test goal failure'); END");
        try { LedgerBackupStore.restoreLedger(context, backup); fail("must fail"); }
        catch (RuntimeException expected) { }
        assertEquals(0, LitePal.count(Account.class));
        assertEquals(0, LitePal.count(AccountCategory.class));
        assertEquals(0, LitePal.count(com.ccg.futurerealization.bean.DoSth.class));
    }

    @Test public void rollsBackAccountsAndCategoriesWhenAnyWriteFails() {
        LitePal.getDatabase().execSQL("CREATE TRIGGER abort_second_account BEFORE INSERT ON account WHEN NEW.amount = 1251 BEGIN SELECT RAISE(ABORT, 'test failure'); END");
        try { LedgerBackupStore.restoreLedger(context, fixture()); fail("must fail"); }
        catch (RuntimeException expected) { }
        assertEquals(0, LitePal.count(Account.class));
        assertEquals(0, LitePal.count(AccountCategory.class));
    }
}
