package com.arbelkilani.bicoloredprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by arbelkilani on 3/28/18.
 */

public class BiColoredProgress extends View {

    // progress
    private final static int MAX_PROGRESS_VALUE = 100; // default max progress value
    private final static int MIN_PROGRESS_VALUE = 0; // default min progress value
    private final static int MAX_CIRCLE_ANGLE = 360;

    private final static float DEFAULT_OUTER_STROKE_WIDTH = 3; // default outer circle stroke width
    private final static float DEFAULT_INNER_ALPHA = 0.8f; // default alpha from outer to inner circles
    private final static float DEFAULT_PROGRESS = 0f;

    private final static int DEFAULT_ANIMATION_DURATION = 7000; // default animation duration

    private final static float START_ANGLE = 270f;
    private final static float INNER_STROKE_FACTOR = 0.2f;

    // label
    private final static float TEXT_FACTOR = 0.2f;
    private final static String PERCENT = "%";
    private final static float SPAN_FACTOR = 0.6f;
    private final static float LABEL_FACTOR = 0.6f;

    protected int mWidth;
    protected int mStrokeWidth;
    protected int mRightSidedColor, mLeftSidedColor;
    protected int mInnerAlpha;
    protected float mSweepAngle;
    protected float mProgress;
    protected String mLabel;
    protected int mDuration;

    // animation

    /**
     * @param context
     */
    public BiColoredProgress(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public BiColoredProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TwiceColoredProgress, 0, 0);

        if (typedArray != null) {

            mStrokeWidth = (int) typedArray.getDimension(R.styleable.TwiceColoredProgress_stroke_width, AppUtils.convertDpToPixel(DEFAULT_OUTER_STROKE_WIDTH, context));

            mInnerAlpha = getAlphaValue(typedArray.getFloat(R.styleable.TwiceColoredProgress_inner_alpha_factor, DEFAULT_INNER_ALPHA));

            mRightSidedColor = typedArray.getColor(R.styleable.TwiceColoredProgress_right_sided_color, Color.BLUE);
            mLeftSidedColor = typedArray.getColor(R.styleable.TwiceColoredProgress_left_sided_color, Color.GREEN);

            mProgress = typedArray.getFloat(R.styleable.TwiceColoredProgress_progress, DEFAULT_PROGRESS);
            mLabel = typedArray.getString(R.styleable.TwiceColoredProgress_label);

            mDuration = typedArray.getInt(R.styleable.TwiceColoredProgress_duration, DEFAULT_ANIMATION_DURATION);

            typedArray.recycle();
        }

    }

    /**
     * @param progress
     * @return
     */
    private float getSweepAngle(float progress) {

        if (progress > MAX_PROGRESS_VALUE)
            progress = MAX_PROGRESS_VALUE;

        if (progress < MIN_PROGRESS_VALUE)
            progress = MIN_PROGRESS_VALUE;

        return (progress / MAX_PROGRESS_VALUE) * MAX_CIRCLE_ANGLE;
    }

    /**
     * @param input
     * @return
     */
    private int getAlphaValue(float input) {
        return Math.round(input * 255);
    }

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int x = mWidth / 2;
        int y = mWidth / 2;

        mSweepAngle = getSweepAngle(mProgress);

        drawOuterCircle(x, y, canvas);
        drawInnerCircle(x, y, canvas);
        drawProgressValue(x, y, canvas, mProgress);

    }

    /**
     * @param x
     * @param y
     * @param canvas
     * @param progress
     */
    private void drawProgressValue(int x, int y, Canvas canvas, float progress) {

        TextPaint paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(mWidth * TEXT_FACTOR);

        SpannableStringBuilder spannableString = getSpannableValue(progress);

        StaticLayout layout = new StaticLayout(spannableString, paint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(x - layout.getWidth() / 2, y - layout.getHeight() / 2);
        layout.draw(canvas);
    }

    /**
     * @param progress
     * @return
     */
    @NonNull
    private SpannableStringBuilder getSpannableValue(float progress) {

        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.DOWN);

        String textProgress = df.format(progress);

        textProgress = !textProgress.contains(".") ? textProgress : textProgress.replaceAll("0*$", "").replaceAll("\\.$", ""); // remove .0 in end of string 89.0 -> 89
        textProgress = textProgress + PERCENT; // append percent to string

        SpannableStringBuilder spannableString = new SpannableStringBuilder(textProgress);
        spannableString.setSpan(new SuperscriptSpan(), spannableString.length() - 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new RelativeSizeSpan(SPAN_FACTOR), spannableString.length() - 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        if (!TextUtils.isEmpty(mLabel)) {
            spannableString.append("\n");
            spannableString.append(mLabel);
            spannableString.setSpan(new RelativeSizeSpan(LABEL_FACTOR), spannableString.length() - mLabel.length(), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // set label
        }

        return spannableString;
    }

    /**
     * @param x
     * @param y
     * @param canvas
     */
    private void drawInnerCircle(int x, int y, Canvas canvas) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((mWidth / 2 * INNER_STROKE_FACTOR) + mStrokeWidth);
        paint.setAlpha(mInnerAlpha);

        float radius = (mWidth / 2) * (1 - INNER_STROKE_FACTOR / 2) - mStrokeWidth / 2;
        RectF rectF = new RectF();

        rectF.set(x - radius, y - radius, x + radius, y + radius);

        paint.setShader(new LinearGradient(x - radius, y, x + radius, y, mLeftSidedColor, mRightSidedColor, Shader.TileMode.CLAMP));

        canvas.drawArc(rectF, START_ANGLE, mSweepAngle, false, paint);

    }

    /**
     * @param x
     * @param y
     * @param canvas
     */
    private void drawOuterCircle(int x, int y, Canvas canvas) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mStrokeWidth);

        float radius = (mWidth / 2) - mStrokeWidth / 2;

        RectF rectF = new RectF();
        rectF.set(x - radius, y - radius, x + radius, y + radius);

        paint.setShader(new LinearGradient(x - radius, y, x + radius, y, mLeftSidedColor, mRightSidedColor, Shader.TileMode.CLAMP));

        canvas.drawArc(rectF, START_ANGLE, mSweepAngle, false, paint);
    }

    /**
     *
     * @param progress
     */
    public void setProgress(float progress) {

        mProgress = progress;
        invalidate();
    }

    /**
     *
     * @param canAnimate
     */
    public void setAnimated(boolean canAnimate) {
        if (canAnimate) {
            startAnimation(new ResizeAnimation(mProgress));
        }
    }

    /**
     *
     * @param canAnimate
     * @param animationDuration
     */
    public void setAnimated(boolean canAnimate, int animationDuration) {
        if (canAnimate) {
            startAnimation(new ResizeAnimation(mProgress, animationDuration));
        }
    }

    /**
     *
     */
    private class ResizeAnimation extends Animation {

        private float mToProgress;

        public ResizeAnimation(float toProgress) {
            mToProgress = toProgress;
            setDuration(mDuration);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        public ResizeAnimation(float toProgress, int animationDuration) {
            mDuration = animationDuration;
            mToProgress = toProgress;
            setDuration(mDuration);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            float progress = interpolatedTime * 100;
            if (progress > mToProgress) {
                progress = mToProgress;
            }
            mProgress = progress;

            invalidate();
        }
    }
}