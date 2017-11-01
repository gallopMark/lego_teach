package com.haoyu.app.adapter;

import android.support.v4.util.ArrayMap;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.EvaluateItemSubmissions;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.view.StarBar;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建日期：2017/2/7 on 11:31
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class EvaluateItemAdapter extends BaseArrayRecyclerAdapter<EvaluateItemSubmissions> {

    private ScoreChangeListener scoreChangeListener;
    private ArrayMap<Integer, EvaluateItemSubmissions> evaluateMap = new ArrayMap<>();
    private String state;

    public EvaluateItemAdapter(List<EvaluateItemSubmissions> mDatas, String state) {
        super(mDatas);
        this.state = state;
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
                EvaluateItemSubmissions itemSubmissions;
                if (evaluateMap.get(position) == null) {
                    itemSubmissions = new EvaluateItemSubmissions();
                } else {
                    itemSubmissions = evaluateMap.get(position);
                }
                itemSubmissions.setId(submissions.getId());
                itemSubmissions.setStarCount((int) mark);
                itemSubmissions.setScore(getScore(mark, submissions.getEvaluateMark()));
                evaluateMap.put(position, itemSubmissions);
                if (scoreChangeListener != null) {
                    scoreChangeListener.scoreChange(evaluateMap);
                }
            }
        });
        if (state != null && state.equals("return")) {
            ratingBar.setCanEdit(false);
            ratingBar.setStarMark(0);
        } else {
            ratingBar.setCanEdit(true);
            ratingBar.setStarMark((float) submissions.getScore());
        }
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.teacher_evaluate_list_item;
    }

    private int getScore(float mark, double score) {
        BigDecimal b = new BigDecimal(mark * score / 5);
        int count = (int) b.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
        return count;
    }

    public interface ScoreChangeListener {
        void scoreChange(ArrayMap<Integer, EvaluateItemSubmissions> evaluateMap);
    }
}
