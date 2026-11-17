package com.ccg.futurerealization.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.db.AccountManagerImpl;
import com.ccg.futurerealization.utils.AccountDetails;
import com.ccg.futurerealization.utils.BookkeepingInput;
import com.ccg.futurerealization.utils.CategoryNames;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/** Month/category drill-down: recycled rows show original recorded time and exact cents. */
public class TransactionListActivity extends AppCompatActivity {
    private Disposable load;
    private TextView summary;
    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_transactions);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String month = getIntent().getStringExtra("month"), group = getIntent().getStringExtra("group"), category = getIntent().getStringExtra("category");
        int type = getIntent().getIntExtra("type", 1);
        summary = findViewById(R.id.transaction_summary);
        if (month == null || !month.matches("[0-9]{4}-[0-9]{2}") || group == null || category == null || (type != 0 && type != 1)) {
            summary.setText("无效的分类查询条件"); return;
        }
        setTitle(category + " · " + (type == 0 ? "收入" : "支出") + "明细");
        ((TextView) findViewById(R.id.transaction_path)).setText(month + "  /  " + group + "  /  " + category);
        RecyclerView list = findViewById(R.id.transaction_list); list.setLayoutManager(new LinearLayoutManager(this));
        load = Single.fromCallable(() -> {
            synchronized (LitePalSupport.class) {
                Map<Long, AccountCategory> roots = new HashMap<>();
                for (AccountCategory root : LitePal.where("pid = ?", "0").find(AccountCategory.class)) roots.put(root.getId(), root);
                List<Account> result = new ArrayList<>();
                for (Account account : AccountManagerImpl.getInstance().queryAccountByDate(month)) {
                    if (Integer.valueOf(type).equals(account.getType()) && group.equals(AccountDetails.group(account, roots)) &&
                            CategoryNames.canonical(category).equals(AccountDetails.category(account))) result.add(account);
                }
                result.sort(AccountDetails.NEWEST_FIRST); return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(accounts -> {
            long total = 0; for (Account account : accounts) total += account.getAmount();
            summary.setText("共 " + accounts.size() + " 笔 · " + (type == 0 ? "收入" : "支出") + " ¥" + BookkeepingInput.format(total));
            findViewById(R.id.transaction_empty).setVisibility(accounts.isEmpty() ? View.VISIBLE : View.GONE);
            list.setAdapter(new Records(accounts));
        }, error -> summary.setText("账单加载失败，请返回后重试"));
    }
    private final class Records extends RecyclerView.Adapter<RecordHolder> {
        final List<Account> accounts;
        Records(List<Account> accounts) { this.accounts = accounts; }
        @NonNull @Override public RecordHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            return new RecordHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull RecordHolder holder, int position) {
            Account account = accounts.get(position);
            holder.date.setText(AccountDetails.dateTime(account));
            holder.amount.setText((account.getType() == 0 ? "+" : "−") + " ¥" + BookkeepingInput.format(account.getAmount()));
            holder.amount.setTextColor(getResources().getColor(account.getType() == 0 ? R.color.ledger_income : R.color.ledger_accent));
            holder.remark.setText(account.getRemark() == null || account.getRemark().isEmpty() ? "无备注" : account.getRemark());
            holder.itemView.setOnClickListener(v -> new AlertDialog.Builder(TransactionListActivity.this)
                    .setTitle(AccountDetails.category(account)).setMessage(AccountDetails.dateTime(account) + "\n" + holder.amount.getText() +
                            (account.getRemark() == null || account.getRemark().isEmpty() ? "" : "\n\n" + account.getRemark()))
                    .setPositiveButton("关闭", null).show());
        }
        @Override public int getItemCount() { return accounts.size(); }
    }
    private static final class RecordHolder extends RecyclerView.ViewHolder {
        final TextView date, amount, remark;
        RecordHolder(View view) { super(view); date = view.findViewById(R.id.transaction_time); amount = view.findViewById(R.id.transaction_amount); remark = view.findViewById(R.id.transaction_remark); }
    }
    @Override protected void onDestroy() { if (load != null) load.dispose(); super.onDestroy(); }
    @Override public boolean onOptionsItemSelected(MenuItem item) { if (item.getItemId() == android.R.id.home) { finish(); return true; } return super.onOptionsItemSelected(item); }
}
