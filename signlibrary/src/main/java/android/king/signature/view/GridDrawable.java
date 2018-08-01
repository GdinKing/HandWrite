package android.king.signature.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/***
 * 名称：GridDrawable<br>
 * 描述：自定义米格背景视图
 * 最近修改时间：
 * @since 2017/11/16
 * @author king
 */

public class GridDrawable extends Drawable {

    private Paint mPaint;
    private Paint mDashPaint;
    private Paint mLinePaint;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Path linePath;
    private Path dashPath;

    private int mWidth;
    private int mHeight;
    private int backgroundColor;

    public GridDrawable(int width, int height, int backgroundColor) {
        this.mWidth = width;
        this.mHeight = height;
        this.backgroundColor = backgroundColor;
        init();
    }

    public void init() {
        //边框画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10.0f);
        mPaint.setColor(Color.parseColor("#c4c4c4"));

        //虚线画笔
        mDashPaint = new Paint();
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setAntiAlias(true);
        mDashPaint.setStrokeWidth(5.0f);
        mDashPaint.setColor(Color.parseColor("#c4c4c4"));
        mDashPaint.setPathEffect(new DashPathEffect(new float[]{50, 40}, 0));

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(5.0f);
        mLinePaint.setColor(Color.parseColor("#c4c4c4"));

        linePath = new Path();
        dashPath = new Path();

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(backgroundColor);
        doDraw();
    }

    private void doDraw() {
        Rect rect = new Rect(0, 0, mWidth, mHeight);
        mCanvas.drawRect(rect, mPaint);

        linePath.moveTo(0, mHeight / 2);
        linePath.lineTo(mWidth, mHeight / 2);
        mCanvas.drawPath(linePath, mLinePaint);

        linePath.moveTo(mWidth / 2, 0);
        linePath.lineTo(mWidth / 2, mHeight);
        mCanvas.drawPath(linePath, mLinePaint);

        dashPath.moveTo(0, 0);
        dashPath.lineTo(mWidth, mHeight);
        mCanvas.drawPath(dashPath, mDashPaint);

        dashPath.moveTo(mWidth, 0);
        dashPath.lineTo(0, mHeight);
        mCanvas.drawPath(dashPath, mDashPaint);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
        mDashPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
        mDashPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
