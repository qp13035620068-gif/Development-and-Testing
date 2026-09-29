package com.example.tset;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuButtonView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<RectF> bars = new ArrayList<>();
    private final Random random = new Random();

    public MenuButtonView(Context context) {
        this(context, null);
    }

    public MenuButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        barPaint.setColor(Color.WHITE);
        barPaint.setStyle(Paint.Style.FILL);
        setClickable(true);
        setFocusable(true);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        buildBars(w, h);
    }

    private void buildBars(int w, int h) {
        bars.clear();
        if (w <= 0 || h <= 0) return;

        // ★ 左对齐：三条杠从同一个 left 出发，右端依次内缩
        float left = w * 0.22f;
        float maxRight = w * 0.86f;
        float maxWidth = maxRight - left;

        float len1 = maxWidth;                                        // 最长
        float len2 = maxWidth * (0.58f + random.nextFloat() * 0.12f); // 中等
        float len3 = maxWidth * (0.30f + random.nextFloat() * 0.12f); // 最短
        float[] lengths = new float[]{len1, len2, len3};

        // 极细：基准 3%
        float baseThickness = h * 0.030f;
        float[] thickness = new float[3];
        float sum = 0f;
        for (int i = 0; i < 3; i++) {
            thickness[i] = baseThickness * (0.70f + random.nextFloat() * 0.45f);
            sum += thickness[i];
        }

        float gap = h * 0.10f;
        float blockHeight = sum + gap * 2f;
        float maxBlock = h * 0.55f;
        if (blockHeight > maxBlock) {
            float scale = maxBlock / blockHeight;
            for (int i = 0; i < 3; i++) thickness[i] *= scale;
            gap *= scale;
            blockHeight = maxBlock;
        }

        // 垂直居中
        float top = (h - blockHeight) / 2f;
        for (int i = 0; i < 3; i++) {
            // ★ left 固定，右侧 = left + length → 左对齐
            bars.add(new RectF(left, top, left + lengths[i], top + thickness[i]));
            top += thickness[i] + gap;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF bar : bars) {
            float radius = bar.height() / 2f;
            canvas.drawRoundRect(bar, radius, radius, barPaint);
        }
    }
}
