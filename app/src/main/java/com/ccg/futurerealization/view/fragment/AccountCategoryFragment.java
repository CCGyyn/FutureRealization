package com.ccg.futurerealization.view.fragment;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
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

    private static final int GRID_NUM = 4;

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
        assert getArguments() != null;
        List<AccountCategory> list = getArguments().getParcelableArrayList(CATEGORY_LIST);
        AccountCategoryAdapter accountCategoryAdapter = new AccountCategoryAdapter();
        accountCategoryAdapter.setAccountCategoryList(list);
        mRecyclerView.setAdapter(accountCategoryAdapter);
        //mRecyclerView.addItemDecoration(new RecyclerItemDecoration(10, 10, GRID_NUM));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 原文链接：https://blog.csdn.net/qq_43983650/article/details/105076507
     */
    public class RecyclerItemDecoration extends RecyclerView.ItemDecoration {

        private int itemSpaceLeft;
        private int itemSpaceCenter;
        private int itemNum;

        public RecyclerItemDecoration(int itemSpaceLeft, int itemSpaceCenter, int itemNum) {
            this.itemSpaceLeft = itemSpaceLeft;
            this.itemSpaceCenter = itemSpaceCenter;
            this.itemNum = itemNum;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getResources().getDisplayMetrics().widthPixels;
            //int position = parent.getChildAdapterPosition(view);
            if (parent.getChildCount() > 0) {
                if (position % itemNum == 0) {                  //最左边Item
                    outRect.left = itemSpaceLeft;
                    outRect.right = itemSpaceCenter / 5;
                } else if (position % itemNum == itemNum - 1) { //最右边Item
                    outRect.left = itemSpaceCenter / 5;
                    outRect.right = itemSpaceLeft;
                } else {                                        //中间Item
                    outRect.left = itemSpaceCenter / 5;
                    outRect.right = itemSpaceCenter / 5;
                }
            }
        }
    }

}
