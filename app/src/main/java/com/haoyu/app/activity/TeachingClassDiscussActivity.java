package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.CircularProgressView;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 创建日期：2017/11/2.
 * 描述:评课议课
 * 作者:Administrator
 */
public class TeachingClassDiscussActivity extends BaseActivity implements View.OnClickListener {
    private TeachingClassDiscussActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.ll_layout)
    LinearLayout ll_layout;
    @BindView(R.id.tv_time)
    TextView tv_time;

    /*videoplayer layout*/
    @BindView(R.id.fl_video)
    FrameLayout fl_video;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.tv_loading)
    TextView tv_loading;
    @BindView(R.id.cpvLoading)
    CircularProgressView cpvLoading;
    @BindView(R.id.videoView)
    PLVideoTextureView videoView;
    @BindView(R.id.ll_control)
    LinearLayout ll_control;
    @BindView(R.id.iv_type)
    ImageView iv_type;
    @BindView(R.id.tv_progress)
    TextView tv_progress;
    @BindView(R.id.ll_playState)
    LinearLayout ll_playState;
    @BindView(R.id.iv_playState)
    ImageView iv_playState;
    @BindView(R.id.seekbar)
    SeekBar seekbar;
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_videoSize)
    TextView tv_videoSize;
    @BindView(R.id.iv_expand)
    ImageView iv_expand;
    /*videoplayer layout*/

    @BindView(R.id.ll_videoOutSide)
    LinearLayout ll_videoOutSide;
    private String videoUrl;
    private int smallHeight;
    private boolean isFullScreen;
    private AudioManager mAudioManager;
    private boolean progress_turn;
    private long currentDuration = -1;  //当前播放位置
    private int maxVolume, currentVolume;
    private float mBrightness = -1f; // 亮度
    /* 状态常量*/
    private final int STATUS_ERROR = -1;
    private final int STATUS_IDLE = 0;
    private final int STATUS_PREPARED = 1;
    private final int STATUS_LOADING = 2;
    private final int STATUS_PLAYING = 3;
    private final int STATUS_PAUSE = 4;
    private final int STATUS_COMPLETED = 5;
    private int ERROR_CODE;

    private Disposable timerTask;

    private NetWorkReceiver receiver;
    private MaterialDialog materialDialog;
    private boolean openWithMobile = false, playError;

    private final int CODE_PREPARED = 1;
    private final int CODE_ENDGESTURE = 2;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teachclass_comment;
    }

    @Override
    public void initView() {
        videoUrl = getIntent().getStringExtra("videoUrl");
        String title = getIntent().getStringExtra("title");
        if (title != null)
            toolBar.setTitle_text(title);
        else
            toolBar.setTitle_text("评课议课");
        smallHeight = ScreenUtils.getScreenHeight(context) / 3;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
        fl_video.setLayoutParams(params);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 20 * 1000);
        videoView.setAVOptions(options);
        videoView.setScreenOnWhilePlaying(true);
        receiver = new NetWorkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        iv_play.setOnClickListener(context);
        iv_playState.setOnClickListener(context);
        iv_expand.setOnClickListener(context);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
            }
        });
        final GestureDetector detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private boolean firstTouch;
            private boolean volumeControl;
            private boolean toSeek;

            @Override
            public boolean onDown(MotionEvent e) {
                firstTouch = true;
                if (ll_playState.getVisibility() != View.VISIBLE) {
                    ll_playState.setVisibility(View.VISIBLE);
                }
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                final float deltaX = mOldX - e2.getX();
                int screenWidthPixels = ScreenUtils.getScreenWidth(context);
                if (firstTouch) {
                    toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                    volumeControl = mOldX > screenWidthPixels * 0.5f;
                    firstTouch = false;
                }
                if (toSeek) {
                    onProgressSlide(-deltaX / fl_video.getWidth());
                } else {
                    float percent = deltaY / fl_video.getHeight();
                    if (volumeControl) {
                        onVolumeSlide(percent);
                    } else {
                        onBrightnessSlide(percent);
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        fl_video.setClickable(true);
        fl_video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if (detector.onTouchEvent(event))
                    return true;
                // 处理手势结束
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        endGesture();
                        break;
                }
                return false;
            }
        });
    }

    private void onProgressSlide(float percent) {
        if (ll_control.getVisibility() != View.VISIBLE)
            ll_control.setVisibility(View.VISIBLE);
        //计算并显示 前进后退
        if (!progress_turn) {
            progress_turn = true;
        }
        long position = videoView.getCurrentPosition();
        long duration = videoView.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        currentDuration = delta + position;
        if (currentDuration > duration) {
            currentDuration = duration;
        } else if (currentDuration <= 0) {
            currentDuration = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta > 0)
            iv_type.setImageResource(R.drawable.video_btn_fast_forword);
        else
            iv_type.setImageResource(R.drawable.video_btn_back_forword);
        tv_progress.setText(formatDate(currentDuration) + "/" + formatDate(duration));
    }

    /*滑动改变亮度*/
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            } else if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        getWindow().setAttributes(lpa);
        if (ll_control.getVisibility() != View.VISIBLE)
            ll_control.setVisibility(View.VISIBLE);
        iv_type.setImageResource(R.drawable.ic_brightness);
        tv_progress.setText(Common.accuracy(lpa.screenBrightness, 1.0, 0));
    }

    //加减音量
    private void onVolumeSlide(float percent) {
        if (currentVolume == -1) {
            currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume < 0)
                currentVolume = 0;
        }
        currentVolume = (int) (percent * maxVolume) + currentVolume;
        if (currentVolume > maxVolume)
            currentVolume = maxVolume;
        else if (currentVolume < 0)
            currentVolume = 0;
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        if (ll_control.getVisibility() != View.VISIBLE)
            ll_control.setVisibility(View.VISIBLE);
        if (currentVolume > 0)
            iv_type.setImageResource(R.drawable.ic_voice_max);
        else
            iv_type.setImageResource(R.drawable.ic_voice_min);
        tv_progress.setText(Common.accuracy(currentVolume, maxVolume, 0));
    }

    private void endGesture() {
        currentVolume = -1;
        mBrightness = -1f;
        handler.removeMessages(CODE_ENDGESTURE);
        handler.sendEmptyMessageDelayed(CODE_ENDGESTURE, 5000);
        if (progress_turn) {
            if (ll_control.getVisibility() != View.GONE) {
                ll_control.setVisibility(View.GONE);
            }
            videoView.seekTo(currentDuration);
            progress_turn = false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                if (NetStatusUtil.isConnected(context)) {
                    if (NetStatusUtil.isWifi(context)) {
                        iv_play.setVisibility(View.GONE);
                        playVideo();
                    } else {
                        if (!openWithMobile) {
                            MaterialDialog dialog = new MaterialDialog(context);
                            dialog.setTitle("网络提醒");
                            dialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
                            dialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
                                @Override
                                public void onClick(View v, AlertDialog dialog) {
                                    openWithMobile = true;
                                    iv_play.setVisibility(View.GONE);
                                    playVideo();
                                }
                            });
                            dialog.setPositiveButton("取消", null);
                            dialog.show();
                        } else {
                            toast(context, "当前网络为非Wi-FI环境，请注意您的流量使用情况");
                        }
                    }
                }
                break;
            case R.id.iv_playState:
                if (videoView.isPlaying()) {
                    statusChange(STATUS_PAUSE);
                } else {
                    statusChange(STATUS_PLAYING);
                }
                break;
            case R.id.iv_expand:
                if (!isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
        }
    }

    private void playVideo() {
        statusChange(STATUS_IDLE);
        videoView.setVideoPath(videoUrl);
        videoView.start();
        if (currentDuration != -1) {
            videoView.seekTo(currentDuration);
        }
        videoView.setOnPreparedListener(new PLMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(PLMediaPlayer plMediaPlayer, int i) {
                long duration = plMediaPlayer.getDuration();
                seekbar.setMax((int) duration);
                tv_videoSize.setText(formatDate(duration));
                statusChange(STATUS_PREPARED);
            }
        });
        videoView.setOnInfoListener(new PLMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
                switch (what) {
                    case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        statusChange(STATUS_LOADING);
                        break;
                    case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        statusChange(STATUS_PLAYING);
                        break;
                    case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        statusChange(STATUS_PLAYING);
                        break;
                }
                return false;
            }
        });
        videoView.setOnCompletionListener(new PLMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(PLMediaPlayer plMediaPlayer) {
                statusChange(STATUS_COMPLETED);
            }
        });
        videoView.setOnBufferingUpdateListener(new PLMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int progress) {
                seekbar.setSecondaryProgress(progress);
            }
        });
        videoView.setOnErrorListener(new PLMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
                playError = true;
                currentDuration = plMediaPlayer.getCurrentPosition();
                ERROR_CODE = errorCode;
                statusChange(STATUS_ERROR);
                return false;
            }
        });
        timerTask = Flowable.interval(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                long currentDuration = videoView.getCurrentPosition();
                seekbar.setProgress((int) currentDuration);
                tv_current.setText(formatDate(currentDuration));
            }
        });
    }

    private void statusChange(int status) {
        switch (status) {
            case STATUS_IDLE:
                cancelTimer();
                if (tv_loading.getVisibility() != View.VISIBLE) {
                    tv_loading.setText("即将播放...");
                    tv_loading.setVisibility(View.VISIBLE);
                }
                if (cpvLoading.getVisibility() != View.GONE) {
                    cpvLoading.setVisibility(View.GONE);
                }
                break;
            case STATUS_PREPARED:
                if (tv_loading.getVisibility() != View.GONE) {
                    tv_loading.setVisibility(View.GONE);
                }
                handler.sendEmptyMessageDelayed(CODE_PREPARED, 3000);
                break;
            case STATUS_LOADING:
                if (tv_loading.getVisibility() != View.GONE) {
                    tv_loading.setVisibility(View.GONE);
                }
                if (cpvLoading.getVisibility() != View.VISIBLE) {
                    cpvLoading.setVisibility(View.VISIBLE);
                }
                break;
            case STATUS_PLAYING:
                if (!videoView.isPlaying()) {
                    videoView.start();
                }
                iv_playState.setImageResource(R.drawable.ic_pause);
                if (tv_loading.getVisibility() != View.GONE) {
                    tv_loading.setVisibility(View.GONE);
                }
                if (cpvLoading.getVisibility() != View.GONE) {
                    cpvLoading.setVisibility(View.GONE);
                }
                break;
            case STATUS_PAUSE:
                videoView.pause();
                iv_playState.setImageResource(R.drawable.ic_play);
                break;
            case STATUS_COMPLETED:
                cancelTimer();
                if (!playError)
                    currentDuration = -1;
                if (cpvLoading.getVisibility() != View.GONE) {
                    cpvLoading.setVisibility(View.GONE);
                }
                if (tv_loading.getVisibility() != View.VISIBLE) {
                    tv_loading.setText("播放完毕");
                    tv_loading.setVisibility(View.VISIBLE);
                }
                ll_playState.setVisibility(View.GONE);
                iv_play.setVisibility(View.VISIBLE);
                break;
            case STATUS_ERROR:
                cancelTimer();
                String errorMsg;
                if (ERROR_CODE == PLMediaPlayer.ERROR_CODE_IO_ERROR) {
                    errorMsg = "网络异常";
                } else if (ERROR_CODE == PLMediaPlayer.ERROR_CODE_OPEN_FAILED) {
                    errorMsg = "播放器打开失败";
                } else if (ERROR_CODE == PLMediaPlayer.ERROR_CODE_SEEK_FAILED) {
                    errorMsg = "拖动失败";
                } else if (ERROR_CODE == PLMediaPlayer.ERROR_CODE_HW_DECODE_FAILURE) {
                    errorMsg = "硬解失败";
                } else {
                    errorMsg = "视频播放出错了~";
                }
                tv_loading.setText(errorMsg);
                if (tv_loading.getVisibility() != View.VISIBLE) {
                    tv_loading.setVisibility(View.VISIBLE);
                }
                if (cpvLoading.getVisibility() != View.GONE) {
                    cpvLoading.setVisibility(View.GONE);
                }
                ll_playState.setVisibility(View.GONE);
                iv_play.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String formatDate(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");//初始化Formatter的转换格式。
        return formatter.format(ms);
    }

    private void cancelTimer() {
        if (timerTask != null) {
            timerTask.dispose();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 判断Android当前的屏幕是横屏还是竖屏。横竖屏判断
        setOrientattion(newConfig.orientation);
    }

    private void setOrientattion(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {   //竖屏
            iv_expand.setImageResource(R.drawable.quanping);
            isFullScreen = false;
            showOutSize();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
            fl_video.setLayoutParams(params);
        } else { //横屏
            iv_expand.setImageResource(R.drawable.xiaoping);
            isFullScreen = true;
            hideOutSize();
            int screeWidth = ScreenUtils.getScreenWidth(context);
            int screenHeight = ScreenUtils.getScreenHeight(context);
            int statusHeight = ScreenUtils.getStatusHeight(context);
            int realHeight = screenHeight - statusHeight;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screeWidth, realHeight);
            fl_video.setLayoutParams(params);
        }
    }

    private void showOutSize() {
        tv_time.setVisibility(View.VISIBLE);
        toolBar.setVisibility(View.VISIBLE);
        ll_videoOutSide.setVisibility(View.VISIBLE);
    }

    private void hideOutSize() {
        toolBar.setVisibility(View.GONE);
        tv_time.setVisibility(View.GONE);
        ll_videoOutSide.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_PREPARED:
                    if (ll_playState.getVisibility() != View.GONE) {
                        ll_playState.setVisibility(View.GONE);
                    }
                    break;
                case CODE_ENDGESTURE:
                    if (ll_control.getVisibility() != View.GONE) {
                        ll_control.setVisibility(View.GONE);
                    }
                    if (ll_playState.getVisibility() != View.GONE) {
                        ll_playState.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    private class NetWorkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    typeChange();
                }
            }
        }
    }

    private void typeChange() {
        if (materialDialog != null) {
            materialDialog.dismiss();
        }
        if (!openWithMobile) {
            if (videoView.isPlaying()) {
                statusChange(STATUS_PAUSE);
                materialDialog = new MaterialDialog(context);
                materialDialog.setTitle("网络提醒");
                materialDialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
                materialDialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        openWithMobile = true;
                        statusChange(STATUS_PLAYING);
                        dialog.dismiss();
                    }
                });
                materialDialog.setPositiveButton("取消", null);
                materialDialog.show();
            }
        } else {
            toast(context, "当前网络为非Wi-FI环境，请注意您的流量使用情况");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!videoView.isPlaying()) {
            statusChange(STATUS_PLAYING);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        statusChange(STATUS_PAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        videoView.stopPlayback();
    }

}
