package com.haoyu.app.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.CourseChildSectionEntity;
import com.haoyu.app.entity.CourseSectionEntity;
import com.haoyu.app.entity.MultiItemEntity;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.PixelFormat;

import java.util.List;

/**
 * 创建日期：2017/9/12 on 16:36
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class CourseSectionAdapter extends BaseArrayRecyclerAdapter<MultiItemEntity> {

    private Context context;
    private OnSectionClickListener onSectionClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public CourseSectionAdapter(Context context, List<MultiItemEntity> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getItemType();
    }

    @Override
    public int bindView(int viewtype) {
        if (viewtype == 0)
            return R.layout.course_section_item;
        else
            return R.layout.course_section_child_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, MultiItemEntity item, final int position) {
        int viewType = holder.getItemViewType();
        if (viewType == 0) {
            final CourseSectionEntity sectionEntity = (CourseSectionEntity) item;
            final TextView tv_title = holder.obtainView(R.id.section_title);
            setSectionText(sectionEntity.getTitle(), tv_title);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onItemLongClickListener != null)
                        onItemLongClickListener.onItemLongClick(tv_title, sectionEntity.getTitle());
                    return false;
                }
            });
        } else {
            final CourseChildSectionEntity childEntity = (CourseChildSectionEntity) item;
            final TextView tv_title = holder.obtainView(R.id.tv_selection_title);
            ImageView ic_selection_state = holder.obtainView(R.id.ic_selection_state);
            ic_selection_state.setVisibility(View.GONE);
            setSpannedText(childEntity.getTitle(), tv_title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onSectionClickListener != null)
                        onSectionClickListener.onSectionSelected(childEntity);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onItemLongClickListener != null)
                        onItemLongClickListener.onItemLongClick(tv_title, childEntity.getTitle());
                    return false;
                }
            });
        }
    }

    private void setSectionText(String title, TextView tv) {
        int paddingLeft = tv.getPaddingLeft();
        int paddingRight = tv.getPaddingRight();
        int paddingTop = PixelFormat.formatDipToPx(context, 8);
        int paddingBottom = paddingTop;
        if (title == null || title.trim().length() == 0) {
            tv.setText("无标题");
        } else {
            Spanned spanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spanned = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY);
            else
                spanned = Html.fromHtml(title);
            SpannableStringBuilder ss = new SpannableStringBuilder(spanned);
            if (title.contains("<sup>")) {
                ss.setSpan(new SuperscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                paddingTop = paddingBottom = PixelFormat.formatDipToPx(context, 2);
                tv.setText(null);
                tv.append(ss);
            } else if (title.contains("<sub>")) {
                ss.setSpan(new SubscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                paddingTop = paddingBottom = PixelFormat.formatDipToPx(context, 2);
                tv.setText(null);
                tv.append(ss);
            } else {
                tv.setText(spanned);
            }
        }
        tv.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    private void setSpannedText(String title, TextView tv) {
        int paddingLeft = tv.getPaddingLeft();
        int paddingRight = tv.getPaddingRight();
        int paddingTop = PixelFormat.formatDipToPx(context, 14);
        int paddingBottom = paddingTop;
        if (title == null || title.trim().length() == 0) {
            tv.setText("无标题");
        } else {
            Spanned spanned;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spanned = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY);
            else
                spanned = Html.fromHtml(title);
            SpannableStringBuilder ss = new SpannableStringBuilder(spanned);
            if (title.contains("<sup>")) {
                ss.setSpan(new SuperscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                paddingTop = paddingBottom = PixelFormat.formatDipToPx(context, 8);
                tv.setText(null);
                tv.append(ss);
            } else if (title.contains("<sub>")) {
                ss.setSpan(new SubscriptSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                paddingTop = paddingBottom = PixelFormat.formatDipToPx(context, 8);
                tv.setText(null);
                tv.append(ss);
            } else {
                tv.setText(spanned);
            }
        }
        tv.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    public interface OnSectionClickListener {
        void onSectionSelected(CourseChildSectionEntity entity);
    }

    public void setOnSectionClickListener(OnSectionClickListener onSectionClickListener) {
        this.onSectionClickListener = onSectionClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(TextView tv, CharSequence charSequence);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
}
