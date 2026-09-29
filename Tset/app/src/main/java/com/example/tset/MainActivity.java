package com.example.tset;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tset.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final float PANEL_WIDTH_DP = 128f;
    private static final float SWIPE_THRESHOLD_DP = 60f;

    private ActivityMainBinding binding;

    private boolean menuOpen = false;
    private int panelWidthPx;
    private float swipeThresholdPx;
    private float downX;
    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final float density = getResources().getDisplayMetrics().density;
        panelWidthPx = Math.round(PANEL_WIDTH_DP * density);
        swipeThresholdPx = SWIPE_THRESHOLD_DP * density;

        ViewGroup.LayoutParams lp = binding.sideMenu.getLayoutParams();
        lp.width = panelWidthPx;
        binding.sideMenu.setLayoutParams(lp);
        binding.sideMenu.setTranslationX(-panelWidthPx);

        setupEmptyHint();

        binding.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu();
            }
        });

        binding.menuScrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });

        binding.menuItemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
                showSettingsContent();
            }
        });
    }

    // ----------------------------------------------------------------
    // 初始彩色提示文字
    // ----------------------------------------------------------------
    private void setupEmptyHint() {
        String text = "这个作者很懒，什么都没留下";
        int[] palette = new int[]{
                Color.parseColor("#E53935"),
                Color.parseColor("#FB8C00"),
                Color.parseColor("#FDD835"),
                Color.parseColor("#43A047"),
                Color.parseColor("#00ACC1"),
                Color.parseColor("#3949AB"),
                Color.parseColor("#8E24AA")
        };
        SpannableString ss = new SpannableString(text);
        for (int i = 0; i < text.length(); i++) {
            ss.setSpan(new ForegroundColorSpan(palette[i % palette.length]),
                    i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.emptyHint.setText(ss);
    }

    // ----------------------------------------------------------------
    // 设置内容
    // ----------------------------------------------------------------
    private void showSettingsContent() {
        final String[] items = {"颜色", "分辨率", "主题", "字体", "声音", "触觉", "多功能"};
        binding.functionList.removeAllViews();
        for (String item : items) {
            binding.functionList.addView(createFunctionRow(item));
        }
        binding.emptyHint.setVisibility(View.GONE);
        binding.functionContent.setVisibility(View.VISIBLE);
    }

private View createFunctionRow(String text) {
    float density = getResources().getDisplayMetrics().density;

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    int hMargin = (int) (14 * density);
    int vMargin = (int) (7 * density);
    lp.setMargins(hMargin, vMargin, hMargin, vMargin);
    row.setLayoutParams(lp);

    // ★ 换成白色圆角卡片
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.WHITE);
    bg.setCornerRadius(10 * density);
    row.setBackground(bg);
    row.setElevation(6 * density);

    // padding 正常值，不再为折角留多余空间
    int padH = (int) (20 * density);
    int padV = (int) (18 * density);
    row.setPadding(padH, padV, padH, padV);

    TextView tv = new TextView(this);
    tv.setText(text);
    tv.setTextSize(16f);
    tv.setTextColor(Color.parseColor("#FF1A1A1A"));
    tv.setIncludeFontPadding(false);
    tv.setMaxLines(1);

    row.addView(tv);
    return row;
}

    // ----------------------------------------------------------------
    // 侧栏开关
    // ----------------------------------------------------------------
    private void openMenu() {
        if (menuOpen) return;
        menuOpen = true;
        binding.menuScrim.setVisibility(View.VISIBLE);
        binding.sideMenu.setTranslationX(-panelWidthPx);
        binding.sideMenu.animate()
                .translationX(0f)
                .setDuration(260L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void closeMenu() {
        if (!menuOpen) return;
        menuOpen = false;
        binding.sideMenu.animate()
                .translationX(-panelWidthPx)
                .setDuration(220L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.menuScrim.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!menuOpen) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = ev.getX() - downX;
                    float dy = Math.abs(ev.getY() - downY);
                    if (dx > swipeThresholdPx && dx > dy * 1.5f) {
                        openMenu();
                    }
                    break;
                default:
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (menuOpen) {
            closeMenu();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
