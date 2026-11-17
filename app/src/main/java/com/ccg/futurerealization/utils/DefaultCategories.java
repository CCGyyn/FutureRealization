package com.ccg.futurerealization.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.AccountCategory;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Adds missing bundled defaults on upgrades; existing categories and account links stay intact. */
public final class DefaultCategories {
    private static final int VERSION = 3;
    private DefaultCategories() { }

    public static void ensure(Context context) throws Exception {
        SharedPreferences preferences = context.getSharedPreferences("category_defaults", Context.MODE_PRIVATE);
        synchronized (LitePalSupport.class) {
            List<AccountCategory> existing = new ArrayList<>(LitePal.findAll(AccountCategory.class));
            if (!existing.isEmpty() && preferences.getInt("version", 0) >= VERSION) return;
            SQLiteDatabase db = LitePal.getDatabase();
            db.beginTransaction();
            try (InputStream input = context.getResources().openRawResource(R.raw.account_category)) {
                CategoryNames.mergeLunchAliases();
                existing = new ArrayList<>(LitePal.findAll(AccountCategory.class));
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(input, "UTF-8");
                long parentId = 0;
                int type = 1;
                for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                    if (event != XmlPullParser.START_TAG) continue;
                    String tag = parser.getName();
                    if (!"type_1".equals(tag) && !"type_2".equals(tag)) continue;
                    String name = parser.getAttributeValue(null, "name");
                    if (name == null) continue;
                    boolean root = "type_1".equals(tag);
                    if (root) type = "收".equals(name) ? 0 : 1;
                    long pid = root ? 0 : parentId;
                    AccountCategory found = null;
                    for (AccountCategory candidate : existing) {
                        if (candidate.getPid() == pid && candidate.getType() == type && name.equals(candidate.getCategory())) {
                            found = candidate; break;
                        }
                    }
                    if (found == null) {
                        found = new AccountCategory();
                        found.setCategory(name); found.setPid(pid); found.setType(type);
                        if (!found.save()) throw new IllegalStateException("默认分类初始化失败");
                        existing.add(found);
                    }
                    if (root) parentId = found.getId();
                }
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
            preferences.edit().putInt("version", VERSION).commit();
        }
    }
}
