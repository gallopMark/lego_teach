package com.haoyu.app.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
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
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.CircularProgressView;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoView;

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
    @BindView(R.id.cpvLoading)
    CircularProgressView cpvLoading;
    @BindView(R.id.videoView)
    PLVideoView videoView;
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
    private Disposable timerTask;

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
        smallHeight = ScreenUtils.getScreenHeight(context) / 5 * 2;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
        fl_video.setLayoutParams(params);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
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
            public boolean onSingleTapUp(MotionEvent e) {
                Flowable.timer(5000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        ll_playState.setVisibility(View.GONE);
                    }
                });
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isFullScreen)
                    return false;
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
            public boolean onTouch(View v, MotionEvent event) {
                if (detector.onTouchEvent(event))
                    return true;
                // 处理手势结束
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        endGesture();
                        break;
                }
                return false;
            }
        });
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
        int current = (int) (percent * maxVolume) + currentVolume;
        if (current > maxVolume)
            current = maxVolume;
        else if (current < 0)
            current = 0;
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, 0);
        if (ll_control.getVisibility() != View.VISIBLE)
            ll_control.setVisibility(View.VISIBLE);
        if (current > 0)
            iv_type.setImageResource(R.drawable.ic_voice_max);
        else
            iv_type.setImageResource(R.drawable.ic_voice_min);
        tv_progress.setText(Common.accuracy(current, maxVolume, 0));
    }

    private void endGesture() {
        currentVolume = -1;
        mBrightness = -1f;
        Flowable.timer(3000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (ll_control.getVisibility() != View.GONE) {
                    ll_control.setVisibility(View.GONE);
                }
                if (ll_playState.getVisibility() != View.GONE) {
                    ll_playState.setVisibility(View.GONE);
                }
            }
        });
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
                iv_play.setVisibility(View.GONE);
                playVideo();
                break;
            case R.id.iv_playState:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    iv_playState.setImageResource(R.drawable.ic_play);
                } else {
                    videoView.start();
                    iv_playState.setImageResource(R.drawable.ic_pause);
                }
                break;
            case R.id.iv_expand:
                if (!isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    iv_expand.setImageResource(R.drawable.xiaoping);
                    isFullScreen = true;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    iv_expand.setImageResource(R.drawable.quanping);
                    isFullScreen = false;
                }
                break;
        }
    }

    private void playVideo() {
        cancelTimer();
        videoView.setVideoPath(videoUrl);
        videoView.setScreenOnWhilePlaying(true);
        videoView.start();
        if (currentDuration != -1)
            videoView.seekTo(currentDuration);
        videoView.setOnPreparedListener(new PLMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(PLMediaPlayer player) {
                long duration = player.getDuration();
                seekbar.setMax((int) duration);
                tv_videoSize.setText(formatDate(duration));
                if (ll_playState.getVisibility() != View.GONE) {
                    Flowable.timer(3000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            ll_playState.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
        videoView.setOnInfoListener(new PLMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int i1) {
                switch (what) {
                    case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        if (cpvLoading.getVisibility() != View.VISIBLE) {
                            cpvLoading.setVisibility(View.VISIBLE);
                        }
                        break;
                    case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        cpvLoading.setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });
        videoView.setOnCompletionListener(new PLMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(PLMediaPlayer plMediaPlayer) {
                if (cpvLoading.getVisibility() != View.GONE) {
                    cpvLoading.setVisibility(View.GONE);
                }
                ll_playState.setVisibility(View.GONE);
                iv_play.setVisibility(View.VISIBLE);
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
                cancelTimer();
                toast(context, "视频播放出错了~");
                if (cpvLoading.getVisibility() != View.GONE) {
                    cpvLoading.setVisibility(View.GONE);
                }
                ll_playState.setVisibility(View.GONE);
                iv_play.setVisibility(View.VISIBLE);
                return false;
            }
        });
        timerTask = Flowable.interval(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                long current = videoView.getCurrentPosition();
                seekbar.setProgress((int) current);
                tv_current.setText(formatDate(current));
            }
        });
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
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showOutSize();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, smallHeight);
            fl_video.setLayoutParams(params);
            //竖屏
        } else {
            //横屏
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
            iv_expand.setImageResource(R.drawable.quanping);
            isFullScreen = false;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!videoView.isPlaying())
            videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        videoView.stopPlayback();
    }

}
