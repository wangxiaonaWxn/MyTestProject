package com.mega.carmaintenance.adapter;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.carmaintenance.R;
import com.mega.carmaintenance.callback.PositionItemCallback;
import com.mega.carmaintenance.model.BookTimeModel;
import com.mega.carmaintenance.model.ShopListDateModel;

import java.util.List;

public class PositionListAdapter extends CommonAdapter<ShopListDateModel> {
    private PositionItemCallback mCallback;
    private Context mContext;
    private View mLastSelectedTimeString;
    private View mLastSelectedButton;
    private String mLastSelectedShopId;

    public void setCallback(PositionItemCallback callback) {
        mCallback = callback;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public PositionListAdapter(int layoutId, Context context, List<ShopListDateModel> data) {
        super(layoutId, context, data);
    }

    @Override
    void bingData(ShopListDateModel data, final CommonViewHolder holder) {
        holder.setText(R.id.position_name, data.positionName);
        holder.setText(R.id.position_address, data.positionAddress);
        final View repair = holder.getViewById(R.id.repair);
        final View maintain = holder.getViewById(R.id.maintainance);
        repair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSameShopId(data);
                if (mCallback != null) {
                    mCallback.onRepairClick();
                }
                repair.setSelected(true);
                resetLastSelectedButton(repair);
            }
        });

        holder.getViewById(R.id.maintainance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSameShopId(data);
                if (mCallback != null) {
                    mCallback.onMaintainClick();
                }
                maintain.setSelected(true);
                resetLastSelectedButton(maintain);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext,
                LinearLayoutManager.HORIZONTAL, false);
        ((RecyclerView) holder.getViewById(R.id.booking_time_list))
                .setAdapter(new CommonAdapter(R.layout.booking_fragment_time_list_item, mContext,
                        data.timeList) {
                    @Override
                    void bingData(final Object data1, CommonViewHolder holder) {
                        holder.setText(R.id.time_list_item, ((BookTimeModel) data1).showTimeString);
                        final View item = holder.getViewById(R.id.time_list_item);
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isSameShopId(data);
                                if (mCallback != null) {
                                    mCallback.onTimeClick(((BookTimeModel) data1).timeString);
                                }
                                if (mLastSelectedTimeString != null) {
                                    mLastSelectedTimeString.setSelected(false);
                                }
                                item.setSelected(true);
                                mLastSelectedTimeString = item;
                            }
                        });
                    }
                });
        ((RecyclerView) holder.getViewById(R.id.booking_time_list)).setLayoutManager(layoutManager);
    }

    private void resetLastSelectedButton(View view) {
        if (mLastSelectedButton != null) {
            mLastSelectedButton.setSelected(false);
        }
        mLastSelectedButton = view;
    }

    public boolean isSameShopId(ShopListDateModel data) {
        if (data.shopId == mLastSelectedShopId) {
            return true;
        }
        if (mLastSelectedButton != null) {
            mLastSelectedButton.setSelected(false);
        }
        if (mLastSelectedTimeString != null) {
            mLastSelectedTimeString.setSelected(false);
        }
        mLastSelectedShopId = data.shopId;
        if (mCallback != null) {
            mCallback.updateShopId(data);
        }
        return false;
    }
}
