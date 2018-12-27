package android.king.signature.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.king.signature.util.DisplayUtil;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;

import android.king.signature.util.SystemUtil;



/**
 * 显示手写字的View
 *
 * @author king
 * @since 2018-06-28
 */
public class HandWriteEditView extends AppCompatEditText {

    private float lineHeight = 150;
    boolean reLayout = false;
    private TextWatch textWatcher;

    public HandWriteEditView(Context context) {
        super(context);
        init();
    }

    public HandWriteEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HandWriteEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setTextIsSelectable(false);
        this.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.setGravity(Gravity.START);

        //禁止选择复制粘贴
        SystemUtil.disableCopyAndPaste(this);
        lineHeight = DisplayUtil.dip2px(getContext(),50);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                float add = lineHeight;
                setLineSpacing(0f, 1f);
                setLineSpacing(add, 0);
                setIncludeFontPadding(false);
                setGravity(Gravity.CENTER_VERTICAL);
                int top = (int) ((add - getTextSize()) * 0.5f);
                setPadding(getPaddingLeft(), top, getPaddingRight(), -top);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (textWatcher != null) {
                    textWatcher.afterTextChanged(editable);
                }
            }
        });

    }

    /**
     * 设置行高
     *
     * @param lineHeight
     */
    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        invalidate();
    }


    /**
     * 添加手写文字
     *
     * @param srcBitmap 手写文字图片
     */
    public Editable addBitmapToText(Bitmap srcBitmap) {
        if (srcBitmap == null) {
            return null;
        }
        SpannableString mSpan = new SpannableString("1");
        mSpan.setSpan(new ImageSpan(getContext(), srcBitmap), mSpan.length() - 1, mSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        Editable editable = getText();
        //获取光标所在位置
        int index = getSelectionStart();
        editable.insert(index, mSpan);
        setText(editable);
        setSelection(index + mSpan.length());
        return editable;
    }

    /**
     * 添加空格
     */
    public void addSpace(int fontSize) {
        int size = DisplayUtil.dip2px(getContext(), fontSize);
        ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);
        Bitmap bitmap = Bitmap.createBitmap(fontSize, size, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        addBitmapToText(bitmap);
    }

    /**
     * 删除文字
     */
    public Editable deleteBitmapFromText() {

        Editable editable = getEditableText();
        int start = getSelectionStart();
        if (start == 0) {
            return null;
        }
        editable.delete(start - 1, start);
        return editable;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!reLayout) {
            reLayout = true;
            setIncludeFontPadding(false);
            setGravity(Gravity.CENTER_VERTICAL);
            setLineSpacing(lineHeight, 0);
            int top = (int) ((lineHeight - getTextSize()) * 0.5f);
            setPadding(getPaddingLeft(), top, getPaddingRight(), -top);
            requestLayout();
            invalidate();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(expandSpec, expandSpec);
    }

    boolean canPaste() {
        return false;
    }

    boolean canCut() {
        return false;
    }

    boolean canCopy() {
        return false;
    }

    boolean canSelectAllText() {
        return false;
    }

    boolean canSelectText() {
        return false;
    }

    boolean textCanBeSelected() {
        return false;
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    public void addTextWatcher(TextWatch textWatcher) {
        this.textWatcher = textWatcher;
    }

    public interface TextWatch {
        void afterTextChanged(Editable var1);
    }
}
