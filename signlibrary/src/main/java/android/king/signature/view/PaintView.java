package android.king.signature.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.king.signature.config.PenConfig;
import android.king.signature.pen.BasePenExtend;
import android.king.signature.pen.SteelPen;
import android.king.signature.util.BitmapUtil;
import android.king.signature.util.StepOperator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * 手写画板
 *
 * @author king
 * @since 2018/5/4
 */
public class PaintView extends View {
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Context mContext;
    private BasePenExtend mStokeBrushPen;

    /**
     * 是否有绘制
     */
    private boolean hasDraw;

    /**
     * 是否滚动模式
     */
    private boolean isScroll = false;
    /**
     * 记录当前画笔类型
     */
    private int mPenType = PenConfig.TYPE_PEN;
    /**
     * 记录上次选择的画笔类型（方便在清除画布时切换回上一次画笔类型）
     */
    private int lastPenType = PenConfig.TYPE_PEN;

    /**
     * 画笔轨迹记录
     */
    private StepOperator mStepOperation;

    private StepCallback mCallback;


    /**
     * 可以撤销
     */
    private boolean mCanUndo;
    private boolean mCanRedo;

    private int mWidth;
    private int mHeight;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化画板
     *
     * @param width  画板宽度
     * @param height 画板高度
     * @param path   初始图片路径
     */
    public void init(Context context, int width, int height, String path) {
        this.mContext = context;
        this.mWidth = width;
        this.mHeight = height;

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        mStokeBrushPen = new SteelPen();

        initPaint();
        initCanvas();

        mStepOperation = new StepOperator();
        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            resize(bitmap, mWidth, mHeight);
        } else {
            mStepOperation.addBitmap(mBitmap);
        }

    }

    /**
     * 初始画笔设置
     */
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
        //设置画布的背景色为透明
        mCanvas.drawColor(Color.TRANSPARENT);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        switch (mPenType) {
            case PenConfig.TYPE_PEN:
                mStokeBrushPen.draw(canvas);
                break;
            case PenConfig.TYPE_CLEAR:
                reset();
                break;
            default:
                break;
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(!isScroll);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isScroll) {
            return false;
        }
        mCanUndo = true;
        mStokeBrushPen.onTouchEvent(event, mCanvas);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                hasDraw = true;

                break;
            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onOperateStatusChanged();
                }
                break;
            default:
                break;
        }
        invalidate();
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (mStepOperation != null && hasDraw) {
                mStepOperation.addBitmap(mBitmap);
            }
        }
        return true;
    }


    /**
     * @return 判断是否有绘制内容在画布上
     */
    public boolean isEmpty() {
        return !hasDraw;
    }

    /**
     * 撤销
     */
    public void undo() {

        if (mStepOperation == null || !mCanUndo) {
            return;
        }
        if (!mStepOperation.currentIsFirst()) {
            mCanUndo = true;
            mStepOperation.undo(mBitmap);
            hasDraw = true;
            invalidate();

            if (mStepOperation.currentIsFirst()) {
                mCanUndo = false;
                hasDraw = false;
            }
        } else {
            mCanUndo = false;
            hasDraw = false;
        }
        if (!mStepOperation.currentIsLast() && !mStepOperation.currentIsFirst()) {
            mCanRedo = true;
        }
        if (mCallback != null) {
            mCallback.onOperateStatusChanged();
        }
    }

    /**
     * 恢复
     */
    public void redo() {
        if (mStepOperation == null || !mCanRedo) {
            return;
        }
        if (!mStepOperation.currentIsLast()) {
            mCanRedo = true;
            mStepOperation.redo(mBitmap);
            hasDraw = true;
            invalidate();
            if (mStepOperation.currentIsLast()) {
                mCanRedo = false;
            }
        } else {
            mCanRedo = false;
        }
        if (!mStepOperation.currentIsLast() && !mStepOperation.currentIsFirst()) {
            mCanUndo = true;
        }
        if (mCallback != null) {
            mCallback.onOperateStatusChanged();
        }
    }

    /**
     * 清除画布，记得清除点的集合
     */
    public void reset() {
        mBitmap.eraseColor(Color.TRANSPARENT);
        hasDraw = false;
        mStokeBrushPen.clear();
        mPenType = lastPenType;
        if (mCallback != null) {
            mCallback.onOperateStatusChanged();
        }
        invalidate();
    }


    public void release() {
        destroyDrawingCache();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if (mStepOperation != null) {
            mStepOperation.freeBitmaps();
            mStepOperation = null;
        }
    }


    public interface StepCallback {
        /**
         * 操作变更
         */
        void onOperateStatusChanged();
    }

    public void setStepCallback(StepCallback callback) {
        this.mCallback = callback;
    }

    /**
     * 设置画笔样式
     *
     * @param penType 画笔类型
     */
    public void setPenType(int penType) {
        mPenType = penType;
        if (penType != PenConfig.TYPE_CLEAR) {
            lastPenType = penType;
        }
        if (mPenType == PenConfig.TYPE_PEN) {
            mStokeBrushPen = new SteelPen();
        }
        if (mStokeBrushPen.isNullPaint()) {
            mStokeBrushPen.setPaint(mPaint);
        }
        invalidate();
    }

    /**
     * 设置画笔大小
     *
     * @param width 大小
     */
    public void setPaintWidth(int width) {
        if (mPaint != null) {
            mPaint.setStrokeWidth(width);
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
    public Bitmap buildAreaBitmap(boolean isCrop) {
        if (!hasDraw) {
            return null;
        }
        Bitmap result;
        if (isCrop) {
            result = BitmapUtil.clearBlank(mBitmap, 50, Color.TRANSPARENT);
        } else {
            result = mBitmap;
        }
        destroyDrawingCache();
        return result;
    }

    public boolean canUndo() {
        return mCanUndo;
    }

    public boolean canRedo() {
        return mCanRedo;
    }

    public void setScroll(boolean scroll) {
        this.isScroll = scroll;
    }

    public boolean isScroll() {
        return isScroll;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * 图片大小调整适配画布宽高
     *
     * @param bitmap 源图
     * @param width  新宽度
     * @param height 新高度
     */
    public void resize(Bitmap bitmap, int width, int height) {

        if (mBitmap != null) {
            if (width >= height) {
                height = width * mBitmap.getHeight() / mBitmap.getWidth();
            } else {
                width = height * mBitmap.getWidth() / mBitmap.getHeight();
            }
            this.mWidth = width;
            this.mHeight = height;

            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
            restoreLastBitmap(bitmap, mBitmap);
            initCanvas();
            if (mStepOperation != null) {
                mStepOperation.addBitmap(mBitmap);
            }
            invalidate();
        }
    }

    /**
     * 恢复最后画的bitmap
     *
     * @param srcBitmap 最后的bitmap
     * @param newBitmap 新bitmap
     */
    private void restoreLastBitmap(Bitmap srcBitmap, Bitmap newBitmap) {
        try {
            if (srcBitmap == null || srcBitmap.isRecycled()) {
                return;
            }
            srcBitmap = BitmapUtil.zoomImg(srcBitmap, newBitmap.getWidth(), newBitmap.getHeight());
            //缩放后如果还是超出新图宽高，继续缩放
            if (srcBitmap.getWidth() > newBitmap.getWidth() || srcBitmap.getHeight() > newBitmap.getHeight()) {
                srcBitmap = BitmapUtil.zoomImage(srcBitmap, newBitmap.getWidth(), newBitmap.getHeight());
            }
            //保存所有的像素的数组，图片宽×高
            int[] pixels = new int[srcBitmap.getWidth() * srcBitmap.getHeight()];
            srcBitmap.getPixels(pixels, 0, srcBitmap.getWidth(), 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
            newBitmap.setPixels(pixels, 0, srcBitmap.getWidth(), 0, 0,
                    srcBitmap.getWidth(), srcBitmap.getHeight());
        } catch (OutOfMemoryError e) {
        }

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

                float value = mWidth;

                if (attr == 0) {
                    if (mBitmap != null) {
                        value = mBitmap.getWidth();
                    }
                    // 控件的宽度
                    newSize = (int) (getPaddingLeft() + value + getPaddingRight());

                } else if (attr == 1) {
                    value = mHeight;
                    if (mBitmap != null) {
                        value = mBitmap.getHeight();
                    }
                    // 控件的高度
                    newSize = (int) (getPaddingTop() + value + getPaddingBottom());
                }
                break;
            default:
                break;
        }
        return newSize;
    }

    public Bitmap getLastBitmap() {
        return mBitmap;
    }

}
