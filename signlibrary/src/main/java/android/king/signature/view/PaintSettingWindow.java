package android.king.signature.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.king.signature.R;
import android.king.signature.config.PenConfig;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;


/**
 * 画笔设置窗口
 *
 * @author king
 * @since 2018-06-04
 */
public class PaintSettingWindow extends PopupWindow {
    public static final String[] PEN_COLORS = new String[]{"#101010", "#027de9", "#0cba02", "#f9d403", "#ec041f"};
    public static final int[] PEN_SIZES = new int[]{20, 40, 50, 60, 70};

    private Context context;
    private CircleView lastSelectColorView;
    private CircleView lastSelectSizeView;
    private int selectSize;
    private int selectColor;
    private OnSettingListener settingListener;

    public PaintSettingWindow(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(context).inflate(R.layout.sign_paint_setting, null);
        LinearLayout container = rootView.findViewById(R.id.color_container);
        for (int i = 0; i < container.getChildCount(); i++) {
            final int index = i;
            final CircleView circleView = (CircleView) container.getChildAt(i);
            if (circleView.getPaintColor() == PenConfig.PAINT_COLOR) {
                circleView.showBorder(true);
                lastSelectColorView = circleView;
            }

            circleView.setOnClickListener(v -> {
                if (lastSelectColorView != null) {
                    lastSelectColorView.showBorder(false);
                }
                circleView.showBorder(true);
                selectColor = Color.parseColor(PEN_COLORS[index]);
                lastSelectColorView = circleView;
                PenConfig.PAINT_COLOR = selectColor;
                PenConfig.setPaintColor(context, PenConfig.PAINT_COLOR);
                if (settingListener != null) {
                    settingListener.onColorSetting(PenConfig.PAINT_COLOR);
                }
            });
        }
        LinearLayout sizeContainer = rootView.findViewById(R.id.size_container);
        for (int i = 0; i < sizeContainer.getChildCount(); i++) {
            final int index = i;
            final CircleView circleView = (CircleView) sizeContainer.getChildAt(i);
            if (circleView.getCircleRadius() == PenConfig.PAINT_SIZE) {
                circleView.showBorder(true);
                lastSelectSizeView = circleView;
            }
            circleView.setOnClickListener(v -> {
                if (lastSelectSizeView != null) {
                    lastSelectSizeView.showBorder(false);
                }
                circleView.showBorder(true);
                selectSize = PEN_SIZES[index];
                lastSelectSizeView = circleView;
                PenConfig.PAINT_SIZE = selectSize;
                PenConfig.savePaintSize(context, PenConfig.PAINT_SIZE);
                if (settingListener != null) {
                    settingListener.onSizeSetting(PenConfig.PAINT_SIZE);
                }
            });
        }
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setContentView(rootView);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
    }

    public interface OnSettingListener {
        void onColorSetting(int color);

        void onSizeSetting(int size);
    }

    public void setSettingListener(OnSettingListener settingListener) {
        this.settingListener = settingListener;
    }
}
