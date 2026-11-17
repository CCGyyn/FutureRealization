package com.ccg.futurerealization.utils;

import com.ccg.futurerealization.bean.AccountCategory;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

public final class AccountCategoryStore {
    private AccountCategoryStore() { }

    public static AccountCategory add(long parentId, String value) {
        String name = value == null ? "" : value.trim();
        if (name.isEmpty() || name.length() > 20) throw new IllegalArgumentException("分类名称请输入1～20个字");
        synchronized (LitePalSupport.class) {
            AccountCategory parent = LitePal.find(AccountCategory.class, parentId);
            if (parent == null || parent.getPid() != 0) throw new IllegalArgumentException("请选择有效的分类分组");
            for (AccountCategory existing : LitePal.where("pid = ?", String.valueOf(parentId)).find(AccountCategory.class)) {
                if (name.equalsIgnoreCase(existing.getCategory().trim())) throw new IllegalArgumentException("这个分组下已有同名分类");
            }
            AccountCategory category = new AccountCategory();
            category.setPid(parentId);
            category.setCategory(name);
            category.setType(parent.getType());
            if (!category.save()) throw new IllegalStateException("分类保存失败");
            return category;
        }
    }
}
