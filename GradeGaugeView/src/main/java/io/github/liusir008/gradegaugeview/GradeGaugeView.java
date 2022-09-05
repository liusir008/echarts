package io.github.liusir008.gradegaugeview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.liusir008.gradegaugeview.utils.SizeUtils;

/**
 * 自定义仪表盘
 */
public class GradeGaugeView extends View {

    @FunctionalInterface
    public interface ValueFormatter {
        String format(float value);
    }

    public static final class AxisLine {
        private final float value;
        private final int color;
        private final String label;

        public AxisLine(float value, int color, String label) {
            this.value = value;
            this.color = color;
            this.label = label;
        }
    }

    private float mMin;
    private float mMax;

    private float mAxisLineWidth;

    private final List<AxisLine> mAxisLines = new ArrayList<>();

    private float mTickOffset;
    private float mAxisTickLength;
    private float mAxisTickWidth;
    private float mSplitLineLength;
    private float mSplitLineWidth;

    private float mPointerLength;
    private float mPointerWidth;
    private float mPointerOffset;
    private int mPointerColor;

    private String mTitle;
    private float mTitleSize;
    private int mTitleColor;
    private float mTitleOffset;

    private float mDetailSize;
    private int mDetailColor;
    private float mDataValue;
    private String mDataUnit;
    private float mDataUnitSize;
    private int mDataUnitColor;

    private float mExtBtnOffset;
    private boolean mExtBtnRound;
    private float mExtBtnRoundRadius;
    private float mExtBtnHeight;
    private float mExtBtnWidth;
    private int mExtBtnBackground;
    private String mExtBtnText;
    private float mExtBtnTextSize;
    private int mExtBtnTextColor;

    private boolean mEnableAutoColor;
    private boolean mEnableAnimator;

    private final Paint mPaint;
    private final TextPaint mTextPaint;
    private final Path mPath;

    private float mAnimatedValue = 0;

    private ValueAnimator mValueAnimator = null;

    private ValueFormatter mValueFormatter = (value) -> new DecimalFormat("#.##").format(value);

    public GradeGaugeView(Context context) {
        this(context, null);
    }

    public GradeGaugeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradeGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GradeGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mMin = 0;
        mMax = 1.0f;

        mAxisLineWidth = SizeUtils.dp2px(4);

        mTickOffset = SizeUtils.dp2px(2);
        mAxisTickLength = SizeUtils.dp2px(4);
        mAxisTickWidth = SizeUtils.dp2px(1);
        mSplitLineLength = SizeUtils.dp2px(10);
        mSplitLineWidth = SizeUtils.dp2px(3);

        mPointerLength = SizeUtils.dp2px(24);
        mPointerWidth = SizeUtils.dp2px(16);
        mPointerOffset = SizeUtils.dp2px(8);
        mPointerColor = Color.parseColor("#31DF6F");

        mTitle = "PM2.5";
        mTitleSize = SizeUtils.sp2px(12);
        mTitleColor = Color.WHITE;
        mTitleOffset = SizeUtils.dp2px(4);

        mDetailSize = SizeUtils.sp2px(16);
        mDetailColor = Color.parseColor("#31DF6F");
        mDataValue = 0.50f;
        mDataUnit = "µg/m³";
        mDataUnitSize = SizeUtils.sp2px(12);
        mDataUnitColor = mDetailColor;

        mExtBtnOffset = SizeUtils.dp2px(4);
        mExtBtnRound = true;
        mExtBtnRoundRadius = SizeUtils.dp2px(4);
        mExtBtnWidth = SizeUtils.dp2px(64);
        mExtBtnHeight = SizeUtils.dp2px(24);
        mExtBtnBackground = Color.parseColor("#31DF6F"); // #C1F557 #F4C040 #E55232
        mExtBtnText = "优";
        mExtBtnTextColor = Color.parseColor("#1F1C5F");
        mExtBtnTextSize = SizeUtils.sp2px(12);

        mEnableAutoColor = false;
        mEnableAnimator = false;

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(0);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);

        mTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

        mPath = new Path();

        init(context, attrs);
    }

    /**
     * init attributes
     *
     * @param context widget context
     * @param attrs   attrs
     */
    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GradeGaugeView);
        for (int i = 0, len = array.getIndexCount(); i < len; i++) {
            int index = array.getIndex(i);
            if (index == R.styleable.GradeGaugeView_min) {
                mMin = array.getFloat(index, mMin);
            } else if (index == R.styleable.GradeGaugeView_max) {
                mMax = array.getFloat(index, mMax);
            } else if (index == R.styleable.GradeGaugeView_axisLineWidth) {
                mAxisLineWidth = array.getDimension(index, mAxisLineWidth);
            } else if (index == R.styleable.GradeGaugeView_tickOffset) {
                mTickOffset = array.getDimension(index, mTitleOffset);
            } else if (index == R.styleable.GradeGaugeView_axisTickLength) {
                mAxisTickLength = array.getDimension(index, mAxisTickLength);
            } else if (index == R.styleable.GradeGaugeView_axisTickWidth) {
                mAxisTickWidth = array.getDimension(index, mAxisTickWidth);
            } else if (index == R.styleable.GradeGaugeView_splitLineLength) {
                mSplitLineLength = array.getDimension(index, mSplitLineLength);
            } else if (index == R.styleable.GradeGaugeView_splitLineWidth) {
                mSplitLineWidth = array.getDimension(index, mSplitLineWidth);
            } else if (index == R.styleable.GradeGaugeView_pointerLength) {
                mPointerLength = array.getDimension(index, mPointerLength);
            } else if (index == R.styleable.GradeGaugeView_pointerWidth) {
                mPointerWidth = array.getDimension(index, mPointerWidth);
            } else if (index == R.styleable.GradeGaugeView_pointerOffset) {
                mPointerOffset = array.getDimension(index, mPointerOffset);
            } else if (index == R.styleable.GradeGaugeView_pointerColor) {
                mPointerColor = array.getColor(index, mPointerColor);
            } else if (index == R.styleable.GradeGaugeView_title) {
                mTitle = array.getString(index);
            } else if (index == R.styleable.GradeGaugeView_titleSize) {
                mTitleSize = array.getDimension(index, mTitleSize);
            } else if (index == R.styleable.GradeGaugeView_titleColor) {
                mTitleColor = array.getColor(index, mTitleColor);
            } else if (index == R.styleable.GradeGaugeView_titleOffset) {
                mTitleOffset = array.getDimension(index, mTitleOffset);
            } else if (index == R.styleable.GradeGaugeView_detailSize) {
                mDetailSize = array.getDimension(index, mDetailSize);
            } else if (index == R.styleable.GradeGaugeView_detailColor) {
                mDetailColor = array.getColor(index, mDetailColor);
            } else if (index == R.styleable.GradeGaugeView_dataValue) {
                mDataValue = array.getFloat(index, mDataValue);
            } else if (index == R.styleable.GradeGaugeView_dataUnit) {
                mDataUnit = array.getString(index);
            } else if (index == R.styleable.GradeGaugeView_dataUnitSize) {
                mDataUnitSize = array.getDimension(index, mDataUnitSize);
            } else if (index == R.styleable.GradeGaugeView_dataUnitColor) {
                mDataUnitColor = array.getColor(index, mDataUnitColor);
            } else if (index == R.styleable.GradeGaugeView_extBtnOffset) {
                mExtBtnOffset = array.getDimension(index, mExtBtnOffset);
            } else if (index == R.styleable.GradeGaugeView_extBtnRound) {
                mExtBtnRound = array.getBoolean(index, mExtBtnRound);
            } else if (index == R.styleable.GradeGaugeView_extBtnRoundRadius) {
                mExtBtnRoundRadius = array.getDimension(index, mExtBtnRoundRadius);
            } else if (index == R.styleable.GradeGaugeView_extBtnHeight) {
                mExtBtnHeight = array.getDimension(index, mExtBtnHeight);
            } else if (index == R.styleable.GradeGaugeView_extBtnWidth) {
                mExtBtnWidth = array.getDimension(index, mExtBtnWidth);
            } else if (index == R.styleable.GradeGaugeView_extBtnBackground) {
                mExtBtnBackground = array.getColor(index, mExtBtnBackground);
            } else if (index == R.styleable.GradeGaugeView_extBtnText) {
                mExtBtnText = array.getString(index);
            } else if (index == R.styleable.GradeGaugeView_extBtnTextSize) {
                mExtBtnTextSize = array.getDimension(index, mExtBtnTextSize);
            } else if (index == R.styleable.GradeGaugeView_extBtnTextColor) {
                mExtBtnTextColor = array.getColor(index, mExtBtnTextColor);
            } else if (index == R.styleable.GradeGaugeView_enableAutoColor) {
                mEnableAutoColor = array.getBoolean(index, mEnableAutoColor);
            } else if (index == R.styleable.GradeGaugeView_enableAnimator) {
                mEnableAnimator = array.getBoolean(index, mEnableAnimator);
            }
        }
        array.recycle();

        mAxisLines.clear();
        mAxisLines.add(new AxisLine(mMax / 3, Color.parseColor("#31DF6F"), "优"));
        mAxisLines.add(new AxisLine(mMax * 2 / 3, Color.parseColor("#F4C040"), "良"));
        mAxisLines.add(new AxisLine(mMax, Color.parseColor("#E55232"), "差"));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode() && mEnableAnimator) {
            startAnimator();
        } else {
            mAnimatedValue = 1.0f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        int cx = w / 2;

        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();

        int paddingH = Math.max(paddingLeft, paddingRight);
        int paddingV = Math.max(paddingTop, paddingBottom);
        int padding = Math.max(paddingH, paddingV);

        float pureSize = Math.min(w / 2.0f - padding, h - padding * 2 - mExtBtnHeight - mExtBtnOffset);
        float arcY = h - padding - mExtBtnHeight - mExtBtnOffset;

        Paint.FontMetrics fontMetrics;

        float startAngle = 180.0f;
        int axisNums = mAxisLines.size();
        if (axisNums == 0) {
            axisNums = 3;
        }
        float perAngle = 180.0f / axisNums;

        mPaint.setStyle(Paint.Style.STROKE);

        // 画圆弧
        mPaint.setStrokeWidth(mAxisLineWidth);
        for (int i = 0; i < axisNums; i++) {
            canvas.save();
            canvas.rotate(perAngle * i, cx, arcY);
            mPaint.setColor(mAxisLines.get(i).color);
            canvas.drawArc(cx - pureSize, arcY - pureSize, cx + pureSize, arcY + pureSize, startAngle, perAngle, false, mPaint);
            canvas.restore();
        }

        // 画刻度
        if (!mAxisLines.isEmpty()) {
            mPaint.setStrokeWidth(mSplitLineWidth);
            mPaint.setColor(mAxisLines.get(0).color);
            float tickX = padding + mAxisLineWidth + mTickOffset;
            canvas.drawLine(tickX, arcY, tickX + mSplitLineLength, arcY, mPaint);
        }

        float tickAngle = perAngle / 10;
        for (int i = 0; i < axisNums; i++) {
            mPaint.setColor(mAxisLines.get(i).color);

            for (int j = 1; j <= 10; j++) {
                canvas.save();
                canvas.rotate(perAngle * i + tickAngle * j, cx, arcY);
                float tickX = padding + mAxisLineWidth + mTickOffset;
                if (j % 5 == 0) {
                    mPaint.setStrokeWidth(mSplitLineWidth);
                    canvas.drawLine(tickX, arcY, tickX + mSplitLineLength, arcY, mPaint);
                } else {
                    mPaint.setStrokeWidth(mAxisTickWidth);
                    canvas.drawLine(tickX, arcY, tickX + mAxisTickLength, arcY, mPaint);
                }
                canvas.restore();
            }
        }

        int colorAuto = Color.WHITE;
        float angleAuto = 0;
        String label = mExtBtnText;
        for (int i = 0; i < axisNums; i++) {
            AxisLine axisLine = mAxisLines.get(i);
            if (axisLine.value >= mDataValue) {
                colorAuto = axisLine.color;
                angleAuto = perAngle * i + perAngle / 2;
                label = axisLine.label;
                break;
            }
        }

        // 画指针
        mPaint.setColor(mEnableAutoColor ? colorAuto : mPointerColor);
        mPaint.setStyle(Paint.Style.FILL);

        float pointerX = padding + mAxisLineWidth + mTickOffset + mSplitLineLength + mPointerOffset;
        mPath.reset();
        mPath.moveTo(pointerX, arcY);
        mPath.lineTo(pointerX + mPointerLength, arcY + mPointerWidth / 2.0f);
        mPath.lineTo(pointerX + mPointerLength, arcY - mPointerWidth / 2.0f);
        mPath.lineTo(pointerX, arcY);
        mPath.close();

        canvas.save();
        canvas.rotate(angleAuto * mAnimatedValue, cx, arcY);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();

        // 画数值
        String detailValue = mValueFormatter.format(mDataValue * mAnimatedValue);

        mTextPaint.setTextSize(mDetailSize);
        fontMetrics = mTextPaint.getFontMetrics();
        float detailValueWidth = mTextPaint.measureText(detailValue);
        float detailValueHeight = fontMetrics.bottom - fontMetrics.top;
        float detailValueBaseline = detailValueHeight / 2 - fontMetrics.bottom;

        mTextPaint.setTextSize(mDataUnitSize);
        fontMetrics = mTextPaint.getFontMetrics();
        float valueUnitWidth = mTextPaint.measureText(mDataUnit);
        float valueUnitHeight = fontMetrics.bottom - fontMetrics.top;
        float valueUnitBaseline = valueUnitHeight / 2 - fontMetrics.bottom;

        float valueBaseline = Math.min(detailValueBaseline, valueUnitBaseline);

        float detailTextWidth = detailValueWidth + valueUnitWidth;
        float detailTextHeight = Math.max(detailValueHeight, valueUnitHeight);

        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mEnableAutoColor ? colorAuto : mDetailColor);
        mTextPaint.setTextSize(mDetailSize);
        canvas.drawText(detailValue, cx - (detailTextWidth - detailValueWidth) / 2, arcY - valueBaseline, mTextPaint);

        mTextPaint.setColor(mEnableAutoColor ? colorAuto : mDataUnitColor);
        mTextPaint.setTextSize(mDataUnitSize);
        canvas.drawText(mDataUnit, cx + (detailTextWidth - valueUnitWidth) / 2, arcY - valueBaseline, mTextPaint);

        // 画标题
        mTextPaint.setColor(mTitleColor);
        mTextPaint.setTextSize(mTitleSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mTitle, cx, arcY - detailTextHeight - mTitleOffset, mTextPaint);

        // 画底部扩展按钮
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mEnableAutoColor ? colorAuto : mExtBtnBackground);
        mPaint.setStyle(Paint.Style.FILL);
        if (mExtBtnRound) {
            canvas.drawRoundRect(cx - mExtBtnWidth / 2, h - padding - mExtBtnHeight, cx + mExtBtnWidth / 2, h - padding, mExtBtnRoundRadius, mExtBtnRoundRadius, mPaint);
        } else {
            canvas.drawRect(cx - mExtBtnWidth / 2, h - padding - mExtBtnHeight, cx + mExtBtnWidth / 2, h - padding, mPaint);
        }

        mTextPaint.setColor(mExtBtnTextColor);
        mTextPaint.setTextSize(mExtBtnTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        fontMetrics = mTextPaint.getFontMetrics();
        canvas.drawText(mEnableAutoColor ? label : mExtBtnText, cx, h - padding - mExtBtnHeight / 2 - fontMetrics.bottom / 2 - fontMetrics.top / 2, mTextPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode() && mEnableAnimator && mValueAnimator != null) {
            mValueAnimator.end();
            mValueAnimator = null;
        }
    }

    /**
     * start the animator for this widget
     */
    public void startAnimator() {
        mAnimatedValue = 0;

        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.setDuration(750);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.addUpdateListener(animation -> {
            mAnimatedValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        mValueAnimator.start();
    }

    /**
     * set axis lines
     *
     * @param lines axis lines
     * @return widget itself
     */
    public GradeGaugeView setAxisLines(AxisLine... lines) {
        mAxisLines.clear();
        mAxisLines.addAll(Arrays.asList(lines));
        return this;
    }

    /**
     * set data value formatter
     *
     * @param valueFormatter a formatter to format data value
     * @return widget itself
     */
    public GradeGaugeView setValueFormatter(ValueFormatter valueFormatter) {
        this.mValueFormatter = valueFormatter;
        return this;
    }

    /**
     * update data value
     *
     * @param value new data value
     * @return widget itself
     */
    public GradeGaugeView updateDataValue(float value) {
        this.mDataValue = value;
        if (mEnableAnimator) {
            startAnimator();
        } else {
            postInvalidate();
        }
        return this;
    }
}
