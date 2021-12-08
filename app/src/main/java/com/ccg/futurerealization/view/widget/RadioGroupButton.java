package com.ccg.futurerealization.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.ccg.futurerealization.R;

/**
 * @Description:一次性同时配置radiobutton
 * @Author: cgaopeng
 * @CreateDate: 21-12-8 下午2:24
 * @Version: 1.0
 */
public class RadioGroupButton extends LinearLayout {

    RadioGroup mRg;

    private Context mContext;
    private OnGroupBtnClickListener mListener;

    /**
     * 选项的值
     */
    private String mGroupBtnCode = "1#2";
    /**
     * 选项显示的名
     */
    private String mGroupBtnName = "A#B";

    public RadioGroupButton(Context context) {
        this(context, null);
    }

    public RadioGroupButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioGroupButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GroupButtonView);
        mGroupBtnCode = ta.getString(R.styleable.GroupButtonView_groupBtnCodeText);
        mGroupBtnName = ta.getString(R.styleable.GroupButtonView_groupBtnNameText);
        ta.recycle();

        initContentView();
        initView();
        initData();
    }

    private void initContentView() {
        View.inflate(mContext, R.layout.view_item_radio_group, this);
        mRg = (RadioGroup) findViewById(R.id.rg_item_group_btn);
    }

    private void initView() {
        if (mGroupBtnName == null || mGroupBtnCode == null) {
            throw new RuntimeException("data null");
        }
        String[] groupCode = mGroupBtnCode.split("#");
        String[] groupName = mGroupBtnName.split("#");
        if (groupCode.length != groupName.length) {
            throw new RuntimeException("group not match");
        }
        int len = groupCode.length;
        for (int i = 0; i < len; i++) {
            RadioButton radioButton = (RadioButton) View.inflate(mContext, R.layout.view_item_radio_button, null);
            if (i == 0) {
                radioButton.setBackgroundResource(R.drawable.left_radio_button_selector);
                radioButton.setChecked(true);
            } else if (i == len - 1) {
                radioButton.setBackgroundResource(R.drawable.right_radio_button_selector);
            } else {
                radioButton.setBackgroundResource(R.drawable.mid_radio_button_selector);
            }
            radioButton.setId(i);
            radioButton.setTag(groupCode[i]);
            radioButton.setText(groupName[i]);
            mRg.addView(radioButton);
        }
    }

    private void initData() {
        mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int checkedId) {
                RadioButton rb = (RadioButton) findViewById(checkedId);
                if (rb.isChecked()) {
                    if (null != mListener) {
                        mListener.groupBtnClick(rb.getTag().toString());
                    }
                }
            }
        });
    }

    public interface OnGroupBtnClickListener {
        public void groupBtnClick(String code);
    }

    public void setOnGroupBtnClickListener(OnGroupBtnClickListener listener) {
        this.mListener = listener;
    }
}
