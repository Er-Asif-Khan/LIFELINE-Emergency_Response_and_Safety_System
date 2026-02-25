package com.lifeline.safety.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.lifeline.safety.R;

public class CircularProgressView extends View {

    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint glowPaint;
    private RectF rectF;

    private float progress = 0f; // 0 to 100
    private int progressColor = 0xFFFF3B30; // Default red
    private int backgroundColor = 0x33FF3B30; // Default light red
    private float strokeWidth = 20f;

    private ValueAnimator progressAnimator;

    // ---------------- CONSTRUCTORS ----------------

    public CircularProgressView(Context context) {
        super(context);
        init(null);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    // ---------------- INIT ----------------

    private void init(@Nullable AttributeSet attrs) {

        // 🔥 Read XML attributes
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(
                    attrs,
                    R.styleable.CircularProgressView
            );

            progressColor = ta.getColor(
                    R.styleable.CircularProgressView_progressColor,
                    progressColor
            );

            backgroundColor = ta.getColor(
                    R.styleable.CircularProgressView_backgroundColor,
                    backgroundColor
            );

            ta.recycle();
        }

        // Progress paint
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Background paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);

        // Glow paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(progressColor);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(strokeWidth + 30);
        glowPaint.setAlpha(30);

        rectF = new RectF();
    }

    // ---------------- DRAW ----------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - (int) strokeWidth - 15;

        rectF.set(
                width / 2f - radius,
                height / 2f - radius,
                width / 2f + radius,
                height / 2f + radius
        );

        // Glow
        canvas.drawCircle(width / 2f, height / 2f, radius + 15, glowPaint);

        // Background circle
        canvas.drawCircle(width / 2f, height / 2f, radius, backgroundPaint);

        // Progress arc
        float sweepAngle = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    // ---------------- PROGRESS METHODS ----------------

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void animateProgress(float targetProgress, long duration) {
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }

        progressAnimator = ValueAnimator.ofFloat(progress, targetProgress);
        progressAnimator.setDuration(duration);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation ->
                setProgress((float) animation.getAnimatedValue())
        );
        progressAnimator.start();
    }

    // ---------------- STYLE METHODS ----------------

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        progressPaint.setStrokeWidth(width);
        backgroundPaint.setStrokeWidth(width);
        glowPaint.setStrokeWidth(width + 30);
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        glowPaint.setColor(color);
        invalidate();
    }

    public void setBackgroundCircleColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    // ---------------- CLEANUP ----------------

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (progressAnimator != null) progressAnimator.cancel();
    }
}
