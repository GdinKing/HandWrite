package android.king.signature;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.king.signature.util.DisplayUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import android.king.signature.config.PenConfig;
import android.king.signature.util.BitmapUtil;
import android.king.signature.util.SystemUtil;
import android.king.signature.view.CircleImageView;
import android.king.signature.view.CircleView;
import android.king.signature.view.GridDrawable;
import android.king.signature.view.GridPaintView;
import android.king.signature.view.HVScrollView;
import android.king.signature.view.HandWriteEditView;
import android.king.signature.view.PaintSettingWindow;


/***
 * 名称：GridWriteActivity<br>
 * 描述：米格输入界面
 * 最近修改时间：
 * @since 2017/11/14
 * @author king
 */
public class GridPaintActivity extends BaseActivity implements View.OnClickListener, Handler.Callback {

    private View mCircleContainer;
    private HVScrollView mTextContainer;
    private HandWriteEditView mEditView;
    private CircleImageView mDeleteView;
    private CircleImageView mSpaceView;
    private CircleImageView mClearView;
    private CircleImageView mEnterView;
    private CircleView mPenCircleView;
    private GridPaintView mPaintView;
    private ProgressDialog mSaveProgressDlg;
    private static final int MSG_SAVE_SUCCESS = 1;
    private static final int MSG_SAVE_FAILED = 2;
    private static final int MSG_WRITE_OK = 100;
    private Handler mHandler;

    private String mSavePath;
    private Editable cacheEditable;

    private int bgColor;
    private boolean isCrop;
    private String format;
    private int lineSize;
    private int fontSize;


    private PaintSettingWindow settingWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        bgColor = getIntent().getIntExtra("background", Color.TRANSPARENT);
        lineSize = getIntent().getIntExtra("lineLength", 15);
        fontSize = getIntent().getIntExtra("fontSize", 50);
        isCrop = getIntent().getBooleanExtra("crop", false);
        format = getIntent().getStringExtra("format");

        PenConfig.PAINT_COLOR = PenConfig.getPaintColor(this);
        PenConfig.PAINT_SIZE_LEVEL = PenConfig.getPaintTextLevel(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        SystemUtil.disableShowInput(getApplicationContext(), mEditView);

    }


    /**
     * 横竖屏切换
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.sign_activity_grid_paint);
        initTitleBar();
        initView();
        initData();
        SystemUtil.disableShowInput(getApplicationContext(), mEditView);

        if (mEditView != null && cacheEditable != null) {
            mEditView.setText(cacheEditable);
            mEditView.setSelection(cacheEditable.length());
            mEditView.requestFocus();
        }
        mHandler = new Handler(this);
        if (settingWindow != null) {
            settingWindow.dismiss();
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.sign_activity_grid_paint;
    }

    @Override
    protected void initData() {
        setThemeColor(PenConfig.THEME_COLOR);
        mPenCircleView.setOutBorderColor(PenConfig.THEME_COLOR);

        mClearView.setEnabled(false);
        mClearView.setImage(R.drawable.sign_ic_clear, Color.LTGRAY);
        mEnterView.setImage(R.drawable.sign_ic_enter, PenConfig.THEME_COLOR);
        mSpaceView.setImage(R.drawable.sign_ic_space, PenConfig.THEME_COLOR);
        mDeleteView.setImage(R.drawable.sign_ic_delete, PenConfig.THEME_COLOR);

        mHandler = new Handler(this);

    }

    /**
     * 初始化视图
     */
    @Override
    protected void initView() {
        mPaintView = findViewById(R.id.paint_view);
        mDeleteView = findViewById(R.id.delete);
        mSpaceView = findViewById(R.id.space);
        mPenCircleView = findViewById(R.id.pen_color);
        mClearView = findViewById(R.id.clear);
        mEnterView = findViewById(R.id.enter);
        mEditView = findViewById(R.id.et_view);
        mTextContainer = findViewById(R.id.sv_container);
        mCircleContainer = findViewById(R.id.circle_container);
        mEnterView.setOnClickListener(this);
        mDeleteView.setOnClickListener(this);
        mSpaceView.setOnClickListener(this);
        mPenCircleView.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        mClearView.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        mPenCircleView.setPaintColor(PenConfig.PAINT_COLOR);
        mPenCircleView.setRadiusLevel(PenConfig.PAINT_SIZE_LEVEL);

        int size = getResources().getDimensionPixelSize(R.dimen.sign_grid_size);
        GridDrawable gridDrawable = new GridDrawable(size, size, Color.WHITE);
        mPaintView.setBackground(gridDrawable);

        mPaintView.setGetTimeListener(new GridPaintView.WriteListener() {
            @Override
            public void onWriteStart() {
                mHandler.removeMessages(MSG_WRITE_OK);
            }

            @Override
            public void onWriteCompleted(long time) {
                mHandler.sendEmptyMessageDelayed(MSG_WRITE_OK, 1000);
            }
        });

        int maxWidth = lineSize * DisplayUtil.dip2px(this, fontSize);
        if (!isCrop) {
            mEditView.setWidth(maxWidth + 2);
            mEditView.setMaxWidth(maxWidth);
        } else {
            mEditView.setWidth(maxWidth * 2 / 3);
            mEditView.setMaxWidth(maxWidth * 2 / 3);
        }
        mEditView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        mEditView.setLineHeight(DisplayUtil.dip2px(this, fontSize));
        mEditView.setHorizontallyScrolling(false);
        mEditView.requestFocus();
        if (bgColor != Color.TRANSPARENT) {
            mTextContainer.setBackgroundColor(bgColor);
        }
        mEditView.addTextWatcher(s -> {
            if (s != null && s.length() > 0) {
                mClearView.setEnabled(true);
                mClearView.setImage(R.drawable.sign_ic_clear, PenConfig.THEME_COLOR);
            } else {
                mClearView.setEnabled(false);
                mClearView.setImage(R.drawable.sign_ic_clear, Color.LTGRAY);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(MSG_WRITE_OK);
    }

    @Override
    protected void onDestroy() {
        mPaintView.release();
        super.onDestroy();
        mHandler.removeMessages(MSG_SAVE_FAILED);
        mHandler.removeMessages(MSG_SAVE_SUCCESS);
    }


    private void initSaveProgressDlg() {
        mSaveProgressDlg = new ProgressDialog(this);
        mSaveProgressDlg.setMessage("正在保存,请稍候...");
        mSaveProgressDlg.setCancelable(false);
    }

    /**
     * 弹出清空提示
     */
    private void showClearTips() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("清空文本框内手写内容？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    mEditView.setText("");
                    mEditView.setSelection(0);
                    cacheEditable = null;
                });
        builder.show();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SAVE_FAILED:
                if (mSaveProgressDlg != null) {
                    mSaveProgressDlg.dismiss();
                }
                Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
                break;
            case MSG_SAVE_SUCCESS:
                if (mSaveProgressDlg != null) {
                    mSaveProgressDlg.dismiss();
                }
                Intent intent = new Intent();
                intent.putExtra(PenConfig.SAVE_PATH, mSavePath);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case MSG_WRITE_OK:
                if (!mPaintView.isEmpty()) {
                    Bitmap bitmap = mPaintView.buildBitmap(isCrop, DisplayUtil.dip2px(GridPaintActivity.this, fontSize));
                    this.cacheEditable = mEditView.addBitmapToText(bitmap);
                    mPaintView.reset();
                }
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * 保存
     */
    private void save() {
        if (mEditView.getText() == null || mEditView.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "没有写入任何文字", Toast.LENGTH_SHORT).show();
            return;
        }
        //先检查是否有存储权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "没有读写存储的权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSaveProgressDlg == null) {
            initSaveProgressDlg();
        }
        mEditView.clearFocus();
        mEditView.setCursorVisible(false);

        mSaveProgressDlg.show();
        new Thread(() -> {
            if (PenConfig.FORMAT_JPG.equals(format) && bgColor == Color.TRANSPARENT) {
                bgColor = Color.WHITE;
            }
            Bitmap bm = getWriteBitmap(bgColor);
            bm = BitmapUtil.clearBlank(bm, 20, bgColor);

            if (bm == null) {
                mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                return;
            }
            mSavePath = BitmapUtil.saveImage(GridPaintActivity.this, bm, 100, format);
            if (mSavePath != null) {
                mHandler.obtainMessage(MSG_SAVE_SUCCESS).sendToTarget();
            } else {
                mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
            }
            bm.recycle();
            bm = null;
        }).start();
    }

    /**
     * 获取EdiText截图
     *
     * @param bgColor 背景颜色
     * @return EdiText截图
     */
    private Bitmap getWriteBitmap(int bgColor) {
        int w = 0;
        int h = 0;
        for (int i = 0; i < mTextContainer.getChildCount(); i++) {
            h += mTextContainer.getChildAt(i).getHeight();
            w += mTextContainer.getChildAt(i).getWidth();
        }
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(w, h,
                    Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            bitmap = Bitmap.createBitmap(w, h,
                    Bitmap.Config.ARGB_4444);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(bgColor);
        mTextContainer.draw(canvas);
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.delete) {
            this.cacheEditable = mEditView.deleteBitmapFromText();
        } else if (i == R.id.tv_cancel) {
            if (mEditView.getText() != null && mEditView.getText().length() > 0) {
                showQuitTip();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else if (i == R.id.tv_ok) {
            save();
        } else if (i == R.id.enter) {
            Editable editable = mEditView.getText();
            editable.insert(mEditView.getSelectionStart(), "\n");
        } else if (i == R.id.space) {
            mEditView.addSpace(fontSize);
        } else if (i == R.id.clear) {
            showClearTips();
        } else if (i == R.id.pen_color) {
            showSettingWindow();
        }
    }


    /**
     * 弹出画笔设置
     */
    private void showSettingWindow() {
        settingWindow = new PaintSettingWindow(this);

        settingWindow.setSettingListener(new PaintSettingWindow.OnSettingListener() {
            @Override
            public void onColorSetting(int color) {
                mPaintView.setPaintColor(color);
                mPenCircleView.setPaintColor(PenConfig.PAINT_COLOR);
            }

            @Override
            public void onSizeSetting(int level) {
                mPaintView.setPaintWidth(PaintSettingWindow.PEN_SIZES[level]);
                mPenCircleView.setRadiusLevel(level);
            }
        });

        int[] location = new int[2];
        mCircleContainer.getLocationOnScreen(location);
        View contentView = settingWindow.getContentView();
        //需要先测量，PopupWindow还未弹出时，宽高为0
        contentView.measure(SystemUtil.makeDropDownMeasureSpec(settingWindow.getWidth()),
                SystemUtil.makeDropDownMeasureSpec(settingWindow.getHeight()));

        int padding = DisplayUtil.dip2px(this, 10);
        int offsetX, offsetY;

        Configuration config = getResources().getConfiguration();
        int smallestScreenWidth = config.smallestScreenWidthDp;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && smallestScreenWidth >= 720) {
            //平板上横屏显示
            settingWindow.popAtBottomRight();
            settingWindow.showAsDropDown(mCircleContainer, mCircleContainer.getWidth() - settingWindow.getContentView().getMeasuredWidth() - padding, 10);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏显示
            offsetX = -settingWindow.getContentView().getMeasuredWidth() - padding;
            offsetY = -settingWindow.getContentView().getMeasuredHeight() - mCircleContainer.getHeight() / 2 + padding;
            settingWindow.popAtLeft();
            settingWindow.showAsDropDown(mCircleContainer, offsetX, offsetY);
        } else {
            //竖屏显示
            offsetX = 0;
            offsetY = -(settingWindow.getContentView().getMeasuredHeight() + mPenCircleView.getHeight() + 4 * padding);
            settingWindow.popAtTopLeft();
            settingWindow.showAsDropDown(mPenCircleView, offsetX, offsetY);
        }
    }

    @Override
    public void onBackPressed() {
        if (mEditView.getText() != null && mEditView.getText().length() > 0) {
            showQuitTip();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    /**
     * 弹出退出提示
     */
    private void showQuitTip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("当前文字未保存，是否退出？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                });
        builder.show();
    }


}
