package com.mega.scenemode.menu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LooperLayoutManagerVertical extends LinearLayoutManager {
    private static final String TAG = LooperLayoutManagerVertical.class.getSimpleName();
    private boolean mLooperEnable = true;

    public LooperLayoutManagerVertical(Context context) {
        super(context);
    }

    public LooperLayoutManagerVertical(Context context, int orientation, boolean revers) {
        super(context, orientation, revers);
    }

    public void setLooperEnable(boolean looperEnable) {
        this.mLooperEnable = looperEnable;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() <= 0) {
            return;
        }
        //preLayout主要支持动画，直接跳过
        if (state.isPreLayout()) {
            return;
        }
        //将视图分离放入scrap缓存中，以准备重新对view进行排版
        detachAndScrapAttachedViews(recycler);

        int actualHeight = 0;
        for (int i = 0; i < getItemCount(); i++) {
            //初始化，将在屏幕内的view填充
            View itemView = recycler.getViewForPosition(i);
            addView(itemView);
            //测量itemView的宽高
            measureChildWithMargins(itemView, 0, 0);
            int width = getDecoratedMeasuredWidth(itemView);
            int height = getDecoratedMeasuredHeight(itemView);
            //根据itemView的宽高进行布局
            layoutDecorated(itemView, 0, actualHeight, width, actualHeight + height);
            actualHeight = actualHeight + height;
            //如果当前布局过的itemView的高度总和大于RecyclerView的高，则不再进行布局
            if (actualHeight > getHeight()) {
                break;
            }
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        int travl = fill(dy, recycler, state);
        if (travl == 0) {
            return 0;
        }
        offsetChildrenVertical(travl * -1);
        //回收已经离开界面的
        recyclerHideView(dy, recycler, state);
        return travl;
    }

    /**
     * 滑动的时候，填充
     */
    private int fill(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dy > 0) {
            //向上滚动
            View lastView = getChildAt(getChildCount() - 1);
            if (lastView == null) {
                return 0;
            }
            int lastPos = getPosition(lastView);
            //可见的最后一个itemView完全滑进来了，需要补充新的
            if (lastView.getBottom() < getHeight()) {
                View scrap = null;
                //判断可见的最后一个itemView的索引，
                // 如果是最后一个，则将下一个itemView设置为第一个，否则设置为当前索引的下一个
                if (lastPos == getItemCount() - 1) {
                    if (mLooperEnable) {
                        scrap = recycler.getViewForPosition(0);
                    } else {
                        dy = 0;
                    }
                } else {
                    scrap = recycler.getViewForPosition(lastPos + 1);
                }
                if (scrap == null) {
                    return dy;
                }
                //将新的itemView add进来并对其测量和布局
                addView(scrap);
                measureChildWithMargins(scrap, 0, 0);
                int width = getDecoratedMeasuredWidth(scrap);
                int height = getDecoratedMeasuredHeight(scrap);
                layoutDecorated(scrap, 0, lastView.getBottom(),
                         width, lastView.getBottom() + height);
                return dy;
            }
        } else {
            //向下滚动
            View firstView = getChildAt(0);
            if (firstView == null) {
                return 0;
            }
            int firstPos = getPosition(firstView);

            if (firstView.getTop() >= 0) {
                View scrap = null;
                if (firstPos == 0) {
                    if (mLooperEnable) {
                        scrap = recycler.getViewForPosition(getItemCount() - 1);
                    } else {
                        dy = 0;
                    }
                } else {
                    scrap = recycler.getViewForPosition(firstPos - 1);
                }
                if (scrap == null) {
                    return 0;
                }
                addView(scrap, 0);
                measureChildWithMargins(scrap, 0, 0);
                int width = getDecoratedMeasuredWidth(scrap);
                int height = getDecoratedMeasuredHeight(scrap);
                layoutDecorated(scrap, 0, firstView.getTop() - height,
                        width, firstView.getTop());
            }
        }
        return dy;
    }

    /**
     * 回收界面不可见的view
     */
    private void recyclerHideView(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            if (dy > 0) {
                //向上滚动，移除一个顶部不在内容里的view
                if (view.getBottom() < 0) {
                    removeAndRecycleView(view, recycler);
//                    MLog.d(TAG, "循环: 移除顶部 一个view  childCount=" + getChildCount());
                }
            } else {
                //向下滚动，移除一个底部不在内容里的view
                if (view.getTop() > getHeight()) {
                    removeAndRecycleView(view, recycler);
//                    MLog.d(TAG, "循环: 移除底部一个view  childCount=" + getChildCount());
                }
            }
        }
    }
}
