package com.ccg.futurerealization.backup;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class LedgerBackupTest {
    private LedgerBackup.Data fixture() {
        LedgerBackup.Data data = new LedgerBackup.Data();
        LedgerBackup.Category parent = new LedgerBackup.Category();
        parent.id = 20; parent.name = "食"; parent.type = 1;
        LedgerBackup.Category child = new LedgerBackup.Category();
        child.id = 99; child.parentId = 20; child.name = "中餐"; child.type = 1;
        data.categories.add(parent); data.categories.add(child);
        LedgerBackup.Entry entry = new LedgerBackup.Entry();
        entry.date = "2024-02-29"; entry.amountCents = 2550; entry.type = 1; entry.categoryId = 99;
        entry.remark = "中文 🍚 · <测试>";
        data.accounts.add(entry);
        data.buddyName = "小伴"; data.buddyStyle = 2;
        return data;
    }

    @Test public void roundTripsChineseMoneyAndCategoryReferences() {
        LedgerBackup original = LedgerBackup.decode(LedgerBackup.encode(fixture(), "2026-11-17 12:00:00"));
        assertEquals(2550, original.data.accounts.get(0).amountCents);
        assertEquals(99, original.data.accounts.get(0).categoryId);
        assertEquals(20, original.data.categories.get(1).parentId);
        assertEquals("中文 🍚 · <测试>", original.data.accounts.get(0).remark);
        assertEquals(2, original.data.buddyStyle);
    }

    @Test public void detectsCorruptedAndUnsupportedBackups() {
        String json = new String(LedgerBackup.encode(fixture(), "2026-11-17"), StandardCharsets.UTF_8);
        for (String damaged : new String[]{"", "{}", "null", json.substring(0, json.length() / 2),
                json.replace("2550", "2551"), json.replace("\"version\":1", "\"version\":2"), json + "garbage"}) {
            try { LedgerBackup.decode(damaged.getBytes(StandardCharsets.UTF_8)); fail("Accepted damaged backup"); }
            catch (IllegalArgumentException expected) { }
        }
    }

    @Test public void rejectsBadDatesAndBrokenRelationsBeforeWriting() {
        LedgerBackup.Data data = fixture();
        data.accounts.get(0).date = "2025-02-29";
        reject(data);
        data = fixture(); data.accounts.get(0).categoryId = 1; reject(data);
        data = fixture(); data.accounts.get(0).type = 0; reject(data);
        data = fixture(); data.accounts.get(0).amountCents = 0; reject(data);
        data = fixture(); data.categories.get(0).parentId = 99; reject(data);
        data = fixture(); data.categories.get(1).id = 20; reject(data);
    }

    private void reject(LedgerBackup.Data data) {
        try { LedgerBackup.encode(data, "2026-11-17"); fail("Accepted invalid backup"); }
        catch (IllegalArgumentException expected) { }
    }
}
