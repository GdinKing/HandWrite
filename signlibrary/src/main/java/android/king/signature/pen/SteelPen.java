package android.king.signature.pen;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import android.king.signature.config.ControllerPoint;


/**
 * 钢笔
 *
 * @since 2018/06/15
 * @author king
 */
public class SteelPen extends BasePen {

    @Override
    protected void doPreDraw(Canvas canvas) {
        for (int i = 1; i < mHWPointList.size(); i++) {
            ControllerPoint point = mHWPointList.get(i);
            drawToPoint(canvas, point, mPaint);
            mCurPoint = point;
        }
    }

    @Override
    protected void doMove(double curDis) {
        int steps = 1 + (int) curDis / STEP_FACTOR;
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }
    }

    @Override
    protected void doDraw(Canvas canvas, ControllerPoint point, Paint paint) {
        drawLine(canvas, mCurPoint.x, mCurPoint.y, mCurPoint.width, point.x,
                point.y, point.width, paint);
    }

    /**
     * 绘制方法，实现笔锋效果
     */
    private void drawLine(Canvas canvas, double x0, double y0, double w0, double x1, double y1, double w1, Paint paint) {
        //求两个数字的平方根 x的平方+y的平方在开方记得X的平方+y的平方=1，这就是一个园
        double curDis = Math.hypot(x0 - x1, y0 - y1);
        int steps;
        //绘制的笔的宽度是多少，绘制多少个椭圆
        if (paint.getStrokeWidth() < 6) {
            steps = 1 + (int) (curDis / 2);
        } else if (paint.getStrokeWidth() > 60) {
            steps = 1 + (int) (curDis / 4);
        } else {
            steps = 1 + (int) (curDis / 3);
        }
        double deltaX = (x1 - x0) / steps;
        double deltaY = (y1 - y0) / steps;
        double deltaW = (w1 - w0) / steps;
        double x = x0;
        double y = y0;
        double w = w0;

        for (int i = 0; i < steps; i++) {
            RectF oval = new RectF();
            float top = (float) (y - w / 2.0f);
            float left = (float) (x - w / 4.0f);
            float right = (float) (x + w / 4.0f);
            float bottom = (float) (y + w / 2.0f);
            oval.set(left, top, right, bottom);
            //最基本的实现，通过点控制线，绘制椭圆
            canvas.drawOval(oval, paint);
            x += deltaX;
            y += deltaY;
            w += deltaW;
        }
    }
}
