package com.ccg.futurerealization.utils;

/** Conventional bands; compare the original ratio rather than a rounded percentage. */
public final class EngelLevel {
    private EngelLevel() { }
    public static String label(long foodCents, long expenseCents) {
        if (expenseCents <= 0) return "暂无支出";
        double ratio = (double) foodCents / expenseCents;
        if (ratio >= 0.6) return "贫穷";
        if (ratio >= 0.5) return "温饱";
        if (ratio >= 0.4) return "小康";
        if (ratio >= 0.3) return "富裕";
        return "最富裕";
    }
}
