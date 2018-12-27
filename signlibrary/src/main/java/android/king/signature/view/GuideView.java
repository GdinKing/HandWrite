package android.king.signature.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.king.signature.util.DisplayUtil;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.king.signature.R;
import android.king.signature.config.PenConfig;


/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月19日 15:46分
 * @since 2018-09-19
 * @author king
 */
public class GuideView {


    private View parentView;
    private View leftTarget;
    private View rightTarget;
    private Context context;
    private FrameLayout frameParent;
    private RelativeLayout guideContainer;

    public GuideView(Context context, View parentView, View leftTarget, View rightTarget) {
        this.context = context;
        this.parentView = parentView;
        this.leftTarget = leftTarget;
        this.rightTarget = rightTarget;
        init();
    }

    private void init() {
        View viewParent = parentView.getRootView();
        if (viewParent == null) {
            return;
        }
        if (viewParent instanceof FrameLayout) {
            frameParent = (FrameLayout) viewParent;//整个父布局

            guideContainer = new RelativeLayout(context);
            guideContainer.setId(R.id.sign_guide);
            guideContainer.setOnTouchListener((v, event) -> true);
            guideContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            guideContainer.setBackgroundColor(Color.parseColor("#88000000"));//背景设置灰色透明

            addLeftGuide();
            addRightGuide();


            //“我知道了”按钮
            TextView tvGuideOk = new TextView(context);
            tvGuideOk.setText("我知道了");
            tvGuideOk.setTextColor(Color.WHITE);
            tvGuideOk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tvGuideOk.setPadding(30, 20, 30, 20);
            tvGuideOk.setBackgroundResource(R.drawable.sign_btn_white_shape);
            tvGuideOk.setOnClickListener(v -> frameParent.removeView(guideContainer));

            RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp3.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            int padding = DisplayUtil.dip2px(context, 80);
            lp3.setMargins(20, 20, 20, padding);
            guideContainer.addView(tvGuideOk, lp3);

        }
    }

    private void addLeftGuide() {
        if (leftTarget == null) {
            return;
        }
        Rect rect = new Rect();
        Point point = new Point();
        leftTarget.getGlobalVisibleRect(rect, point);

        CircleImageView topGuideview = new CircleImageView(context);
        topGuideview.setId(R.id.sign_guide_hand);
        topGuideview.setImage(R.drawable.sign_ic_hand, PenConfig.THEME_COLOR);
        topGuideview.setBackgroundResource(R.drawable.sign_bg_btn_clicked);


        ImageView ivHandGuide = new ImageView(context);
        ivHandGuide.setId(R.id.sign_guide_hand_img);
        ivHandGuide.setImageResource(R.drawable.sign_ic_left_guide);
        ivHandGuide.setBackgroundDrawable(null);

        TextView tvHandGuide = new TextView(context);
        tvHandGuide.setId(R.id.sign_guide_hand_text);
        tvHandGuide.setText("这里可切换手写/滚屏，支持笔写的设备还可防手误触哦！");
        tvHandGuide.setTextColor(Color.WHITE);
        tvHandGuide.setMaxWidth(DisplayUtil.dip2px(context, 120));
        tvHandGuide.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);


        Rect rt = new Rect();
        parentView.getWindowVisibleDisplayFrame(rt);
        //被指引对象
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(rect.width(), rect.height());
        lp.topMargin = rect.top;
        lp.leftMargin = rect.left;
        guideContainer.addView(topGuideview, lp);
        //指引箭头
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.BELOW, topGuideview.getId());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp1.topMargin = 20;
        lp1.leftMargin = point.x + rect.width() / 2;
        guideContainer.addView(ivHandGuide, lp1);

        //指引文字
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.BELOW, ivHandGuide.getId());
        lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp2.topMargin = 10;
        lp2.leftMargin = point.x + 10;
        guideContainer.addView(tvHandGuide, lp2);

    }

    /**
     * 添加右边指引视图
     */
    private void addRightGuide() {
        if (rightTarget == null) {
            return;
        }
        Rect rect = new Rect();
        Point point = new Point();
        rightTarget.getGlobalVisibleRect(rect, point);

        CircleImageView topGuideview = new CircleImageView(context);
        topGuideview.setId(R.id.sign_guide_pen);
        topGuideview.setImage(R.drawable.sign_ic_pen, PenConfig.THEME_COLOR);
        topGuideview.setBackgroundResource(R.drawable.sign_bg_btn_clicked);


        ImageView ivHandGuide = new ImageView(context);
        ivHandGuide.setId(R.id.sign_guide_pen_img);
        ivHandGuide.setImageResource(R.drawable.sign_ic_right_guide);
        ivHandGuide.setBackgroundDrawable(null);

        TextView tvHandGuide = new TextView(context);
        tvHandGuide.setId(R.id.sign_guide_pen_text);
        tvHandGuide.setText("点此!\n切换画笔/橡皮擦");
        tvHandGuide.setGravity(Gravity.CENTER_HORIZONTAL);
        tvHandGuide.setTextColor(Color.WHITE);
        tvHandGuide.setMaxWidth(DisplayUtil.dip2px(context, 120));
        tvHandGuide.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);


        Rect rt = new Rect();
        parentView.getWindowVisibleDisplayFrame(rt);
        //被指引对象
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(rect.width(), rect.height());
        lp.topMargin = rect.top;
        lp.leftMargin = rect.left;
        guideContainer.addView(topGuideview, lp);
        //指引箭头
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.addRule(RelativeLayout.BELOW, topGuideview.getId());
        lp1.addRule(RelativeLayout.ALIGN_LEFT, topGuideview.getId());
        lp1.topMargin = 20;
        lp1.leftMargin = 0;
        guideContainer.addView(ivHandGuide, lp1);

        //指引文字
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.BELOW, ivHandGuide.getId());
        lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp2.topMargin = 10;
        lp2.rightMargin = rect.width();
        guideContainer.addView(tvHandGuide, lp2);

    }

    public void show() {
        if (frameParent != null && guideContainer != null) {
            if (frameParent.findViewById(guideContainer.getId()) != null) {
                frameParent.removeView(frameParent.findViewById(guideContainer.getId()));
            }
            frameParent.addView(guideContainer);
        }
    }
}
