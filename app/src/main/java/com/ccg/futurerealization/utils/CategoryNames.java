package com.ccg.futurerealization.utils;

import android.content.ContentValues;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import org.litepal.LitePal;
import java.util.List;

public final class CategoryNames {
    private CategoryNames() { }
    public static String canonical(String name) { return "中餐".equals(name) ? "午餐" : name; }

    /** Caller holds LitePal's monitor and transaction. Move associations before removing aliases. */
    public static void mergeLunchAliases() {
        List<AccountCategory> categories = LitePal.findAll(AccountCategory.class);
        for (AccountCategory old : categories) {
            if (old.getPid() == 0 || !"中餐".equals(old.getCategory())) continue;
            AccountCategory lunch = null;
            for (AccountCategory candidate : categories) {
                if (candidate.getPid().equals(old.getPid()) && candidate.getType().equals(old.getType()) && "午餐".equals(candidate.getCategory())) {
                    lunch = candidate; break;
                }
            }
            if (lunch == null) {
                ContentValues name = new ContentValues(); name.put("category", "午餐");
                if (LitePal.update(AccountCategory.class, name, old.getId()) != 1) throw new IllegalStateException("午餐分类更新失败");
                old.setCategory("午餐");
            } else {
                ContentValues link = new ContentValues(); link.put("accountcategory_id", lunch.getId());
                LitePal.updateAll(Account.class, link, "accountcategory_id = ?", String.valueOf(old.getId()));
                LitePal.delete(AccountCategory.class, old.getId());
            }
        }
    }
}
