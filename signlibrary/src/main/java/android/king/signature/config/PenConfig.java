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
     * 画笔大小等级
     */
    public static int PAINT_SIZE_LEVEL = 2;

    /**
     * 画笔颜色
     */
    public static int PAINT_COLOR = Color.parseColor(PaintSettingWindow.PEN_COLORS[0]);

    /**
     * 笔锋控制值,越小笔锋越粗,越不明显
     */
    public static final float DIS_VEL_CAL_FACTOR = 0.008f;


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
     * 获取画笔颜色
     */
    public static int getPaintColor(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        return sp.getInt("color", PAINT_COLOR);
    }

    /**
     * 保存画笔大小level
     */
    public static void savePaintTextLevel(Context context, int size) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("sizeLevel", size);
        editor.apply();
    }

    /**
     * 从sp文件中获取选中画笔大小level
     */
    public static int getPaintTextLevel(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        return sp.getInt("sizeLevel", PAINT_SIZE_LEVEL);
    }

    /**
     * 获取是否第一次打开
     */
    public static boolean getFirst(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        return sp.getBoolean("isFirst", true);
    }

    /**
     * 设置是否第一次打开
     */
    public static void setFirst(Context context, boolean isFirst) {
        SharedPreferences sp = context.getSharedPreferences(SP_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isFirst", isFirst);
        editor.apply();
    }
}
