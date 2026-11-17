package com.ccg.futurerealization.utils;

import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReportTotals {
    public long income, expense;
    private final List<Account> accounts;
    private final Map<Long, AccountCategory> roots;
    public ReportTotals(List<Account> accounts, Map<Long, AccountCategory> roots) {
        this.accounts = accounts; this.roots = roots;
        for (Account account : accounts) {
            long cents = account.getAmount() == null ? 0 : account.getAmount();
            if (Integer.valueOf(0).equals(account.getType())) income += cents; else expense += cents;
        }
    }
    public List<Part> breakdown(boolean income) { return breakdown(income, null); }
    public List<Part> breakdown(boolean income, String selectedGroup) {
        Map<String, Long> amounts = new HashMap<>(); Map<String, Integer> counts = new HashMap<>();
        for (Account account : accounts) {
            if (Integer.valueOf(0).equals(account.getType()) != income) continue;
            String group = AccountDetails.group(account, roots);
            if (selectedGroup != null && !selectedGroup.equals(group)) continue;
            String name = selectedGroup == null ? group : AccountDetails.category(account);
            amounts.put(name, amounts.getOrDefault(name, 0L) + (account.getAmount() == null ? 0 : account.getAmount()));
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }
        List<Part> parts = new ArrayList<>();
        for (Map.Entry<String, Long> entry : amounts.entrySet()) parts.add(new Part(entry.getKey(), entry.getValue(), counts.get(entry.getKey())));
        parts.sort((a, b) -> a.cents == b.cents ? a.name.compareTo(b.name) : Long.compare(b.cents, a.cents));
        return parts;
    }
    public static final class Part {
        public final String name;
        public final long cents;
        public final int count;
        public Part(String name, long cents) { this(name, cents, 0); }
        public Part(String name, long cents, int count) { this.name = name; this.cents = cents; this.count = count; }
    }
}
