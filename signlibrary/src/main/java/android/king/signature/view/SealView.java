package android.king.signature.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import android.king.signature.R;

/**
 * 印章
 *
 * @author king
 * @since 2018-06-01
 */
public class SealView extends View {

    private Paint borderPaint;
    private Paint textPaint;
    private Paint timePaint;

    private int sealColor;
    private float borderWidth;
    private float textSize;
    private float timeTextSize;
    /**
     * 印章文字
     */
    private String sealName;
    /**
     * 文字与上边框间距
     */
    private int paddingTop = 6;
    /**
     * 文字与下边框间距
     */
    private int paddingBottom = 6;
    /**
     * 文字与左边框间距
     */
    private int paddingLeft = 10;
    /**
     * 文字与右边框间距
     */
    private int paddingRight = 10;

    /**
     * 时间戳与文字间距
     */
    private int timePadding = 10;

    private String sealLabel;


    public SealView(Context context) {
        this(context, null);
    }

    public SealView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SealView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SealView);
        borderWidth = ta.getDimension(R.styleable.SealView_borderWidth, 1.0f);
        textSize = ta.getDimension(R.styleable.SealView_sealTextSize, getResources().getDimension(R.dimen.seal_text_size));
        timeTextSize = ta.getDimension(R.styleable.SealView_timeTextSize, getResources().getDimension(R.dimen.seal_time_text_size));
        sealColor = ta.getColor(R.styleable.SealView_sealColor, ContextCompat.getColor(context, R.color.sign_seal_red));
        sealName = ta.getString(R.styleable.SealView_sealName);
        ta.recycle();
        init();
    }

    public void init() {
        borderPaint = new Paint();
        borderPaint.setColor(sealColor);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeJoin(Paint.Join.ROUND);
        borderPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(sealColor);
        textPaint.setStrokeWidth(2);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        timePaint = new Paint();

        timePaint.setColor(Color.BLACK);
        timePaint.setStrokeWidth(2);
        timePaint.setTextSize(timeTextSize);
        timePaint.setAntiAlias(true);
        textPaint.setStrokeJoin(Paint.Join.ROUND);
        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setTextAlign(Paint.Align.CENTER);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        int sealTop = (int) (borderWidth + paddingTop);
        int sealLeft = (int) (getSealWidth() - getTextWidth() - paddingLeft - paddingLeft - borderWidth);
        int sealRight = (int) (sealLeft + getTextWidth() + paddingLeft + paddingRight);
        int sealBottom = (int) (getTextHeight() + paddingTop + paddingBottom + borderWidth);

        Rect sealRect = new Rect(sealLeft, sealTop, sealRight, sealBottom);
        Rect borderRect = new Rect(sealRect.left, (int) borderWidth, sealRect.right, sealRect.bottom);

        canvas.drawColor(Color.TRANSPARENT);
        if (!TextUtils.isEmpty(sealName)) {
            // 绘制印章名字
            canvas.drawText(sealName, sealRect.centerX(), sealRect.bottom - paddingBottom - 2 * borderWidth, textPaint);
            //  绘制一个矩形边框
            canvas.drawRect(borderRect, borderPaint);
        }
        //绘制时间
        if (!TextUtils.isEmpty(sealLabel)) {
            Rect timeRect = new Rect(0, borderRect.bottom, getWidth(), getHeight());
            canvas.drawText(sealLabel, timeRect.centerX(), getHeight() - borderWidth, timePaint);
        }
    }

    private float getTextHeight() {
        if (TextUtils.isEmpty(sealName)) {
            return 0;
        }
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        return Math.abs((fontMetrics.descent - fontMetrics.ascent));
    }

    private float getTextWidth() {
        if (TextUtils.isEmpty(sealName)) {
            return 0;
        }
        return textPaint.measureText(sealName);
    }

    /**
     * 获取所绘制的印章bitmap
     */
    public Bitmap getBitmap() {
        // 创建对应大小的bitmap
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = onMeasureR(0, widthMeasureSpec);
        int height = onMeasureR(1, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private float getSealWidth() {

        float timeWidth = timePaint.measureText(sealLabel);
        float signWidth;
        if (TextUtils.isEmpty(sealName)) {
            signWidth = 0;
        } else {
            signWidth = textPaint.measureText(sealName) + paddingLeft + paddingRight + 2 * borderWidth;
        }

        if (signWidth < timeWidth) {
            return timeWidth;
        } else {
            return signWidth;
        }
    }

    private float getSealHeight() {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        Paint.FontMetrics timeMetrics = timePaint.getFontMetrics();
        float timeHeight = Math.abs((timeMetrics.descent - timeMetrics.ascent));
        float textHeight;
        if (TextUtils.isEmpty(sealName)) {
            textHeight = 0;
        } else {
            textHeight = Math.abs((fontMetrics.descent - fontMetrics.ascent));
        }

        if (!TextUtils.isEmpty(sealLabel)) {
            return timeHeight + textHeight + paddingTop + paddingBottom + timePadding + 2 * borderWidth;
        }
        return textHeight + paddingTop + paddingBottom + 2 * borderWidth;
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
                if (attr == 0) {
                    newSize = (int) (getPaddingLeft() + getSealWidth() + getPaddingRight());
                } else if (attr == 1) {
                    newSize = (int) (getPaddingTop() + getSealHeight() + getPaddingBottom());
                }
                break;
            default:
                break;
        }

        return newSize;
    }


    public void setSealColor(int sealColor) {
        this.sealColor = sealColor;
        borderPaint.setColor(sealColor);
        textPaint.setColor(sealColor);
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        textPaint.setStrokeWidth(borderWidth);
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
        invalidate();
    }

    public void setTimeTextSize(float textSize) {
        this.timeTextSize = textSize;
        timePaint.setTextSize(textSize);
        invalidate();
    }

    public void setTextContent(String sealName) {
        if (TextUtils.isEmpty(sealName) || sealName.length() > 15) {
            return;
        }
        this.sealName = sealName;
        invalidate();
    }

    public void setLabel(String sealTime) {
        this.sealLabel = sealTime;
        invalidate();
    }

}
