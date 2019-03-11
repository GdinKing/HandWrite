package android.king.signature.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import android.king.signature.R;
import android.king.signature.config.PenConfig;

/**
 * 画笔设置窗口
 *
 * @author king
 * @since 2018-06-04
 */
public class PaintSettingWindow extends PopupWindow {
    public static final String[] PEN_COLORS = new String[]{"#101010", "#027de9", "#0cba02", "#f9d403", "#ec041f"};
    public static final int[] PEN_SIZES = new int[]{5, 15, 20, 25, 30};

    private Context context;
    private CircleView lastSelectColorView;
    private CircleView lastSelectSizeView;
    private int selectColor;
    private View rootView;
    private OnSettingListener settingListener;

    public PaintSettingWindow(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(context).inflate(R.layout.sign_paint_setting, null);
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
                PenConfig.setPaintColor(context, selectColor);
                if (settingListener != null) {
                    settingListener.onColorSetting(selectColor);
                }
            });
        }
        LinearLayout sizeContainer = rootView.findViewById(R.id.size_container);
        for (int i = 0; i < sizeContainer.getChildCount(); i++) {
            final int index = i;
            final CircleView circleView = (CircleView) sizeContainer.getChildAt(i);
            if (circleView.getRadiusLevel() == PenConfig.PAINT_SIZE_LEVEL) {
                circleView.showBorder(true);
                lastSelectSizeView = circleView;
            }
            circleView.setOnClickListener(v -> {
                if (lastSelectSizeView != null) {
                    lastSelectSizeView.showBorder(false);
                }
                circleView.showBorder(true);
                lastSelectSizeView = circleView;
                PenConfig.PAINT_SIZE_LEVEL = index;
                PenConfig.savePaintTextLevel(context, index);
                if (settingListener != null) {
                    settingListener.onSizeSetting(index);
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

    /**
     * 显示在左上角
     */
    public void popAtTopLeft() {
        View sv = rootView.findViewById(R.id.size_container);
        View cv = rootView.findViewById(R.id.color_container);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 10, 20, 0);
        lp.gravity = Gravity.CENTER;
        sv.setLayoutParams(lp);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(20, 0, 20, 40);
        lp1.gravity = Gravity.CENTER;
        cv.setLayoutParams(lp1);
        rootView.setBackgroundResource(R.drawable.sign_top_left_pop_bg);
    }

    /**
     * 显示在右上角
     */
    public void popAtTopRight() {
        View sv = rootView.findViewById(R.id.size_container);
        View cv = rootView.findViewById(R.id.color_container);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 10, 20, 0);
        lp.gravity = Gravity.CENTER;
        sv.setLayoutParams(lp);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(20, 0, 20, 40);
        lp1.gravity = Gravity.CENTER;
        cv.setLayoutParams(lp1);
        rootView.setBackgroundResource(R.drawable.sign_top_right_pop_bg);
    }

    /**
     * 显示在右下角
     */
    public void popAtBottomRight() {
        View sv = rootView.findViewById(R.id.size_container);
        View cv = rootView.findViewById(R.id.color_container);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 40, 20, 0);
        lp.gravity = Gravity.CENTER;
        sv.setLayoutParams(lp);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(20, 0, 20, 10);
        lp1.gravity = Gravity.CENTER;
        cv.setLayoutParams(lp1);
        rootView.setBackgroundResource(R.drawable.sign_bottom_right_pop_bg);
    }

    /**
     * 显示在左边
     */
    public void popAtLeft() {
        View sv = rootView.findViewById(R.id.size_container);
        View cv = rootView.findViewById(R.id.color_container);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 20, 55, 0);
        lp.gravity = Gravity.CENTER;
        sv.setLayoutParams(lp);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(40, 0, 55, 20);
        lp1.gravity = Gravity.CENTER;
        cv.setLayoutParams(lp1);
        rootView.setBackgroundResource(R.drawable.sign_left_pop_bg);
    }

    public interface OnSettingListener {
        void onColorSetting(int color);

        void onSizeSetting(int index);
    }

    public void setSettingListener(OnSettingListener settingListener) {
        this.settingListener = settingListener;
    }
}
