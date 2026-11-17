package com.ccg.futurerealization.utils;

import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AccountDetails {
    private static final Pattern IMPORT_TIME = Pattern.compile("\\[XLS导入\\][^\\n]*? 时间：([0-9]{2}:[0-9]{2}:[0-9]{2})(?:；|$)");
    private AccountDetails() { }
    public static boolean validTime(String time) {
        return time != null && time.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
    }
    public static String timeOf(Account account) {
        if (validTime(account.getTime())) return account.getTime();
        if (account.getRemark() != null) {
            Matcher match = IMPORT_TIME.matcher(account.getRemark());
            if (match.find() && validTime(match.group(1))) return match.group(1);
        }
        return null;
    }
    public static String dateTime(Account account) {
        String time = timeOf(account);
        return account.getDate() + "  " + (time == null ? "时间未记录" : time);
    }
    public static String category(Account account) {
        return account.getAccountCategory() == null ? "未分类" : CategoryNames.canonical(account.getAccountCategory().getCategory());
    }
    public static String group(Account account, Map<Long, AccountCategory> roots) {
        AccountCategory leaf = account.getAccountCategory();
        AccountCategory root = leaf == null ? null : roots.get(leaf.getPid());
        return root != null ? root.getCategory() : leaf != null ? CategoryNames.canonical(leaf.getCategory()) : "未分类";
    }
    public static final Comparator<Account> NEWEST_FIRST = (a, b) -> {
        int date = String.valueOf(b.getDate()).compareTo(String.valueOf(a.getDate()));
        if (date != 0) return date;
        String at = timeOf(a), bt = timeOf(b);
        int time = (bt == null ? "" : bt).compareTo(at == null ? "" : at);
        return time != 0 ? time : Long.compare(b.getId(), a.getId());
    };
}
