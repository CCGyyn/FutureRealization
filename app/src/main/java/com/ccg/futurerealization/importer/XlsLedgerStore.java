package com.ccg.futurerealization.importer;

import android.database.sqlite.SQLiteDatabase;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Append-only transactional import. Multiset matching retains genuine identical source rows. */
public final class XlsLedgerStore {
    private XlsLedgerStore() { }
    public static Plan preview(XlsLedgerReader.Batch batch) {
        synchronized (LitePalSupport.class) { return new Snapshot().plan(batch); }
    }

    public static Result importBatch(XlsLedgerReader.Batch batch) {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = LitePal.getDatabase();
            db.beginTransaction();
            try {
                Snapshot snapshot = new Snapshot();
                Plan plan = snapshot.plan(batch); // Recheck against the current DB when the user confirms.
                int roots = 0, children = 0, imported = 0;
                Map<String, Integer> occurrences = new HashMap<>();
                for (XlsLedgerReader.Entry row : batch.entries) {
                    String parentKey = XlsLedgerReader.key(row.type, row.parent);
                    AccountCategory parent = snapshot.roots.get(parentKey);
                    if (parent == null) {
                        parent = new AccountCategory(); parent.setCategory(row.parent); parent.setType(row.type); parent.setPid(0L);
                        if (!parent.save()) throw new IllegalStateException("一级分类保存失败，导入已回滚");
                        snapshot.roots.put(parentKey, parent); roots++;
                    }
                    String categoryKey = row.categoryKey();
                    AccountCategory category = snapshot.children.get(categoryKey);
                    if (category == null) {
                        category = new AccountCategory(); category.setCategory(row.category); category.setType(row.type); category.setPid(parent.getId());
                        if (!category.save()) throw new IllegalStateException("二级分类保存失败，导入已回滚");
                        snapshot.children.put(categoryKey, category); children++;
                    }
                    String signature = row.signature();
                    int occurrence = occurrences.getOrDefault(signature, 0) + 1;
                    occurrences.put(signature, occurrence);
                    if (occurrence <= snapshot.existing.getOrDefault(signature, 0)) continue;
                    Account account = new Account(); account.setDate(row.date); account.setType(row.type); account.setAmount(row.cents);
                    account.setAccountCategory(category); account.setRemark(row.remark);
                    if (!account.save()) throw new IllegalStateException("账单保存失败，导入已回滚");
                    imported++;
                }
                db.setTransactionSuccessful();
                return new Result(imported, plan.skipped, roots, children, plan.incomeCents, plan.expenseCents);
            } finally { db.endTransaction(); }
        }
    }

    private static final class Snapshot {
        final Map<String, AccountCategory> roots = new HashMap<>(), children = new HashMap<>();
        final Map<String, Integer> existing = new HashMap<>();
        Snapshot() {
            List<AccountCategory> categories = LitePal.findAll(AccountCategory.class);
            Map<Long, AccountCategory> byId = new HashMap<>();
            for (AccountCategory category : categories) {
                byId.put(category.getId(), category);
                if (category.getPid() == 0) roots.putIfAbsent(XlsLedgerReader.key(category.getType(), category.getCategory()), category);
            }
            for (AccountCategory category : categories) {
                AccountCategory parent = byId.get(category.getPid());
                if (parent != null) children.putIfAbsent(XlsLedgerReader.key(category.getType(), parent.getCategory(), category.getCategory()), category);
            }
            for (Account account : LitePal.findAll(Account.class, true)) {
                AccountCategory category = account.getAccountCategory();
                AccountCategory parent = category == null ? null : byId.get(category.getPid());
                if (parent == null) continue;
                String signature = XlsLedgerReader.key(account.getDate(), account.getType(), parent.getCategory(), category.getCategory(), account.getAmount(), account.getRemark());
                existing.put(signature, existing.getOrDefault(signature, 0) + 1);
            }
        }
        Plan plan(XlsLedgerReader.Batch batch) {
            Set<String> newRoots = new HashSet<>(), newChildren = new HashSet<>();
            List<String> names = new ArrayList<>();
            Map<String, Integer> occurrences = new HashMap<>();
            int skipped = 0; long income = 0, expense = 0;
            for (XlsLedgerReader.Entry row : batch.entries) {
                String parentKey = XlsLedgerReader.key(row.type, row.parent);
                if (!roots.containsKey(parentKey)) newRoots.add(parentKey);
                if (!children.containsKey(row.categoryKey()) && newChildren.add(row.categoryKey()))
                    names.add((row.type == 0 ? "收入" : "支出") + " · " + row.parent + " / " + row.category);
                String signature = row.signature();
                int occurrence = occurrences.getOrDefault(signature, 0) + 1;
                occurrences.put(signature, occurrence);
                if (occurrence <= existing.getOrDefault(signature, 0)) skipped++;
                else if (row.type == 0) income += row.cents; else expense += row.cents;
            }
            return new Plan(batch.entries.size() - skipped, skipped, newRoots.size(), newChildren.size(), names, income, expense);
        }
    }
    public static final class Plan {
        public final int toImport, skipped, newRoots, newCategories;
        public final List<String> categoryNames;
        public final long incomeCents, expenseCents;
        Plan(int toImport, int skipped, int roots, int categories, List<String> names, long income, long expense) {
            this.toImport = toImport; this.skipped = skipped; newRoots = roots; newCategories = categories; categoryNames = names;
            incomeCents = income; expenseCents = expense;
        }
    }
    public static final class Result {
        public final int imported, skipped, rootsAdded, categoriesAdded;
        public final long incomeCents, expenseCents;
        Result(int imported, int skipped, int roots, int categories, long income, long expense) {
            this.imported = imported; this.skipped = skipped; rootsAdded = roots; categoriesAdded = categories;
            incomeCents = income; expenseCents = expense;
        }
    }
}
