package android.king.signature.view;

import android.content.Context;
import android.king.signature.util.SystemUtil;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;



/**
 * 禁止选择复制粘贴的EditText
 *
 * @author king
 * @since 2018-06-28
 */
public class NoSelectEditText extends AppCompatEditText {

    public NoSelectEditText(Context context) {
        super(context);
        init();
    }

    public NoSelectEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoSelectEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setTextIsSelectable(false);
        this.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.setGravity(Gravity.START);
        SystemUtil.disableCopyAndPaste(this);
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
}
