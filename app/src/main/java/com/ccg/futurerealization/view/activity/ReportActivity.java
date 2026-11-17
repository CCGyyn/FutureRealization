package com.ccg.futurerealization.view.activity;

import android.os.Bundle;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.ReportContract;
import com.ccg.futurerealization.present.ReportPresent;
import com.ccg.futurerealization.utils.BookkeepingInput;
import com.ccg.futurerealization.utils.ReportTotals;
import com.ccg.futurerealization.view.widget.ReportChartView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity implements ReportContract.View {
    private ReportPresent presenter;
    private final Calendar month = Calendar.getInstance();
    private final Map<Long, AccountCategory> roots = new HashMap<>();
    private List<Account> accounts = new ArrayList<>();
    private boolean income, loading;
    private String loadError;
    private TextView monthText, status, incomeButton, expenseButton;
    private LinearLayout legend;
    private ReportChartView chart;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_report);
        setTitle("收支报表");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        month.set(Calendar.DAY_OF_MONTH, 1);
        if (state != null) { month.setTimeInMillis(state.getLong("month", month.getTimeInMillis())); income = state.getBoolean("income"); }
        presenter = new ReportPresent(this);
        monthText = findViewById(R.id.report_month);
        status = findViewById(R.id.report_status);
        legend = findViewById(R.id.report_legend);
        chart = findViewById(R.id.account_pie_chart);
        incomeButton = findViewById(R.id.report_income_tab);
        expenseButton = findViewById(R.id.report_expense_tab);
        monthText.setOnClickListener(v -> chooseMonth());
        findViewById(R.id.report_previous).setOnClickListener(v -> changeMonth(-1));
        findViewById(R.id.report_next).setOnClickListener(v -> changeMonth(1));
        findViewById(R.id.report_this_month).setOnClickListener(v -> {
            month.setTimeInMillis(System.currentTimeMillis()); month.set(Calendar.DAY_OF_MONTH, 1); loadMonth();
        });
        incomeButton.setOnClickListener(v -> { income = true; render(); });
        expenseButton.setOnClickListener(v -> { income = false; render(); });
        presenter.queryCategoryByPid(0L);
        loadMonth();
    }

    private String monthKey() { return String.format(Locale.ROOT, "%04d-%02d", month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1); }
    private void changeMonth(int amount) {
        month.add(Calendar.MONTH, amount);
        loadMonth();
    }
    private void loadMonth() {
        monthText.setText(String.format(Locale.CHINA, "%d年%02d月 ▾", month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1));
        accounts = new ArrayList<>();
        loading = true;
        loadError = null;
        render();
        presenter.queryAccountByMonth(monthKey());
    }

    private void chooseMonth() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        NumberPicker year = new NumberPicker(this), number = new NumberPicker(this);
        year.setMinValue(1900); year.setMaxValue(2200); year.setValue(month.get(Calendar.YEAR));
        number.setMinValue(1); number.setMaxValue(12); number.setValue(month.get(Calendar.MONTH) + 1);
        year.setWrapSelectorWheel(false); number.setWrapSelectorWheel(false);
        row.addView(year); row.addView(number);
        new AlertDialog.Builder(this).setTitle("选择年月").setView(row).setNegativeButton("取消", null)
                .setPositiveButton("查看", (d, w) -> {
                    year.clearFocus(); number.clearFocus();
                    month.set(year.getValue(), number.getValue() - 1, 1);
                    loadMonth();
                }).show();
    }

    @Override public void loadAccountDataByMonth(String key, List<Account> list) {
        if (!key.equals(monthKey())) return;
        accounts = list;
        loading = false;
        loadError = null;
        render();
    }
    @Override public void loadRootCategory(List<AccountCategory> list) {
        roots.clear();
        for (AccountCategory category : list) roots.put(category.getId(), category);
        render();
    }
    @Override public void reportError(String message) {
        loading = false;
        loadError = message;
        status.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void render() {
        ReportTotals totals = new ReportTotals(accounts, roots);
        ((TextView) findViewById(R.id.report_income)).setText("¥" + BookkeepingInput.format(totals.income));
        ((TextView) findViewById(R.id.report_expense)).setText("¥" + BookkeepingInput.format(totals.expense));
        ((TextView) findViewById(R.id.report_balance)).setText("¥" + BookkeepingInput.format(totals.income - totals.expense));
        status.setText(loadError != null ? loadError : loading ? "正在加载…" : accounts.isEmpty() ? "这个月暂无账单" : "本月共 " + accounts.size() + " 笔账单");
        incomeButton.setSelected(income);
        expenseButton.setSelected(!income);
        List<ReportTotals.Part> parts = totals.breakdown(income);
        chart.setParts(parts, income ? "收入" : "支出");
        legend.removeAllViews();
        long positiveTotal = 0;
        for (ReportTotals.Part part : parts) if (part.cents > 0) positiveTotal += part.cents;
        for (ReportTotals.Part part : parts) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(12), 0, dp(12));
            View dot = new View(this);
            GradientDrawable color = new GradientDrawable(); color.setShape(GradientDrawable.OVAL); color.setColor(ReportChartView.colorFor(part.name));
            dot.setBackground(color);
            row.addView(dot, new LinearLayout.LayoutParams(dp(9), dp(9)));
            TextView name = new TextView(this); name.setText(part.name); name.setTextSize(15); name.setTextColor(getResources().getColor(R.color.ledger_text)); name.setPadding(dp(12), 0, dp(8), 0);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            TextView amount = new TextView(this);
            amount.setText("¥" + BookkeepingInput.format(part.cents) + "  ·  " + String.format(Locale.CHINA, "%.1f%%", positiveTotal > 0 ? Math.max(0, part.cents) * 100d / positiveTotal : 0));
            amount.setTextSize(14); amount.setTextColor(getResources().getColor(R.color.ledger_muted));
            row.addView(amount);
            legend.addView(row);
        }
    }
    private int dp(int value) { return (int) (getResources().getDisplayMetrics().density * value); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putLong("month", month.getTimeInMillis()); out.putBoolean("income", income); super.onSaveInstanceState(out); }
    @Override protected void onDestroy() { presenter.destroy(); super.onDestroy(); }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
