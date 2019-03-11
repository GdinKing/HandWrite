package android.king.signature.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import android.king.signature.config.PenConfig;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图像操作工具类
 *
 * @author king
 * @since 2018/07/05
 */
public class BitmapUtil {

    /**
     * 逐行扫描 清除边界空白
     *
     * @param blank 边距留多少个像素
     * @param color 背景色限定
     * @return 清除边界后的Bitmap
     */
    public static Bitmap clearBlank(Bitmap mBitmap, int blank, int color) {
        if (mBitmap != null) {
            int height = mBitmap.getHeight();
            int width = mBitmap.getWidth();
            int top = 0, left = 0, right = 0, bottom = 0;
            int[] widthPixels = new int[width];
            boolean isStop;
            for (int y = 0; y < height; y++) {
                mBitmap.getPixels(widthPixels, 0, width, 0, y, width, 1);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {

                        top = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int y = height - 1; y >= 0; y--) {
                mBitmap.getPixels(widthPixels, 0, width, 0, y, width, 1);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {
                        bottom = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            widthPixels = new int[height];
            for (int x = 0; x < width; x++) {
                mBitmap.getPixels(widthPixels, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {
                        left = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int x = width - 1; x > 0; x--) {
                mBitmap.getPixels(widthPixels, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : widthPixels) {
                    if (pix != color) {
                        right = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            if (blank < 0) {
                blank = 0;
            }
            left = left - blank > 0 ? left - blank : 0;
            top = top - blank > 0 ? top - blank : 0;
            right = right + blank > width - 1 ? width - 1 : right + blank;
            bottom = bottom + blank > height - 1 ? height - 1 : bottom + blank;
            return Bitmap.createBitmap(mBitmap, left, top, right - left, bottom - top);
        } else {
            return null;
        }
    }

    /**
     * 清除bitmap左右边界空白
     *
     * @param mBitmap 源图
     * @param blank   边距留多少个像素
     * @param color   背景色限定
     * @return 清除后的bitmap
     */
    public static Bitmap clearLRBlank(Bitmap mBitmap, int blank, int color) {
        if (mBitmap != null) {
            int height = mBitmap.getHeight();
            int width = mBitmap.getWidth();
            int left = 0, right = 0;
            int[] pixs = new int[height];
            boolean isStop;
            for (int x = 0; x < width; x++) {
                mBitmap.getPixels(pixs, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != color) {
                        left = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int x = width - 1; x > 0; x--) {
                mBitmap.getPixels(pixs, 0, 1, x, 0, 1, height);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != color) {
                        right = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            if (blank < 0) {
                blank = 0;
            }
            left = left - blank > 0 ? left - blank : 0;
            right = right + blank > width - 1 ? width - 1 : right + blank;
            return Bitmap.createBitmap(mBitmap, left, 0, right - left, height);
        } else {
            return null;
        }
    }

    /**
     * 给Bitmap添加背景色
     *
     * @param srcBitmap 源图
     * @param color     背景颜色
     * @return 修改背景后的bitmap
     */
    public static Bitmap drawBgToBitmap(Bitmap srcBitmap, int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), paint);
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return bitmap;
    }

    /**
     * 保存图像到本地
     *
     * @param bmp     源图
     * @param quality 压缩质量
     * @param format  图片格式
     * @return 保存后的图片地址
     */
    public static String saveImage(Context context, Bitmap bmp, int quality, String format) {
        if (bmp == null) {
            return null;
        }
        FileOutputStream fos = null;
        try {
            String appDir = context.getExternalCacheDir().getAbsolutePath();
            File saveDir = new File(appDir, "signImg");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            String fileName;
            Bitmap.CompressFormat compressFormat;
            if (PenConfig.FORMAT_JPG.equals(format)) {
                compressFormat = Bitmap.CompressFormat.JPEG;
                fileName = System.currentTimeMillis() + ".jpg";
            } else {
                compressFormat = Bitmap.CompressFormat.PNG;
                fileName = System.currentTimeMillis() + ".png";
            }
            File file = new File(saveDir, fileName);
            fos = new FileOutputStream(file);
            bmp.compress(compressFormat, quality, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 根据宽度缩放图片，高度等比例
     *
     * @param bm       源图
     * @param newWidth 新宽度
     * @return 缩放后的bitmap
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float ratio = ((float) newWidth) / width;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    /**
     * 缩放图片至指定宽高
     *
     * @param bm        源图
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放后的bitmap
     */
    public static Bitmap zoomImage(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    /**
     * 根据宽高之中最大缩放比缩放图片
     *
     * @param bitmap    源图
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放后的bitmap
     */
    public static Bitmap zoomImg(Bitmap bitmap, int newWidth,
                                 int newHeight) {
        // 获取这个图片的宽和高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float ratio = Math.max(scaleWidth, scaleHeight);
        // 缩放图片动作
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
    }


    /**
     * 给图片右下角添加水印
     *
     * @param src       源图
     * @param watermark 水印图
     * @param bgColor   背景色
     * @param fixed     源图是否固定大小，固定则在源图上绘制印章，不固定则动态改变图片大小
     * @return 添加水印后的图片
     */
    public static Bitmap addWaterMask(Bitmap src, Bitmap watermark, int bgColor, boolean fixed) {
        int w = src.getWidth();
        int h = src.getHeight();
        //获取原始水印图片的宽、高
        int w2 = watermark.getWidth();
        int h2 = watermark.getHeight();

        //合理控制水印大小
        Matrix matrix1 = new Matrix();
        float ratio;

        ratio = (float) w2 / w;
        if (ratio > 1.0f && ratio <= 2.0f) {
            ratio = 0.7f;
        } else if (ratio > 2.0f) {
            ratio = 0.5f;
        } else if (ratio <= 0.2f) {
            ratio = 2.0f;
        } else if (ratio < 0.3f) {
            ratio = 1.5f;
        } else if (ratio <= 0.4f) {
            ratio = 1.2f;
        } else if (ratio < 1.0f) {
            ratio = 1.0f;
        }
        matrix1.postScale(ratio, ratio);
        watermark = Bitmap.createBitmap(watermark, 0, 0, w2, h2, matrix1, true);

        //获取新的水印图片的宽、高
        w2 = watermark.getWidth();
        h2 = watermark.getHeight();
        if (!fixed) {
            if (w < 1.5 * w2) {
                w = w + w2;
            }
            if (h < 2 * h2) {
                h = h + h2;
            }
        }
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas cv = new Canvas(result);
        cv.drawColor(bgColor);
        //在canvas上绘制原图和新的水印图
        cv.drawBitmap(src, 0, 0, null);
        //水印图绘制在画布的右下角，距离右边和底部都为20
        cv.drawBitmap(watermark, w - w2 - 20, h - h2 - 20, null);
        cv.save();
        cv.restore();
        return result;
    }

    /**
     * 修改图片颜色
     *
     * @param inBitmap 源图
     * @param color    颜色
     * @return 修改颜色后的图片
     */
    public static Bitmap changeBitmapColor(Bitmap inBitmap, int color) {
        if (inBitmap == null) {
            return null;
        }
        Bitmap outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inBitmap, 0, 0, paint);
        return outBitmap;
    }

    /**
     * 设置ImageView的图片，支持改变图片颜色
     * @param iv
     * @param id
     * @param color
     */
    public static void setImage(ImageView iv, int id, int color) {
        Bitmap bitmap = BitmapFactory.decodeResource(iv.getResources(), id);
        iv.setImageBitmap(BitmapUtil.changeBitmapColor(bitmap, color));
    }

}
