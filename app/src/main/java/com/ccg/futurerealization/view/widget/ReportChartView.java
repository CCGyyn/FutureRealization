package com.ccg.futurerealization.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.ccg.futurerealization.utils.ReportTotals;
import java.util.ArrayList;
import java.util.List;

/** Native chart: retains cent precision and completely resets on every month change. */
public class ReportChartView extends View {
    private static final int[] COLORS = {0xffa34c62, 0xff38786a, 0xffd19a48, 0xff7376a8, 0xff6693a4, 0xffb97c65, 0xff8f9271};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<ReportTotals.Part> parts = new ArrayList<>();
    private String label = "支出";

    public ReportChartView(Context context, AttributeSet attrs) { super(context, attrs); }
    public static int colorFor(String category) { return COLORS[Math.floorMod(category.hashCode(), COLORS.length)]; }
    public void setParts(List<ReportTotals.Part> values, String label) {
        parts = new ArrayList<>(values);
        this.label = label;
        setContentDescription(label + "分类占比，共" + parts.size() + "类");
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float stroke = 24 * density;
        float radius = Math.min(getWidth(), getHeight()) / 2f - stroke;
        if (radius <= 0) return;
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        RectF circle = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(0xffeee9e5);
        canvas.drawOval(circle, paint);
        long positiveTotal = 0;
        for (ReportTotals.Part part : parts) if (part.cents > 0) positiveTotal += part.cents;
        float start = -90;
        for (ReportTotals.Part part : parts) {
            if (part.cents <= 0 || positiveTotal == 0) continue;
            float sweep = (float) ((double) part.cents * 360 / positiveTotal);
            paint.setColor(colorFor(part.name));
            canvas.drawArc(circle, start, sweep, false, paint);
            start += sweep;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(0xff302f35);
        paint.setTextSize(20 * getResources().getDisplayMetrics().scaledDensity);
        canvas.drawText(positiveTotal == 0 ? "暂无数据" : label + "占比", cx, cy, paint);
        paint.setColor(0xff77747c);
        paint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
        canvas.drawText(parts.size() + " 个分类", cx, cy + 25 * density, paint);
    }
}
