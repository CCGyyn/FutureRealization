package com.ccg.futurerealization.importer;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reader for the supplied app's BIFF .xls export. Never evaluates spreadsheet formulas. */
public final class XlsLedgerReader {
    public static final int MAX_BYTES = 20 * 1024 * 1024;
    private static final String[] REQUIRED = {"记账日期", "记账类型", "一级分类", "二级分类", "记账金额"};
    private XlsLedgerReader() { }

    public static Batch read(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) throw new IllegalArgumentException("请选择不超过20MB的 XLS 账单文件");
        byte[] magic = {(byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0, (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1};
        if (bytes.length < magic.length) throw new IllegalArgumentException("不是有效的 XLS 文件");
        for (int i = 0; i < magic.length; i++) if (bytes[i] != magic[i]) throw new IllegalArgumentException("请使用原始 .xls 文件，暂不支持 XLSX 或 CSV");
        List<Entry> entries = new ArrayList<>();
        int transfers = 0;
        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
            if (workbook.getNumberOfSheets() > 20) throw new IllegalArgumentException("工作表过多，无法识别为账单导出文件");
            for (Sheet sheet : workbook) {
                if (sheet.getPhysicalNumberOfRows() == 0) continue;
                Map<String, Integer> columns = null;
                int header = -1;
                for (int i = 0; i <= Math.min(sheet.getLastRowNum(), 20); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    Map<String, Integer> names = new HashMap<>();
                    for (Cell cell : row) names.put(text(cell, formatter).trim(), cell.getColumnIndex());
                    boolean valid = true;
                    for (String required : REQUIRED) if (!names.containsKey(required)) valid = false;
                    if (valid) { columns = names; header = i; break; }
                }
                if (columns == null) throw new IllegalArgumentException("工作表“" + sheet.getSheetName() + "”缺少账单表头，请使用原始 XLS 导出文件");
                for (int i = header + 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || empty(row, formatter)) continue;
                    try {
                        String kind = value(row, columns, "记账类型", formatter);
                        if ("转账".equals(kind)) { transfers++; continue; }
                        if (!"收入".equals(kind) && !"支出".equals(kind)) throw new IllegalArgumentException("记账类型必须是收入或支出");
                        String currency = value(row, columns, "记账货币", formatter);
                        if (!(currency.isEmpty() || "人民币".equals(currency) || "CNY".equalsIgnoreCase(currency) || "RMB".equalsIgnoreCase(currency)))
                            throw new IllegalArgumentException("暂只支持人民币，不会自动换算外币");
                        String date = date(cell(row, columns, "记账日期"), formatter);
                        String time = value(row, columns, "记账时间", formatter);
                        if (!time.isEmpty()) {
                            if (time.matches("[0-9]{2}:[0-9]{2}")) time += ":00";
                            if (!validDate(time, "HH:mm:ss", "[0-9]{2}:[0-9]{2}:[0-9]{2}")) throw new IllegalArgumentException("记账时间格式无效");
                        }
                        String parent = category(value(row, columns, "一级分类", formatter));
                        String child = category(value(row, columns, "二级分类", formatter));
                        Cell amount = cell(row, columns, "记账金额");
                        if (amount == null || amount.getCellTypeEnum() == CellType.FORMULA) throw new IllegalArgumentException("金额缺失或使用了公式");
                        BigDecimal money;
                        try {
                            money = amount.getCellTypeEnum() == CellType.NUMERIC ? BigDecimal.valueOf(amount.getNumericCellValue()) : new BigDecimal(text(amount, formatter).trim());
                        } catch (NumberFormatException e) { throw new IllegalArgumentException("金额格式无效"); }
                        int cents;
                        try { cents = money.movePointRight(2).intValueExact(); }
                        catch (ArithmeticException e) { throw new IllegalArgumentException("金额需精确到分，且不能超过21474836.47元"); }
                        if (cents <= 0) throw new IllegalArgumentException("金额必须大于0");
                        String original = text(cell(row, columns, "备注"), formatter);
                        String account = value(row, columns, "使用账户", formatter);
                        String accountType = value(row, columns, "账户类型", formatter);
                        StringBuilder remark = new StringBuilder(original);
                        if (remark.length() > 0) remark.append('\n');
                        remark.append("[XLS导入]");
                        if (!time.isEmpty()) remark.append(" 时间：").append(time);
                        if (!account.isEmpty()) remark.append("；账户：").append(account);
                        if (!accountType.isEmpty()) remark.append("；账户类型：").append(accountType);
                        remark.append("；币种：人民币");
                        if (remark.length() > 10000) throw new IllegalArgumentException("备注内容过长");
                        entries.add(new Entry(date, "收入".equals(kind) ? 0 : 1, parent, child, cents, remark.toString()));
                        if (entries.size() > 100000) throw new IllegalArgumentException("单次最多导入10万笔账目");
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("“" + sheet.getSheetName() + "”第" + (i + 1) + "行：" + e.getMessage());
                    }
                }
            }
        } catch (org.apache.poi.EncryptedDocumentException e) {
            throw new IllegalArgumentException("文件已加密，请先导出未加密的 XLS 文件");
        }
        if (entries.isEmpty()) throw new IllegalArgumentException("文件中没有可导入的人民币收入或支出记录");
        return new Batch(entries, transfers);
    }

    private static String category(String value) {
        String name = value.trim();
        if (name.startsWith("'")) name = name.substring(1).trim();
        if (name.isEmpty() || name.length() > 100 || name.indexOf('\0') >= 0) throw new IllegalArgumentException("分类名称缺失或无效");
        return name;
    }
    private static boolean empty(Row row, DataFormatter formatter) {
        for (Cell cell : row) if (!text(cell, formatter).trim().isEmpty()) return false;
        return true;
    }
    private static Cell cell(Row row, Map<String, Integer> columns, String name) { Integer index = columns.get(name); return index == null ? null : row.getCell(index); }
    private static String value(Row row, Map<String, Integer> columns, String name, DataFormatter formatter) { return text(cell(row, columns, name), formatter).trim(); }
    private static String text(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        if (cell.getCellTypeEnum() == CellType.FORMULA || cell.getCellTypeEnum() == CellType.ERROR) throw new IllegalArgumentException("账单包含公式或错误单元格，请使用原始导出数据");
        return formatter.formatCellValue(cell);
    }
    private static String date(Cell cell, DataFormatter formatter) {
        String value;
        if (cell != null && cell.getCellTypeEnum() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
            value = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(cell.getDateCellValue());
        else value = text(cell, formatter).trim();
        if (!validDate(value, "yyyy-MM-dd", "[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw new IllegalArgumentException("记账日期格式无效，应为yyyy-MM-dd");
        return value;
    }
    private static boolean validDate(String value, String pattern, String shape) {
        if (!value.matches(shape)) return false;
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ROOT); format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(value, position) != null && position.getIndex() == value.length();
    }
    public static String key(Object... values) {
        StringBuilder result = new StringBuilder();
        for (Object value : values) { String text = String.valueOf(value); result.append(text.length()).append(':').append(text); }
        return result.toString();
    }
    public static final class Entry {
        public final String date, parent, category, remark;
        public final int type, cents;
        private Entry(String date, int type, String parent, String category, int cents, String remark) {
            this.date = date; this.type = type; this.parent = parent; this.category = category; this.cents = cents; this.remark = remark;
        }
        public String signature() { return key(date, type, parent, category, cents, remark); }
        public String categoryKey() { return key(type, parent, category); }
    }
    public static final class Batch {
        public final List<Entry> entries;
        public final int transfers;
        public final long incomeCents, expenseCents;
        public final int incomeCount, expenseCount;
        public final String firstDate, lastDate;
        private Batch(List<Entry> entries, int transfers) {
            this.entries = Collections.unmodifiableList(new ArrayList<>(entries)); this.transfers = transfers;
            long income = 0, expense = 0; int incomes = 0, expenses = 0;
            String first = entries.get(0).date, last = first;
            for (Entry entry : entries) {
                if (entry.type == 0) { income += entry.cents; incomes++; } else { expense += entry.cents; expenses++; }
                if (entry.date.compareTo(first) < 0) first = entry.date;
                if (entry.date.compareTo(last) > 0) last = entry.date;
            }
            incomeCents = income; expenseCents = expense; incomeCount = incomes; expenseCount = expenses; firstDate = first; lastDate = last;
        }
    }
}
