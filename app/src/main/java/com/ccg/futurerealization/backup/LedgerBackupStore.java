package com.ccg.futurerealization.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.view.fragment.BookKeepingFragment;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** All DB work runs off the UI thread. Restoration never clears existing accounts. */
public final class LedgerBackupStore {
    private LedgerBackupStore() { }

    public static byte[] exportLedger(Context context) {
        LedgerBackup.Data data = new LedgerBackup.Data();
        // Match LitePal's lock order for multi-operation transactions.
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = LitePal.getDatabase();
            db.beginTransaction();
            try {
                for (AccountCategory category : LitePal.findAll(AccountCategory.class)) {
                    LedgerBackup.Category row = new LedgerBackup.Category();
                    row.id = category.getId();
                    row.parentId = category.getPid();
                    row.name = category.getCategory();
                    row.type = category.getType();
                    data.categories.add(row);
                }
                for (Account account : LitePal.findAll(Account.class, true)) {
                    if (account.getAccountCategory() == null) throw new IllegalArgumentException("存在未关联分类的账单，请先修复后备份");
                    LedgerBackup.Entry row = new LedgerBackup.Entry();
                    row.date = account.getDate();
                    row.type = account.getType();
                    row.amountCents = account.getAmount();
                    row.remark = account.getRemark();
                    row.categoryId = account.getAccountCategory().getId();
                    data.accounts.add(row);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
        SharedPreferences prefs = context.getSharedPreferences(BookKeepingFragment.BUDDY_PREFS, Context.MODE_PRIVATE);
        data.buddyName = prefs.getString("name", "小伴");
        data.buddyStyle = prefs.getInt("style", 0);
        return LedgerBackup.encode(data, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date()));
    }

    public static int restoreLedger(Context context, LedgerBackup backup) {
        LedgerBackup.validate(backup.data);
        // Match LitePal's lock order for multi-operation transactions.
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = LitePal.getDatabase();
            db.beginTransaction();
            try {
                if (LitePal.count(Account.class) != 0) throw new IllegalArgumentException("当前已有账单。为防止覆盖或重复导入，请在新手机的空记账本恢复。");
                Map<Long, AccountCategory> restored = new HashMap<>();
                List<AccountCategory> existing = LitePal.findAll(AccountCategory.class);
                for (int level = 0; level < 2; level++) {
                    for (LedgerBackup.Category row : backup.data.categories) {
                        if ((row.parentId == 0) != (level == 0)) continue;
                        long parentId = row.parentId == 0 ? 0 : restored.get(row.parentId).getId();
                        AccountCategory category = null;
                        for (AccountCategory candidate : existing) {
                            if (candidate.getPid() == parentId && candidate.getType() == row.type && row.name.equals(candidate.getCategory())) {
                                category = candidate;
                                break;
                            }
                        }
                        if (category == null) {
                            category = new AccountCategory();
                            category.setCategory(row.name);
                            category.setPid(parentId);
                            category.setType(row.type);
                            if (!category.save()) throw new IllegalStateException("分类恢复失败，已回滚");
                            existing.add(category);
                        }
                        restored.put(row.id, category);
                    }
                }
                for (LedgerBackup.Entry row : backup.data.accounts) {
                    Account account = new Account();
                    account.setDate(row.date);
                    account.setType(row.type);
                    account.setAmount(row.amountCents);
                    account.setRemark(row.remark == null ? "" : row.remark);
                    account.setAccountCategory(restored.get(row.categoryId));
                    if (!account.save()) throw new IllegalStateException("账单恢复失败，已回滚");
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
        context.getSharedPreferences(BookKeepingFragment.BUDDY_PREFS, Context.MODE_PRIVATE).edit()
                .putString("name", backup.data.buddyName).putInt("style", backup.data.buddyStyle).commit();
        return backup.data.accounts.size();
    }
}
