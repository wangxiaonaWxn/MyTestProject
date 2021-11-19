package com.mega.carmaintenance.adapter;

import android.content.Context;

import com.mega.carmaintenance.R;
import com.mega.carmaintenance.model.RecordsModel;
import com.mega.carmaintenance.network.ServiceType;

import java.util.List;

public class RecordsAdapter extends CommonAdapter<RecordsModel> {
    public RecordsAdapter(int layoutId, Context context, List<RecordsModel> data) {
        super(layoutId, context, data);
    }

    @Override
    void bingData(RecordsModel data, CommonViewHolder holder) {
        holder.setText(R.id.store_name, data.storeName);
        holder.setText(R.id.store_address, data.storeAddress);
        holder.setText(R.id.booked_time, data.recordTime);
        holder.setText(R.id.record_type,  data.recordTypeName);
        if (data.recordType.equals(ServiceType.MAINTAIN.value)) {
            holder.getViewById(R.id.record_type).setBackgroundResource(R.drawable.record_matain_bg);
        } else {
            holder.getViewById(R.id.record_type).setBackgroundResource(R.drawable.record_repair_bg);
        }
    }
}
