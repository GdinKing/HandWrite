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
     * 橡皮擦指示器
     */
    private EraserView eraserView;

    /**
     * 画笔路径
     */
    private Path mPath;
    /**
     * 记录上一个点的坐标
     */
    private float mLastX;
    private float mLastY;

    public Eraser(int paintWidth, EraserView view) {
        this.eraserView = view;
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
        final float rawX = event.getRawX();
        final float rawY = event.getRawY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (eraserView != null) {
                    eraserView.setX(rawX);
                    eraserView.setY(rawY);
                    eraserView.setAlpha(1);
                }

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
                if (eraserView != null) {
                    eraserView.setX(rawX);
                    eraserView.setY(rawY);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (eraserView != null) {
                    eraserView.setAlpha(0);
                }
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(mLastX, mLastY);
                canvas.drawPath(mPath, eraserPaint);
                if (eraserView != null) {
                    eraserView.setAlpha(0);
                }
                break;
        }
        return true;
    }
}
