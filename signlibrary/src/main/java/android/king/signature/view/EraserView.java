package android.king.signature.view;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import android.king.signature.config.PenConfig;
import android.king.signature.util.StatusBarCompat;
import android.king.signature.util.SystemUtil;

/***
 * 名称：EraserView
 * 描述：橡皮擦指示器
 * 最近修改时间：2018年09月13日 16:57分
 * @since 2018-09-13
 * @author king
 */
public class EraserView extends View {

    private Paint paint;
    private int statusBarHeight;

    public EraserView(Context context) {
        this(context, null);
    }

    public EraserView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EraserView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundDrawable(null);
        setBackground(null);
        setDrawingCacheEnabled(false);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        if (Build.MODEL.contains("EBEN")) {//E人E本顶部没有状态栏
            statusBarHeight = 0;
        } else {
            statusBarHeight = StatusBarCompat.getStatusBarHeight(context);
        }
    }

    @Override
    public void setX(float x) {
        super.setX(x - getWidth() / 2);
    }

    @Override
    public void setY(float y) {
        super.setY(y - getHeight() / 2 - statusBarHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        paint.setColor(Color.LTGRAY);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - 8, paint);
    }
}
