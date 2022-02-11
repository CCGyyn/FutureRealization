package com.ccg.futurerealization.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.Account;
import com.ccg.futurerealization.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-11 上午10:26
 * @Version: 1.0
 */
public class CalendarAccountInfoAdapter extends RecyclerView.Adapter<CalendarAccountInfoAdapter.AccountInfoViewHolder> {

    private List<Account> mAccountList = new ArrayList<>();

    @NonNull
    @Override
    public AccountInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_info, parent, false);
        return new AccountInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountInfoViewHolder holder, int position) {
        Account account = mAccountList.get(position);
        Integer amount = account.getAmount();
        BigDecimal money = Utils.convertIntegerToBigDecimal(amount);
        Integer type = account.getType();
        holder.mAccountName.setText(account.getAccountCategory().getCategory());
        String remark = account.getRemark();
        TextView remarkView = holder.mAccountRemark;
        if (remark != null && !"".equals(remark)) {
            remarkView.setText("\t" + remarkView.getContext().getString(R.string.item_account_remark_text)
                    + remark);
        } else {
            remarkView.setText("");
        }
        holder.mAccountAmount.setText(money.multiply(new BigDecimal(Math.pow(-1, type))).toString());
    }

    @Override
    public int getItemCount() {
        return mAccountList.size();
    }

    public void setAccountList(List<Account> accountList) {
        mAccountList.clear();
        mAccountList = accountList;
        notifyDataSetChanged();
    }

    public void clearData() {
        mAccountList.clear();
        notifyDataSetChanged();
    }

    class AccountInfoViewHolder extends RecyclerView.ViewHolder {

        private TextView mAccountName;
        private TextView mAccountRemark;
        private TextView mAccountAmount;

        public AccountInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            mAccountName = itemView.findViewById(R.id.account_name);
            mAccountRemark = itemView.findViewById(R.id.account_remark);
            mAccountAmount = itemView.findViewById(R.id.account_amount);
        }
    }
}
