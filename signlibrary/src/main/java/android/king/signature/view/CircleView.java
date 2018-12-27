package android.king.signature.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.king.signature.util.DisplayUtil;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import android.king.signature.R;
import android.king.signature.config.PenConfig;



/**
 * 自定义圆形View
 *
 * @author king
 * @since 2018-06-01
 */
public class CircleView extends View {

    private Paint mPaint;
    private Paint backPaint;
    private Paint borderPaint;
    private Paint outBorderPaint;
    private int paintColor;
    private int outBorderColor = Color.parseColor("#0c53ab");
    private int circleRadius;
    private int radiusLevel;
    private boolean showBorder = false;
    private boolean showOutBorder = false;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        paintColor = ta.getColor(R.styleable.CircleView_penColor, PenConfig.PAINT_COLOR);
        outBorderColor = ta.getColor(R.styleable.CircleView_penColor, Color.parseColor("#0c53ab"));
        radiusLevel = ta.getInteger(R.styleable.CircleView_sizeLevel, 2);
        circleRadius = DisplayUtil.dip2px(context, PaintSettingWindow.PEN_SIZES[radiusLevel]);
        showBorder = ta.getBoolean(R.styleable.CircleView_showBorder, false);
        showOutBorder = ta.getBoolean(R.styleable.CircleView_showOutBorder, false);
        ta.recycle();
        init();
    }


    private void init() {

        borderPaint = new Paint();
        borderPaint.setColor(paintColor);
        borderPaint.setStrokeWidth(5);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeJoin(Paint.Join.ROUND);
        borderPaint.setStyle(Paint.Style.STROKE);

        outBorderPaint = new Paint();
        outBorderPaint.setColor(outBorderColor);
        outBorderPaint.setStrokeWidth(3.5f);
        outBorderPaint.setAntiAlias(true);
        outBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        outBorderPaint.setStyle(Paint.Style.STROKE);

        backPaint = new Paint();
        backPaint.setColor(Color.WHITE);
        backPaint.setAntiAlias(true);
        backPaint.setStrokeJoin(Paint.Join.ROUND);
        backPaint.setStyle(Paint.Style.FILL);

        mPaint = new Paint();
        mPaint.setColor(paintColor);
        mPaint.setStrokeWidth(20);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.FILL);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, backPaint);
        //绘制内边框
        if (showBorder) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, circleRadius / 2.5f + 10, borderPaint);
        }
        //绘制外边框
        if (showOutBorder) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - 2f, outBorderPaint);
        }
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, circleRadius / 2.5f, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = onMeasureR(0, widthMeasureSpec);
        int height = onMeasureR(1, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    /**
     * 计算控件宽高
     */
    public int onMeasureR(int attr, int oldMeasure) {

        int newSize = 0;
        int mode = MeasureSpec.getMode(oldMeasure);
        int oldSize = MeasureSpec.getSize(oldMeasure);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                newSize = oldSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                float value;
                if (attr == 0) {
                    if(showOutBorder) {
                        value = (circleRadius / 2.5f + 40) * 2;
                    }else{
                        value = (circleRadius / 2.5f + 25) * 2;
                    }
                    newSize = (int) (getPaddingLeft() + value + getPaddingRight());
                } else if (attr == 1) {
                    if(showOutBorder) {
                        value = (circleRadius / 2.5f + 40) * 2;
                    }else{
                        value = (circleRadius / 2.5f + 25) * 2;
                    }
//                    value = (circleRadius / 2.5f + 20) * 2;
                    // 控件的高度  + getPaddingTop() +  getPaddingBottom()
                    newSize = (int) (getPaddingTop() + value + getPaddingBottom());

                }
                break;
            default:
                break;
        }

        return newSize;
    }

    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
        mPaint.setColor(paintColor);
        invalidate();
    }

    public void setRadiusLevel(int level) {
        this.radiusLevel = level;
        this.circleRadius = DisplayUtil.dip2px(getContext(), PaintSettingWindow.PEN_SIZES[level]);
        invalidate();
    }

    public void showBorder(boolean showBorder) {
        this.showBorder = showBorder;
        invalidate();
    }

    public void setOutBorderColor(int outBorderColor) {
        this.outBorderColor = outBorderColor;
        outBorderPaint.setColor(outBorderColor);
        invalidate();
    }

    public int getPaintColor() {
        return paintColor;
    }

    public int getRadiusLevel() {
        return radiusLevel;
    }
}
