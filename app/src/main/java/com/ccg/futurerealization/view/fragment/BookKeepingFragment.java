package com.ccg.futurerealization.view.fragment;

import android.content.Context;
import android.content.Intent;
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

/** Local ledger using the existing account schema. */
public class BookKeepingFragment extends EventBusFragment implements BookKeepingContract.View {
    private RecyclerView messages;
    private ChatAdapter chat;
    private EditText input;
    private Button send;
    private TextView categoryButton, remarkButton, daySummary;
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
    private View emptyView, addCategoryButton;
    private final List<AccountCategory> rootCategories = new ArrayList<>();
    private AlertDialog categoryDialog;
    private EditText categoryName;

    public static BookKeepingFragment newInstance(String param1, String param2) {
        return new BookKeepingFragment();
    }

    @Override protected void onCreateViewBefore(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        presenter = new BookKeepingPresenter(this, requireContext());
    }

    @Override protected int getContentViewId() { return R.layout.fragment_book_keeping; }

    @Override protected void onCreateViewInit(View root) {
        messages = root.findViewById(R.id.chat_msg_list);
        chat = new ChatAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        manager.setStackFromEnd(false);
        messages.setLayoutManager(manager);
        messages.setAdapter(chat);
        input = root.findViewById(R.id.send_msg);
        send = root.findViewById(R.id.send_btn);
        categoryButton = root.findViewById(R.id.book_keep_type);
        remarkButton = root.findViewById(R.id.remark_msg);
        emptyView = root.findViewById(R.id.ledger_empty);
        addCategoryButton = root.findViewById(R.id.add_category_btn);
        addCategoryButton.setOnClickListener(v -> showAddCategory());
        daySummary = root.findViewById(R.id.day_summary);
        incomeText = root.findViewById(R.id.month_income);
        expenseText = root.findViewById(R.id.month_expense);
        balanceText = root.findViewById(R.id.month_balance);
        date = root.findViewById(R.id.date_text);
        categoryPanel = root.findViewById(R.id.category_panel);
        categoryPager = root.findViewById(R.id.view_list);
        categoryTabs = root.findViewById(R.id.bookkeeping_category_tabs);
        categories.clear();
        selected = null;
        busy = false;
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
        account.setTime(new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.ROOT).format(new java.util.Date()));
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
        String normalized = description.replace("中餐", "午餐").replace("早饭", "早餐")
                .replace("午饭", "午餐").replace("晚饭", "晚餐").replace("薪水", "工资");
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
        addCategoryButton.setEnabled(!value);
        send.setText(value ? "保存中" : "记一笔");
    }

    private void showRemark() {
        if (busy) return;
        EditText edit = new EditText(requireContext());
        edit.setText(remark);
        edit.setHint("备注（可选）");
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        new AlertDialog.Builder(requireContext()).setTitle("账单备注").setView(edit)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存备注", (d, w) -> {
                    remark = edit.getText().toString().trim();
                    remarkButton.setText(remark.isEmpty() ? "备注" : "备注 ✓");
                }).show();
    }

    private void showAddCategory() {
        if (busy || rootCategories.isEmpty()) { toast("分类正在加载，请稍后再试"); return; }
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, 0, pad, 0);
        android.widget.Spinner group = new android.widget.Spinner(requireContext());
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < rootCategories.size(); i++) {
            AccountCategory root = rootCategories.get(i);
            labels.add((root.getType() == 0 ? "收入" : "支出") + " · " + root.getCategory());
            if (selected != null && selected.getPid().equals(root.getId())) index = i;
        }
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels);
        group.setId(R.id.category_editor_group);
        group.setAdapter(adapter);
        group.setSelection(index);
        layout.addView(group, new LinearLayout.LayoutParams(-1, (int) (52 * getResources().getDisplayMetrics().density)));
        categoryName = new EditText(requireContext());
        categoryName.setId(R.id.category_editor_text);
        categoryName.setHint("分类名称，例如 下午茶");
        categoryName.setSingleLine(true);
        categoryName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        layout.addView(categoryName);
        categoryDialog = new AlertDialog.Builder(requireContext()).setTitle("添加记账分类").setView(layout)
                .setNegativeButton("取消", null).setPositiveButton("添加", null).create();
        categoryDialog.setOnShowListener(d -> categoryDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = categoryName.getText().toString().trim();
            if (name.isEmpty()) { categoryName.setError("请输入分类名称"); return; }
            setBusy(true);
            categoryDialog.setCancelable(false);
            categoryDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            categoryDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            presenter.addCategory(rootCategories.get(group.getSelectedItemPosition()).getId(), name);
        }));
        categoryDialog.show();
    }

    @Override public void categoryAdded(AccountCategory category, String error) {
        setBusy(false);
        if (category != null) {
            if (categoryDialog != null) categoryDialog.dismiss();
            selectCategory(category);
            presenter.queryAccountCategory();
            toast("分类已添加");
        } else if (categoryDialog != null && categoryDialog.isShowing()) {
            categoryDialog.setCancelable(true);
            categoryDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            categoryDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
            categoryName.setError(error);
        }
    }

    private static String formatAccount(Account account) {
        String category = account.getAccountCategory() == null ? "未分类" : account.getAccountCategory().getCategory();
        return (account.getType() == 0 ? "收入" : "支出") + " · " + category + "\n" +
                (account.getType() == 0 ? "+" : "−") + " ¥" + BookkeepingInput.format(account.getAmount()) +
                "\n" + com.ccg.futurerealization.utils.AccountDetails.dateTime(account) +
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
                    setBusy(true);
                    presenter.deleteAccount(msg.getAccountId());
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
        rootCategories.clear();
        rootCategories.addAll(titles);
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
            for (int i = 0; i < nonempty.size(); i++) {
                if (nonempty.get(i).getId().equals(next.getPid())) categoryPager.setCurrentItem(i, false);
            }
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
        long income = 0, expense = 0;
        for (Account account : accounts) {
            list.add(message(formatAccount(account), ChatType.SEND_MSG, account.getId()));
            if (account.getType() == 0) income += account.getAmount(); else expense += account.getAmount();
        }
        daySummary.setVisibility(accounts.isEmpty() ? View.GONE : View.VISIBLE);
        daySummary.setText(accounts.isEmpty() ? "暂无账单" : accounts.size() + " 笔 · 收入 " + BookkeepingInput.format(income) + " · 支出 " + BookkeepingInput.format(expense));
        chat.setChatMsgs(list);
        emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        if (!list.isEmpty()) messages.scrollToPosition(list.size() - 1);
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
        if (categoryDialog != null) categoryDialog.dismiss();
        presenter.destroy();
        presenter = null;
        messages.setAdapter(null);
        date = null;
        super.onDestroyView();
    }
}
