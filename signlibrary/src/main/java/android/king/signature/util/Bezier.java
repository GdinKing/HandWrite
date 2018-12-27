package android.king.signature.util;

import android.king.signature.config.ControllerPoint;

/**
 * 贝塞尔操作工具类，对点的位置和宽度控制的bezier曲线，主要是两个点，都包含了宽度和点的坐标
 *
 * @since 2018/06/15
 * @author king
 */
public class Bezier {
    /**
     * 控制点
     */
    private ControllerPoint mControl = new ControllerPoint();
    /**
     * 距离
     */
    private ControllerPoint mDestination = new ControllerPoint();
    /**
     * 下一个需要控制点
     */
    private ControllerPoint mNextControl = new ControllerPoint();
    /**
     * 资源的点
     */
    private ControllerPoint mSource = new ControllerPoint();

    /**
     * 初始化两个点，
     *
     * @param last 最后的点的信息
     * @param cur  当前点的信息,当前点的信息，当前点的是根据事件获得，同时这个当前点的宽度是经过计算的得出的
     */
    public void init(ControllerPoint last, ControllerPoint cur) {
        init(last.x, last.y, last.width, cur.x, cur.y, cur.width);
    }

    public void init(float lastx, float lasty, float lastWidth, float x, float y, float width) {
        //资源点设置，最后的点的为资源点
        mSource.set(lastx, lasty, lastWidth);
        float xmid = getMid(lastx, x);
        float ymid = getMid(lasty, y);
        float wmid = getMid(lastWidth, width);
        //距离点为平均点
        mDestination.set(xmid, ymid, wmid);
        //控制点为当前的距离点
        mControl.set(getMid(lastx, xmid), getMid(lasty, ymid), getMid(lastWidth, wmid));
        //下个控制点为当前点
        mNextControl.set(x, y, width);
    }

    public void addNode(ControllerPoint cur) {
        addNode(cur.x, cur.y, cur.width);
    }

    /**
     * 替换就的点，原来的距离点变换为资源点，控制点变为原来的下一个控制点，距离点取原来控制点的和新的的一半
     * 下个控制点为新的点
     *
     * @param x     新的点的坐标
     * @param y     新的点的坐标
     * @param width
     */
    public void addNode(float x, float y, float width) {
        mSource.set(mDestination);
        mControl.set(mNextControl);
        mDestination.set(getMid(mNextControl.x, x), getMid(mNextControl.y, y), getMid(mNextControl.width, width));
        mNextControl.set(x, y, width);
    }

    /**
     * 结合手指抬起来的动作，告诉现在的曲线控制点也必须变化，其实在这里也不需要结合着up事件使用
     * 因为在down的事件中，所有点都会被重置，然后设置这个没有多少意义，但是可以改变下个事件的朝向改变
     * 先留着，因为后面如果需要控制整个颜色的改变的话，我的依靠这个方法，还有按压的时间的变化
     */
    public void end() {
        mSource.set(mDestination);
        float x = getMid(mNextControl.x, mSource.x);
        float y = getMid(mNextControl.y, mSource.y);
        float w = getMid(mNextControl.width, mSource.width);
        mControl.set(x, y, w);
        mDestination.set(mNextControl);
    }

    /**
     * 获取点信息
     */
    public ControllerPoint getPoint(double t) {
        float x = (float) getX(t);
        float y = (float) getY(t);
        float w = (float) getW(t);
        ControllerPoint point = new ControllerPoint();
        point.set(x, y, w);
        return point;
    }

    /**
     * 三阶曲线的控制点
     */
    private double getValue(double p0, double p1, double p2, double t) {
        double a = p2 - 2 * p1 + p0;
        double b = 2 * (p1 - p0);
        double c = p0;
        return a * t * t + b * t + c;
    }

    private double getX(double t) {
        return getValue(mSource.x, mControl.x, mDestination.x, t);
    }

    private double getY(double t) {
        return getValue(mSource.y, mControl.y, mDestination.y, t);
    }

    private double getW(double t) {
        return getWidth(mSource.width, mDestination.width, t);
    }

    /**
     * 获取两个数平均值
     *
     * @param x1 一个点的x
     * @param x2 一个点的x
     */
    private float getMid(float x1, float x2) {
        return (float) ((x1 + x2) / 2.0);
    }

    private double getWidth(double w0, double w1, double t) {
        return w0 + (w1 - w0) * t;
    }

}
