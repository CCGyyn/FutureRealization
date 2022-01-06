package com.ccg.futurerealization.view.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.AccountCategoryAdapter;
import com.ccg.futurerealization.base.BaseFragment;
import com.ccg.futurerealization.bean.AccountCategory;
import com.ccg.futurerealization.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-1-6 下午3:50
 * @Version: 1.0
 */
public class AccountCategoryFragment extends BaseFragment {

    private static final int GRID_NUM = 5;

    private static final String PID = "pid";

    private static final String CATEGORY_LIST = "category_list";

    private RecyclerView mRecyclerView;

    public static AccountCategoryFragment newInstance(Long pid, List<AccountCategory> list) {

        Bundle args = new Bundle();
        AccountCategoryFragment fragment = new AccountCategoryFragment();
        args.putLong(PID, pid);
        args.putParcelableArrayList(CATEGORY_LIST, (ArrayList<? extends Parcelable>) list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewId() {
        return R.layout.account_category_fragment;
    }

    @Override
    protected void onCreateViewInit(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.account_category_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), GRID_NUM));
        List<AccountCategory> list = getArguments().getParcelableArrayList(CATEGORY_LIST);
        AccountCategoryAdapter accountCategoryAdapter = new AccountCategoryAdapter();
        accountCategoryAdapter.setAccountCategoryList(list);
        mRecyclerView.setAdapter(accountCategoryAdapter);
    }
}
