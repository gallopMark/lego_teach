package com.haoyu.app.activity;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.CoursehwNumResult;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 摇一摇领取作业
 */
public class AssignmentShakeActivity extends BaseActivity {
    private AssignmentShakeActivity context = this;
    @BindView(R.id.iv_back)
    View iv_back;
    private SensorManager sensorManager = null;
    private Vibrator vibrator = null;
    private SensorEventListener shakeListener;
    private String courseId;
    private boolean isRefresh = false;
    @BindView(R.id.tv_readOverNum)
    TextView tv_readOverNum;  //领取的作业数
    @BindView(R.id.ll_notReceivedNum)
    View ll_notReceivedNum;
    @BindView(R.id.tv_notReceivedNum)
    TextView tv_notReceivedNum;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_shake_it_off;
    }

    @Override
    public void initView() {
        courseId = getIntent().getStringExtra("courseId");
        CoursehwNumResult numResult = (CoursehwNumResult) getIntent().getSerializableExtra("numResult");
        shakeListener = new ShakeSensorListener();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        updateUI(numResult);
    }

    private void updateUI(CoursehwNumResult userNumResult) {
        if (userNumResult != null && userNumResult.getResponseData() != null) {
            tv_readOverNum.setText(userNumResult.getResponseData().getMarkNum() + "/" + userNumResult.getResponseData().getAllNum());
            if (userNumResult.getResponseData().getNotMarkedNum() == 0
                    && userNumResult.getResponseData().getNotReceivedNum() > 0) {
                ll_notReceivedNum.setVisibility(View.VISIBLE);
                tv_notReceivedNum.setText(String.valueOf(userNumResult.getResponseData().getNotReceivedNum()));
            }
        }
    }

    @Override
    public void setListener() {
        iv_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册手机摇一摇监听
        sensorManager.registerListener(shakeListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // acitivity后台时取消监听
        sensorManager.unregisterListener(shakeListener);
    }

    private class ShakeSensorListener implements SensorEventListener {
        private static final int ACCELERATE_VALUE = 20;

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 判断是否处于刷新状态(例如微信中的查找附近人)
            if (isRefresh) {
                return;
            }
            int sensorType = event.sensor.getType();
            // values[0]:X轴，values[1]：Y轴，values[2]：Z轴
            float[] values = event.values;
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                if ((Math.abs(values[0]) > ACCELERATE_VALUE
                        || Math.abs(values[1]) > ACCELERATE_VALUE || Math
                        .abs(values[2]) > ACCELERATE_VALUE)) {
                    // 摇动手机后，再伴随震动提示~~
                    vibrator.vibrate(500);
                    isRefresh = true;
                    getAssignment();
                }

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    /**
     * 领取作业
     */
    private void getAssignment() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/unique_uid_" + userId + "/m/assignment/mark/" + courseId;
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                isRefresh = false;
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                isRefresh = false;
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.GET_COURSE_ASSIGNMENT;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast(context, "暂时没有未批阅的作业");
                    }
                }
            }
        });
    }

}
