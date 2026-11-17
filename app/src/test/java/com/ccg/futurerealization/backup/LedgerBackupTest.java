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

    @Test public void readsOriginalVersionOneFileWithoutNewGoalsField() {
        String legacy = "{\"format\":\"FutureRealization-Ledger\",\"version\":1,\"exportedAt\":\"2026-11-17\",\"data\":{\"categories\":[],\"accounts\":[],\"buddyName\":\"小伴\",\"buddyStyle\":0},\"sha256\":\"HASH\"}";
        String payload = "{\"categories\":[],\"accounts\":[],\"buddyName\":\"小伴\",\"buddyStyle\":0}";
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
            LedgerBackup read = LedgerBackup.decode(legacy.replace("HASH", hex.toString()).getBytes(StandardCharsets.UTF_8));
            assertNull(read.data.goals);
            assertEquals(1, read.version);
        } catch (java.security.NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }

    @Test public void newBackupPreservesGoalsAndRejectsIncompleteGoalRecords() {
        LedgerBackup.Data data = fixture(); data.goals = new java.util.ArrayList<>();
        LedgerBackup.Goal goal = new LedgerBackup.Goal();
        goal.content = "每天读书"; goal.completed = false; goal.type = 1; data.goals.add(goal);
        LedgerBackup read = LedgerBackup.decode(LedgerBackup.encode(data, "2026-11-17"));
        assertEquals(4, read.version);
        assertEquals("每天读书", read.data.goals.get(0).content);
        assertFalse(read.data.goals.get(0).completed);
        goal.completed = null; reject(data);
    }

    @Test public void backupRetainsRecordedTimeAndRejectsImpossibleTime() {
        LedgerBackup.Data data = fixture(); data.goals = new java.util.ArrayList<>();
        data.accounts.get(0).time = "07:42:30";
        LedgerBackup restored = LedgerBackup.decode(LedgerBackup.encode(data, "2026-11-17"));
        assertEquals("07:42:30", restored.data.accounts.get(0).time);
        assertEquals(4, restored.version);
        data.accounts.get(0).time = "24:01:00"; reject(data);
    }

    @Test public void versionTwoFilesWithoutTimeRemainReadable() throws Exception {
        String payload = "{\"categories\":[],\"accounts\":[],\"goals\":[],\"buddyStyle\":0}";
        byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder checksum = new StringBuilder();
        for (byte value : hash) checksum.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
        String json = "{\"format\":\"FutureRealization-Ledger\",\"version\":2,\"exportedAt\":\"2026-11-17\",\"data\":" + payload + ",\"sha256\":\"" + checksum + "\"}";
        assertEquals(2, LedgerBackup.decode(json.getBytes(StandardCharsets.UTF_8)).version);
    }

    @Test public void abandonedGoalsRoundTripAndCannotAlsoBeCompleted() {
        LedgerBackup.Data data = fixture(); data.goals = new java.util.ArrayList<>();
        LedgerBackup.Goal goal = new LedgerBackup.Goal();
        goal.content = "暂停的计划"; goal.completed = false; goal.type = 2; goal.abandoned = true;
        data.goals.add(goal);
        assertTrue(LedgerBackup.decode(LedgerBackup.encode(data, "2026-11-17")).data.goals.get(0).abandoned);
        goal.completed = true; reject(data);
    }

    @Test public void versionThreeGoalsWithoutAbandonedRemainReadable() throws Exception {
        LedgerBackup.Data data = fixture(); data.goals = new java.util.ArrayList<>();
        LedgerBackup.Goal goal = new LedgerBackup.Goal(); goal.content = "旧版目标"; goal.completed = true; goal.type = 1;
        data.goals.add(goal);
        String json = new String(LedgerBackup.encode(data, "2026-11-17"), StandardCharsets.UTF_8);
        assertFalse(json.contains("abandoned"));
        LedgerBackup legacy = LedgerBackup.decode(json.replace("\"version\":4", "\"version\":3").getBytes(StandardCharsets.UTF_8));
        assertEquals(3, legacy.version); assertNull(legacy.data.goals.get(0).abandoned);
        assertTrue(legacy.data.goals.get(0).completed);
    }

    private void reject(LedgerBackup.Data data) {
        try { LedgerBackup.encode(data, "2026-11-17"); fail("Accepted invalid backup"); }
        catch (IllegalArgumentException expected) { }
    }
}
