package com.ccg.futurerealization.utils;

import com.ccg.futurerealization.bean.Account;
import org.junit.Test;
import static org.junit.Assert.*;

public class AccountDetailsTest {
    @Test public void usesRecordedTimeThenLegacyImportMetadataWithoutInventingMissingTime() {
        Account nativeEntry = new Account(); nativeEntry.setDate("2026-11-17"); nativeEntry.setTime("07:42:30");
        assertEquals("2026-11-17  07:42:30", AccountDetails.dateTime(nativeEntry));
        Account oldImport = new Account(); oldImport.setDate("2026-11-17");
        oldImport.setRemark("早餐\n[XLS导入] 时间：08:12:00；账户：现金；币种：人民币");
        assertEquals("08:12:00", AccountDetails.timeOf(oldImport));
        Account unknown = new Account(); unknown.setDate("2026-11-17"); unknown.setRemark("开会时间：09:00:00");
        assertNull(AccountDetails.timeOf(unknown)); assertTrue(AccountDetails.dateTime(unknown).contains("时间未记录"));
        assertFalse(AccountDetails.validTime("24:00:00")); assertFalse(AccountDetails.validTime("12:61:00"));
    }
}
