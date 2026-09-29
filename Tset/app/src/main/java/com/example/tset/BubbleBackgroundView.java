package com.example.tset;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BubbleBackgroundView extends View {

    /** ★ 所有圆半径统一 8mm ≈ 30.24dp */
    private static final float RADIUS_DP = 30.24f;

    /**
     * ★ 蜂窝格距 = 直径 × 0.70 → 相邻圆心基准距离 0.70d
     *   抖动后最大距离 ≈ 0.70d × 1.35 = 0.945d < d → 数学上保证 0 空隙
     */
    private static final float CELL_FACTOR = 0.70f;

    /** 位置随机抖动幅度（±15% 格距），再大就会破坏"零空隙"保证 */
    private static final float JITTER = 0.30f;

    /** 模糊半径（dp） */
    private static final float BLUR_RADIUS_DP = 6.0f;

    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Bubble> bubbles = new ArrayList<>();
    private final Random random = new Random();

    public BubbleBackgroundView(Context context) {
        this(context, null);
    }

    public BubbleBackgroundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // BlurMaskFilter 必须走软件渲染
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        basePaint.setColor(Color.parseColor("#F4F7FA"));
        basePaint.setStyle(Paint.Style.FILL);

        shadowPaint.setColor(Color.parseColor("#262D4A66"));
        shadowPaint.setStyle(Paint.Style.FILL);

        bubblePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        buildBubbles(w, h);
    }

    private void buildBubbles(int w, int h) {
        bubbles.clear();
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float r = RADIUS_DP * density;                  // 统一半径
        float d = r * 2f;
        float cell = d * CELL_FACTOR;                   // 蜂窝格距
        float rowStep = cell * 0.8660254f;              // 行距 = cell × √3/2

        float blurPx = BLUR_RADIUS_DP * density;
        BlurMaskFilter blur = new BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL);
        shadowPaint.setMaskFilter(blur);
        bubblePaint.setMaskFilter(blur);

        // 向外多铺 4 圈，保证屏幕边缘也铺满
        int cols = (int) Math.ceil((double) w / cell) + 4;
        int rows = (int) Math.ceil((double) h / rowStep) + 4;

        for (int row = -2; row < rows; row++) {
            float baseY = row * rowStep;
            // ★ 奇数行整体右移半格 → 六边形（蜂窝）排列
            float xOffset = (row % 2 == 0) ? 0f : cell / 2f;

            for (int col = -2; col < cols; col++) {
                // 位置抖动幅度 ±15% 格距
                float cx = col * cell + xOffset
                         + (random.nextFloat() - 0.5f) * cell * JITTER;
                float cy = baseY
                         + (random.nextFloat() - 0.5f) * cell * JITTER;
                bubbles.add(new Bubble(cx, cy, r));
            }
        }

        // 打乱绘制顺序 → 谁压谁完全随机，视觉上不显网格
        Collections.shuffle(bubbles, random);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), basePaint);

        for (Bubble b : bubbles) {
            // 1) 阴影：偏右下 + 模糊 → 圆片浮起的层次感
            canvas.drawCircle(b.cx + b.r * 0.06f,
                              b.cy + b.r * 0.10f,
                              b.r, shadowPaint);

            // 2) 圆主体：中心纯白 → 55% 后淡出 → 边缘柔化
            int solid = Color.WHITE;
            int transparent = Color.argb(0, 255, 255, 255);
            RadialGradient gradient = new RadialGradient(
                    b.cx, b.cy, b.r,
                    new int[]{solid, solid, transparent},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP);
            bubblePaint.setShader(gradient);
            canvas.drawCircle(b.cx, b.cy, b.r, bubblePaint);
        }
        bubblePaint.setShader(null);
    }

    private static class Bubble {
        final float cx;
        final float cy;
        final float r;

        Bubble(float cx, float cy, float r) {
            this.cx = cx;
            this.cy = cy;
            this.r = r;
        }
    }
}
