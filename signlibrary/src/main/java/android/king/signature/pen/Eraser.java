package android.king.signature.pen;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.king.signature.view.EraserView;

/***
 * 名称：Eraser<br>
 * 描述：橡皮擦
 * 最近修改时间：
 * @since 2018/5/7
 * @author king
 */
public class Eraser {

    private Paint eraserPaint;

    /**
     * 画笔路径
     */
    private Path mPath;
    /**
     * 记录上一个点的坐标
     */
    private float mLastX;
    private float mLastY;

    public Eraser(int paintWidth) {
        eraserPaint = new Paint();
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setStrokeWidth(paintWidth);
        eraserPaint.setFilterBitmap(true);

        eraserPaint.setColor(Color.WHITE);
        eraserPaint.setDither(true);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraserPaint.setAntiAlias(true);

        mPath = new Path();
    }

//    public void setPaintWidth(int width) {
//        eraserPaint.setStrokeWidth(width);
//    }

    public boolean handleEraserEvent(MotionEvent event, Canvas canvas) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                mLastX = x;
                mLastY = y;
                mPath.reset();
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(mLastX, mLastY, (x + mLastX) / 2, (y + mLastY) / 2);
                canvas.drawPath(mPath, eraserPaint);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(mLastX, mLastY);
                canvas.drawPath(mPath, eraserPaint);
                break;
        }
        return true;
    }
}
