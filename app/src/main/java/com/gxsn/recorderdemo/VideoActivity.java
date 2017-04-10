package com.gxsn.recorderdemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.gxsn.recorderdemo.view.CircleProgressView;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VideoActivity extends Activity implements SurfaceHolder.Callback, CircleProgressView.ProgressStatusListener, View.OnClickListener {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private boolean isRecording = false;//标记是否已经在录制
    private MediaRecorder mRecorder;//音视频录制类
    private Camera mCamera = null;//相机
    private Camera.Size mSize = null;//相机的尺寸
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;//默认后置摄像头
    private static final SparseIntArray orientations = new SparseIntArray();//手机旋转对应的调整角度
    private static final int MAX_RECORDER_TIME = 20 * 1000;

    private MediaPlayer mediaPlayer;
    private String path;

    static {
        orientations.append(Surface.ROTATION_0, 90);
        orientations.append(Surface.ROTATION_90, 0);
        orientations.append(Surface.ROTATION_180, 270);
        orientations.append(Surface.ROTATION_270, 180);
    }

    private CircleProgressView progressView;
    private ImageView ivReset;
    private ImageView ivComplete;
    private ImageView ivFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindow();
        setContentView(R.layout.activity_video);
        initViews();
        initAudioManager();
    }


    private void initAudioManager() {
        //关闭MediaRecorder录制状态改变时的声音
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
    }

    private void setWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // 设置竖屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private void initViews() {
        progressView = (CircleProgressView) findViewById(R.id.recorder);
        ivReset = (ImageView) findViewById(R.id.iv_reset);
        ivComplete = (ImageView) findViewById(R.id.iv_compltet);
        ivFinish = (ImageView) findViewById(R.id.iv_finish);

        progressView.setOnprogressStatusListener(this);
        ivComplete.setOnClickListener(this);
        ivReset.setOnClickListener(this);
        ivFinish.setOnClickListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder holder = mSurfaceView.getHolder();// 取得holder
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.setKeepScreenOn(true);
        holder.addCallback(this); // holder加入回调接口
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        if (Camera.getNumberOfCameras() == 2) {
            mCamera = Camera.open(mCameraFacing);
        } else {
            mCamera = Camera.open();
        }

        CameraSizeComparator sizeComparator = new CameraSizeComparator();
        Camera.Parameters parameters = mCamera.getParameters();

        if (mSize == null) {
            List<Camera.Size> vSizeList = parameters.getSupportedPreviewSizes();
            Collections.sort(vSizeList, sizeComparator);

            for (int num = 0; num < vSizeList.size(); num++) {
                Camera.Size size = vSizeList.get(num);

                if (size.width >= 800 && size.height >= 480) {
                    this.mSize = size;
                    break;
                }
            }
            mSize = vSizeList.get(0);

            List<String> focusModesList = parameters.getSupportedFocusModes();

            //增加对聚焦模式的判断
            if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation = orientations.get(rotation);
        mCamera.setDisplayOrientation(orientation);


    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    /**
     * 开始录制
     */
    private void startRecord() {

        if (mRecorder == null) {
            mRecorder = new MediaRecorder(); // 创建MediaRecorder
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
        }
        try {

            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            // Step 2: Set sources
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//before setOutputFormat()
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//before setOutputFormat()
            //设置视频输出的格式和编码
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            CamcorderProfile mProfile;
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            } else {
                mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            }


            //after setVideoSource(),after setOutFormat()
            mRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            mRecorder.setAudioEncodingBitRate(44100);
            if (mProfile.videoBitRate > 2 * 1024 * 1024)
                mRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
            else
                mRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            //after setVideoSource(),after setOutFormat();
            mRecorder.setVideoFrameRate(mProfile.videoFrameRate);
            //after setOutputFormat()
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //after setOutputFormat()
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //设置记录会话的最大持续时间（毫秒）
            mRecorder.setMaxDuration(MAX_RECORDER_TIME);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = orientations.get(rotation);
            mRecorder.setOrientationHint(orientation);

            path = MainActivity.FILE_PATH;

            if (path != null) {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                path = dir + "/" + System.currentTimeMillis() + ".mp4";
                //设置输出文件的路径
                mRecorder.setOutputFile(path);
                //准备录制
                mRecorder.prepare();
                //开始录制
                mRecorder.start();
                isRecording = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ivFinish.setVisibility(View.GONE);
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        try {
            //停止录制
            mRecorder.stop();
            //重置
            mRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = false;
    }

    /**
     * 释放MediaRecorder
     */
    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.unlock();
                mCamera.release();
            }
        } catch (RuntimeException e) {
        } finally {
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;
        if (!isRecording && mCamera == null) {
            return;
        }

        if (mCamera == null) return;

        try {
            //设置显示
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
            finish();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surfaceDestroyed的时候同时对象设置为null
        if (isRecording && mCamera != null) {
            mCamera.lock();
        }
//        mSurfaceView = null;
//        mSurfaceHolder = null;
//        releaseMediaRecorder();
//        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSurfaceView = null;
        mSurfaceHolder = null;
        releaseMediaRecorder();
        releaseCamera();
    }

    @Override
    public void onStartRecorded() {
        startRecord();
    }

    private void startAnimation() {
        progressView.setVisibility(View.GONE);
        ivFinish.setVisibility(View.GONE);

        ivReset.setVisibility(View.VISIBLE);
        ValueAnimator animation = ObjectAnimator.ofFloat(ivReset, "translationX", 0, -320);
        animation.setDuration(100);
        animation.start();

        ivComplete.setVisibility(View.VISIBLE);
        ValueAnimator animation2 = ObjectAnimator.ofFloat(ivComplete, "translationX", 0, 320);
        animation2.setDuration(100);
        animation2.start();
    }

    private void stopAnimation() {
        progressView.setVisibility(View.VISIBLE);
        ivFinish.setVisibility(View.VISIBLE);

        ivReset.setVisibility(View.GONE);
        ValueAnimator animation = ObjectAnimator.ofFloat(ivReset, "translationX", -320, 0);
        animation.setDuration(100);
        animation.start();

        ivComplete.setVisibility(View.GONE);
        ValueAnimator animation2 = ObjectAnimator.ofFloat(ivComplete, "translationX", -320, 0);
        animation2.setDuration(100);
        animation2.start();
    }

    @Override
    public void onComplete() {
        stopRecord();
        releaseMediaRecorder();
        releaseCamera();

        startAnimation();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            SurfaceHolder holder = mSurfaceView.getHolder();// 取得holder
            holder.setFormat(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(holder);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    mediaPlayer.setLooping(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void resetSufaceView() {
        SurfaceHolder holder = mSurfaceView.getHolder();// 取得holder
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.setKeepScreenOn(true);
        holder.addCallback(this); // holder加入回调接口
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_compltet:
                finish();
                break;
            case R.id.iv_reset:
                stopAnimation();
                new File(path).delete();
                isRecording = true;
                stopPlay();
                initCamera();
                resetSufaceView();
                break;
            case R.id.iv_finish:
                finish();
                break;
        }
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
