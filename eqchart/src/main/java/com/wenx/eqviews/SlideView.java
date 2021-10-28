package com.wenx.eqviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Created By WenXiong on 2021/10/21.
 */
public class SlideView extends androidx.appcompat.widget.AppCompatImageView {
    private final static String TAG = "CustomView";

    public SlideView(@NonNull Context context) {
        super(context);
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int width;
    private int height;

    /**
     * 是否拖动
     */
    private boolean isDrag = false;
    /**
     * 容器/父布局宽高
     */
    private float parentWidth;
    private float parentHeight;
    /**
     * 中心点的X轴活动范围
     */
    private float startX = 0.f;
    private float endX = 0.f;
    /**
     * 中心点的X轴活动范围
     */
    private float startY = 0.f;
    private float endY = 0.f;
    /**
     * 是否为容器中首位两个view
     */
    private boolean isVerScroll;

    private final int SPACE = 2;

    private OnScrollCallBack mCallBack;

    public interface OnScrollCallBack {
        void onCoordinateBack(float centerX, float centerY);

        void onPressUp();

        void onBorder(SlideView view);
    }

    public void setOnScrollListener(OnScrollCallBack callBack) {
        this.mCallBack = callBack;
    }

    public boolean isDrag() {
        return isDrag;
    }

    public SlideView setLayoutSize(float w, float h) {
        this.parentWidth = w;
        this.parentHeight = h;
        return this;
    }

    public SlideView setLimitX(float start, float end) {
        this.startX = start;
        this.endX = end;
        return this;
    }

    public SlideView setLimitY(float start, float end) {
        this.startY = start;
        this.endY = end;
        return this;
    }

    public SlideView isStartOrEnd(boolean is) {
        isVerScroll = is;
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    private float downX;
    private float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (this.isEnabled()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDrag = false;
                    downX = event.getX();
                    downY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float xDistance = event.getX() - downX;
                    final float yDistance = event.getY() - downY;
                    float l, r, t, b;
                    //当水平或者垂直滑动距离大于10,才算拖动事件
                    if (Math.abs(xDistance) > 10 || Math.abs(yDistance) > 10) {
                        isDrag = true;
                        l = getLeft() + xDistance;
                        r = l + width;
                        t = getTop() + yDistance;
                        b = t + height;

                        if (startX == 0.f || endX == 0.f) {
                            if (l < 0) {
                                l = 0;
                                r = l + width;
                            } else if (r > parentWidth) {
                                r = parentWidth;
                                l = r - width;
                            }
                        } else {
                            // 到达前后两点边界时的操作
                            if (l < startX - (width / 2.f)) {
                                l = startX - (width / 2.f) + SPACE;
                                r = l + width;
                                if (mCallBack != null) {
                                    mCallBack.onBorder(this);
                                }
                            } else if (r > endX + (width / 2.f)) {
                                r = endX + (width / 2.f) - SPACE;
                                l = r - width;
                                if (mCallBack != null) {
                                    mCallBack.onBorder(this);
                                }
                            }
                        }

                        if (t < startY) {
                            t = startY;
                            b = t + height;
                        } else if (b > endY) {
                            b = endY == 0.f ? parentHeight : endY;
                            t = b - height;
                        }

                        if (isVerScroll) {
//                            Log.e(TAG, getLeft() + "  " + t + "  " + getRight() + "  " + b);
                            this.layout(getLeft(), (int) t, getRight(), (int) b);
                            if (mCallBack != null) {
                                mCallBack.onCoordinateBack((getLeft() + getRight()) / 2.f, (t + b) / 2.f);
                            }
                        } else {
                            this.layout((int) l, (int) t, (int) r, (int) b);
//                            Log.e(TAG, l + "  " + t + "  " + r + "  " + b);
                            if (mCallBack != null) {
                                mCallBack.onCoordinateBack((l + r) / 2.f, (t + b) / 2.f);
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mCallBack != null) {
                        mCallBack.onPressUp();
                    }
                    setPressed(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    setPressed(false);
                    break;
            }
            return true;
        }
        return false;
    }

}
