package com.ccg.futurerealization.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.event.BookKeepingEvent;
import com.ccg.futurerealization.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-6 下午4:11
 * @Version: 1.0
 */
public class AccountCategoryAdapter extends RecyclerView.Adapter<AccountCategoryAdapter.AccountCategoryHoder> {

    private List<AccountCategory> mAccountCategoryList = new ArrayList<>();

    @NonNull
    @Override
    public AccountCategoryHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_category_item, parent, false);
        return new AccountCategoryHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountCategoryHoder holder, int position) {
        AccountCategory accountCategory = mAccountCategoryList.get(position);
        holder.setAccountCategoryText(accountCategory.getCategory());
        holder.setTextClickListen(v -> {
            EventBus.getDefault().post(BookKeepingEvent.getInstance(accountCategory));
        });
        LogUtils.v("AccountCategoryAdapter " + position + ", " + accountCategory.getCategory());
    }

    @Override
    public int getItemCount() {
        return mAccountCategoryList.size();
    }

    public void setAccountCategoryList(List<AccountCategory> accountCategoryList) {
        mAccountCategoryList = accountCategoryList;
        notifyDataSetChanged();
    }

    public class AccountCategoryHoder extends RecyclerView.ViewHolder {

        private TextView accountCategoryText;

        public AccountCategoryHoder(@NonNull View itemView) {
            super(itemView);
            accountCategoryText = itemView.findViewById(R.id.account_category);
        }

        public void setAccountCategoryText(String text) {
            accountCategoryText.setText(text);
        }

        public void setTextClickListen(View.OnClickListener onClickListener) {
            accountCategoryText.setOnClickListener(onClickListener);
        }
    }
}
