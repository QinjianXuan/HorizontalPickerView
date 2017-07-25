package com.example.xuanq.horizontalpickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 横向选择器
 * 目标 可滑动变换 选中文字变大  未选中自动缩小
 * 可添加其他文字等空间 但只在选中文字下方或者右上方显示
 * 为了小仙女还有美好的假期 -- Finghting！！！
 * Created by xuanq on 2017/7/22.
 */

public class HorizontalPickerView extends View {

    private int mViewHeight;
    private int mViewWidth;

    private float mMaxTextSize;// 最大的字号
    private float mMinTextSize;// 最小的字号

    private boolean isInit;

    private List<DataModel> mDataList;
    private Paint mTextPaint;
    private Paint mPaint;

    private int mColorText = 0x333333;

    private float mMaxTextAlpha = 250;
    private float mMinTextAlpha = 100;
    private float baseline;
    private onSelectListener mSelectListener;
    private Timer timer;
    private MyTimerTask mTask;
    private float mLastDownX;
    private Context mContext;
    Handler updateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (Math.abs(mMoveLen) < SPEED) {
                mMoveLen = 0;
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                    performSelect();//每次选择完成后,对接口方法里要传递的数据进行设置
                }
            } else
                // 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * SPEED;
            invalidate();
        }

    };

    /**
     * 自动回滚到中间的速度
     */
    public static final float SPEED = 3;

    /**
     * 选中的位置，这个位置是mDataList的中心位置，一直不变
     */
    private int mCurrentSelected;
    /**
     * text之间间距和minTextSize之比
     */
    public static final float MARGIN_ALPHA = 12f;

    /**
     * 滑动的距离
     */
    private float mMoveLen = 0;

    public HorizontalPickerView(Context context) {
        this(context, null);
    }

    public HorizontalPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    public HorizontalPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    /**
     * 暴露一个方法，让外界可以获得选中的值
     *
     * @param listener
     */
    public void setOnSelectListener(onSelectListener listener) {
        mSelectListener = listener;
    }

    private void performSelect() {
        if (mSelectListener != null)
            mSelectListener.onSelect(mDataList.get(mCurrentSelected).getRate());
    }


    public void setData(List<DataModel> datas) {
        mDataList = datas;
        mCurrentSelected = datas.size() / 2;
        invalidate();
    }

    public void setSelected(int selected) {
        mCurrentSelected = selected;
    }

    private void moveHeadToTail() {
        DataModel head = mDataList.get(0);
        mDataList.remove(0);
        mDataList.add(head);

    }

    private void moveTailToHead() {
        DataModel tail = mDataList.get(mDataList.size() - 1);
        mDataList.remove(mDataList.size() - 1);
        mDataList.add(0, tail);

    }


    @Override
    /**
     * 计算控件的宽高
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();
        // 按照View的高度计算字体大小
        mMaxTextSize = mViewWidth / 12.0f;
        mMinTextSize = mMaxTextSize / 3.0f;
        isInit = true;
        invalidate();
    }

    /**
     * 初始化
     */
    private void init() {
        timer = new Timer();
        mDataList = new ArrayList<DataModel>();
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mColorText);


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(Color.parseColor("#FF0000"));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 根据index绘制view
        if (isInit)
            drawData(canvas);
    }


    private void drawData(Canvas canvas) {

        // 先绘制选中的text再往上往下绘制其余的text
        float scale = parabola(mViewWidth / 2.0f, mMoveLen);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize + 5;
        // 设置字体的大小
        mTextPaint.setTextSize(size);
        // 设置字体的清晰度
        mTextPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));// 设置字体的清晰度
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        float y = (float) (mViewHeight / 2);
        float x = (float) ((mViewWidth / 2.0) - DisplayUtil.dp2px(10) + mMoveLen);
        Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
        baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        Rect rectText = new Rect();
        //画左上角 图标

        if(mMoveLen <= 0){
            canvas.drawColor(evaluateColor(mDataList.get(mCurrentSelected+ 1).getColor(),mDataList.get(mCurrentSelected).getColor(),(float) Math.pow(scale,6)));
        }else if(mMoveLen > 0){
            canvas.drawColor(evaluateColor(mDataList.get(mCurrentSelected - 1).getColor(),mDataList.get(mCurrentSelected).getColor(),(float) Math.pow(scale,6)));
        }

        mTextPaint.setColor(Color.WHITE);
        canvas.drawText(mDataList.get(mCurrentSelected).getRate(), x, baseline, mTextPaint);
        mTextPaint.getTextBounds(mDataList.get(mCurrentSelected).getRate(), 0, mDataList.get(mCurrentSelected).getRate().length(), rectText);

        //画图片背景
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.discount_compensation);
        Rect rect = new Rect((int) x + ((rectText.right - rectText.left) / 2) + DisplayUtil.dp2px(5), (int) y + rectText.top - DisplayUtil.dp2px(2), (int) x + ((rectText.right - rectText.left) / 2) + DisplayUtil.dp2px(62), (int) y + rectText.top + DisplayUtil.dp2px(17));
        canvas.drawBitmap(bitmap, null, rect, mPaint);

        //画图标
        Bitmap bitmap2 = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.risk_level);
        Rect rect2 = new Rect((int) x + ((rectText.right - rectText.left) / 2) + DisplayUtil.dp2px(13), (int) y + rectText.top + DisplayUtil.dp2px(1.5f), (int) x + ((rectText.right - rectText.left) / 2) + DisplayUtil.dp2px(25), (int) y + rectText.top + DisplayUtil.dp2px(13.5f));
        canvas.drawBitmap(bitmap2, null, rect2, mPaint);

        //画文字
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(DisplayUtil.dip2px(mContext, 10));
        canvas.drawText("+贴息",  x + ((rectText.right - rectText.left) / 2) + DisplayUtil.dp2px(40),y + rectText.top + DisplayUtil.dp2px(12),mPaint);

        //画百分号
        mPaint.setTextSize(DisplayUtil.dip2px(mContext, 16));
        canvas.drawText("%",x + ((rectText.right - rectText.left) / 2) + DisplayUtil.dp2px(10), y + ((rect.bottom - rect.top) / 1.5f), mPaint);

        //画下方文字
        mPaint.setTextSize(DisplayUtil.dip2px(mContext, 12));
        canvas.drawText("历史年化收益率", x + DisplayUtil.dp2px(10), y * 1.35f, mPaint);

        //画收益率取值
        mPaint.setTextSize(DisplayUtil.dip2px(mContext, 12));
        Rect month = new Rect();
        mPaint.getTextBounds(mDataList.get(mCurrentSelected).getMonth(), 0, mDataList.get(mCurrentSelected).getMonth().length(), month);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(x - ((month.right - month.left)/ 2), y * 1.50f,x + ((month.right - month.left)/ 2) + DisplayUtil.dp2px(20),y * 1.60f + (month.bottom - month.top),50,50,mPaint);
        canvas.drawText(mDataList.get(mCurrentSelected).getMonth(), x + DisplayUtil.dp2px(10), (float) y * 1.65f, mPaint);
        for (int i = 1; (mCurrentSelected - i) >= 0; i++) {    //System.out.println(i);
            //System.out.println("上");
            drawOtherText(canvas, i, -1);
        }
        // 绘制下方data
        for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++) {
            //System.out.println("下");
            drawOtherText(canvas, i, 1);
        }

    }

    /**
     * 抛物线
     *
     * @param zero 零点坐标
     * @param x    偏移量
     * @return scale
     */
    private float parabola(float zero, float x) {
        float f = (float) (1 - Math.pow(x / zero, 2));
        return f < 0 ? 0 : f;
    }

    /**
     * @param canvas
     * @param position 距离mCurrentSelected的差值
     * @param type     1表示向下绘制，-1表示向上绘制
     */
    private void drawOtherText(Canvas canvas, int position, int type) {
        //scale都为0
        float d = (float) (MARGIN_ALPHA * mMinTextSize * position + type
                * mMoveLen);
        float scale = parabola(mViewWidth / 2.0f, d);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mTextPaint.setTextSize(size);
        mTextPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale));
        float x = (float) (mViewWidth / 2.0 + type * d);
        //float y = (float)(mViewHeight / 2.0);
        Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
        //System.out.println(fmi.toString());
        //float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        canvas.drawText(mDataList.get(mCurrentSelected + type * position).getRate() + "%", x, baseline, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                doDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                doMove(event);
                break;
            case MotionEvent.ACTION_UP:
                doUp(event);
                break;
        }
        return true;
    }

    private int evaluateColor(int startValue, int endValue, float fraction) {
        if (fraction <= 0) {
            return startValue;
        }
        if (fraction >= 1) {
            return endValue;
        }
        int startInt = startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) | ((startR + (int) (fraction * (endR - startR))) << 16) | ((startG + (int) (fraction * (endG - startG))) << 8) | ((startB + (int) (fraction * (endB - startB))));
    }

    private void doDown(MotionEvent event) {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mLastDownX = event.getX();
    }

    private void doMove(MotionEvent event) {
        //System.out.println("first:"+mMoveLen);
        mMoveLen += (event.getX() - mLastDownX);
        System.out.println("second:" + mMoveLen);
        if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2) {
            // 往下滑超过离开距离
            moveTailToHead();
            mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
        } else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2) {
            // 往上滑超过离开距离
            moveHeadToTail();
            mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
        }

        mLastDownX = event.getX();
        invalidate();
    }

    private void doUp(MotionEvent event) {
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(mMoveLen) < 0.0001) {
            mMoveLen = 0;
            return;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mTask = new MyTimerTask(updateHandler);
        timer.schedule(mTask, 0, 10);
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }

    }

    public interface onSelectListener {
        void onSelect(String text);
    }


}
