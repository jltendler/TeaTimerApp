package com.example.multibrewtimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleTimerView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF circleBounds;
    private float progress = 1.0f; // 0.0 to 1.0

    public CircleTimerView(Context context) {
        super(context);
        init();
    }

    public CircleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20f);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.parseColor("#FF6200EE")); // Purple 500
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        circleBounds = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = 20f;
        // Half Circle: We use the full width, but the height is technically the radius.
        // However, standard drawArc uses a bound that defines the full oval.
        // To draw a top half circle, we need the oval height to be 2x the view height roughly.
        // But for simplicity, we constrain to a square area and just draw the top half.
        
        circleBounds.set(padding, padding, w - padding, (h * 2) - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw Half Circle Background (180 to 0 degrees, clockwise)
        // Android drawArc: startAngle 180 (left), sweep 180.
        canvas.drawArc(circleBounds, 180, 180, false, backgroundPaint);
        
        // Draw progress arc
        // Progress defaults to 100% (full half circle 180 sweep)
        // As time ticks down, sweep reduces.
        float sweepAngle = 180 * progress;
        canvas.drawArc(circleBounds, 180, sweepAngle, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate(); // Redraw
    }

    public void setColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }
}
