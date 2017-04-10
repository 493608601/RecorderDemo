package com.gxsn.recorderdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/4/6.
 */

public class CircleProgressView extends View {

    private static final String TAG = "CircleProgressBar";

    boolean mIsWaitUpEvent = false;
    boolean mIsWaitDoubleClick = false;
    private static final int MAX_INTERVAL_FOR_CLICK = 250;
    private static final int MAX_DOUBLE_CLICK_INTERVAL = 500;
    private static final int MAX_DISTANCE_FOR_CLICK = 100;

    private Timer mTimer;
    private TimerTaskForClick mTimerTaskForClick;
    private int mTimeCount = 0;
    private static int taskPeriod = 60;
    private static int MAX_TASK_TIME = 30 * 1000;

    private boolean isRecorder = false;
    private boolean isComplete = false;

    int mDownX = 0;
    int mDownY = 0;
    int mTempX = 0;
    int mTempY = 0;

    private int mMaxProgress = 100;

    private int mProgress = 0;

    private final int mCircleLineStrokeWidth = 25;


    private final Paint mPaint;


    private ProgressStatusListener listener;

    public interface ProgressStatusListener {
        void onStartRecorded();

        void onComplete();
    }

    public void setOnprogressStatusListener(ProgressStatusListener listener) {
        this.listener = listener;
    }


    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        int centre = getWidth() / 2; //获取圆心的x坐标
        int radius = (int) (centre - mCircleLineStrokeWidth / 2); //圆环的半径

        // 设置画笔相关属性
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#ffffff"));
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centre, centre, radius, mPaint); //画出圆环

        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius);  //用于定义的圆弧的形状和大小的界限

        mPaint.setColor(Color.parseColor("#ddd4d4"));
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval, 0, 360, false, mPaint);  //外侧圆弧

        mPaint.setColor(Color.RED);
        canvas.drawArc(oval, -90, (mProgress * 360 / 100), false, mPaint);  //根据进度画圆弧


    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        this.invalidate();
    }

    public void setProgressNotInUiThread(int progress) {
        this.mProgress = progress;
        this.postInvalidate();
    }

    Runnable mTimerForUpEvent = new Runnable() {
        public void run() {
            if (mIsWaitUpEvent) {
                Log.d(TAG,
                        "The mTimerForUpEvent has executed, so set the mIsWaitUpEvent as false");
                mIsWaitUpEvent = false;
            } else {
                Log.d(TAG,
                        "The mTimerForUpEvent has executed, mIsWaitUpEvent is false,so do nothing");
            }
        }
    };

    Runnable mTimerForSecondClick = new Runnable() {
        @Override
        public void run() {
            if (mIsWaitDoubleClick) {
                Log.d(TAG,
                        "The mTimerForSecondClick has executed,so as a singleClick");
                mIsWaitDoubleClick = false;
                // at here can do something for singleClick!!
                if (!isRecorder) {
                    if (listener != null) listener.onStartRecorded();
                    mTimer = new Timer();
                    mTimerTaskForClick = new TimerTaskForClick();
                    mTimer.schedule(mTimerTaskForClick, 0, taskPeriod);
                    isRecorder = true;
                } else {
                    complete();
                }
            } else {
                Log.d(TAG,
                        "The mTimerForSecondClick has executed, the doubleclick has executed ,so do thing");
            }
        }
    };


    public void complete() {
        post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) listener.onComplete();
                if (mTimer != null) mTimer.cancel();
                if (mTimerTaskForClick != null) mTimerTaskForClick.cancel();
                isRecorder = false;
                mTimeCount = 0;
                setProgress(mTimeCount);
            }
        });

//        setVisibility(GONE);
    }

    public class TimerTaskForClick extends TimerTask {
        @Override
        public void run() {
            mTimeCount += taskPeriod;
            if (mTimeCount <= MAX_TASK_TIME) {
                int percent = (int) ((float) mTimeCount / (float) MAX_TASK_TIME * 100);
                setProgressNotInUiThread(percent);
            } else {
                complete();
                cancel();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mIsWaitUpEvent = true;
                postDelayed(mTimerForUpEvent, MAX_INTERVAL_FOR_CLICK);
                break;
            case MotionEvent.ACTION_MOVE:
                mTempX = (int) event.getX();
                mTempY = (int) event.getY();
                if (Math.abs(mTempX - mDownX) > MAX_DISTANCE_FOR_CLICK
                        || Math.abs(mTempY - mDownY) > MAX_DISTANCE_FOR_CLICK) {
                    mIsWaitUpEvent = false;
                    removeCallbacks(mTimerForUpEvent);
                    Log.d(TAG, "The move distance too far:cancel the click");
                }
                break;
            case MotionEvent.ACTION_UP:
                mTempX = (int) event.getX();
                mTempY = (int) event.getY();
                if (Math.abs(mTempX - mDownX) > MAX_DISTANCE_FOR_CLICK
                        || Math.abs(mTempY - mDownY) > MAX_DISTANCE_FOR_CLICK) {
                    mIsWaitUpEvent = false;
                    removeCallbacks(mTimerForUpEvent);
                    Log.d(TAG,
                            "The touch down and up distance too far:cancel the click");
                    break;
                } else {
                    mIsWaitUpEvent = false;
                    removeCallbacks(mTimerForUpEvent);
                    onSingleClick();
                    return true;
                }
            case MotionEvent.ACTION_CANCEL:
                mIsWaitUpEvent = false;
                removeCallbacks(mTimerForUpEvent);
                Log.d(TAG, "The touch cancel state:cancel the click");
                break;
            default:
                Log.d(TAG, "irrelevant MotionEvent state:" + event.getAction());
        }
        return true;
    }

    public void onSingleClick() {
        if (mIsWaitDoubleClick) {
            onDoubleClick();
            mIsWaitDoubleClick = false;
            removeCallbacks(mTimerForSecondClick);
        } else {
            mIsWaitDoubleClick = true;
            postDelayed(mTimerForSecondClick, MAX_DOUBLE_CLICK_INTERVAL);
        }
    }

    public void onDoubleClick() {
        Log.d(TAG, "we can do sth for double click here");
    }

}
