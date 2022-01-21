package com.mega.scenemode.menu;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class CommonAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {
    private Context mContext;
    private List<T> mDataList;
    private int mLayoutId;

    public CommonAdapter(int layoutId, Context context, List<T> data) {
        mContext = context;
        mLayoutId = layoutId;
        mDataList = data;
    }

    abstract void bingData(T data, CommonViewHolder holder, int position);

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CommonViewHolder.getHolder(mContext, mLayoutId, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        bingData(mDataList.get(position), holder, position);
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
}
