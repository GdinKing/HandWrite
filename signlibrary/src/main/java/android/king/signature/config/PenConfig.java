package android.king.signature.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.king.signature.view.PaintSettingWindow;

/**
 * 画笔配置
 *
 * @author king
 * @since 2018/06/15
 */
public class PenConfig {

    /**
     * 画笔操作
     */
    public static final int TYPE_PEN = 0;

    /**
     * 清除操作
     */
    public static final int TYPE_CLEAR = -1;

    /**
     * 画笔大小
     */
    public static int PAINT_SIZE = PaintSettingWindow.PEN_SIZES[2];

    /**
     * 画笔颜色
     */
    public static int PAINT_COLOR = Color.parseColor(PaintSettingWindow.PEN_COLORS[0]);

    /**
     * 笔锋控制值,越小笔锋越粗
     */
    public static final float DIS_VEL_CAL_FACTOR = 0.005f;

    /**
     * 绘制计算的次数，数值越小计算的次数越多
     */
    public static final int STEP_FACTOR = 20;

    /**
     * 主题颜色
     */
    public static int THEME_COLOR = Color.parseColor("#0c53ab");

    public static final String SAVE_PATH = "path";
    private static final String SP_SETTING = "sp_sign_setting";

    /**
     * jpg格式
     */
    public static final String FORMAT_JPG = "JPG";
    /**
     * png格式
     */
    public static final String FORMAT_PNG = "PNG";

    /**
     * 保存画笔颜色设置
     */
    public static void setPaintColor(Context context, int color) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("color", color);
        editor.apply();
    }

    /**
     * 保存画笔大小设置
     */
    public static void savePaintSize(Context context, int size) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("size", size);
        editor.apply();
    }

    /**
     * 获取画笔颜色
     */
    public static int getPaintColor(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        return sp.getInt("color", PAINT_COLOR);
    }

    /**
     * 从sp文件中获取画笔大小
     */
    public static int getPaintSize(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        return sp.getInt("size", PAINT_SIZE);
    }
}
