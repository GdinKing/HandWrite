package android.king.signature.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * 撤销/恢复 操作工具类
 *
 * @author king
 * @since 2018/06/27
 */
public class StepOperator {

    /**
     * 缓存步骤数
     */
    private static final int CAPACITY = 12;
    /**
     * 保存每一步绘制的bitmap
     */
    private List<Bitmap> mBitmaps = null;

    /**
     * 允许缓存Bitmap的最大宽度限制，过大容易内存溢出
     */
    public static int MAX_CACHE_BITMAP_WIDTH = 1024;
    /**
     * 当前位置
     */
    private int currentIndex;

    private boolean isUndo = false;

    /**
     * 最大缓存内存大小
     */
    private int cacheMemory;

    public StepOperator() {
        if (mBitmaps == null) {
            mBitmaps = new ArrayList<>();
        }
        currentIndex = -1;
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        cacheMemory = maxMemory / 3;
    }

    /**
     * 重置
     */
    public void reset() {
        if (mBitmaps != null) {
            for (Bitmap bitmap : mBitmaps) {
                bitmap.recycle();
            }
            mBitmaps.clear();
        }
        currentIndex = -1;
    }

    /**
     * 内存是否足够
     *
     * @return
     */
    private boolean isMemoryEnable() {
        int bitmapCache = 0;
        for (Bitmap bitmap : mBitmaps) {
            bitmapCache += bitmap.getRowBytes() * bitmap.getHeight();
        }
        if (bitmapCache > cacheMemory) {
            return false;
        }
        return true;
    }

    /**
     * 缓存绘制的Bitmap
     *
     * @param bitmap
     */
    public void addBitmap(Bitmap bitmap) {
        if (mBitmaps == null) {
            return;
        }
        try {
            if (!isMemoryEnable() && mBitmaps.size() > 1) {
                mBitmaps.get(1).recycle();
                //删除第一笔（0的位置有空的占位图）
                mBitmaps.remove(1);
            }
            if (currentIndex != -1 && isUndo) {
                for (int i = currentIndex + 1; i < mBitmaps.size(); i++) {
                    mBitmaps.get(i).recycle();
                }
                mBitmaps = mBitmaps.subList(0, currentIndex + 1);
                isUndo = false;
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
            mBitmaps.add(bitmap);
            currentIndex = mBitmaps.size() - 1;

            if (mBitmaps.size() > CAPACITY) {
                mBitmaps.get(1).recycle();
                //删除第一笔（0的位置有空的占位图）
                mBitmaps.remove(1);
            }
        } catch (Exception e) {
        } catch (OutOfMemoryError e) {
        }
    }

    /**
     * 判断当前是否第一步
     *
     * @return
     */
    public boolean currentIsFirst() {
        if (mBitmaps != null && currentIndex == 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前是否最后一步
     *
     * @return
     */
    public boolean currentIsLast() {
        if (mBitmaps != null && currentIndex == mBitmaps.size() - 1) {
            return true;
        }
        return false;
    }


    /**
     * 撤销
     */
    public void undo(Bitmap srcBitmap) {
        if (mBitmaps == null) {
            return;
        }
        isUndo = true;
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        try {
            Bitmap bitmap = mBitmaps.get(currentIndex);
            if (bitmap.isRecycled()) {
                return;
            }
            bitmap = BitmapUtil.zoomImg(bitmap, srcBitmap.getWidth());
            if (bitmap.getWidth() > srcBitmap.getWidth() || bitmap.getHeight() > srcBitmap.getHeight()) {
                bitmap = BitmapUtil.zoomImage(bitmap, srcBitmap.getWidth(), srcBitmap.getHeight());
            }
            //保存所有的像素的数组，图片宽×高
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            srcBitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                    bitmap.getWidth(), bitmap.getHeight());
        } catch (OutOfMemoryError e) {
        }
    }

    /**
     * 恢复
     */
    public void redo(Bitmap srcBitmap) {
        if (mBitmaps == null) {
            return;
        }

        currentIndex++;
        int lastIndex = mBitmaps.size() - 1;
        if (currentIndex >= lastIndex) {
            currentIndex = lastIndex;
        }
        try {
            Bitmap bitmap = mBitmaps.get(currentIndex);
            if (bitmap.isRecycled()) {
                return;
            }
            bitmap = BitmapUtil.zoomImg(bitmap, srcBitmap.getWidth());
            if (bitmap.getWidth() > srcBitmap.getWidth() || bitmap.getHeight() > srcBitmap.getHeight()) {
                bitmap = BitmapUtil.zoomImage(bitmap, srcBitmap.getWidth(), srcBitmap.getHeight());
            }
            //保存所有的像素的数组，图片宽×高
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            srcBitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                    bitmap.getWidth(), bitmap.getHeight());
        } catch (OutOfMemoryError e) {
        }

    }

    /**
     * 清空
     */
    public void freeBitmaps() {
        if (mBitmaps == null) {
            return;
        }
        for (Bitmap bitmap : mBitmaps) {
            bitmap.recycle();
        }
        mBitmaps.clear();
        mBitmaps = null;
        currentIndex = -1;
    }

}
