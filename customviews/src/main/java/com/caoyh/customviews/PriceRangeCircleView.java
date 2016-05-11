package com.caoyh.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by caoyh
 * Created on 2016/4/20
 * Description:
 */
public class PriceRangeCircleView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private static final int DEFAULT_RADIUS = 75; //75px,setStrokeWidth=15
    private static final int DEFAULT_PERCENT_SIZE = 44; //44px
    private static final int DEFAULT_BOTTOM_SIZE = 28; //28px

    private int mDefaultColor;
    private int mProgressColor;
    private int mPercentColor;
    private int mBottomColor;
    private float mBottomTextSize;
    private float mPercentTextSize;
    private String mPercent;
    private String mBottomText;
    private int mRadius;
    private int mCenterX;
    private int mCenterY;
    private RectF percentRectF;
    private boolean isShortBottom;
    private int bottomTextWith;

    public PriceRangeCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PriceRangeCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(30);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PriceRangeCircleView);
//
        mRadius = a.getDimensionPixelSize(R.styleable.PriceRangeCircleView_circle_radius,
                DEFAULT_RADIUS);
        mPercentTextSize = a.getDimensionPixelSize(R.styleable.PriceRangeCircleView_percent_text_size,
                DEFAULT_PERCENT_SIZE);
        mBottomTextSize = a.getDimensionPixelSize(R.styleable.PriceRangeCircleView_bottom_text_size,
                DEFAULT_BOTTOM_SIZE);

        mDefaultColor = a.getColor(R.styleable.PriceRangeCircleView_default_color, Color.parseColor("#e5e5e5"));
        mProgressColor = a.getColor(R.styleable.PriceRangeCircleView_progress_color, Color.parseColor("#ff6600"));
        mPercentColor = a.getColor(R.styleable.PriceRangeCircleView_percent_text_color, Color.parseColor("#333333"));
        mBottomColor = a.getColor(R.styleable.PriceRangeCircleView_bottom_text_color, Color.parseColor("#999999"));
        mTextPaint.setTextSize(mBottomTextSize);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            bottomTextWith = (int) mTextPaint.measureText(mBottomText);
            if (bottomTextWith > (mRadius * 2 + mPaint.getStrokeWidth())) {
                isShortBottom = false;
                result = bottomTextWith + getPaddingLeft() + getPaddingRight();
            } else {
                result = (int) (mRadius * 2 + mPaint.getStrokeWidth() +
                        getPaddingLeft() + getPaddingRight());
                isShortBottom = true;
            }

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (mRadius * 2 + mPaint.getStrokeWidth() + 20 - mTextPaint.ascent()
                    + mTextPaint.descent()) + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }

        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isShortBottom) {
            mCenterX = getPaddingLeft() + (int) (mPaint.getStrokeWidth() / 2) + mRadius;
        } else {
            mCenterX = getPaddingLeft() + bottomTextWith/2;
        }

        mCenterY = getPaddingTop() + (int) (mPaint.getStrokeWidth() / 2) + mRadius;
        percentRectF = new RectF(mCenterX - mRadius, mCenterY - mRadius,
                mCenterX + mRadius, mCenterY + mRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //center radius,30dp,45dp
        //圆
        mPaint.setColor(mDefaultColor);
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        if (!TextUtils.isEmpty(mPercent)) {
            //进度
            mPaint.setColor(mProgressColor);
            canvas.drawArc(percentRectF, -90, Float.valueOf(mPercent) / 100 * 360, false, mPaint);

            //圆心字体
            mTextPaint.setColor(mPercentColor);
            mTextPaint.setTextSize(mPercentTextSize);

            //文字中心绘制在圆心
            float percentTextWidth = mTextPaint.measureText(mPercent + "%");
            float percentTextX = mCenterX - percentTextWidth / 2;
            float percentTextY = mCenterY - (-mTextPaint.ascent() + mTextPaint.descent()) / 2 - mTextPaint.ascent();
            canvas.drawText(mPercent + "%", percentTextX, percentTextY, mTextPaint);
        }

        //底部字体
        mTextPaint.setTextSize(mBottomTextSize);
        mTextPaint.setColor(mBottomColor);
        if (!TextUtils.isEmpty(mBottomText)) {
            float bottomTextWidth = mTextPaint.measureText(mBottomText);
            //////
            float bottomBaseLine = mCenterY + mRadius + mPaint.getStrokeWidth() / 2 + 20 - mTextPaint.ascent();


            canvas.drawText(mBottomText, mCenterX - bottomTextWidth / 2, bottomBaseLine, mTextPaint);
        }
    }

    public PriceRangeCircleView setPercent(String percent) {
        mPercent = percent;
        return this;
    }

    public PriceRangeCircleView setBottomText(String bottomText) {
        mBottomText = bottomText;
        return this;
    }

    public void draw() {
        requestLayout();
        invalidate();
    }
}