package com.ccg.futurerealization.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.AccountCategoryPageAdapter;
import com.ccg.futurerealization.adapter.ChatAdapter;
import com.ccg.futurerealization.base.EventBusFragment;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.contract.BookKeepingContract;
import com.ccg.futurerealization.event.BookKeepingEvent;
import com.ccg.futurerealization.event.ChatMsgDeleteEvent;
import com.ccg.futurerealization.pojo.ChatMsgEntity;
import com.ccg.futurerealization.pojo.ChatType;
import com.ccg.futurerealization.present.BookKeepingPresenter;
import com.ccg.futurerealization.utils.BookkeepingInput;
import com.ccg.futurerealization.view.activity.BackupActivity;
import com.ccg.futurerealization.view.activity.CalendarActivity;
import com.ccg.futurerealization.view.activity.ReportActivity;
import com.ccg.futurerealization.view.widget.DateTextView;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Offline chat-style ledger. Account is the source of truth; no schema migration is needed. */
public class BookKeepingFragment extends EventBusFragment implements BookKeepingContract.View {
    public static final String BUDDY_PREFS = "bookkeeping_buddy";
    private RecyclerView messages;
    private ChatAdapter chat;
    private EditText input;
    private Button send;
    private TextView categoryButton, remarkButton, buddyTitle, daySummary;
    private TextView incomeText, expenseText, balanceText;
    private DateTextView date;
    private View categoryPanel;
    private ViewPager categoryPager;
    private TabLayout categoryTabs;
    private BookKeepingContract.Present presenter;
    private final List<AccountCategory> categories = new ArrayList<>();
    private AccountCategory selected;
    private String remark = "";
    private boolean busy;
    private long deletingId;
    private SharedPreferences preferences;

    public static BookKeepingFragment newInstance(String param1, String param2) {
        return new BookKeepingFragment();
    }

    @Override protected void onCreateViewBefore(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        presenter = new BookKeepingPresenter(this, requireContext());
    }

    @Override protected int getContentViewId() { return R.layout.fragment_book_keeping; }

    @Override protected void onCreateViewInit(View root) {
        preferences = requireContext().getSharedPreferences(BUDDY_PREFS, Context.MODE_PRIVATE);
        messages = root.findViewById(R.id.chat_msg_list);
        chat = new ChatAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        manager.setStackFromEnd(true);
        messages.setLayoutManager(manager);
        messages.setAdapter(chat);
        input = root.findViewById(R.id.send_msg);
        send = root.findViewById(R.id.send_btn);
        categoryButton = root.findViewById(R.id.book_keep_type);
        remarkButton = root.findViewById(R.id.remark_msg);
        buddyTitle = root.findViewById(R.id.buddy_title);
        daySummary = root.findViewById(R.id.day_summary);
        incomeText = root.findViewById(R.id.month_income);
        expenseText = root.findViewById(R.id.month_expense);
        balanceText = root.findViewById(R.id.month_balance);
        date = root.findViewById(R.id.date_text);
        categoryPanel = root.findViewById(R.id.category_panel);
        categoryPager = root.findViewById(R.id.view_list);
        categoryTabs = root.findViewById(R.id.tab_layout);
        categories.clear();
        selected = null;
        busy = false;
        updateBuddyTitle();
        buddyTitle.setOnClickListener(v -> showBuddySettings());
        root.findViewById(R.id.backup_btn).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BackupActivity.class)));
        root.findViewById(R.id.calendar_img_btn).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CalendarActivity.class)));
        root.findViewById(R.id.report_img_btn).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ReportActivity.class)));
        categoryButton.setOnClickListener(v -> {
            if (busy) return;
            hideKeyboard();
            categoryPanel.setVisibility(categoryPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        input.setOnFocusChangeListener((v, focused) -> {
            if (focused) categoryPanel.setVisibility(View.GONE);
        });
        input.setOnClickListener(v -> categoryPanel.setVisibility(View.GONE));
        remarkButton.setOnClickListener(v -> showRemark());
        send.setOnClickListener(v -> sendEntry());
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendEntry(); return true; }
            return false;
        });
        date.setListener((picker, year, month, day) -> {
            presenter.queryDayAccounts(date.getText().toString());
        });
        presenter.queryAccountCategory();
    }

    @Override public void onResume() {
        super.onResume();
        if (presenter != null && date != null && !busy) {
            updateBuddyTitle();
            if (!categories.isEmpty()) presenter.queryAccountCategory();
            presenter.queryCurrentMonthAccount();
            presenter.queryDayAccounts(date.getText().toString());
        }
    }

    private void sendEntry() {
        if (busy) return;
        if (selected == null) { toast("分类正在加载，请稍后再记"); return; }
        final BookkeepingInput.Entry entry;
        try {
            entry = BookkeepingInput.parse(input.getText().toString());
        } catch (IllegalArgumentException e) {
            input.setError(e.getMessage());
            return;
        }
        AccountCategory inferred = inferCategory(entry.description);
        AccountCategory chosen = inferred == null ? selected : inferred;
        Account account = new Account();
        account.setDate(date.getText().toString());
        account.setType(chosen.getType());
        account.setAccountCategory(chosen);
        account.setAmount(entry.cents);
        account.setRemark(entry.description.isEmpty() ? remark : entry.description +
                (remark.isEmpty() ? "" : " · " + remark));
        if (!entry.description.isEmpty()) {
            new AlertDialog.Builder(requireContext()).setTitle("确认这笔账")
                    .setMessage(formatAccount(account) + (inferred == null ? "\n\n未识别分类，使用当前选择。分类不对可取消后重选。" : "\n\n已按文字匹配分类，请确认。"))
                    .setNegativeButton("再改改", null)
                    .setPositiveButton("确认记账", (dialog, which) -> save(account)).show();
        } else {
            save(account);
        }
    }

    private AccountCategory inferCategory(String description) {
        if (description.isEmpty()) return null;
        String normalized = description.replace("午餐", "中餐").replace("早饭", "早餐")
                .replace("午饭", "中餐").replace("晚饭", "晚餐").replace("薪水", "工资");
        AccountCategory match = null;
        for (AccountCategory category : categories) {
            if (normalized.contains(category.getCategory())) {
                if (match != null) return null; // Ambiguous descriptions keep the explicitly selected category.
                match = category;
            }
        }
        return match;
    }

    private void save(Account account) {
        if (busy) return;
        setBusy(true);
        hideKeyboard();
        categoryPanel.setVisibility(View.GONE);
        presenter.addAccount(account);
    }

    private void setBusy(boolean value) {
        busy = value;
        send.setEnabled(!value);
        input.setEnabled(!value);
        date.setEnabled(!value);
        categoryButton.setEnabled(!value);
        remarkButton.setEnabled(!value);
        send.setText(value ? "保存中" : "记一笔");
    }

    private void showRemark() {
        if (busy) return;
        EditText edit = new EditText(requireContext());
        edit.setText(remark);
        edit.setHint("这笔钱背后的小故事（可选）");
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        new AlertDialog.Builder(requireContext()).setTitle("账单备注").setView(edit)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存备注", (d, w) -> {
                    remark = edit.getText().toString().trim();
                    remarkButton.setText(remark.isEmpty() ? "备注" : "备注 ✓");
                }).show();
    }

    private void showBuddySettings() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, 0, padding, 0);
        EditText name = new EditText(requireContext());
        name.setSingleLine(true);
        name.setHint("搭子的名字");
        name.setText(buddyName());
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        layout.addView(name);
        TextView note = new TextView(requireContext());
        note.setText("只在本地陪你记账，不联网聊天。\n选一种你喜欢的回应语气：");
        note.setPadding(0, padding / 2, 0, padding / 2);
        layout.addView(note);
        android.widget.RadioGroup styles = new android.widget.RadioGroup(requireContext());
        String[] labels = {"温柔鼓励", "元气搭子", "简洁理性"};
        int current = preferences.getInt("style", 0);
        for (int i = 0; i < labels.length; i++) {
            android.widget.RadioButton button = new android.widget.RadioButton(requireContext());
            button.setId(100 + i);
            button.setText(labels[i]);
            styles.addView(button);
        }
        styles.check(100 + current);
        layout.addView(styles);
        new AlertDialog.Builder(requireContext()).setTitle("我的记账搭子").setView(layout)
                .setNegativeButton("取消", null).setPositiveButton("就这样", (d, w) -> {
                    String value = name.getText().toString().trim();
                    preferences.edit().putString("name", value.isEmpty() ? "小伴" : value)
                            .putInt("style", styles.getCheckedRadioButtonId() - 100).apply();
                    updateBuddyTitle();
                    presenter.queryDayAccounts(date.getText().toString());
                }).show();
    }

    private String buddyName() { return preferences.getString("name", "小伴"); }
    private void updateBuddyTitle() { buddyTitle.setText(buddyName() + " · 陪你记账  ›"); }

    private String companionReply(Account account) {
        String amount = "¥" + BookkeepingInput.format(account.getAmount());
        int style = preferences.getInt("style", 0);
        if (style == 2) return "已保存" + (account.getType() == 0 ? "收入 " : "支出 ") + amount + "。长按账单可删除。";
        if (account.getType() == 0) return style == 1 ? "叮！" + amount + " 进账啦，一起攒下小目标！" : "这笔 " + amount + " 收入记好啦。每一份努力都值得被记录。";
        String category = account.getAccountCategory().getCategory();
        if (category.contains("餐") || category.contains("早餐")) return style == 1 ? "吃饱才有力气追梦！这顿 " + amount + " 我记下啦。" : "这顿 " + amount + " 记好了，忙碌的时候也要好好吃饭。";
        return style == 1 ? "收到！" + amount + " 已记好，今天又认真生活了一点点。" : "已经记下 " + amount + "。慢慢记录，钱花在哪里就更清楚了。";
    }

    private static String formatAccount(Account account) {
        String category = account.getAccountCategory() == null ? "未分类" : account.getAccountCategory().getCategory();
        return (account.getType() == 0 ? "收入" : "支出") + " · " + category + "\n" +
                (account.getType() == 0 ? "+" : "−") + " ¥" + BookkeepingInput.format(account.getAmount()) +
                "\n" + account.getDate() +
                (account.getRemark() == null || account.getRemark().isEmpty() ? "" : "\n" + account.getRemark());
    }

    private ChatMsgEntity message(String text, ChatType type, Long accountId) {
        ChatMsgEntity msg = new ChatMsgEntity();
        msg.setMsg(text);
        msg.setChatType(type);
        msg.setAccountId(accountId);
        msg.setDate(new Date(System.currentTimeMillis()));
        msg.setTime(new Time(System.currentTimeMillis()));
        return msg;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setAccountCategoryId(BookKeepingEvent event) {
        if (busy || categoryButton == null) return;
        selectCategory(event.getAccountCategory());
        categoryPanel.setVisibility(View.GONE);
        input.requestFocus();
    }

    private void selectCategory(AccountCategory category) {
        selected = category;
        categoryButton.setText((category.getType() == 0 ? "收 · " : "支 · ") + category.getCategory() + " ▾");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deleteAccountMsg(ChatMsgDeleteEvent event) {
        if (busy || presenter == null) return;
        ChatMsgEntity msg = event.getChatMsgEntity();
        if (msg.getAccountId() == null) return;
        new AlertDialog.Builder(requireContext()).setTitle("删除这笔账？")
                .setMessage(msg.getMsg()).setNegativeButton("保留", null)
                .setPositiveButton("删除", (d, w) -> {
                    deletingId = msg.getAccountId();
                    setBusy(true);
                    presenter.deleteAccount(deletingId);
                }).show();
    }

    @Override public void loadAccountCategoryData(List<AccountCategory> list) {
        List<AccountCategory> titles = new ArrayList<>();
        Map<Long, List<AccountCategory>> map = new LinkedHashMap<>();
        for (AccountCategory category : list) {
            if (category.getPid() == 0) titles.add(category);
            else {
                List<AccountCategory> children = map.get(category.getPid());
                if (children == null) { children = new ArrayList<>(); map.put(category.getPid(), children); }
                children.add(category);
            }
        }
        loadAccountCategoryData(titles, map);
    }

    @Override public void loadAccountCategoryData(List<AccountCategory> titles, Map<Long, List<AccountCategory>> map) {
        categories.clear();
        List<Fragment> fragments = new ArrayList<>();
        List<AccountCategory> nonempty = new ArrayList<>();
        for (AccountCategory title : titles) {
            List<AccountCategory> children = map.get(title.getId());
            if (children == null || children.isEmpty()) continue;
            nonempty.add(title);
            categories.addAll(children);
            fragments.add(AccountCategoryFragment.newInstance(title.getId(), new ArrayList<>(children)));
        }
        categoryPager.setAdapter(new AccountCategoryPageAdapter(getChildFragmentManager(), nonempty, fragments));
        categoryTabs.setupWithViewPager(categoryPager);
        if (!categories.isEmpty()) {
            AccountCategory next = categories.get(0);
            for (AccountCategory category : categories) {
                if (selected != null && category.getId().equals(selected.getId())) next = category;
            }
            selectCategory(next);
        }
        else toast("暂无可用分类，请重新打开页面");
    }

    @Override public void addAccountState(Boolean inserted, Account account) {
        setBusy(false);
        if (!Boolean.TRUE.equals(inserted)) { toast("保存失败，输入内容已保留，请重试"); return; }
        input.setText("");
        selectCategory(account.getAccountCategory());
        remark = "";
        remarkButton.setText("备注");
        presenter.queryCurrentMonthAccount();
        presenter.queryDayAccounts(date.getText().toString());
    }

    @Override public void loadCurrentMonthAccountData(List<Account> accounts) {
        long income = 0, expense = 0;
        for (Account account : accounts) {
            if (account.getType() == 0) income += account.getAmount();
            else expense += account.getAmount();
        }
        incomeText.setText("¥" + BookkeepingInput.format(income));
        expenseText.setText("¥" + BookkeepingInput.format(expense));
        balanceText.setText("¥" + BookkeepingInput.format(income - expense));
    }

    @Override public void loadDayAccounts(String selectedDate, List<Account> accounts) {
        if (!selectedDate.equals(date.getText().toString())) return;
        List<ChatMsgEntity> list = new ArrayList<>();
        list.add(message("我是" + buddyName() + "，今天也陪你认真生活。\n选分类输入金额，或试试“午餐25元”。", ChatType.RECEIVE_MSG, null));
        long income = 0, expense = 0;
        for (Account account : accounts) {
            list.add(message(formatAccount(account), ChatType.SEND_MSG, account.getId()));
            list.add(message(companionReply(account), ChatType.RECEIVE_MSG, null));
            if (account.getType() == 0) income += account.getAmount(); else expense += account.getAmount();
        }
        daySummary.setText(accounts.isEmpty() ? "这天还没有账单，记下第一笔吧" : accounts.size() + " 笔 · 收入 " + BookkeepingInput.format(income) + " · 支出 " + BookkeepingInput.format(expense));
        chat.setChatMsgs(list);
        messages.scrollToPosition(list.size() - 1);
    }

    @Override public void loadDeleteAccountState(Integer deleted) {
        setBusy(false);
        if (deleted > 0) {
            presenter.queryCurrentMonthAccount();
            presenter.queryDayAccounts(date.getText().toString());
            toast("账单已删除");
        } else toast("删除失败，账单仍保留，请重试");
    }

    @Override public void showBookkeepingError(String message) { toast(message); }
    private void toast(String text) { if (getContext() != null) Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show(); }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        input.clearFocus();
    }

    @Override public void onDestroyView() {
        presenter.destroy();
        presenter = null;
        messages.setAdapter(null);
        date = null;
        super.onDestroyView();
    }
}
