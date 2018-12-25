package com.easyui.uilib.bottombar;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.easyui.uilib.utils.DensityUtils;

class TabItem extends RelativeLayout {

    private Context context;
    private ImageView iconView;
    private Drawable normalIcon;
    private Drawable selectedIcon;
    private LayerDrawable compundIcon;
    private int iconSize;
    private int topMargin;
    private String title;
    private Paint textPaint;
    private int textSize;
    private Typeface typeFace;

    public TabItem(Context context) {
        super(context);
    }


    private void init(Context context) {
        this.context = context;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 3;
        setLayoutParams(lp);
        initPaint();
        initIconView();
    }

    private void initPaint() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(DensityUtils.sp2px(context, textSize));
        textPaint.setTypeface(typeFace);
    }

    private void initIconView() {
        iconView = new ImageView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(iconSize, iconSize);
        lp.addRule(title == null ? RelativeLayout.CENTER_IN_PARENT : RelativeLayout.CENTER_HORIZONTAL);
        if (title != null) {
            lp.topMargin = topMargin;
        }
        iconView.setScaleType(ImageView.ScaleType.FIT_XY);
        iconView.setLayoutParams(lp);
        addView(iconView);
        updateIcon();
    }

    private void updateIcon() {
        if (selectedIcon == null) {
            iconView.setImageDrawable(normalIcon);
        } else {
            compundIcon = new LayerDrawable(new Drawable[]{normalIcon, selectedIcon});
            normalIcon.setAlpha(255);
            selectedIcon.setAlpha(0);
            iconView.setImageDrawable(compundIcon);
        }
    }
}
