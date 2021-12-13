package com.ccg.futurerealization.view.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.base.BaseActivity;
import com.ccg.futurerealization.bean.DoSth;
import com.ccg.futurerealization.view.widget.RadioGroupButton;

import de.mrapp.android.dialog.MaterialDialog;

@Deprecated
public class MsgShowDialogActivity extends Activity {

    public static final String DATA = "update_data";

    private DoSth mDoSth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showReadToActivateDialog();
    }

    private void showReadToActivateDialog() {
        Context context = this;

        mDoSth = getIntent().getParcelableExtra(DATA);

        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.update_dosth_dialog, null);
        RadioGroupButton rgType = linearLayout.findViewById(R.id.rg_item_group_type);
        RadioGroupButton rgState = linearLayout.findViewById(R.id.rg_item_group_state);
        TextView content = linearLayout.findViewById(R.id.future_content);

        rgType.setDefualtSelected(mDoSth.getType());
        rgState.setDefualtSelected(mDoSth.getState() ? 1 : 0);
        content.setText(mDoSth.getFuture_content());

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .setTitle(context.getString(R.string.update_msg_alerdialog_title))
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        MaterialDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}