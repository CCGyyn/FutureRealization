package com.ccg.futurerealization.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccg.futurerealization.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 直接写个继承textview来修改就好了,因为不是viewgroup,所以不用像RadioGroupButton进行View.inflate添加view
 *              用于二选一的条件
 * @Author: cgaopeng
 * @CreateDate: 22-1-18 下午4:32
 * @Version: 1.0
 */
public class AccountTypeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private static final String SPLICT_SYMBOL = "#";

    private Context mContext;

    private String mNameText;
    private String mCodeText;

    private Map<Integer, String> mMap;

    private Integer mCurrentCode = 0;

    /**
     * 代码里new一个View的时候，调用的是第一个构造函数
     * @param context
     */
    public AccountTypeTextView(@NonNull Context context) {
        //super(context);
        this(context, null);
    }

    /**
     * 在XML布局中调用的时候，调用的是第二个构造函数
     * 在XML布局中调用并且有自定义属性也是调用第二个构造函数
     * @param context
     * @param attrs
     */
    public AccountTypeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        //super(context, attrs);
        this(context, attrs, 0);
    }

    public AccountTypeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AccountTypeTextView);
        mNameText = ta.getString(R.styleable.AccountTypeTextView_nameText);
        mCodeText = ta.getString(R.styleable.AccountTypeTextView_codeText);
        initContentView();
        initView();
        initData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        int width = getWidth();
        int height = getBaseline();
        canvas.drawLine(0, height, width, height, paint);
    }

    private void initContentView() {
        if (mNameText == null && mCodeText == null) {
            mNameText = mContext.getResources().getString(R.string.account_type_view_text);
            mCodeText = mContext.getResources().getString(R.string.account_type_view_code);
        } else if ((mNameText == null && mCodeText != null)
                || (mNameText != null && mCodeText == null)) {
            throw new RuntimeException("data set error");
        }
        setClickable(true);
    }

    private void initView() {
        mMap = new HashMap<>();
        String[] names = mNameText.split(SPLICT_SYMBOL);
        String[] codes = mCodeText.split(SPLICT_SYMBOL);
        if (names.length != 2 && codes.length != 2) {
            throw new RuntimeException("data length error");
        }
        int i = 0;
        for (String code:codes
             ) {
            mMap.put(Integer.parseInt(code), names[i]);
            i++;
        }
        setText(names[0]);
        mCurrentCode = Integer.parseInt(codes[0]);
    }

    private void initData() {
        setOnClickListener(v -> {
            for (Integer key:mMap.keySet()
                 ) {
                String str = mMap.get(key);
                if (!getText().toString().equals(str)) {
                    mCurrentCode = key;
                    setText(str);
                    break;
                }
            }
        });
    }

    public Integer getAccountType() {
        return mCurrentCode;
    }

    public String getAccountName() {
        return mMap.get(mCurrentCode);
    }
}
