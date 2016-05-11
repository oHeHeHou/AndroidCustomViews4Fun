package com.caoyh.customviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caoyh
 * Created on 2016/4/25
 * Description:
 */
public class BarChartView extends View {

    private Paint mLabelPaint;
    private Paint mValuePaint;
    private Paint mBarPaint;

    private int mLabelColor;
    private int mLabelTextSize;
    private int mBarPrimaryColor;
    private int mBarSecondaryColor;
    private float mBarWidth;
    private float mBarSpace;
    private int mTopValueTextSize;
    private int mTopValueTextColor;
    private boolean mIsDrawYLabel;

    private float mBarStartX;

    private float mMaxValue = Float.MIN_VALUE;
    private float mMinValue = Float.MAX_VALUE;
    private float mYLabelMinValue;
    private float mYLabelMaxValue;

    private List<BarItem> itemList = new ArrayList<>();

    private static final int DEFAULT_BAR_WIDTH = 50;
    private static final int DEFAULT_BOTTOM_TEXT_SIZE = 22;
    private static final int DEFAULT_TOP_TEXT_SIZE = 28;
    private float mTopBaseLineY;
    private ValueAnimator mBarHeightAnimator;
    private static final long ANIM_DURATION = 2000;
    private float mBarHeightFactor;
    private boolean mAnimateY;
    private boolean mAnimateStart;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setStyle(Paint.Style.FILL);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BarChart);
        mLabelColor = a.getColor(R.styleable.BarChart_bottom_label_color, Color.parseColor("#B3B3B3"));
        mLabelTextSize = a.getDimensionPixelSize(R.styleable.BarChart_bottom_label_size, DEFAULT_BOTTOM_TEXT_SIZE);
        mBarPrimaryColor = a.getColor(R.styleable.BarChart_bar_primary_color, Color.parseColor("#FF8532"));
        mBarSecondaryColor = a.getColor(R.styleable.BarChart_bar_secondary_color, Color.parseColor("#ff6600"));
        mTopValueTextSize = a.getDimensionPixelSize(R.styleable.BarChart_top_value_size, DEFAULT_TOP_TEXT_SIZE);
        mTopValueTextColor = a.getColor(R.styleable.BarChart_top_value_color, Color.parseColor("#666666"));
        mBarWidth = a.getDimensionPixelSize(R.styleable.BarChart_bar_width, DEFAULT_BAR_WIDTH);
        mIsDrawYLabel = a.getBoolean(R.styleable.BarChart_show_y_label, false);
        mAnimateY = a.getBoolean(R.styleable.BarChart_animate, false);
        a.recycle();

        mBarPaint.setColor(mBarPrimaryColor);
        mLabelPaint.setTextSize(mLabelTextSize);
        mLabelPaint.setColor(mLabelColor);
        mValuePaint.setTextSize(mTopValueTextSize);
        mValuePaint.setColor(mTopValueTextColor);

        mBarHeightAnimator = ValueAnimator.ofFloat(0f, 1f);
        mBarHeightAnimator.setDuration(ANIM_DURATION);
        mBarHeightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                invalidate();
            }
        });
        mBarHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBarHeightFactor = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBarHeightAnimator.removeAllUpdateListeners();
        mBarHeightAnimator.cancel();
        mBarHeightAnimator = null;
    }

    public void animateY() {
        mAnimateStart = true;
        mBarHeightAnimator.start();
    }

    public void animateY(long duration) {
        mAnimateStart = true;
        mBarHeightAnimator.setDuration(duration);
        mBarHeightAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        cacBarWidth();
    }

    private void cacBarWidth() {
        if (hasData() && getWidth() > 0) {

            if (mIsDrawYLabel) {
                float barAreaWidth = getWidth() - getPaddingLeft() - getPaddingRight()
                        - 30 - 40 * 2 - mLabelPaint.measureText(mMaxValue + "");

                if (mBarWidth * itemList.size() > barAreaWidth) {
                    mBarWidth = barAreaWidth / itemList.size() * 0.8f;
                }
                mBarSpace = (barAreaWidth - mBarWidth * itemList.size()) / (itemList.size() - 1);

                mBarStartX = mLabelPaint.measureText(mMaxValue + "") + 30 + 40;

            } else {
                float barAreaWidth = getWidth() - getPaddingLeft() - getPaddingRight() - 80;
                if (mBarWidth * itemList.size() > barAreaWidth) {
                    mBarWidth = barAreaWidth / itemList.size() * 0.8f;
                }
                mBarSpace = (barAreaWidth - mBarWidth * itemList.size()) / (itemList.size() - 1);

                mBarStartX = 40;
            }
        }
    }

    private boolean hasData() {
        return itemList != null && !itemList.isEmpty();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawYBaseLine(canvas);
        drawYLabel(canvas);
        if (mAnimateY) {
            if (mAnimateStart) {
                drawBarWithAnimate(canvas);
            }
        } else {
            drawBar(canvas);
        }
    }

    private void drawYLabel(Canvas canvas) {
        float max = mMaxValue + mMaxValue / 20;
        float min = mMinValue - mMaxValue / 20;
        float valueBase = (max - min) / 5;
        mYLabelMinValue = min;
        float labelHeight = -mLabelPaint.ascent() + mLabelPaint.descent();
        float bottomBaseY = getHeight() - labelHeight - 20;
        float showValue = 0;
        for (int i = 0; i < 6; i++) {
            float bottomLineY = bottomBaseY - i * (bottomBaseY - labelHeight / 2 - 12) / 5;
            float centerTextY = bottomLineY - (-mLabelPaint.ascent() + mLabelPaint.descent()) / 2 - mLabelPaint.ascent();
            showValue = min + valueBase * i;
            DecimalFormat format = new DecimalFormat("##0.00");
            if (hasData() && mIsDrawYLabel) {
                canvas.drawText(format.format(showValue), getPaddingLeft(),
                        centerTextY, mLabelPaint);
            }
        }
        mYLabelMaxValue = showValue;
    }

    private void drawYBaseLine(Canvas canvas) {
        float labelHeight = -mLabelPaint.ascent() + mLabelPaint.descent();
        float bottomBaseY = getHeight() - labelHeight - 20;

        for (int i = 0; i < 6; i++) {
            //计算每个横线的位置
            float labelLineY = bottomBaseY - i * (bottomBaseY - labelHeight / 2 - 12) / 5;
            if (mIsDrawYLabel) {
                float leftSpace = mLabelPaint.measureText(mMaxValue + "") + 30;
                canvas.drawLine(getPaddingLeft() + leftSpace, labelLineY,
                        getWidth() - getPaddingRight(), labelLineY, mLabelPaint);
            } else {
                canvas.drawLine(getPaddingLeft(), labelLineY,
                        getWidth() - getPaddingRight(), labelLineY, mLabelPaint);
            }

            mTopBaseLineY = labelLineY;
        }
    }

    private void drawBarWithAnimate(Canvas canvas) {
        if (hasData()) {
            float labelHeight = -mLabelPaint.ascent() + mLabelPaint.descent();
            float bottomBaseY = getHeight() - labelHeight - 20;
            float startX = getPaddingLeft() + mBarStartX;
            //第一个柱子
            float firstCalcY = (bottomBaseY - mTopBaseLineY) * (mYLabelMaxValue - Float.valueOf(itemList.get(0).value))
                    / (mYLabelMaxValue - mYLabelMinValue);

            if (!TextUtils.isEmpty(itemList.get(0).factor)) {
                float factor = Float.valueOf(itemList.get(0).factor);
                if (mBarHeightFactor * 100f < 100 - factor) {
                    //primary部分
                    canvas.drawRect(startX,//
                            bottomBaseY - (bottomBaseY - (mTopBaseLineY + firstCalcY)) * (100 - factor) / 100f * mBarHeightFactor * 100 / (100 - factor),
                            startX + mBarWidth,
                            bottomBaseY,
                            mBarPaint);
                } else {
                    canvas.drawRect(startX,
                            bottomBaseY - (bottomBaseY - (mTopBaseLineY + firstCalcY)) * (100 - factor) / 100f,
                            startX + mBarWidth,
                            bottomBaseY,
                            mBarPaint);
                    mBarPaint.setColor(mBarSecondaryColor);
                    //secondary部分
                    float primaryTop = bottomBaseY - (bottomBaseY - (mTopBaseLineY + firstCalcY)) * (100 - factor) / 100f;
                    canvas.drawRect(startX,
                            primaryTop - (primaryTop - (mTopBaseLineY + firstCalcY)) * (100 - factor) / 100f * mBarHeightFactor * 100 / (100 - factor),
                            startX + mBarWidth,
                            primaryTop,
                            mBarPaint);
                    mBarPaint.setColor(mBarPrimaryColor);
                }
            } else {
                canvas.drawRect(startX,
                        mTopBaseLineY + firstCalcY + (bottomBaseY - (mTopBaseLineY + firstCalcY)) * (1 - mBarHeightFactor),
                        startX + mBarWidth, bottomBaseY, mBarPaint);
            }

            //下方文字
            float firstLabelWidth = mLabelPaint.measureText(itemList.get(0).label);
            canvas.drawText(itemList.get(0).label,
                    startX + mBarWidth / 2 - firstLabelWidth / 2,
                    bottomBaseY + 20 - mLabelPaint.ascent(),
                    mLabelPaint);
            //上方数字
            float fistValueLabelWidth = mValuePaint.measureText(itemList.get(0).value);
            if (mBarHeightFactor == 1) {
                canvas.drawText(itemList.get(0).value,
                        startX + mBarWidth / 2 - fistValueLabelWidth / 2,
                        mTopBaseLineY + firstCalcY - 12 - mValuePaint.descent(),
                        mValuePaint);
            }

            startX += mBarWidth;

            for (int i = 1; i < itemList.size(); i++) {
                float labelWidth = mLabelPaint.measureText(itemList.get(i).label);
                float valueLabelWidth = mValuePaint.measureText(itemList.get(i).value);
                //柱子
                float calcY = (bottomBaseY - mTopBaseLineY) * (mYLabelMaxValue - Float.valueOf(itemList.get(i).value))
                        / (mYLabelMaxValue - mYLabelMinValue);

                if (!TextUtils.isEmpty(itemList.get(i).factor)) {
                    float factor = Float.valueOf(itemList.get(i).factor);
                    //如果动画执行时间的比例<=primary部分所占高度的比例，则绘制primary部分，否则绘制secondary部分
                    if (mBarHeightFactor * 100f < 100 - factor) {
                        //primary部分
                        canvas.drawRect(startX + mBarSpace,//
                                bottomBaseY - (bottomBaseY - (mTopBaseLineY + calcY)) * (100 - factor) / 100f * mBarHeightFactor * 100 / (100 - factor),
                                startX + mBarSpace + mBarWidth,
                                bottomBaseY,
                                mBarPaint);
                    } else {
                        canvas.drawRect(startX + mBarSpace,
                                bottomBaseY - (bottomBaseY - (mTopBaseLineY + calcY)) * (100 - factor) / 100f,
                                startX + mBarSpace + mBarWidth,
                                bottomBaseY,
                                mBarPaint);
                        mBarPaint.setColor(mBarSecondaryColor);
                        //secondary部分
                        float primaryTop = bottomBaseY - (bottomBaseY - (mTopBaseLineY + calcY)) * (100 - factor) / 100f;
                        canvas.drawRect(startX + mBarSpace,
                                primaryTop - (primaryTop - (mTopBaseLineY + calcY)) * (100 - factor) / 100f * mBarHeightFactor * 100 / (100 - factor),
                                startX + mBarSpace + mBarWidth,
                                primaryTop,
                                mBarPaint);
                        mBarPaint.setColor(mBarPrimaryColor);
                    }
                } else {
                    canvas.drawRect(startX + mBarSpace,
                            mTopBaseLineY + calcY + (bottomBaseY - (mTopBaseLineY + calcY)) * (1 - mBarHeightFactor),
                            startX + mBarSpace + mBarWidth,
                            bottomBaseY,
                            mBarPaint);
                }
                //下方文字
                canvas.drawText(itemList.get(i).label,
                        startX + mBarSpace + mBarWidth / 2 - labelWidth / 2,
                        bottomBaseY + 20 - mLabelPaint.ascent(),
                        mLabelPaint);
                //上方数字
                if (mBarHeightFactor == 1) {
                    canvas.drawText(itemList.get(i).value,
                            startX + mBarSpace + mBarWidth / 2 - valueLabelWidth / 2,
                            mTopBaseLineY + calcY - 12 - mValuePaint.descent(),
                            mValuePaint);
                }

                startX += mBarSpace + mBarWidth;
            }
        }
    }

    private void drawBar(Canvas canvas) {
        if (hasData()) {
            float labelHeight = -mLabelPaint.ascent() + mLabelPaint.descent();
            float bottomBaseY = getHeight() - labelHeight - 20;
            float startX = getPaddingLeft() + mBarStartX;
            //第一个柱子
            float firstCalcY = (bottomBaseY - mTopBaseLineY) * (mYLabelMaxValue - Float.valueOf(itemList.get(0).value))
                    / (mYLabelMaxValue - mYLabelMinValue);

            if (!TextUtils.isEmpty(itemList.get(0).factor)) {
                float factor = Float.valueOf(itemList.get(0).factor);
                //primary部分
                canvas.drawRect(startX,
                        bottomBaseY - (bottomBaseY - (mTopBaseLineY + firstCalcY)) * (100 - factor) / 100f,
                        startX + mBarWidth,
                        bottomBaseY,
                        mBarPaint);
                mBarPaint.setColor(mBarSecondaryColor);
                //secondary部分
                canvas.drawRect(startX,
                        mTopBaseLineY + firstCalcY,
                        startX + mBarWidth,
                        bottomBaseY - (bottomBaseY - (mTopBaseLineY + firstCalcY)) * (100 - factor) / 100f,
                        mBarPaint);
                mBarPaint.setColor(mBarPrimaryColor);
            } else {
                canvas.drawRect(startX,
                        mTopBaseLineY + firstCalcY,
                        startX + mBarWidth, bottomBaseY, mBarPaint);
            }

            //下方文字
            float firstLabelWidth = mLabelPaint.measureText(itemList.get(0).label);
            canvas.drawText(itemList.get(0).label,
                    startX + mBarWidth / 2 - firstLabelWidth / 2,
                    bottomBaseY + 20 - mLabelPaint.ascent(),
                    mLabelPaint);
            //上方数字
            float fistValueLabelWidth = mValuePaint.measureText(itemList.get(0).value);

            canvas.drawText(itemList.get(0).value,
                    startX + mBarWidth / 2 - fistValueLabelWidth / 2,
                    mTopBaseLineY + firstCalcY - 12 - mValuePaint.descent(),
                    mValuePaint);

            startX += mBarWidth;

            for (int i = 1; i < itemList.size(); i++) {
                float labelWidth = mLabelPaint.measureText(itemList.get(i).label);
                float valueLabelWidth = mValuePaint.measureText(itemList.get(i).value);
                //柱子
                float calcY = (bottomBaseY - mTopBaseLineY) * (mYLabelMaxValue - Float.valueOf(itemList.get(i).value))
                        / (mYLabelMaxValue - mYLabelMinValue);

                if (!TextUtils.isEmpty(itemList.get(i).factor)) {
                    float factor = Float.valueOf(itemList.get(i).factor);
                    //primary部分
                    canvas.drawRect(startX + mBarSpace,
                            bottomBaseY - (bottomBaseY - (mTopBaseLineY + calcY)) * (100 - factor) / 100f,
                            startX + mBarSpace + mBarWidth,
                            bottomBaseY,
                            mBarPaint);
                    mBarPaint.setColor(mBarSecondaryColor);
                    //secondary部分
                    canvas.drawRect(startX + mBarSpace,
                            mTopBaseLineY + calcY,
                            startX + mBarSpace + mBarWidth,
                            bottomBaseY - (bottomBaseY - (mTopBaseLineY + calcY)) * (100 - factor) / 100f,
                            mBarPaint);
                    mBarPaint.setColor(mBarPrimaryColor);

                } else {
                    canvas.drawRect(startX + mBarSpace,
                            mTopBaseLineY + calcY,
                            startX + mBarSpace + mBarWidth,
                            bottomBaseY,
                            mBarPaint);
                }
                //下方文字
                canvas.drawText(itemList.get(i).label,
                        startX + mBarSpace + mBarWidth / 2 - labelWidth / 2,
                        bottomBaseY + 20 - mLabelPaint.ascent(),
                        mLabelPaint);
                //上方数字
                canvas.drawText(itemList.get(i).value,
                        startX + mBarSpace + mBarWidth / 2 - valueLabelWidth / 2,
                        mTopBaseLineY + calcY - 12 - mValuePaint.descent(),
                        mValuePaint);
                startX += mBarSpace + mBarWidth;
            }
        }
    }

    public static class BarItem {
        String label;
        String value;
        String factor;

        public BarItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public BarItem(String label, String value, String factor) {
            this.label = label;
            this.value = value;
            this.factor = factor;
        }
    }

    public BarChartView addBarItem(BarItem barItem) {
        itemList.add(barItem);
        float fValue;
        if (TextUtils.isEmpty(barItem.value)) {
            fValue = 0;
        } else {
            fValue = Float.valueOf(barItem.value);
        }

        if (fValue < mMinValue) {
            mMinValue = fValue;
        }
        if (fValue > mMaxValue) {
            mMaxValue = fValue;
        }
        return this;
    }

    public BarChartView initData(List<BarItem> barItemList) {
        itemList = barItemList;
        for (int i = 0; i < barItemList.size(); i++) {
            BarItem item = barItemList.get(i);
            float fValue = Float.valueOf(item.value);
            if (fValue < mMinValue) {
                mMinValue = fValue;
            }
            if (fValue > mMaxValue) {
                mMaxValue = fValue;
            }
        }
        return this;
    }

    public void draw() {
        requestLayout();
        invalidate();
    }

}
