package com.ccg.futurerealization.view.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.MsgAdapter;
import com.ccg.futurerealization.base.BaseFragment;
import com.ccg.futurerealization.bean.DoSth;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:主界面功能
 * @Author: cgaopeng
 * @CreateDate: 21-12-7 上午10:51
 * @Version: 1.0
 */
public class MainFragment extends BaseFragment {

    private static final String DATA_LIST = "data_list";

    private RecyclerView mRecyclerView;

    /**
     * 这种方式主要是防止类似竖屏切成横屏时 传过来数据丢失
     * @param list
     * @return
     */
    public static MainFragment newInstance(List<DoSth> list) {
        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(DATA_LIST, (ArrayList <? extends Parcelable>)list);
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    @Override
    protected int getOptionsMenuId() {
        return R.menu.main_fragment_menu;
    }

    @Override
    protected void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewId() {
        return R.layout.main_fragment;
    }

    @Override
    protected void onCreateViewInit(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.msg_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<DoSth> list = getArguments().getParcelableArrayList(DATA_LIST);
        MsgAdapter msgAdapter = new MsgAdapter();
        msgAdapter.setDoSthList(list);
        mRecyclerView.setAdapter(msgAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_sth:
                Toast.makeText(getContext(), "add sth", Toast.LENGTH_SHORT).show();
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
