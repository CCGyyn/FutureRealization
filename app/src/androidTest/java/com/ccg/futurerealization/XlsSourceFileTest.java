package com.ccg.futurerealization;

import android.content.Context;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.importer.XlsLedgerReader;
import com.ccg.futurerealization.importer.XlsLedgerStore;
import com.ccg.futurerealization.utils.DefaultCategories;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.litepal.LitePal;
import org.litepal.LitePalDB;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/** Optional private fixture supplied through instrumentation arguments, never packaged in the APK. */
@RunWith(AndroidJUnit4.class)
public class XlsSourceFileTest {
    @Test public void fullSourceFileImportsAndRepeatedImportAddsNothing() throws Exception {
        Bundle args = InstrumentationRegistry.getArguments();
        assumeTrue(args.containsKey("source_file"));
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String databaseName = "xls_full_source_test";
        LitePal.deleteDatabase(databaseName);
        LitePalDB database = new LitePalDB(databaseName, 1);
        database.addClassName(Account.class.getName()); database.addClassName(AccountCategory.class.getName()); database.addClassName(DoSth.class.getName());
        database.addClassName(com.ccg.futurerealization.bean.Note.class.getName());
        LitePal.use(database); LitePal.getDatabase();
        try {
            byte[] bytes;
            try (FileInputStream input = new FileInputStream(new File(context.getFilesDir(), args.getString("source_file"))); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int read; while ((read = input.read(buffer)) != -1) out.write(buffer, 0, read); bytes = out.toByteArray();
            }
            XlsLedgerReader.Batch batch = XlsLedgerReader.read(bytes);
            assertEquals(Integer.parseInt(args.getString("expected_rows")), batch.entries.size());
            assertEquals(Long.parseLong(args.getString("expected_income")), batch.incomeCents);
            assertEquals(Long.parseLong(args.getString("expected_expense")), batch.expenseCents);
            DefaultCategories.ensure(context);
            XlsLedgerStore.Plan preview = XlsLedgerStore.preview(batch);
            assertEquals(0, preview.newCategories); assertEquals(0, preview.newRoots);
            assertEquals(batch.entries.size(), XlsLedgerStore.importBatch(batch).imported);
            assertEquals(batch.entries.size(), LitePal.count(Account.class));
            long income = 0, expense = 0;
            for (Account account : LitePal.findAll(Account.class, true)) {
                assertNotNull(account.getAccountCategory());
                assertTrue(account.getRemark().contains("[XLS导入]"));
                if (account.getType() == 0) income += account.getAmount(); else expense += account.getAmount();
            }
            assertEquals(batch.incomeCents, income); assertEquals(batch.expenseCents, expense);
            assertEquals(0, XlsLedgerStore.importBatch(batch).imported);
            assertEquals(batch.entries.size(), LitePal.count(Account.class));
        } finally { LitePal.deleteDatabase(databaseName); LitePal.useDefault(); }
    }
}
