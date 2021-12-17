package com.ccg.futurerealization.view.fragment;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ccg.futurerealization.Constant;
import com.ccg.futurerealization.R;
import com.ccg.futurerealization.adapter.MsgAdapter;
import com.ccg.futurerealization.base.BaseFragment;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.contract.MainFragmentContract;
import com.ccg.futurerealization.present.MainFragmentPresenter;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.ToastUtils;
import com.ccg.futurerealization.utils.Utils;
import com.ccg.futurerealization.view.widget.RadioGroupButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.mrapp.android.dialog.MaterialDialog;

/**
 * @Description:主界面功能
 * @Author: cgaopeng
 * @CreateDate: 21-12-7 上午10:51
 * @Version: 1.0
 *
 * @update: cgaopeng 21-12-17 添加下拉刷新
 */
public class MainFragment extends BaseFragment implements MainFragmentContract.View {

    private static final String DATA_LIST = "data_list";

    private RecyclerView mRecyclerView;

    private MaterialDialog.Builder mBuilder;

    private MainFragmentContract.Presenter mPresenter;

    private MsgAdapter mMsgAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (Constant.TEST_MAIN_MSG_ADD) {
            menu.add(0, R.id.test_menu_add, 1, R.string.msg_menu_add_test);
        }
    }

    @Override
    protected int getOptionsMenuId() {
        return R.menu.main_fragment_menu;
    }

    @Override
    protected void onCreateViewBefore(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPresenter = new MainFragmentPresenter(this);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.main_fragment;
    }

    @Override
    protected void onCreateViewInit(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swip_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LogUtils.v("onRefresh");
                mPresenter.queryDoSthData();
            }
        });
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.msg_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<DoSth> list = getArguments().getParcelableArrayList(DATA_LIST);
        mMsgAdapter = new MsgAdapter();
        mMsgAdapter.setListen(new MsgAdapter.Listen() {
            @Override
            public void updateItem(DoSth doSth, int position) {
                mPresenter.updateDoSth(doSth, position);
            }

            @Override
            public void deleteItemById(Long id, int position) {
                mPresenter.deleteDoSthById(id, position);
            }
        });
        mMsgAdapter.setDoSthList(list);
        mRecyclerView.setAdapter(mMsgAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_sth: {
                showAddMsgDialog();
                break;
            }
            case R.id.delete_all_sth: {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                        .setTitle(R.string.delete_all_msg_dialog_title)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            mPresenter.deleteAllDoSth();
                            dialog.dismiss();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        });
                MaterialDialog dialog = builder.create();
                dialog.show();
                Resources resources = getContext().getResources();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.material_blue_700));
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.material_blue_700));
                break;
            }
            case R.id.test_menu_add: {
                Random random = new Random();
                int r = 1 + random.nextInt(10);
                for (int i = 0; i < r; i++) {
                    DoSth doSth = new DoSth();
                    doSth.setFuture_content(Utils.getRandomString(random.nextInt(62) + 1));
                    doSth.setType(random.nextInt(3));
                    doSth.setState(random.nextBoolean());
                    mPresenter.addDoSth(doSth);
                }
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        mPresenter = null;
        mBuilder = null;
        super.onDestroy();
    }

    @Override
    public void addMsgSuccess(DoSth doSth) {
        //修复第二次添加时，更改还是用原来的数据，导致后来的修改都是基于第一次添加的
        mBuilder = null;
        mMsgAdapter.addNewMsgItem(doSth);
    }

    @Override
    public void refreshAllMsgItem(List<DoSth> list) {
        mMsgAdapter.setDoSthList(list);
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void refreshMsgItem(DoSth doSth, int position) {
        mMsgAdapter.updateMsgItem(doSth, position);
    }

    @Override
    public void deleteItem(int position) {
        mMsgAdapter.deleteMsgItem(position);
        ToastUtils.success(R.string.toast_delete_msg_item_success);
    }

    @Override
    public void actionFailed() {
        ToastUtils.warn(R.string.toast_action_msg_item_failed);
    }


    /**
     * 显示添加msg的dialog
     */
    private void showAddMsgDialog() {
        if (mBuilder == null) {
            DoSth doSth = new DoSth();
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.add_dosth_dialog, null);
            RadioGroupButton rgType = linearLayout.findViewById(R.id.rg_item_group_type);
            RadioGroupButton rgState = linearLayout.findViewById(R.id.rg_item_group_state);
            EditText content = linearLayout.findViewById(R.id.future_content);
            doSth.setType(0);
            doSth.setState(false);
            rgType.setOnGroupBtnClickListener(code -> {
                doSth.setType(Integer.valueOf(code));
            });
            rgState.setOnGroupBtnClickListener(code -> {
                doSth.setState(Integer.parseInt(code) == 1);
            });
            // https://github.com/michael-rapp/AndroidMaterialDialog
            mBuilder = new MaterialDialog.Builder(getContext())
                .setView(linearLayout)
                .setTitle(R.string.add_msg_alerdialog_title)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    LogUtils.d("test content = " + content.getEditableText().toString());
                    doSth.setFuture_content(content.getEditableText().toString());
                    mPresenter.addDoSth(doSth);
                    content.setText("");
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                });
        }
        MaterialDialog dialog = mBuilder.create();
        dialog.show();
        Resources resources = getContext().getResources();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.material_blue_700));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.material_blue_700));
    }

}
