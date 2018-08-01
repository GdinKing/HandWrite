package android.king.signature.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.king.signature.R;
import android.king.signature.config.PenConfig;
import android.king.signature.pen.BasePenExtend;
import android.king.signature.pen.SteelPen;
import android.king.signature.util.BitmapUtil;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * 田字格手写画板
 *
 * @author king
 * @since 2018/5/4
 */
public class GridPaintView extends View {
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private BasePenExtend mStokeBrushPen;

    /**
     * 是否有绘制
     */
    private boolean hasDraw;
    /**
     * 画笔大小
     */
    private int mStrokeWidth = PenConfig.PAINT_SIZE;

    /**
     * 画布宽度
     */
    private int mWidth;
    /**
     * 画布高度
     */
    private int mHeight;

    public GridPaintView(Context context) {
        this(context, null);
    }

    public GridPaintView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridPaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mWidth = (int) getResources().getDimension(R.dimen.grid_size);
        mHeight = (int) getResources().getDimension(R.dimen.grid_size);
        initParameter(context);
    }

    private void initParameter(Context context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        mStokeBrushPen = new SteelPen();

        initPaint();
        initCanvas();
    }


    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(PenConfig.PAINT_COLOR);
        mPaint.setStrokeWidth(PenConfig.PAINT_SIZE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAlpha(0xFF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeMiter(1.0f);
        mStokeBrushPen.setPaint(mPaint);
    }

    private void initCanvas() {
        mCanvas = new Canvas(mBitmap);
        //设置画布的颜色的问题
        mCanvas.drawColor(Color.TRANSPARENT);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        mStokeBrushPen.draw(canvas);
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mStokeBrushPen.onTouchEvent(event, mCanvas);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mGetWriteListener != null) {
                    mGetWriteListener.onWriteStart();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                hasDraw = true;
                if (mGetWriteListener != null) {
                    mGetWriteListener.onWriteStart();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mGetWriteListener != null) {
                    mGetWriteListener.onWriteCompleted(System.currentTimeMillis());
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    /**
     * @return 判断是否有绘制内容在画布上
     */
    public boolean isEmpty() {
        return !hasDraw;
    }


    /**
     * 清除画布
     */
    public void reset() {
        mBitmap.eraseColor(Color.TRANSPARENT);
        hasDraw = false;
        mStokeBrushPen.clear();
        invalidate();
    }


    public void release() {
        destroyDrawingCache();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }


    public WriteListener mGetWriteListener;

    public void setGetTimeListener(WriteListener l) {
        mGetWriteListener = l;
    }


    public interface WriteListener {
        /**
         * 开始书写
         */
        void onWriteStart();

        /**
         * 书写完毕
         * @param time 当前时间
         */
        void onWriteCompleted(long time);

    }


    /**
     * 设置画笔大小
     *
     * @param width 大小
     */
    public void setPaintWidth(int width) {
        if (mPaint != null) {
            mStrokeWidth = width;
            mPaint.setStrokeWidth(mStrokeWidth);
            mStokeBrushPen.setPaint(mPaint);
            invalidate();
        }
    }


    /**
     * 设置画笔颜色
     *
     * @param color 颜色
     */
    public void setPaintColor(int color) {
        if (mPaint != null) {
            mPaint.setColor(color);
            mStokeBrushPen.setPaint(mPaint);
            invalidate();
        }
    }


    /**
     * 构建Bitmap
     *
     * @return 所绘制的bitmap
     */
    public Bitmap buildBitmap(boolean clearBlank, int zoomSize) {
        if (!hasDraw) {
            return null;
        }
        Bitmap result = BitmapUtil.zoomImg(mBitmap, zoomSize);
        if (clearBlank) {
            result = BitmapUtil.clearLRBlank(result, 50, Color.TRANSPARENT);
        }
        return result;
    }

}
