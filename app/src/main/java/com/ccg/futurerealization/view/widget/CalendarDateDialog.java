package com.ccg.futurerealization.view.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.ccg.futurerealization.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Calendar with explicit single-selection and double-tap confirmation on the same day. */
final class CalendarDateDialog {
    interface Selection { void select(int year, int month, int day); }
    private final Context context;
    private final Calendar displayed = Calendar.getInstance(), selected = Calendar.getInstance();
    private final List<TextView> cells = new ArrayList<>();
    private final LinearLayout days;
    private final TextView heading;
    private final Selection listener;
    private final AlertDialog dialog;
    private String lastDay;
    private long lastTap;

    CalendarDateDialog(Context context, String initial, Selection listener) {
        this.context = context; this.listener = listener;
        try {
            String[] values = initial.split("-");
            selected.set(Integer.parseInt(values[0]), Integer.parseInt(values[1]) - 1, Integer.parseInt(values[2]));
        } catch (RuntimeException ignored) { selected.setTimeInMillis(System.currentTimeMillis()); }
        displayed.setTimeInMillis(selected.getTimeInMillis()); displayed.set(Calendar.DAY_OF_MONTH, 1);
        LinearLayout content = new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(16), 0, dp(16), dp(8));
        LinearLayout navigation = new LinearLayout(context); navigation.setGravity(Gravity.CENTER_VERTICAL);
        TextView previous = button("‹", 40), next = button("›", 40);
        heading = button("", 48); heading.setTextSize(17); heading.setTypeface(null, Typeface.BOLD);
        navigation.addView(previous, new LinearLayout.LayoutParams(dp(44), dp(48)));
        navigation.addView(heading, new LinearLayout.LayoutParams(0, dp(48), 1));
        navigation.addView(next, new LinearLayout.LayoutParams(dp(44), dp(48))); content.addView(navigation);
        previous.setContentDescription("上个月"); next.setContentDescription("下个月");
        previous.setOnClickListener(v -> { displayed.add(Calendar.MONTH, -1); render(); });
        next.setOnClickListener(v -> { displayed.add(Calendar.MONTH, 1); render(); });
        heading.setOnClickListener(v -> chooseMonth());
        LinearLayout weekdays = new LinearLayout(context);
        for (String name : new String[]{"一", "二", "三", "四", "五", "六", "日"}) {
            TextView label = button(name, 32); label.setTextSize(12); label.setTextColor(color(R.color.ledger_muted));
            weekdays.addView(label, new LinearLayout.LayoutParams(0, dp(32), 1));
        }
        content.addView(weekdays);
        days = new LinearLayout(context); days.setOrientation(LinearLayout.VERTICAL); content.addView(days);
        TextView hint = button("单击选中日期，双击直接确定", 36); hint.setTextSize(12); hint.setTextColor(color(R.color.ledger_muted)); content.addView(hint);
        dialog = new AlertDialog.Builder(context).setTitle("选择日期").setView(content)
                .setNegativeButton("取消", null).setNeutralButton("今天", (d, w) -> {
                    selected.setTimeInMillis(System.currentTimeMillis()); commit();
                }).setPositiveButton("确定", (d, w) -> commit()).create();
        render();
    }
    AlertDialog show() { dialog.show(); return dialog; }
    private void commit() {
        listener.select(selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH));
        dialog.dismiss();
    }
    private void render() {
        lastDay = null; lastTap = 0;
        heading.setText(String.format(Locale.CHINA, "%d年%02d月 ▾", displayed.get(Calendar.YEAR), displayed.get(Calendar.MONTH) + 1));
        days.removeAllViews(); cells.clear();
        int offset = (displayed.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int length = displayed.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int week = 0; week < 6; week++) {
            LinearLayout row = new LinearLayout(context);
            for (int column = 0; column < 7; column++) {
                int day = week * 7 + column - offset + 1;
                TextView cell = button(day > 0 && day <= length ? String.valueOf(day) : "", 42);
                row.addView(cell, new LinearLayout.LayoutParams(0, dp(42), 1));
                if (day < 1 || day > length) continue;
                String key = String.format(Locale.ROOT, "%04d-%02d-%02d", displayed.get(Calendar.YEAR), displayed.get(Calendar.MONTH) + 1, day);
                cell.setTag(key); cell.setContentDescription(key); cell.setClickable(true); cell.setFocusable(true);
                cell.setOnClickListener(v -> {
                    long now = SystemClock.uptimeMillis();
                    boolean doubleTap = key.equals(lastDay) && now - lastTap <= ViewConfiguration.getDoubleTapTimeout();
                    selected.set(displayed.get(Calendar.YEAR), displayed.get(Calendar.MONTH), day);
                    lastDay = key; lastTap = now; highlight();
                    if (doubleTap) commit();
                });
                cells.add(cell);
            }
            days.addView(row);
        }
        highlight();
    }
    private void highlight() {
        String key = String.format(Locale.ROOT, "%04d-%02d-%02d", selected.get(Calendar.YEAR), selected.get(Calendar.MONTH) + 1, selected.get(Calendar.DAY_OF_MONTH));
        for (TextView cell : cells) {
            boolean checked = key.equals(cell.getTag());
            cell.setSelected(checked);
            cell.setTextColor(color(checked ? R.color.white : R.color.ledger_text));
            GradientDrawable background = new GradientDrawable(); background.setCornerRadius(dp(12));
            background.setColor(checked ? color(R.color.ledger_accent) : android.graphics.Color.TRANSPARENT);
            cell.setBackground(background);
        }
    }
    private void chooseMonth() {
        LinearLayout row = new LinearLayout(context); row.setGravity(Gravity.CENTER);
        NumberPicker year = new NumberPicker(context), month = new NumberPicker(context);
        year.setMinValue(1900); year.setMaxValue(2200); year.setValue(displayed.get(Calendar.YEAR));
        month.setMinValue(1); month.setMaxValue(12); month.setValue(displayed.get(Calendar.MONTH) + 1);
        year.setWrapSelectorWheel(false); month.setWrapSelectorWheel(false); row.addView(year); row.addView(month);
        new AlertDialog.Builder(context).setTitle("选择年月").setView(row).setNegativeButton("取消", null)
                .setPositiveButton("查看", (d, w) -> { year.clearFocus(); month.clearFocus(); displayed.set(year.getValue(), month.getValue() - 1, 1); render(); }).show();
    }
    private TextView button(String text, int height) {
        TextView view = new TextView(context); view.setText(text); view.setGravity(Gravity.CENTER); view.setTextSize(16);
        view.setTextColor(color(R.color.ledger_accent)); view.setMinHeight(dp(height)); return view;
    }
    private int dp(int value) { return (int) (value * context.getResources().getDisplayMetrics().density); }
    private int color(int id) { return context.getResources().getColor(id); }
}
