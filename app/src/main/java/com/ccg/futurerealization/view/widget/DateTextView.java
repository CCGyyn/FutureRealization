package com.ccg.futurerealization.view.widget;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.Utils;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.Calendar;

import de.mrapp.android.dialog.MaterialDialog;

/**
 * @Description: 日期view
 * @Author: cgaopeng
 * @CreateDate: 22-2-11 下午2:54
 * @Version: 1.0
 */
public class DateTextView extends androidx.appcompat.widget.AppCompatTextView {

    private DateChangeListener mListener;

    private boolean isOnlyYM;

    public DateTextView(@NonNull Context context) {
        //super(context);
        this(context, null);
    }

    public DateTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        //super(context, attrs);
        this(context, attrs, 0);
    }

    public DateTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DateTextView);
        isOnlyYM = ta.getBoolean(R.styleable.DateTextView_only_show_year_month, false);
        LogUtils.v(isOnlyYM);
        initData();
        initAction();
    }

    private void initData() {
        Date date = new Date(System.currentTimeMillis());
        String dateTime = isOnlyYM ? Utils.getYearMonthStrByDate(date.toString()) : date.toString();
        setText(dateTime);
    }

    private void initAction() {
        setOnClickListener(v -> {
            if (isOnlyYM) {
                showOnlyYearMonth();
                return;
            }
            showDatePickerDialog();
        });
    }

    /**
     * 只显示yyyy-MM
     */
    private void showOnlyYearMonth() {
        LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), R.layout.datepick_dialog, null);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext())
                .setView(linearLayout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                });
        DatePicker datePicker = linearLayout.findViewById(R.id.date_picker);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(year).append("-").append(monthOfYear + 1);
                        setText(sb.toString());
                        if (null != mListener) {
                            mListener.onDateChange(view, year, monthOfYear, dayOfMonth);
                        }
                    }
                });
        //在xml中使用
        //datePicker.setCalendarViewShown(false);
        //datePicker.setSpinnersShown(true);
        hideDay(datePicker);
        MaterialDialog dialog = builder.create();
        dialog.show();
        Resources resources = getResources();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.material_blue_700));
    }

    /**
     * 显示yyyy-MM-dd
     */
    private void showDatePickerDialog() {
        final Calendar cd=Calendar.getInstance();
            java.util.Date date= new java.util.Date();
            cd.setTime(date);
            DatePickerDialog dp= new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener(){
                public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
                    setText(Utils.stringConvertSqlDate(sb.toString()).toString());
                    if (null != mListener) {
                        mListener.onDateChange(view, year, monthOfYear, dayOfMonth);
                    }
                }
            },
                    cd.get(Calendar.YEAR),
                    cd.get(Calendar.MONTH),
                    cd.get(Calendar.DAY_OF_MONTH));
            dp.show();
    }

    /**
     * 隐藏时间控件中的日期选择，只显示控件中的年、月
     *
     * @param mDatePicker
     */
    private void hideDay(DatePicker mDatePicker) {
        try {
            /* 处理android5.0以上的特殊情况 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
                if (daySpinnerId != 0) {
                    View daySpinner = mDatePicker.findViewById(daySpinnerId);
                    if (daySpinner != null) {
                        daySpinner.setVisibility(View.GONE);
                    }
                }
            } else {
                Field[] datePickerfFields = mDatePicker.getClass().getDeclaredFields();
                for (Field datePickerField : datePickerfFields) {
                    if ("mDaySpinner".equals(datePickerField.getName()) || ("mDayPicker").equals(datePickerField.getName())) {
                        datePickerField.setAccessible(true);
                        Object dayPicker = new Object();
                        try {
                            dayPicker = datePickerField.get(mDatePicker);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        ((View) dayPicker).setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(DateChangeListener listener) {
        mListener = listener;
    }

    public interface DateChangeListener {
        void onDateChange(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }
}
