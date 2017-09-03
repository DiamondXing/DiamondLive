package com.zxx.diamondlive.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.zxx.diamondlive.R;
import com.zxx.diamondlive.activity.base.BaseActivity;
import com.zxx.diamondlive.bean.LiveReposeBean;
import com.zxx.diamondlive.bean.UpdateStatusBean;
import com.zxx.diamondlive.network.RetrofitManager;
import com.zxx.diamondlive.network.api.CreateApi;
import com.zxx.diamondlive.network.api.UpdateStatusApi;
import com.zxx.diamondlive.utils.ToastUtils;
import com.zxx.diamondlive.view.CameraHintView;
import com.zxx.diamondlive.view.VerticalSeekBar;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.FormBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zxx.diamondlive.R.id.camera_start_live_bt;
import static com.zxx.diamondlive.R.id.switch_cam;

public class CameraActivity extends BaseActivity {

    @BindView(R.id.camera_preview)
    GLSurfaceView mCameraPreview;
    @BindView(R.id.exposure_seekBar)
    VerticalSeekBar mExposureSeekBar;
    @BindView(R.id.chronometer)
    Chronometer mChronometer;
    @BindView(switch_cam)
    ImageView mSwitchCameraView;
    @BindView(R.id.flash)
    ImageView mFlashView;
    @BindView(R.id.exposure)
    ImageView mExposureView;
    @BindView(R.id.url)
    TextView mUrlTextView;
    @BindView(R.id.debuginfo)
    TextView mDebugInfoTextView;
    @BindView(R.id.backoff)
    ImageView mDeleteView;
    @BindView(R.id.click_to_record)
    TextView mRecordingText;
    @BindView(R.id.click_to_capture_screenshot)
    TextView mCaptureSceenShot;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.click_to_switch_beauty)
    TextView clickToSwitchBeauty;
    @BindView(R.id.camera_hint)
    CameraHintView mCameraHintView;
    @BindView(R.id.camera_create_live_et)
    EditText cameraCreateLiveEt;
    @BindView(camera_start_live_bt)
    Button cameraStartLiveBt;

    private KSYStreamer mStreamer;
    private static final String TAG = "CameraActivity";
    private boolean mIsFlashOpened = false;
    private boolean mRecording = false;
    private boolean mIsFileRecording = false;
    private ButtonObserver mObserverButton;
    private boolean mHWEncoderUnsupported;
    private boolean mSWEncoderUnsupported;
    private Timer mTimer;
    private boolean mPrintDebugInfo = false;
    private Handler mMainHandler;
    private String mDebugInfo = "";
    private String mRecordUrl = "/sdcard/rec_test.mp4";
    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;
    private static final String START_RECORDING = "开始录制";
    private static final String STOP_RECORDING = "停止录制";
    private static final String START_BEAUTY = "开启美颜";
    private static final String STOP_BEAUTY = "关闭美颜";
    private boolean startBeauty = false;
    private boolean mAutoStart = false;
    private View bottom_view;
    private boolean isNewLive = true;
    private long live_id;//直播id

    @Override
    protected void initTitleBar(HeaderBuilder builder) {
        builder.goneToolbar();
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        live_id = intent.getLongExtra("live_id", 0L);
        if (live_id != 0L){
            isNewLive = false;
            cameraCreateLiveEt.setVisibility(View.GONE);
        }
        mExposureSeekBar.setProgress(50);
        mExposureSeekBar.setSecondaryProgress(50);
        mExposureSeekBar.setOnSeekBarChangeListener(getVerticalSeekListener());
        mMainHandler = new Handler();
        mObserverButton = new ButtonObserver();
        mRecordingText.setOnClickListener(mObserverButton);
        mCaptureSceenShot.setOnClickListener(mObserverButton);
        mDeleteView.setOnClickListener(mObserverButton);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mFlashView.setOnClickListener(mObserverButton);
        mExposureView.setOnClickListener(mObserverButton);
        // 创建KSYStreamer实例
        mStreamer = new KSYStreamer(this);
        initData();
        bottom_view = findViewById(R.id.bar_bottom);
        bottom_view.setVisibility(View.GONE);

    }

    private void handleEncodeError() {
        int encodeMethod = mStreamer.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got SW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Log.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
    }


    private VerticalSeekBar.OnSeekBarChangeListener getVerticalSeekListener() {
        VerticalSeekBar.OnSeekBarChangeListener listener = new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
                Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
                if (parameters != null) {
                    int minValue = parameters.getMinExposureCompensation();
                    int maxValue = parameters.getMaxExposureCompensation();
                    int range = 100 / (maxValue - minValue);
                    parameters.setExposureCompensation(progress / range - maxValue);
                }
                mStreamer.getCameraCapture().setCameraParameters(parameters);
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar seekBar) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean enable) {

            }
        };
        return listener;
    }

    private void initData() {
        // 设置预览View
        mStreamer.setDisplayPreview(mCameraPreview);
        // 设置推流url（需要向相关人员申请，测试地址并不稳定！）
//        mStreamer.setUrl("rtmp://test.uplive.ksyun.com/live/{streamName}");
//        mStreamer.setUrl("rtmp://121.42.26.175:1935/mytv/1234");
        // 设置预览分辨率, 当一边为0时，SDK会根据另一边及实际预览View的尺寸进行计算
        mStreamer.setPreviewResolution(480, 0);
        // 设置推流分辨率，可以不同于预览分辨率（不应大于预览分辨率，否则推流会有画质损失）
        mStreamer.setTargetResolution(480, 0);
        // 设置预览帧率
        mStreamer.setPreviewFps(15);
        // 设置推流帧率，当预览帧率大于推流帧率时，编码模块会自动丢帧以适应设定的推流帧率
        mStreamer.setTargetFps(15);
        // 设置视频码率，分别为初始平均码率、最高平均码率、最低平均码率，单位为kbps，另有setVideoBitrate接口，单位为bps
        mStreamer.setVideoKBitrate(600, 800, 400);
        // 设置音频采样率
        mStreamer.setAudioSampleRate(44100);
        // 设置音频码率，单位为kbps，另有setAudioBitrate接口，单位为bps
        mStreamer.setAudioKBitrate(48);
        /**
         * 设置编码模式(软编、硬编)，请根据白名单和系统版本来设置软硬编模式，不要全部设成软编或者硬编,白名单可以联系金山云商务:
         * StreamerConstants.ENCODE_METHOD_SOFTWARE
         * StreamerConstants.ENCODE_METHOD_HARDWARE
         */
        mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
        // 设置屏幕的旋转角度，支持 0, 90, 180, 270
        mStreamer.setRotateDegrees(0);
        // 设置开始预览使用前置还是后置摄像头
        mStreamer.setCameraFacing(CameraCapture.FACING_FRONT);
        mStreamer.setOnInfoListener(mOnInfoListener);
        mStreamer.setOnErrorListener(mOnErrorListener);
        //水印
        mStreamer.showWaterMarkTime(0.03f, 0.01f, 0.35f, Color.WHITE, 1.0f);
        clickToSwitchBeauty.setOnClickListener(mObserverButton);
        CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
        cameraTouchHelper.setCameraCapture(mStreamer.getCameraCapture());
        mCameraPreview.setOnTouchListener(cameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        cameraTouchHelper.setCameraHintView(mCameraHintView);
    }

    private void setCameraAntiBanding50Hz() {
        Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
            mStreamer.getCameraCapture().setCameraParameters(parameters);
        }
    }

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_INIT_DONE");
                    setCameraAntiBanding50Hz();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    beginInfoUploadTimer();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_FILE_SUCCESS");
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    break;
                case StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED:
                    Log.d(TAG, "KSY_STREAMER_FILE_RECORD_STOPPED");
                    mRecordingText.setText(START_RECORDING);
                    mRecordingText.postInvalidate();
                    mIsFileRecording = false;
                    stopChronometer();
                    break;
                case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                    Log.d(TAG, "KSY_STREAMER_FRAME_SEND_SLOW " + msg1 + "ms");
                    Toast.makeText(CameraActivity.this, "Network not good!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:
                    Log.d(TAG, "BW raise to " + msg1 / 1000 + "kbps");
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_DROP:
                    Log.d(TAG, "BW drop to " + msg1 / 1000 + "kpbs");
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    mStreamer.stopCameraPreview();
                    break;
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                    stopRecord();
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN: {
                    handleEncodeError();
                    if (mRecording) {
                        stopStream();
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startStream();
                            }
                        }, 3000);
                    }
                    if (mIsFileRecording) {
                        stopRecord();
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startRecord();
                            }
                        }, 50);
                    }
                }
                break;
                default:
                    if (mStreamer.getEnableAutoRestart()) {
                        mRecording = false;
                        stopChronometer();
                    } else {
                        stopStream();
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startStream();
                            }
                        }, 3000);
                    }
                    break;
            }
        }
    };

    private void beginInfoUploadTimer() {
        if (mPrintDebugInfo && mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateDebugInfo();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDebugInfoTextView.setText(mDebugInfo);
                        }
                    });
                }
            }, 100, 1000);
        }
    }

    //update debug info
    private void updateDebugInfo() {
        if (mStreamer == null) return;
        mDebugInfo = String.format(Locale.getDefault(),
                "RtmpHostIP()=%s DroppedFrameCount()=%d \n " +
                        "ConnectTime()=%d DnsParseTime()=%d \n " +
                        "UploadedKB()=%d EncodedFrames()=%d \n" +
                        "CurrentKBitrate=%d Version()=%s",
                mStreamer.getRtmpHostIP(), mStreamer.getDroppedFrameCount(),
                mStreamer.getConnectTime(), mStreamer.getDnsParseTime(),
                mStreamer.getUploadedKBytes(), mStreamer.getEncodedFrames(),
                mStreamer.getCurrentUploadKBitrate(), KSYStreamer.getVersion());
    }

    private void startRecord() {
        if (mIsFileRecording) {
            return;
        }
        //录制开始成功后会发送StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS消息
        mStreamer.startRecord(mRecordUrl);
        mRecordingText.setText(STOP_RECORDING);
        mRecordingText.postInvalidate();
        mIsFileRecording = true;
    }

    private void onSwitchCamera() {
        mStreamer.switchCamera();
        mCameraHintView.hideAll();
    }

    private void onFlashClick() {
        if (mIsFlashOpened) {
            mStreamer.toggleTorch(false);
            mIsFlashOpened = false;
        } else {
            mStreamer.toggleTorch(true);
            mIsFlashOpened = true;
        }
    }

    private void stopStream() {
        // stop stream
        mStreamer.stopStream();
        if (live_id != 0L) {
            updateStatus(live_id, 1);
            finish();
        }
        mRecording = false;
        stopChronometer();
    }

    private void startStream() {
        Log.d("aaa live id", String.valueOf(live_id));
        if (live_id != 0L) {
            mStreamer.setUrl("rtmp://uplive.geekniu.com/live/abc"+live_id);
            mStreamer.startStream();
            updateStatus(live_id, 0);
        }
        mRecording = true;
    }

    private void stopChronometer() {
        if (mRecording || mIsFileRecording) {
            return;
        }
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
    }

    private void stopRecord() {
        //录制结束为异步接口，录制结束后，
        //会发送StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED消息，在这里再处理UI恢复工作
        mStreamer.stopRecord();
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.click_to_switch_beauty:
                    if (startBeauty) {
                        startBeauty = false;
                        clickToSwitchBeauty.setText(START_BEAUTY);
                    } else {
                        startBeauty = true;
                        clickToSwitchBeauty.setText(STOP_BEAUTY);
                    }
                    onBeautyChecked(startBeauty);
                    break;
                case switch_cam:
                    onSwitchCamera();
                    break;
                case R.id.backoff:
                    onBackoffClick();
                    break;
                case R.id.flash:
                    onFlashClick();
                    break;
                case R.id.click_to_record:
                    onRecordClick();
                    break;
                case R.id.click_to_capture_screenshot:
                    onCaptureScreenShotClick();
                    break;
                case R.id.exposure:
                    onExposureClick();
                    break;
                default:
                    break;
            }
        }
    }

    private void onBeautyChecked(boolean startBeauty) {
        if (startBeauty) {
            mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(),
                    ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO);
            List<ImgFilterBase> filters = mStreamer.getImgTexFilterMgt().getFilter();
            if (filters != null && !filters.isEmpty()) {
                final ImgFilterBase filter = filters.get(0);
                filter.setGrindRatio(99 / 100.f);
                filter.setWhitenRatio(80 / 100.f);

                if (filter instanceof ImgBeautyProFilter) {
                    float val = 50 / 50.f - 1.0f;
                    filter.setRuddyRatio(val);
                }
            }
        } else {
            mStreamer.getImgTexFilterMgt().setFilter((ImgFilterBase) null);
        }
    }

    private void onBackoffClick() {
        new AlertDialog.Builder(CameraActivity.this).setCancelable(true)
                .setTitle("结束直播?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mChronometer.stop();
                        stopStream();
                        CameraActivity.this.finish();
                    }
                }).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mRecording){
                    onBackoffClick();
                }else{
                    finish();
                }

                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 曝光度调节
     */
    private void onExposureClick() {
        if (mExposureSeekBar.getVisibility() == View.VISIBLE) {
            mExposureSeekBar.setVisibility(View.GONE);
        } else {
            mExposureSeekBar.setVisibility(View.VISIBLE);
        }
    }

    private void onCaptureScreenShotClick() {
        mStreamer.requestScreenShot(new GLRender.ScreenShotListener() {
            @Override
            public void onBitmapAvailable(Bitmap bitmap) {
                BufferedOutputStream bos = null;
                try {
                    Date date = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                    final String filename = "/sdcard/screenshot" + dateFormat.format(date) + ".jpg";

                    bos = new BufferedOutputStream(new FileOutputStream(filename));
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(CameraActivity.this, "保存截图到 " + filename,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (bos != null) try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void onShootClick() {
        if (mRecording) {
            stopStream();
        } else {
            startStream();
        }
    }

    private void onRecordClick() {
        if (mIsFileRecording) {
            stopRecord();
        } else {
            startRecord();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 一般可以在onResume中开启摄像头预览
        mStreamer.startCameraPreview();
        // 调用KSYStreamer的onResume接口
        mStreamer.onResume();
        // 如果onPause中切到了DummyAudio模块，可以在此恢复
        mStreamer.setUseDummyAudioCapture(false);
        startCameraPreviewWithPermCheck(true);
        mStreamer.setEnableAudioLowDelay(true);
        mCameraHintView.hideAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        mStreamer.onPause();
        // 一般在这里停止摄像头采集
        mStreamer.stopCameraPreview();
        // 如果希望App切后台后，停止录制主播端的声音，可以在此切换为DummyAudio采集，
        // 该模块会代替mic采集模块产生静音数据，同时释放占用的mic资源
        mStreamer.setUseDummyAudioCapture(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mStreamer.setOnLogEventListener(null);
        mStreamer.release();
    }

    private void startCameraPreviewWithPermCheck(boolean request) {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !request) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                Toast.makeText(this, "No CAMERA or AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            mStreamer.startCameraPreview();
            if (mAutoStart) {
                mAutoStart = false;
                startStream();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStreamer.startCameraPreview();
                    if (mAutoStart) {
                        mAutoStart = false;
                        startStream();
                    }
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(this, "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    //创建直播
    @OnClick(camera_start_live_bt)
    public void startLive(View view){
        if (isNewLive){//新建的直播
            createLive();
        }else{
            cameraStartLiveBt.setVisibility(View.GONE);
            bottom_view.setVisibility(View.VISIBLE);
            onShootClick();
        }
    }
    //创建直播
    private void createLive(){
        String live_name = cameraCreateLiveEt.getText().toString().trim();
        SharedPreferences sp = getSharedPreferences("user",MODE_PRIVATE);
        long user_id = sp.getLong("user_id", 0);
        //生成随机数，作为直播类型
        Random random = new Random();
        int live_type = random.nextInt(2) + 1;
        //封面图片的数组
        String[] pics = new String[]{
                "http://img1.imgtn.bdimg.com/it/u=2376399524,1027148780&fm=214&gp=0.jpg",
                "http://img3.imgtn.bdimg.com/it/u=963833047,3937484028&fm=26&gp=0.jpg",
                "http://img0.imgtn.bdimg.com/it/u=241544545,2518283365&fm=214&gp=0.jpg",
                "http://www.az-meter.com/d/imgs/bd24704890.jpg",
                "http://img3.imgtn.bdimg.com/it/u=614690089,4034631071&fm=214&gp=0.jpg",
                "http://pic2016.5442.com:82/2016/0615/27/2.jpg!960.jpg",
                "http://pic1.5442.com/2015/0814/03/01.jpg!960.jpg",
                "http://c.hiphotos.baidu.com/zhidao/pic/item/7a899e510fb30f24d8689e34ce95d143ad4b0312.jpg"
        };
        int picIndex = random.nextInt(pics.length);
        Log.d("aaa:index",picIndex+"");
        Log.d("aaa:live_type",live_type+"");
        CreateApi createApi = RetrofitManager.getTestRetrofit().create(CreateApi.class);
        FormBody body = new FormBody.Builder()
                .add("uid", String.valueOf(user_id))
                .add("pic",pics[picIndex])
                .add("live_name",live_name)
                .add("live_type", String.valueOf(live_type))
                .build();
        Call<LiveReposeBean> createCall = createApi.createLive(body);
        createCall.enqueue(new Callback<LiveReposeBean>() {
            @Override
            public void onResponse(Call<LiveReposeBean> call, Response<LiveReposeBean> response) {
                LiveReposeBean bean = response.body();
                if (bean.getResult() == null || bean.getError_code() != 0){
                    ToastUtils.showShort(bean.getError_msg());
                    return;
                }
                cameraCreateLiveEt.setVisibility(View.GONE);
                cameraStartLiveBt.setVisibility(View.GONE);
                bottom_view.setVisibility(View.VISIBLE);
                live_id = bean.getResult().getId();
                //创建成功
                onShootClick();
            }

            @Override
            public void onFailure(Call<LiveReposeBean> call, Throwable t) {
                ToastUtils.showShort("创建失败");
            }
        });
    }
    //改变直播状态
    private void updateStatus(long live_id, final int status){
        UpdateStatusApi statusApi = RetrofitManager.getTestRetrofit().create(UpdateStatusApi.class);
        FormBody body = new FormBody.Builder()
                .add("live_id",live_id+"")
                .add("status",status+"")
                .build();
        Call<UpdateStatusBean> updateStatus = statusApi.updateStatus(body);
        updateStatus.enqueue(new Callback<UpdateStatusBean>() {
            @Override
            public void onResponse(Call<UpdateStatusBean> call, Response<UpdateStatusBean> response) {
                List<UpdateStatusBean.ResultBean> result = response.body().getResult();
                if (result == null || result.size() == 0){
                    return;
                }
                String preStatus;
                if (status == 0){
                    preStatus = "直播";
                }else{
                    preStatus = "录播";
                }
                ToastUtils.showShort("直播状态已更改为"+preStatus);
            }

            @Override
            public void onFailure(Call<UpdateStatusBean> call, Throwable t) {
                ToastUtils.showShort("更改直播失败"+t);
            }
        });
    }
}
