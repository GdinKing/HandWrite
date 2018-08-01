package android.king.signature.pen;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.king.signature.config.ControllerPoint;
import android.king.signature.config.MotionElement;
import android.king.signature.config.PenConfig;
import android.king.signature.util.Bezier;
import android.view.MotionEvent;


import java.util.ArrayList;

/**
 * 画笔操作基类
 *
 * @author king
 * @since 2018/06/15
 */
public abstract class BasePenExtend {

    protected ArrayList<ControllerPoint> mHWPointList = new ArrayList<>();
    protected ArrayList<ControllerPoint> mPointList = new ArrayList<ControllerPoint>();
    protected ControllerPoint mLastPoint = new ControllerPoint(0, 0);
    protected Paint mPaint;

    /**
     * 笔的宽度信息
     */
    private double mBaseWidth;

    private double mLastVel;
    private double mLastWidth;

    protected Bezier mBezier = new Bezier();

    protected ControllerPoint mCurPoint;

    public void setPaint(Paint paint) {
        mPaint = paint;
        mBaseWidth = paint.getStrokeWidth();
    }

    public void draw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        //点的集合少 不去绘制
        if (mHWPointList == null || mHWPointList.size() < 1) {
            return;
        }
        mCurPoint = mHWPointList.get(0);
        doPreDraw(canvas);
    }


    public boolean onTouchEvent(MotionEvent event, Canvas canvas) {
        // event会被下一次事件重用，这里必须生成新的，否则会有问题
        int action = event.getAction() & event.getActionMasked();
        MotionEvent event2 = MotionEvent.obtain(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onDown(createMotionElement(event2), canvas);
                return true;
            case MotionEvent.ACTION_MOVE:
                onMove(createMotionElement(event2), canvas);
                return true;
            case MotionEvent.ACTION_UP:
                onUp(createMotionElement(event2), canvas);
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 按下的事件
     */
    public void onDown(MotionElement mElement, Canvas canvas) {
        if (mPaint == null) {
            throw new NullPointerException("paint不能为null");
        }
        if (getNewPaint(mPaint) != null) {
            Paint paint = getNewPaint(mPaint);
            mPaint = paint;
            paint = null;
        }
        mPointList.clear();
        mHWPointList.clear();
        //记录down的控制点的信息
        ControllerPoint curPoint = new ControllerPoint(mElement.x, mElement.y);
        //如果用笔画的画我的屏幕，记录他宽度的和压力值的乘
        if (mElement.toolType == MotionEvent.TOOL_TYPE_STYLUS) {
            mLastWidth = mElement.pressure * mBaseWidth;
        } else {
            //如果是手指画的，我们取他的0.8
            mLastWidth = 0.8 * mBaseWidth;
        }
        //down下的点的宽度
        curPoint.width = (float) mLastWidth;
        mLastVel = 0;
        mPointList.add(curPoint);
        //记录当前的点
        mLastPoint = curPoint;
    }

    protected Paint getNewPaint(Paint paint) {
        return null;
    }

    /**
     * 手指移动的事件
     *
     */
    public void onMove(MotionElement mElement, Canvas canvas) {

        ControllerPoint curPoint = new ControllerPoint(mElement.x, mElement.y);
        double deltaX = curPoint.x - mLastPoint.x;
        double deltaY = curPoint.y - mLastPoint.y;
        //deltaX和deltay平方和的二次方根 想象一个例子 1+1的平方根为1.4 （x²+y²）开根号
        //同理，当滑动的越快的话，deltaX+deltaY的值越大，这个越大的话，curDis也越大
        double curDis = Math.hypot(deltaX, deltaY);
        //我们求出的这个值越小，画的点或者是绘制椭圆形越多，这个值越大的话，绘制的越少，笔就越细，宽度越小
        double curVel = curDis * PenConfig.DIS_VEL_CAL_FACTOR;
        double curWidth;
        //点的集合少，我们得必须改变宽度,每次点击的down的时候，这个事件
        if (mPointList.size() < 2) {
            if (mElement.toolType == MotionEvent.TOOL_TYPE_STYLUS) {
                curWidth = mElement.pressure * mBaseWidth;
            } else {
                curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5,
                        mLastWidth);
            }
            curPoint.width = (float) curWidth;
            mBezier.init(mLastPoint, curPoint);
        } else {
            mLastVel = curVel;
            if (mElement.toolType == MotionEvent.TOOL_TYPE_STYLUS) {
                curWidth = mElement.pressure * mBaseWidth;
            } else {
                curWidth = calcNewWidth(curVel, mLastVel, curDis, 1.5,
                        mLastWidth);
            }
            curPoint.width = (float) curWidth;
            mBezier.addNode(curPoint);
        }
        //每次移动的话，这里赋值新的值
        mLastWidth = curWidth;
        mPointList.add(curPoint);
        doMove(curDis);
        mLastPoint = curPoint;
    }

    /**
     * 手指抬起来的事件
     */
    public void onUp(MotionElement mElement, Canvas canvas) {

        mCurPoint = new ControllerPoint(mElement.x, mElement.y);
        double deltaX = mCurPoint.x - mLastPoint.x;
        double deltaY = mCurPoint.y - mLastPoint.y;
        double curDis = Math.hypot(deltaX, deltaY);
        if (mElement.toolType == MotionEvent.TOOL_TYPE_STYLUS) {
            mCurPoint.width = (float) (mElement.pressure * mBaseWidth);
        } else {
            mCurPoint.width = 0;
        }

        mPointList.add(mCurPoint);

        mBezier.addNode(mCurPoint);

        int steps = 1 + (int) curDis / PenConfig.STEP_FACTOR;
        double step = 1.0 / steps;
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }
        mBezier.end();
        for (double t = 0; t < 1.0; t += step) {
            ControllerPoint point = mBezier.getPoint(t);
            mHWPointList.add(point);
        }
        draw(canvas);
        clear();
    }

    /**
     * 计算新的宽度信息
     */
    public double calcNewWidth(double curVel, double lastVel, double curDis,
                               double factor, double lastWidth) {
        double calVel = curVel * 0.6 + lastVel * (1 - 0.6);
        double vfac = Math.log(factor * 2.0f) * (-calVel);
        double calWidth = mBaseWidth * Math.exp(vfac);
        return calWidth;
    }

    /**
     * 创建触摸点信息
     */
    public MotionElement createMotionElement(MotionEvent motionEvent) {
        MotionElement motionElement = new MotionElement(motionEvent.getX(), motionEvent.getY(),
                motionEvent.getPressure(), motionEvent.getToolType(0));
        return motionElement;
    }

    /**
     * 清除缓存的触摸点
     */
    public void clear() {
        mPointList.clear();
        mHWPointList.clear();
    }

    /**
     * 绘制
     * 当现在的点和触摸点的位置在一起的时候不用去绘制
     */
    protected void drawToPoint(Canvas canvas, ControllerPoint point, Paint paint) {
        if ((mCurPoint.x == point.x) && (mCurPoint.y == point.y)) {
            return;
        }
        //水彩笔的效果和钢笔的不太一样，交给自己去实现
        doDraw(canvas, point, paint);
    }


    /**
     * 判断笔是否为空
     */
    public boolean isNullPaint() {
        return mPaint == null;
    }

    /**
     * 移动的时候的处理方法
     */
    protected abstract void doMove(double f);

    /**
     * 绘制方法
     */
    protected abstract void doDraw(Canvas canvas, ControllerPoint point, Paint paint);

    /**
     * onDraw之前的操作
     */
    protected abstract void doPreDraw(Canvas canvas);
}
