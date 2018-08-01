package android.king.signature.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.king.signature.R;
import android.king.signature.config.PenConfig;
import android.king.signature.util.BitmapUtil;
import android.king.signature.util.DisplayUtil;
import android.king.signature.util.StatusBarCompat;
import android.king.signature.util.SystemUtil;
import android.king.signature.view.CircleView;
import android.king.signature.view.PaintView;
import android.king.signature.view.CircleImageView;
import android.king.signature.view.PaintSettingWindow;
import android.king.signature.view.SealView;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

/**
 * 空白手写画板
 *
 * @author king
 * @since 2018/07/10 14:20
 */
public class PaintActivity extends BaseActivity implements View.OnClickListener, PaintView.StepCallback {

    /**
     * 画布最大宽度
     */
    private static final int CANVAS_MAX_WIDTH = 3000;
    /**
     * 画布最大高度
     */
    private static final int CANVAS_MAX_HEIGHT = 3000;

    private View mContainerView;
    private CircleImageView mUndoView;
    private CircleImageView mRedoView;
    private CircleImageView mPenView;
    private CircleImageView mClearView;
    private CircleView mSettingView;
    private SealView mSealView;

    private PaintView mPaintView;

    private ProgressDialog mSaveProgressDlg;
    private static final int MSG_SAVE_SUCCESS = 1;
    private static final int MSG_SAVE_FAILED = 2;

    private String mSavePath;
    private boolean hasSize = false;

    private float widthRate;
    private float heightRate;
    private int bgColor;
    private boolean showSeal;
    private boolean isCrop;
    private String format;

    /**
     * 画笔设置
     */
    private PaintSettingWindow settingWindow;

    @Override
    protected int getLayout() {
        return R.layout.sign_activity_paint;
    }

    @Override
    protected void initView() {
        PenConfig.PAINT_COLOR = PenConfig.getPaintColor(this);
        PenConfig.PAINT_SIZE = PenConfig.getPaintSize(this);

        isCrop = getIntent().getBooleanExtra("crop", false);
        String sealName = getIntent().getStringExtra("sealName");
        format = getIntent().getStringExtra("format");
        boolean showSealTime = getIntent().getBooleanExtra("showSealTime", false);
        bgColor = getIntent().getIntExtra("background", Color.TRANSPARENT);
        String mInitPath = getIntent().getStringExtra("image");
        int bitmapWidth = getIntent().getIntExtra("bitmapWidth", 0);
        int bitmapHeight = getIntent().getIntExtra("bitmapHeight", 0);
        widthRate = getIntent().getFloatExtra("widthRate", 1.0f);
        heightRate = getIntent().getFloatExtra("heightRate", 1.0f);
        if (bitmapWidth > CANVAS_MAX_WIDTH || bitmapHeight > CANVAS_MAX_HEIGHT) {
            Toast.makeText(this, "画布宽高超过限制", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int resizeWidth = getResizeWidth();
        int resizeHeight = getResizeHeight();
        if (bitmapWidth == 0 || bitmapHeight == 0) {
            //表示没有传入固定的画布大小
            hasSize = false;
            bitmapWidth = resizeWidth;
            bitmapHeight = resizeHeight;

        } else {
            hasSize = true;
        }

        View mCancelView = findViewById(R.id.tv_cancel);
        View mOkView = findViewById(R.id.tv_ok);

        mContainerView = findViewById(R.id.sign_container);
        mPaintView = findViewById(R.id.paint_view);
        mUndoView = findViewById(R.id.btn_undo);
        mRedoView = findViewById(R.id.btn_redo);
        mPenView = findViewById(R.id.btn_pen);
        mClearView = findViewById(R.id.btn_clear);
        mSettingView = findViewById(R.id.btn_setting);
        mSealView = findViewById(R.id.seal_view);
        mUndoView.setOnClickListener(this);
        mRedoView.setOnClickListener(this);
        mPenView.setOnClickListener(this);
        mClearView.setOnClickListener(this);
        mSettingView.setOnClickListener(this);
        mCancelView.setOnClickListener(this);
        mOkView.setOnClickListener(this);

        mPenView.setSelected(true);
        mUndoView.setEnabled(false);
        mRedoView.setEnabled(false);

        mPaintView.setBackgroundColor(Color.WHITE);
        mPaintView.setStepCallback(this);

        mSettingView.setPaintColor(PenConfig.PAINT_COLOR);
        mSettingView.setCircleRadius(PenConfig.PAINT_SIZE);

        if (!TextUtils.isEmpty(sealName) || showSealTime) {
            mSealView.setVisibility(View.VISIBLE);
            mSealView.setTextContent(sealName);
            mSealView.showTime(showSealTime);
            showSeal = true;
        } else {
            mSealView.setVisibility(View.GONE);
            showSeal = false;
        }

        if (bitmapWidth < resizeWidth) {
            mSealView.setTextSize(getResources().getDimension(R.dimen.seal_small_text_size));
            mSealView.setTimeTextSize(getResources().getDimension(R.dimen.seal_small_time_text_size));
        }

        //初始画板设置
        mPaintView.init(this, bitmapWidth, bitmapHeight, mInitPath);
        mSettingView.setPaintColor(PenConfig.PAINT_COLOR);
        if (bgColor != Color.TRANSPARENT) {
            mPaintView.setBackgroundColor(bgColor);
        }
        //显示隐藏拖拽按钮
        hideShowDrag();
    }

    private int getResizeWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && dm.widthPixels < dm.heightPixels) {
            return (int) (dm.heightPixels* widthRate);
        }
        return (int) (dm.widthPixels * widthRate);
    }

    private int getResizeHeight() {
        int toolBarHeight = getResources().getDimensionPixelSize(R.dimen.grid_toolbar_height);
        int actionbarHeight = getResources().getDimensionPixelSize(R.dimen.sign_actionbar_height);
        int statusBarHeight = StatusBarCompat.getStatusBarHeight(this);
        int otherHeight = toolBarHeight + actionbarHeight + statusBarHeight;

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && dm.widthPixels < dm.heightPixels) {
            return (int) ((dm.widthPixels - otherHeight) * heightRate);
        }
        return (int) ((dm.heightPixels - otherHeight) * heightRate);
    }

    @Override
    protected void initData() {
        setThemeColor(PenConfig.THEME_COLOR);
        mClearView.setImage(R.drawable.sign_ic_clear, PenConfig.THEME_COLOR);
        mPenView.setImage(R.drawable.sign_ic_pen, PenConfig.THEME_COLOR);
        mRedoView.setImage(R.drawable.sign_ic_redo, mPaintView.canRedo() ? PenConfig.THEME_COLOR : Color.LTGRAY);
        mUndoView.setImage(R.drawable.sign_ic_undo, mPaintView.canUndo() ? PenConfig.THEME_COLOR : Color.LTGRAY);
        mSettingView.setOutBorderColor(PenConfig.THEME_COLOR);
    }

    /**
     * 横竖屏切换
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (settingWindow != null) {
            settingWindow.dismiss();
        }
        int resizeWidth = getResizeWidth();
        int resizeHeight = getResizeHeight();
        if (mPaintView != null && !hasSize) {
            mPaintView.resize(mPaintView.getLastBitmap(), resizeWidth, resizeHeight);
        }
        hideShowDrag();
    }

    /**
     * 控制拖动按钮是否显示
     */
    private void hideShowDrag() {
        ViewTreeObserver vto2 = mContainerView.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContainerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int limitWidth = mContainerView.getWidth();
                int limitHeight = mContainerView.getHeight();
                if (mPaintView.getBitmap().getWidth() > limitWidth || mPaintView.getBitmap().getHeight() > limitHeight) {
                    mPenView.setVisibility(View.VISIBLE);
                } else {
                    mPenView.setVisibility(View.GONE);
                    mPaintView.setScroll(false);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_setting) {
            showSettingWindow();

        } else if (i == R.id.btn_clear) {
            mPaintView.setPenType(PenConfig.TYPE_CLEAR);

        } else if (i == R.id.btn_undo) {
            mPaintView.undo();

        } else if (i == R.id.btn_redo) {
            mPaintView.redo();

        } else if (i == R.id.btn_pen) {
            mPaintView.setScroll(!mPaintView.isScroll());
            if (!mPaintView.isScroll()) {
                mPenView.setImage(R.drawable.sign_ic_pen, PenConfig.THEME_COLOR);
            } else {
                mPenView.setImage(R.drawable.sign_ic_drag, PenConfig.THEME_COLOR);
            }

        } else if (i == R.id.tv_ok) {
            save();

        } else if (i == R.id.tv_cancel) {
            setResult(RESULT_CANCELED);
            finish();

        }
    }

    @Override
    protected void onDestroy() {
        if (mPaintView != null) {
            mPaintView.release();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }


    /**
     * 弹出画笔设置
     */
    private void showSettingWindow() {
        settingWindow = new PaintSettingWindow(this);
        settingWindow.setSettingListener(new PaintSettingWindow.OnSettingListener() {
            @Override
            public void onColorSetting(int color) {
                mPaintView.setPaintColor(PenConfig.PAINT_COLOR);
                mSettingView.setPaintColor(PenConfig.PAINT_COLOR);
            }

            @Override
            public void onSizeSetting(int size) {
                mSettingView.setCircleRadius(PenConfig.PAINT_SIZE);
                mPaintView.setPaintWidth(PenConfig.PAINT_SIZE);
            }
        });

        View contentView = settingWindow.getContentView();
        //需要先测量，PopupWindow还未弹出时，宽高为0
        contentView.measure(SystemUtil.makeDropDownMeasureSpec(settingWindow.getWidth()),
                SystemUtil.makeDropDownMeasureSpec(settingWindow.getHeight()));
        int padding = DisplayUtil.dip2px(this, 10);
        settingWindow.getContentView().setBackgroundResource(R.drawable.bottom_right_pop_bg);
        settingWindow.showAsDropDown(mSettingView, mSettingView.getWidth() - settingWindow.getContentView().getMeasuredWidth() + 2 * padding, 10);

    }

    private void initSaveProgressDlg() {
        mSaveProgressDlg = new ProgressDialog(this);
        mSaveProgressDlg.setMessage("正在保存,请稍候...");
        mSaveProgressDlg.setCancelable(false);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAVE_FAILED:
                    mSaveProgressDlg.dismiss();
                    Toast.makeText(PaintActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SAVE_SUCCESS:
                    mSaveProgressDlg.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra(PenConfig.SAVE_PATH, mSavePath);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 保存
     */
    private void save() {
        if (mPaintView.isEmpty()) {
            Toast.makeText(this, "没有写入任何文字", Toast.LENGTH_SHORT).show();
            return;
        }
        //先检查是否有存储权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            return;
        }
        if (mSaveProgressDlg == null) {
            initSaveProgressDlg();
        }
        mSaveProgressDlg.show();
        mSealView.setTimeStamp(System.currentTimeMillis());
        new Thread(() -> {
            try {
                Bitmap result = mPaintView.buildAreaBitmap(isCrop);
                if (PenConfig.FORMAT_JPG.equals(format) && bgColor == Color.TRANSPARENT) {
                    bgColor = Color.WHITE;
                }
                if (bgColor != Color.TRANSPARENT) {
                    result = BitmapUtil.drawBgToBitmap(result, bgColor);
                }
                if (showSeal) {
                    Bitmap sealBitmap = mSealView.getBitmap();
                    if (sealBitmap != null) {
                        result = BitmapUtil.addWaterMask(result, sealBitmap, bgColor);
                    }
                }
                if (result == null) {
                    mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                    return;
                }
                mSavePath = BitmapUtil.saveImage(PaintActivity.this, result, 100, format);
                if (mSavePath != null) {
                    mHandler.obtainMessage(MSG_SAVE_SUCCESS).sendToTarget();
                } else {
                    mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                }
            } catch (Exception e) {

            }
        }).start();

    }

    @Override
    public void onOperateStatusChanged() {
        mUndoView.setEnabled(mPaintView.canUndo());
        mRedoView.setEnabled(mPaintView.canRedo());

        mRedoView.setImage(R.drawable.sign_ic_redo, mPaintView.canRedo() ? PenConfig.THEME_COLOR : Color.LTGRAY);
        mUndoView.setImage(R.drawable.sign_ic_undo, mPaintView.canUndo() ? PenConfig.THEME_COLOR : Color.LTGRAY);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//获得了权限
                    save();
                } else {
                    Toast.makeText(this, "禁止了读写存储权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
