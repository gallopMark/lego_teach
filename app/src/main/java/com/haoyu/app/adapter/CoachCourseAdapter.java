//package com.haoyu.app.adapter;
//
//import android.content.Context;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
//import com.haoyu.app.entity.CourseMobileEntity;
//import com.haoyu.app.entity.TimePeriod;
//import com.haoyu.app.imageloader.GlideImgManager;
//import com.haoyu.app.lego.teach.R;
//import com.haoyu.app.utils.PixelFormat;
//import com.haoyu.app.utils.ScreenUtils;
//import com.haoyu.app.utils.TimeUtil;
//
//import java.util.List;
//
///**
// * 创建日期：2017/12/15.
// * 描述:我参与辅导的课程  首页课程列表
// * 作者:xiaoma
// */
//
//public class CoachCourseAdapter extends BaseArrayRecyclerAdapter<CourseMobileEntity> {
//    private Context context;
//    private int imageWidth;
//    private int imageHeight;
//
//    public CoachCourseAdapter(Context context, List<CourseMobileEntity> mDatas) {
//        super(mDatas);
//        this.context = context;
//        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - PixelFormat.formatPxToDip(context, 20);
//        imageHeight = imageWidth / 3 * 2;
//    }
//
//    @Override
//    public void onBindHoder(RecyclerHolder holder, final CourseMobileEntity entity, int position) {
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                imageWidth, imageHeight);
//        ImageView course_img = holder.obtainView(R.id.course_img);
//        TextView course_title = holder.obtainView(R.id.course_title);
//        TextView course_code = holder.obtainView(R.id.course_code);
//        TextView course_period = holder.obtainView(R.id.course_period);
//        TextView course_state = holder.obtainView(R.id.course_state);
//        View line = holder.obtainView(R.id.line);
//        course_img.setLayoutParams(params);
//        GlideImgManager.loadImage(context, entity.getImage(), R.drawable.app_default, R.drawable.app_default, course_img);
//        course_title.setText(entity.getTitle());
//        String codeStr = "";
//        if (entity.getCode() != null && entity.getCode().trim().length() > 0) {
//            codeStr += entity.getCode();
//        }
//        if (entity.getTermNo() != null && entity.getCode().trim().length() > 0) {
//            codeStr += "/" + entity.getTermNo();
//        }
//        course_code.setText(codeStr);
//        TimePeriod timePeriod = entity.getmTimePeriod();
//        String text_period = "开课：";
//        if (timePeriod != null) {
//            text_period += TimeUtil.getSlashDate(entity.getmTimePeriod().getStartTime());
//            course_period.setText("开课：" + TimeUtil.getSlashDate(entity.getmTimePeriod().getStartTime()));
//        }
//        course_period.setText(text_period);
//        if (timePeriod != null && timePeriod.getMinutes() > 0) {
//            course_state.setText("进行中");
//        } else {
//            if (timePeriod != null && timePeriod.getState() != null) {
//                course_state.setText(timePeriod.getState());
//            } else {
//                course_state.setText("进行中");
//            }
//        }
//        if (position == getItemCount() - 1) {
//            line.setVisibility(View.GONE);
//        } else {
//            line.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    public int bindView(int viewtype) {
//        return R.layout.coachcourse_item;
//    }
//}
