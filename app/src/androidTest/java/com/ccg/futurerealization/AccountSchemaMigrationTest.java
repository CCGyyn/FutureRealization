package com.ccg.futurerealization;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.utils.AccountDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountSchemaMigrationTest {
    @Test public void upgradingRealVersionFourSchemaAddsTimeAndPreservesLedgerAndGoals() {
        String name = "legacy_time_migration_test";
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LitePal.deleteDatabase(name);
        SQLiteDatabase old = context.openOrCreateDatabase(name + ".db", 0, null);
        old.execSQL("CREATE TABLE table_schema (id integer primary key autoincrement,name text,type integer)");
        old.execSQL("CREATE TABLE dosth (id integer primary key autoincrement,future_content text,state integer,type integer)");
        old.execSQL("CREATE TABLE accountcategory (id integer primary key autoincrement,category text,pid integer,type integer)");
        old.execSQL("CREATE TABLE account (id integer primary key autoincrement,amount integer,date text,remark text,type integer,accountcategory_id integer)");
        old.execSQL("INSERT INTO table_schema(name,type) VALUES ('dosth',0),('accountcategory',0),('account',0)");
        old.execSQL("INSERT INTO dosth(future_content,state,type) VALUES ('保留旧目标',1,1)");
        old.execSQL("INSERT INTO accountcategory(category,pid,type) VALUES ('食',0,1),('早餐',1,1)");
        old.execSQL("INSERT INTO account(amount,date,remark,type,accountcategory_id) VALUES (1234,'2026-11-17','旧版账单',1,2)");
        old.setVersion(4); old.close();
        LitePalDB database = new LitePalDB(name, 6);
        database.addClassName(DoSth.class.getName()); database.addClassName(AccountCategory.class.getName()); database.addClassName(Account.class.getName());
        LitePal.use(database);
        try {
            assertEquals(6, LitePal.getDatabase().getVersion());
            Account account = LitePal.find(Account.class, 1, true);
            assertEquals(Integer.valueOf(1234), account.getAmount()); assertEquals("早餐", account.getAccountCategory().getCategory());
            assertNull(account.getTime()); assertTrue(AccountDetails.dateTime(account).contains("时间未记录"));
            account.setTime("09:10:11"); assertTrue(account.save());
            assertEquals("09:10:11", LitePal.find(Account.class, 1).getTime());
            DoSth goal = LitePal.find(DoSth.class, 1); assertEquals("保留旧目标", goal.getFuture_content()); assertTrue(goal.getState()); assertFalse(goal.isAbandoned());
            goal.setAbandoned(true); assertTrue(goal.save());
            assertTrue(LitePal.find(DoSth.class, 1).isAbandoned());
            assertFalse(LitePal.find(DoSth.class, 1).getState());
            assertEquals(1, LitePal.count(Account.class));
        } finally { LitePal.deleteDatabase(name); LitePal.useDefault(); }
    }
}
