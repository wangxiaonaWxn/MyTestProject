package com.mega.scenemode.menu;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.scenemode.R;
import com.mega.scenemode.view.MatrixTranslateLayout;
import java.util.List;

public class MenuAdapter extends CommonAdapter<MenuItem> {
    private RecyclerView mRecyclerView;
    private MenuItemClickListener mClickListener;

    public MenuAdapter(int layoutId, Context context, List<MenuItem> data, RecyclerView view) {
        super(layoutId, context, data);
        mRecyclerView = view;
    }

    public void setMenuItemClickListener(MenuItemClickListener listener) {
        mClickListener = listener;
    }

    @Override
    void bingData(MenuItem data, CommonViewHolder holder, int position) {
        ((MatrixTranslateLayout) holder.itemView).setParentHeight(mRecyclerView.getHeight());
        holder.setImage(R.id.image, data.srcId);
        holder.itemView.setTag(data.type);
        final int fp = position;
        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onMenuItemClick(holder.itemView);
            }
        });
    }
}
