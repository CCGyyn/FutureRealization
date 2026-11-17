package com.ccg.futurerealization.importer;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;

public class XlsLedgerReaderTest {
    private static final String[] HEADER = {"记账日期", "记账时间", "记账类型", "使用账户", "账户类型", "一级分类", "二级分类", "记账金额", "记账货币", "备注"};
    private byte[] file(String date, String type, String parent, String category, double amount, String currency, String remark, boolean formula) throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("测试账单");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADER.length; i++) header.createCell(i).setCellValue(HEADER[i]);
            Row row = sheet.createRow(1);
            String[] values = {date, "12:30:45", type, "现金", "现金账户", parent, category, "", currency, remark};
            for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
            if (formula) row.getCell(7).setCellFormula("1+2"); else row.getCell(7).setCellValue(amount);
            workbook.write(output); return output.toByteArray();
        }
    }
    @Test public void parsesMoneyDatesAndExcelCategoryPrefixWithoutLosingMetadata() throws Exception {
        XlsLedgerReader.Batch batch = XlsLedgerReader.read(file("2024-02-29", "支出", "食", "'午餐", 25.50, "人民币", " 午饭\n和朋友 ", false));
        assertEquals(1, batch.entries.size()); assertEquals(2550, batch.expenseCents); assertEquals(0, batch.incomeCents);
        XlsLedgerReader.Entry row = batch.entries.get(0);
        assertEquals("午餐", row.category); assertEquals("食", row.parent); assertEquals(1, row.type);
        assertTrue(row.remark.startsWith(" 午饭\n和朋友 \n[XLS导入]"));
        assertTrue(row.remark.contains("时间：12:30:45")); assertTrue(row.remark.contains("账户：现金"));
        assertEquals("2024-02-29", batch.firstDate);
    }
    @Test public void incomeRetainsCentPrecision() throws Exception {
        XlsLedgerReader.Batch batch = XlsLedgerReader.read(file("2026-01-01", "收入", "收", "'奖金", 0.01, "CNY", "", false));
        assertEquals(1, batch.incomeCents); assertEquals(1, batch.incomeCount); assertEquals(0, batch.entries.get(0).type);
    }
    @Test public void rejectsInvalidDatesCurrenciesMoneyAndFormulas() throws Exception {
        reject(file("2025-02-29", "支出", "食", "早餐", 1, "人民币", "", false));
        reject(file("2026-01-01", "支出", "食", "早餐", 1, "美元", "", false));
        reject(file("2026-01-01", "支出", "食", "早餐", 1.001, "人民币", "", false));
        reject(file("2026-01-01", "支出", "食", "早餐", 0, "人民币", "", false));
        reject(file("2026-01-01", "支出", "食", "早餐", -1, "人民币", "", false));
        reject(file("2026-01-01", "支出", "食", "早餐", 1, "人民币", "", true));
        reject("not an xls".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    @Test public void rejectsMissingRequiredHeaderAndEmptyLedger() throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet("错误表头").createRow(0).createCell(0).setCellValue("金额");
            workbook.write(output); reject(output.toByteArray());
        }
        reject(file("2026-01-01", "转账", "其", "转账", 1, "人民币", "", false));
    }
    private void reject(byte[] bytes) throws Exception {
        try { XlsLedgerReader.read(bytes); fail("Invalid file accepted"); } catch (IllegalArgumentException expected) { }
    }
}
