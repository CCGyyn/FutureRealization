package com.ccg.futurerealization.utils;

import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReportTotals {
    public long income, expense;
    private final Map<String, Long> incomes = new HashMap<>(), expenses = new HashMap<>();

    public ReportTotals(List<Account> accounts, Map<Long, AccountCategory> roots) {
        for (Account account : accounts) {
            long cents = account.getAmount() == null ? 0 : account.getAmount();
            boolean isIncome = Integer.valueOf(0).equals(account.getType());
            if (isIncome) income += cents; else expense += cents;
            AccountCategory leaf = account.getAccountCategory();
            AccountCategory root = leaf == null ? null : roots.get(leaf.getPid());
            String name = root != null ? root.getCategory() : leaf != null ? leaf.getCategory() : "未分类";
            Map<String, Long> map = isIncome ? incomes : expenses;
            Long previous = map.get(name);
            map.put(name, (previous == null ? 0 : previous) + cents);
        }
    }

    public List<Part> breakdown(boolean income) {
        List<Part> parts = new ArrayList<>();
        for (Map.Entry<String, Long> entry : (income ? incomes : expenses).entrySet()) parts.add(new Part(entry.getKey(), entry.getValue()));
        parts.sort((a, b) -> a.cents == b.cents ? a.name.compareTo(b.name) : Long.compare(b.cents, a.cents));
        return parts;
    }

    public static final class Part {
        public final String name;
        public final long cents;
        public Part(String name, long cents) { this.name = name; this.cents = cents; }
    }
}
