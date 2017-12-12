package com.haoyu.app.adapter;

import android.support.v4.util.ArrayMap;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.EvaluateItemSubmissions;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.view.StarBar;

import java.util.List;

/**
 * 创建日期：2017/2/7 on 11:31
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class EvaluateItemAdapter extends BaseArrayRecyclerAdapter<EvaluateItemSubmissions> {

    private ScoreChangeListener scoreChangeListener;
    private ArrayMap<Integer, Integer> evaluateMap = new ArrayMap<>();
    private boolean enable;

    public EvaluateItemAdapter(List<EvaluateItemSubmissions> mDatas, boolean enable) {
        super(mDatas);
        this.enable = enable;
    }

    public void setScoreChangeListener(ScoreChangeListener scoreChangeListener) {
        this.scoreChangeListener = scoreChangeListener;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final EvaluateItemSubmissions submissions, final int position) {
        TextView tv_content = holder.obtainView(R.id.tv_content);
        final StarBar ratingBar = holder.obtainView(R.id.ratingBar);
        tv_content.setText(submissions.getContent());
        ratingBar.setIntegerMark(true);
        ratingBar.setOnStarChangeListener(new StarBar.OnStarChangeListener() {
            @Override
            public void onStarChange(float mark) {
                evaluateMap.put(position, (int) mark);
                if (scoreChangeListener != null) {
                    scoreChangeListener.scoreChange(evaluateMap);
                }
            }
        });
        if (enable) {
            ratingBar.setCanEdit(true);
        } else {
            ratingBar.setCanEdit(false);
        }
        ratingBar.setStarMark((float) submissions.getScore());
        evaluateMap.put(position, (int) submissions.getScore());
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teacher_evaluate_list_item;
    }

    public interface ScoreChangeListener {
        void scoreChange(ArrayMap<Integer, Integer> evaluateMap);
    }
}
