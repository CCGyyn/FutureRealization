package com.ccg.futurerealization.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Portable versioned backup. Contains no paths, SQL or executable content. */
public final class LedgerBackup {
    public static final int MAX_BYTES = 20 * 1024 * 1024;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public String format;
    public int version;
    public String exportedAt;
    public Data data;
    public String sha256;

    public static class Data {
        public List<Category> categories = new ArrayList<>();
        public List<Entry> accounts = new ArrayList<>();
        // Null distinguishes legacy ledger-only backups and keeps their checksum unchanged.
        public List<Goal> goals;
        // Retained solely for reading old backups; the companion feature has been removed.
        public String buddyName;
        public int buddyStyle;
    }

    public static class Goal {
        public String content;
        public Boolean completed;
        public Integer type;
    }

    public static class Category {
        public long id;
        public long parentId;
        public int type;
        public String name;
    }

    public static class Entry {
        public String date;
        public int type;
        public int amountCents;
        public long categoryId;
        public String remark;
    }

    public static byte[] encode(Data data, String exportedAt) {
        validate(data);
        LedgerBackup backup = new LedgerBackup();
        backup.format = "FutureRealization-Ledger";
        backup.version = data.goals == null ? 1 : 2;
        backup.data = data;
        backup.exportedAt = exportedAt;
        backup.sha256 = digest(GSON.toJson(data));
        byte[] bytes = GSON.toJson(backup).getBytes(StandardCharsets.UTF_8);
        require(bytes.length <= MAX_BYTES, "备份超过 20MB，请联系开发者处理");
        return bytes;
    }

    public static LedgerBackup decode(byte[] bytes) {
        require(bytes != null && bytes.length > 0 && bytes.length <= MAX_BYTES, "请选择有效的备份文件（不超过20MB）");
        final LedgerBackup backup;
        try {
            backup = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), LedgerBackup.class);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("文件内容损坏，无法读取备份");
        }
        require(backup != null && "FutureRealization-Ledger".equals(backup.format) && (backup.version == 1 || backup.version == 2),
                "不是受支持的备份，请使用本应用导出的备份文件");
        require(backup.data != null && backup.sha256 != null && backup.sha256.equals(digest(GSON.toJson(backup.data))),
                "备份完整性校验失败，文件可能已被修改或损坏");
        require(backup.version == 1 ? backup.data.goals == null : backup.data.goals != null, "备份版本与内容不一致");
        validate(backup.data);
        require(backup.exportedAt != null && backup.exportedAt.length() <= 40, "备份时间无效");
        return backup;
    }

    public static void validate(Data data) {
        require(data != null && data.categories != null && data.accounts != null, "备份缺少账目或分类");
        require(data.categories.size() <= 10000 && data.accounts.size() <= 100000, "备份记录数量超出支持范围");
        if (data.goals != null) {
            require(data.goals.size() <= 100000, "目标记录数量超出支持范围");
            for (Goal goal : data.goals) require(goal != null && goal.content != null && goal.content.length() <= 10000 &&
                    goal.completed != null && goal.type != null && goal.type >= 0 && goal.type <= 2, "目标信息无效");
        }
        Map<Long, Category> categories = new HashMap<>();
        for (Category c : data.categories) {
            require(c != null && c.id > 0 && c.parentId >= 0 && (c.type == 0 || c.type == 1) &&
                    c.name != null && !c.name.trim().isEmpty() && c.name.length() <= 100, "分类信息无效");
            require(categories.put(c.id, c) == null, "备份包含重复分类编号");
        }
        Set<Long> referencedParents = new HashSet<>();
        for (Category c : data.categories) {
            if (c.parentId == 0) continue;
            Category parent = categories.get(c.parentId);
            require(parent != null && parent.parentId == 0 && parent.type == c.type, "分类层级或收支类型无效");
            referencedParents.add(c.parentId);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setLenient(false);
        for (Entry entry : data.accounts) {
            require(entry != null && entry.amountCents > 0 && (entry.type == 0 || entry.type == 1), "账单金额或收支类型无效");
            require(entry.date != null && entry.date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"), "账单日期格式无效");
            ParsePosition position = new ParsePosition(0);
            require(format.parse(entry.date, position) != null && position.getIndex() == 10, "账单日期无效");
            Category category = categories.get(entry.categoryId);
            require(category != null && category.parentId != 0 && !referencedParents.contains(category.id) && category.type == entry.type,
                    "账单对应分类不存在或收支类型不一致");
            require(entry.remark == null || entry.remark.length() <= 10000, "账单备注过长");
        }
    }

    private static String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new IllegalArgumentException(message);
    }
}
