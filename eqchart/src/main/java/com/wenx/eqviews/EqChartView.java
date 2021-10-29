package com.wenx.eqviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.wenx.eqchart.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created By WenXiong on 2021/10/21.
 */
public class EqChartView extends RelativeLayout {
    private final static String TAG = EqChartView.class.getSimpleName();
    private final static float SPACE = 25f;
    private static int MAX_COUNT = 10;
    private final static int MIN_COUNT = 2;

    /**
     * 预留文字区域高度
     */
    private final float textAreaY = 2 * SPACE;

    /**
     * 此宽度
     */
    private int mWidth = 0;

    /**
     * 此高度
     */
    private int mHeight = 0;

    /**
     * item控件间隔
     */
    private float spanX = 0;

    /**
     * 曲线path
     */
    private Path path = new Path();

    /**
     * 背景线条画笔
     */
    private Paint backPaint;

    /**
     * 曲线画笔
     */
    private Paint bezierPaint;

    /**
     * 文字画笔
     */
    private Paint textPaint;

    /**
     * 子控件集合
     */
    private List<SlideView> chartViews = new ArrayList<>();

    /**
     * 中心坐标集合
     */
    private List<PointF> points = new ArrayList<>();

    /**
     * 底部文字
     */
    public String[] hertzs = new String[]{
            "32", "64", "125",
            "250", "500", "1k",
            "2k", "4k", "8k", "16k"
    };

    /**
     * 左边文字
     */
    private String[] dbs = new String[]{
            "+6db", "-6db"
    };

    /**
     * 最新点的背景图片资源id
     */
    private int newPointImageId = 0;
    private boolean isSaveNewPointImg = false;

    /**
     * 所有点的背景图片资源id
     */
    private int pointImageId = 0;

    /**
     * 自定义背景
     */
    private int canvasCustom = 0;
    private Bitmap customBitmap;
    private RectF bmRectF;

    /**
     * 画布背景颜色
     */
    private int canvasBg = 0;

    /**
     * 曲线画笔颜色
     */
    private int beziColor = Color.WHITE;

    /**
     * 文字画笔颜色
     */
    private int textColor = Color.WHITE;

    /**
     * 背景线条画笔颜色
     */
    private int backColor = Color.WHITE;

    /**
     * 虚线y轴坐标
     */
    private float yAxisY;
    /**
     * y轴半个有效区间
     */
    private float yInterval_p2;
    /**
     * x轴整个区间
     */
    private float xInterval;
    /**
     * x轴值的范围 其实就是 maxFreqs - minFreqs
     */
    private float freqInterval;


    /**
     * 数据:频点 -- x轴内容,会做排序操作
     */
    private float[] freqs;
    /**
     * 数据:增益 -- y轴内容,会做排序操作
     */
    private float[] gains;
    private float minFreq = 20.f;
    private float maxFreq = 16000.f;
    private float minGain = -6.f;
    private float maxGain = 6.f;

    /**
     * setConfig中是否要刷新呢
     */
    private boolean needInvalidate = true;

    /**
     * 是否支持边界删除
     */
    private boolean needBorderDelete = false;

    /**
     * 这个开关是为了防止减点时,滑动事件额外修改了points,而导致下一个点移动到减点位置
     */
    private boolean canChangePoints = true;

    /**
     * 防止误触、虽然我写了,但我感觉没什么乱用,以后再说
     */
    private final float touchSpace = 20.f;

    /**
     * 当前点数量
     */
    private int mPointCount;

    /**
     * 最近一个添加点的index
     */
    private int newPointIndex;

    public EqChartView(Context context) {
        this(context, null);
        init();
    }

    public EqChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public EqChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.EqChartView);
        canvasCustom = array.getResourceId(R.styleable.EqChartView_canvas_custom, 0);
        canvasBg = array.getColor(R.styleable.EqChartView_canvas_custom, Color.GRAY);
        newPointImageId = array.getResourceId(R.styleable.EqChartView_points_new_bg, 0);
        pointImageId = array.getResourceId(R.styleable.EqChartView_points_bg, R.drawable.bg_chartview_64);
        array.recycle();

        init();

    }

    private void init() {
        backPaint = new Paint();
        textPaint = new Paint();
        bezierPaint = new Paint();
        bezierPaint.setColor(beziColor);
        bezierPaint.setStyle(Paint.Style.STROKE);
        bezierPaint.setPathEffect(new CornerPathEffect(50));
        bezierPaint.setStrokeWidth(10.f);
        bezierPaint.setAntiAlias(true);
    }

    private void changeBackPaint(boolean isDash, float size) {
        backPaint.reset();
        backPaint.setColor(backColor);
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStrokeWidth(size);
        backPaint.setAntiAlias(true);
        backPaint.setPathEffect(isDash ? new DashPathEffect(new float[]{20, 20}, 5) : null);
    }

    private void changeTextPaint(float size) {
        textPaint.reset();
        textPaint.setColor(textColor);
        textPaint.setTextSize(size);
        textPaint.setAntiAlias(true);
    }

    private void setBeziColor(@ColorInt int color) {
        beziColor = color;
        invalidate();
    }

    private void setBgColor(@ColorInt int color) {
        backColor = color;
        invalidate();
    }

    private void setTextColor(@ColorInt int color) {
        textColor = color;
        invalidate();
    }

    public void setAxisText(String[] xText, String[] yText) {
        hertzs = Arrays.copyOf(xText, xText.length);
        dbs = Arrays.copyOf(yText, yText.length);
        canvasCustom = 0;
        invalidate();
    }

    public void setCanvasCustom(@DrawableRes int id) {
        canvasCustom = id;
        invalidate();
    }

    public void setCanvasBg(@ColorRes int id) {
        canvasCustom = 0;
        canvasBg = id;
        invalidate();
    }

    public void setPointsBackGround(@DrawableRes int id, boolean cover) {
        if (chartViews.size() < 2) {
            return;
        }
        pointImageId = id;
        for (int i = 0; i < chartViews.size(); i++) {
            if (cover) {
                chartViews.get(i).setBackgroundResource(pointImageId);
            } else {
                if (i == newPointIndex && isSaveNewPointImg) {
                    chartViews.get(i).setBackgroundResource(newPointImageId == 0 ? pointImageId : newPointImageId);
                } else {
                    chartViews.get(i).setBackgroundResource(pointImageId);
                }
            }
        }
    }

    public void setNewPointsBackGround(@DrawableRes int id, boolean isSave) {
        isSaveNewPointImg = isSave;
        newPointImageId = id;
        for (int i = 0; i < chartViews.size(); i++) {
            chartViews.get(i).setBackgroundResource(pointImageId);
        }
        if (newPointIndex != 0) {
            chartViews.get(newPointIndex).setBackgroundResource(newPointImageId);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawBg(canvas);
        super.dispatchDraw(canvas);
    }

    private void drawBg(Canvas canvas) {
        if (canvasCustom == 0) {
            // 背景上色
            canvas.drawColor(canvasBg);
            // 画顶部实线 中部虚线 底部实线
            changeBackPaint(false, 5.f);
            canvas.drawLine(SPACE, SPACE, mWidth - SPACE, SPACE, backPaint);
            changeBackPaint(true, 3.f);
            canvas.drawLine(SPACE, (mHeight - textAreaY) / 2.f, mWidth - SPACE, (mHeight - textAreaY) / 2.f, backPaint);
            changeBackPaint(false, 5.f);
            canvas.drawLine(SPACE, mHeight - SPACE - textAreaY, mWidth - SPACE, mHeight - SPACE - textAreaY, backPaint);
            // 画左右竖线
            changeBackPaint(false, 3.f);
            canvas.drawLine(2.f * SPACE, 3.f * SPACE, 2.f * SPACE, mHeight - (3.f * SPACE) - textAreaY, backPaint);
            canvas.drawLine(mWidth - (2.f * SPACE), 2.f * SPACE, mWidth - (2.f * SPACE), mHeight - (2.f * SPACE) - textAreaY, backPaint);
            // 画等比竖线
            changeBackPaint(false, 1.5f);
            for (int i = 1; i <= 8; i++) {
                float dx = 2.f * SPACE + (i * spanX);
                canvas.drawLine(dx, 2.f * SPACE, dx, mHeight - (2.f * SPACE) - textAreaY, backPaint);
            }
            // 写横坐标的字
            changeTextPaint(36.f);
            for (int i = 0; i < hertzs.length; i++) {
                float dx = 2.f * SPACE + (i * spanX) - (textPaint.getTextSize());
                // 两个字时
                if (hertzs[i].length() == 2) {
                    dx = 2.f * SPACE + (i * spanX) - textPaint.getTextSize() / 2f;
                }
                // 三个字时
                else if (hertzs[i].length() == 3) {
                    dx = 2.f * SPACE + (i * spanX) - (textPaint.getTextSize() / 1.25f);
                }
                canvas.drawText(hertzs[i], dx, mHeight - textAreaY + SPACE, textPaint);
            }

            // 写纵坐标的字
            changeTextPaint(24.f);
            canvas.drawText(dbs[0], 2.f * SPACE - textPaint.getTextSize(), 2.33f * SPACE, textPaint);
            canvas.drawText(dbs[1], 2.f * SPACE - textPaint.getTextSize(), mHeight - (1.66f * SPACE) - textAreaY, textPaint);
        } else {
            customBitmap = ((BitmapDrawable) AppCompatResources.getDrawable(getContext(), canvasCustom)).getBitmap();
            canvas.drawBitmap(customBitmap, null, bmRectF, backPaint);
        }

        if (chartViews != null && points != null) {
            canvas.drawPath(path, bezierPaint);
            canvas.save();
        }
    }

    private void processDatas() {
        path.reset();
        processPoints();
        invalidate();
    }

    private void processPoints() {
        path.moveTo(points.get(0).x, points.get(0).y);
        float t = 0.34f;
        for (int i = 0; i < mPointCount - 1; i++) {
            PointF startP = points.get(i);// 每一次的起点
            PointF endP = points.get(i + 1);// 每一次的终点
            PointF cp1 = new PointF();// 控制点1
            cp1.x = startP.x + (endP.x - startP.x) * t;
            cp1.y = startP.y;
            PointF cp2 = new PointF();// 控制点2
            cp2.x = startP.x + (endP.x - startP.x) * (1 - t);
            cp2.y = endP.y;
            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, endP.x, endP.y);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        bmRectF = new RectF(0, 0, mWidth, mHeight);

        spanX = (mWidth - (4 * SPACE)) / 9;

        yAxisY = (mHeight - textAreaY) / 2.f;
        yInterval_p2 = (mHeight - (4.f * SPACE) - textAreaY) / 2.f;
        xInterval = mWidth - (4.f * SPACE);
        freqInterval = (maxFreq - minFreq);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 点击了item区域,事件就丢给item去搞
         * */
        float x = event.getX();
        float y = event.getY();
        for (int i = 0; i < chartViews.size(); i++) {
            if (x > chartViews.get(i).getLeft() - touchSpace
                    && x < chartViews.get(i).getRight() + touchSpace
                    && y > chartViews.get(i).getTop() - touchSpace
                    && y < chartViews.get(i).getBottom() + touchSpace) {
                return false;
            }
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x <= 2.f * SPACE || x >= mWidth - (2.f * SPACE)
                        || y <= 2.f * SPACE || y > mHeight - (2.f * SPACE) - textAreaY) {
                    break;
                }
                addPoint(x, y);
                break;
        }
        return true;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        canChangePoints = true;

        if (mPointCount != getChildCount()) {
            mPointCount = getChildCount();
        }

        for (int i = 0; i < mPointCount; i++) {
            if (!(getChildAt(i) instanceof SlideView)) {
                throw new IllegalStateException("The child control must be ChartView !");
            }
        }

        if (mPointCount < MIN_COUNT) {
            throw new IllegalStateException("ChartView count at least 2 or more !");
        }

        if (chartViews.size() == 0 || points.size() == 0) {
            for (int i = 0; i < mPointCount; i++) {
                if (getChildAt(i).getWidth() == 0 || getChildAt(i).getHeight() == 0) {
                    getChildAt(i).setMinimumWidth((int) (getResources().getDisplayMetrics().density * 20.f));
                    getChildAt(i).setMinimumHeight((int) (getResources().getDisplayMetrics().density * 20.f));
                }
                getChildAt(i).setBackgroundResource(pointImageId);
                chartViews.add(i, (SlideView) getChildAt(i));
                points.add(i, new PointF());
            }
            freqs = new float[mPointCount];
            gains = new float[mPointCount];

            /**
             * 初次进入，等比分配好位置
             * */
            for (int i = 0; i < mPointCount; i++) {
                float left, right;
                float top = ((mHeight - textAreaY) / 2.f - chartViews.get(i).getHeight() / 2.f);
                float bottom = top + chartViews.get(i).getHeight();

                if (i == 0) {
                    left = (2 * SPACE - chartViews.get(i).getWidth() / 2.f);
                    right = left + chartViews.get(i).getWidth();
                } else if (i == chartViews.size() - 1) {
                    left = ((2 * SPACE - chartViews.get(i).getWidth() / 2.f) + (spanX * 9));
                    right = left + chartViews.get(i).getWidth();
                } else {
                    left = ((2 * SPACE - chartViews.get(i).getWidth() / 2.f) + (spanX * i));
                    right = left + chartViews.get(i).getWidth();
                }

                points.get(i).x = (left + right) / 2.f;
                points.get(i).y = (top + bottom) / 2.f;

                chartViews.get(i).layout((int) left, (int) top, (int) right, (int) bottom);
                chartViews.get(i).setLimitY(2.f * SPACE - chartViews.get(i).getWidth() / 2.f
                        , mHeight - 4 * SPACE + chartViews.get(i).getHeight() / 2.f);
                chartViews.get(i).setLayoutSize(mWidth, mHeight - textAreaY).isStartOrEnd((i == 0) || (i == chartViews.size() - 1));
                chartViews.get(i).setOnScrollListener(new SlideParentListener(i));
            }
        } else {
            for (int i = 0; i < mPointCount; i++) {
                chartViews.get(i).layout((int) (points.get(i).x - chartViews.get(i).getWidth() / 2.f)
                        , (int) (points.get(i).y - chartViews.get(i).getHeight() / 2.f)
                        , (int) (points.get(i).x + chartViews.get(i).getWidth() / 2.f)
                        , (int) (points.get(i).y + chartViews.get(i).getHeight() / 2.f));
                chartViews.get(i).setLimitY(2.f * SPACE - chartViews.get(i).getHeight() / 2.f
                        , mHeight - 4.f * SPACE + chartViews.get(i).getHeight() / 2.f);
                chartViews.get(i).setLayoutSize(mWidth, mHeight - textAreaY).isStartOrEnd((i == 0) || (i == chartViews.size() - 1));
                chartViews.get(i).setOnScrollListener(new SlideParentListener(i));
            }
        }
        processDatas();
    }

    private class SlideParentListener implements SlideView.OnScrollCallBack {
        private int index;

        public SlideParentListener(final int i) {
            this.index = i;
        }

        @Override
        public void onCoordinateBack(float centerX, float centerY) {
            if (!canChangePoints) {
                return;
            }
            points.get(index).x = centerX;
            points.get(index).y = centerY;
            processDatas();
            /**
             * 前后边距限制
             * */
            if (index >= 1 && index <= mPointCount - 2 && mPointCount > MIN_COUNT) {
                chartViews.get(index).setLimitX(points.get(index - 1).x, points.get(index + 1).x);
            }
        }

        @SuppressLint("NewApi")
        @Override
        public void onPressUp() {
            getRealValue();
            if (onSlideListener != null) {
                float[] fs = Arrays.copyOf(freqs, mPointCount);
                float[] gs = Arrays.copyOf(gains, mPointCount);
                onSlideListener.onDataBack(fs, gs);
            }
        }

        @Override
        public void onBorder(SlideView view) {
            if (needBorderDelete) {
                removePoint(view);
            }
        }
    }


    /**
     * 点转数据
     */
    private void getRealValue() {
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                freqs[i] = minFreq;
            } else if (i == points.size() - 1) {
                freqs[i] = maxFreq;
            } else {
                freqs[i] = Math.round(freqInterval * (points.get(i).x - (2 * SPACE)) / xInterval);
            }
            gains[i] = points.get(i).y - yAxisY > 0 ?
                    (points.get(i).y - yAxisY) / yInterval_p2 * minGain :
                    Math.abs(points.get(i).y - yAxisY) / yInterval_p2 * maxGain;
        }
    }

    /**
     * 数据转点
     */
    private void datasToPoints(float[] fs, float[] gs) {
        this.freqs = Arrays.copyOf(fs, mPointCount);
        this.gains = Arrays.copyOf(gs, mPointCount);
        minFreq = fs[0];
        maxFreq = fs[fs.length - 1];
        freqInterval = maxFreq - minFreq;
        for (int i = 0; i < mPointCount; i++) {
            float x;
            if (i == 0) {
                x = 0.f;
            } else if (i == mPointCount - 1) {
                x = xInterval;
            } else {
                x = 2.f * SPACE + (fs[i] / (maxFreq - minFreq)) * xInterval;
            }
            float y = gs[i] > 0 ? yAxisY - (gains[i] / maxGain * yInterval_p2)
                    : yAxisY + (gains[i] / minGain * yInterval_p2);
            points.set(i, new PointF(x + (2.f * SPACE), y));
        }
        requestLayout();
    }

    /**
     * 增加点
     */
    @SuppressLint("NewApi")
    public void addPoint(float x, float y) {
        if (mPointCount == MAX_COUNT) {
            return;
        }
        // TODO: 2021/10/26 因为freqs为递增数据,方便确定加点的坐标
        // TODO: 2021/10/25 根据points拿到插叙index
        // TODO: 2021/10/25 points添加新的 pointF
        // TODO: 2021/10/25 修改chartviews
        // TODO: 2021/10/25 修改freqs、gains
        // TODO: 2021/10/25 调用系统方法重绘线条、子控件
        float[] pointsX = new float[mPointCount];
        for (int i = 0; i < mPointCount; i++) {
            pointsX[i] = points.get(i).x;
        }
        float[] newPointsX = Arrays.copyOf(pointsX, pointsX.length + 1);
        newPointsX[pointsX.length] = x;
        Arrays.sort(newPointsX);
        final int index = Arrays.binarySearch(newPointsX, x);
        points.add(index, new PointF(x, y));

        addItem(index, chartViews.get(0).getWidth(), chartViews.get(0).getHeight());

        freqs = Arrays.copyOf(freqs, mPointCount + 1);
        gains = Arrays.copyOf(gains, mPointCount + 1);
        getRealValue();
        mPointCount++;

        requestLayout();
        invalidate();

    }

    private void addItem(int index, int width, int height) {
        SlideView slideView = new SlideView(getContext());// 绕啊绕  气不气
        if (isSaveNewPointImg) {
            slideView.setBackgroundResource(newPointImageId == 0 ? R.drawable.bg_chartview_64 : newPointImageId);
        } else {
            slideView.setBackgroundResource(pointImageId);
        }
        slideView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        chartViews.add(index, slideView);
        newPointIndex = index;
        addView(slideView);
    }

    /**
     * 删除点
     */
    public void removePoint(SlideView view) {
        if (mPointCount == 2) {
            return;
        }
        // TODO: 2021/10/26 根据view拿到chartViews中的position,并更新chartViews
        // TODO: 2021/10/26 根据position更新points
        // TODO: 2021/10/26 根据position更新freqs、gains
        // TODO: 2021/10/26 调用系统方法重绘线条,重分配子控件
        int pos = chartViews.indexOf(view);
        chartViews.remove(view);
        points.remove(pos);
        canChangePoints = false;

        for (int i = 0; i < freqs.length; i++) {
            if (i >= pos && i < freqs.length - 1) {
                freqs[i] = freqs[i + 1];
                gains[i] = gains[i + 1];
            }
        }
        freqs = Arrays.copyOf(freqs, freqs.length - 1);
        gains = Arrays.copyOf(gains, gains.length - 1);
        removeView(view);
        mPointCount--;
        newPointIndex--;

        requestLayout();
        invalidate();
    }

    public void setMaxCount(int count) {
        MAX_COUNT = count;
        invalidate();
    }

    public void setBorderDelete(boolean need) {
        this.needBorderDelete = need;
    }

    public void refreshData(float[] fs, float[] gs) {
        if (fs.length != gs.length) {
            return;
        }
        if (fs.length != mPointCount) {
            mPointCount = fs.length;
            removeAllViews();
            int w = chartViews.get(0).getWidth();
            int h = chartViews.get(0).getHeight();
            chartViews.clear();
            points.clear();
            for (int i = 0; i < mPointCount; i++) {
                addItem(i, w, h);
                points.add(i, new PointF());
            }
        }
        datasToPoints(fs, gs);
        processDatas();
    }

    public void setDataConfig(DataConfig config) {
        if (minFreq == maxFreq || (minFreq == 0.f && maxFreq == 0.f) ||
                minGain == maxGain || (minGain == 0.f && maxGain == 0.f)) {
        } else {
            this.minFreq = config.minFreq;
            this.maxFreq = config.maxFreq;
            this.needInvalidate = config.isInvalidate;
            this.needBorderDelete = config.needBorderDelete;
            freqInterval = maxFreq - minGain;
            requestLayout();
        }

        if (config.freqs != null && config.gains != null) {
            datasToPoints(freqs, gains);
        }

        if (needInvalidate) {
            processDatas();
        }

    }

    public static class DataConfig {
        public DataConfig() {
        }

        private float[] freqs;
        private float[] gains;

        public DataConfig(float[] freqs, float[] gains) {
            this.freqs = freqs;
            this.gains = gains;
        }


        public DataConfig setMinFreq(float minFreq) {
            this.minFreq = minFreq;
            return this;
        }

        public DataConfig setMaxFreq(float maxFreq) {
            this.maxFreq = maxFreq;
            return this;
        }

        public DataConfig setInvalidate(boolean invalidate) {
            isInvalidate = invalidate;
            return this;
        }

        public DataConfig setNeedBorderDelete(boolean needBorderDelete) {
            this.needBorderDelete = needBorderDelete;
            return this;
        }

        private float minFreq;
        private float maxFreq;
        private boolean isInvalidate = true;
        private boolean needBorderDelete = false;

    }


    private OnSlideListener onSlideListener;

    public void setOnSlideListener(OnSlideListener listener) {
        this.onSlideListener = listener;
    }

    public interface OnSlideListener {
        default void onDown(int pos) {
        }

        default void onSliding(float x, float y, int pos) {
        }

        void onDataBack(float[] freqs, float[] gains);
    }


}
