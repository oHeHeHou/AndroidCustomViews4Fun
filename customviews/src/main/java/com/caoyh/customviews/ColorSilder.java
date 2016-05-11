package com.caoyh.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by caoyh
 * Created on 2016/4/22
 * Description:
 */
public class ColorSilder extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mProgressColor;
    private int mLineThick;
    private int mSlideBarRadius;
    private int mUnderColor;
    private int mTextColor;
    private float mTextSize;
    private float mInitValue;
    private float mCurValue;
    private float mMinValue;
    private float mMaxValue;
    private float mCirleX;
    private boolean mIsInit = true;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_SLIDE_BAR_RADIUS = 30;
    private static final int DEFAULT_LINE_THICK = 25;
    private static final int DEFAULT_TEXT_SIZE = 26;

    private float lenPercent = 0;

    private RectF underLineRecf = new RectF();
    private RectF progressLineRecf = new RectF();

    private OnProgressChangeListener mOnProgressChangeListener;

    public ColorSilder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ColorSilder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public interface OnProgressChangeListener {
        void onProgressChange(float valueStr);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener l) {
        this.mOnProgressChangeListener = l;
    }

    public void reset() {

    }

    public void initValue(float mMinValue, float mCurValue, float mMaxValue) {
        if (mCurValue > mMaxValue || mCurValue < mMinValue) {
            throw new RuntimeException("中间值超过上下限范围");
        }
        this.mMinValue = mMinValue;
        this.mCurValue = mCurValue;
        this.mInitValue = mCurValue;
        this.mMaxValue = mMaxValue;
        if (mMaxValue != 0) {
            lenPercent = (mCurValue - mMinValue) / (mMaxValue - mMinValue);
        }
        invalidate();
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColorSilder);

        mUnderColor = a.getColor(R.styleable.ColorSilder_base_color, Color.parseColor("#e5e5e5"));
        mProgressColor = a.getColor(R.styleable.ColorSilder_progress_color, Color.parseColor("#ff6600"));
        mTextColor = a.getColor(R.styleable.ColorSilder_bottom_text_color, Color.parseColor("#e5e5e5"));
        mLineThick = a.getDimensionPixelSize(R.styleable.ColorSilder_line_height, DEFAULT_LINE_THICK);
        mSlideBarRadius = a.getDimensionPixelSize(R.styleable.ColorSilder_slide_bar_radius, DEFAULT_SLIDE_BAR_RADIUS);
        mTextSize = a.getDimensionPixelSize(R.styleable.ColorSilder_bottom_text_size, DEFAULT_TEXT_SIZE);

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));

        underLineRecf.left = getPaddingLeft() + mSlideBarRadius;
        underLineRecf.top = getPaddingTop() + mSlideBarRadius - mLineThick / 2;
        underLineRecf.right = getWidth() - getPaddingRight() - mSlideBarRadius;
        underLineRecf.bottom = underLineRecf.top + mLineThick;
        mCirleX = underLineRecf.left;
//        mLineLen = underLineRecf.right - underLineRecf.left;

        progressLineRecf.left = underLineRecf.left;
        progressLineRecf.top = underLineRecf.top;
        progressLineRecf.right = underLineRecf.right;
        progressLineRecf.bottom = underLineRecf.bottom;
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = DEFAULT_WIDTH + mSlideBarRadius * 2 +
                    getPaddingLeft() + getPaddingRight();
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
            result = getPaddingBottom() + getPaddingTop() + mSlideBarRadius * 2 +
                    20 + (int) (-mTextPaint.ascent() + mTextPaint.descent());
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawUnderLine(canvas);
        drawProgressLine(canvas, lenPercent);
        drawSlideBar(canvas);
        drawText(canvas);
    }

    private void drawUnderLine(Canvas canvas) {
        mPaint.setColor(mUnderColor);
        canvas.drawRoundRect(underLineRecf, 5f, 5f, mPaint);
    }

    private void drawProgressLine(Canvas canvas, float lenPercent) {
        mPaint.setColor(mProgressColor);
        if (mIsInit) {
            progressLineRecf.right = underLineRecf.left +
                    (underLineRecf.right - underLineRecf.left) * lenPercent;
        } else {
            progressLineRecf.right = mCirleX;
        }
        canvas.drawRoundRect(progressLineRecf, 5f, 5f, mPaint);
    }

    private void drawSlideBar(Canvas canvas) {
        float yPos = getPaddingTop() + underLineRecf.top + mLineThick / 2;
        mPaint.setColor(mUnderColor);
        if (mIsInit) {
            mIsInit = false;
            canvas.drawCircle(progressLineRecf.right, yPos, mSlideBarRadius, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(progressLineRecf.right, yPos, mSlideBarRadius - 5, mPaint);
            mPaint.setColor(mProgressColor);
            canvas.drawCircle(progressLineRecf.right, yPos, mSlideBarRadius - 16, mPaint);
        } else {
            canvas.drawCircle(mCirleX, yPos, mSlideBarRadius, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(mCirleX, yPos, mSlideBarRadius - 5, mPaint);
            mPaint.setColor(mProgressColor);
            canvas.drawCircle(mCirleX, yPos, mSlideBarRadius - 16, mPaint);
        }
        //根据计算距离计算比例

    }

    private void drawText(Canvas canvas) {
        float yPos = getPaddingTop() + mSlideBarRadius * 2 + 20 - mTextPaint.ascent();
        if (mMinValue != 0 && mMaxValue != 0) {
            canvas.drawText(mMinValue + "万", underLineRecf.left, yPos, mTextPaint);
            float maxTextLen = mTextPaint.measureText(String.valueOf(mMaxValue));
            canvas.drawText(mMaxValue + "万", underLineRecf.right - maxTextLen, yPos, mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        final int actionIndex = event.getActionIndex();
        if (actionIndex == 0) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    float eventX = event.getX();

                    if (eventX >= underLineRecf.left && eventX <= underLineRecf.right) {
                        mCirleX = eventX;
                        if (mOnProgressChangeListener != null) {
                            mOnProgressChangeListener.onProgressChange(eventX/getWidth());
                        }
                        invalidate();
                    }
                    break;
            }
        }
        return true;
    }

}
