package com.ccg.futurerealization.utils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local, deliberately limited parsing: one positive amount in yuan, at most two decimals. */
public final class BookkeepingInput {
    private static final Pattern AMOUNT = Pattern.compile("[0-9]+(?:\\.[0-9]+)?");

    private BookkeepingInput() { }

    public static Entry parse(String input) {
        String text = input == null ? "" : input.trim();
        Matcher matcher = AMOUNT.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("请输入金额，例如 25.50");
        }
        String number = matcher.group();
        int start = matcher.start();
        int end = matcher.end();
        String rest = text.substring(0, start) + text.substring(end);
        if (matcher.find() || rest.matches("(?s).*[-−+.,，。/].*")) {
            throw new IllegalArgumentException("一次只记一笔，请输入正数金额，不支持算式或千位分隔符");
        }
        if (!number.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("金额最多保留两位小数");
        }
        int cents;
        try {
            cents = new BigDecimal(number).movePointRight(2).intValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("单笔金额不能超过 21474836.47 元");
        }
        if (cents <= 0) {
            throw new IllegalArgumentException("金额必须大于 0");
        }
        String description = (text.substring(0, start) + text.substring(end))
                .replace("人民币", "").replace("元", "").replace("块钱", "")
                .replace("块", "").replace("¥", "").replace("￥", "").trim();
        return new Entry(cents, description);
    }

    public static String format(long cents) {
        return BigDecimal.valueOf(cents, 2).toPlainString();
    }

    public static final class Entry {
        public final int cents;
        public final String description;

        private Entry(int cents, String description) {
            this.cents = cents;
            this.description = description;
        }
    }
}
