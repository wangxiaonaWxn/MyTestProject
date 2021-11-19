package com.mega.carmaintenance.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.carmaintenance.R;
import com.mega.carmaintenance.adapter.RecordsAdapter;
import com.mega.carmaintenance.model.RecordsModel;
import java.util.ArrayList;
import java.util.List;

public class RecordsFragment extends Fragment {
    private List<RecordsModel> mRecordDataList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.records_fragment, null);
        for (int i = 0; i < 10; i++) {
            RecordsModel model = new RecordsModel();
            model.storeName = "北京望京4S店";
            model.storeAddress = "10.6公里·北京市朝阳区望京新兴产业园利泽东园306号";
            model.recordTime = "2021年10月28日  周六  13:00-14:00";
            model.recordType = i % 2 == 0 ? "1" : "2";
            model.recordTypeName = i % 2 == 0 ? "维修" : "保养";
            mRecordDataList.add(model);
        }
        RecyclerView recordView = view.findViewById(R.id.records_list);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recordView.setLayoutManager(manager);
        RecordsAdapter adapter = new RecordsAdapter(R.layout.booking_records_list_item,
                getContext(), mRecordDataList);
        recordView.setAdapter(adapter);
        return view;
    }
}
