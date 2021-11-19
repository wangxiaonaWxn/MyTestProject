package com.mega.carmaintenance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.carmaintenance.R;
import com.mega.carmaintenance.adapter.DateListAdapter;
import com.mega.carmaintenance.adapter.PositionListAdapter;
import com.mega.carmaintenance.callback.DateItemClickCallback;
import com.mega.carmaintenance.callback.PositionItemCallback;
import com.mega.carmaintenance.model.DateModel;
import com.mega.carmaintenance.model.ShopListDateModel;
import com.mega.carmaintenance.network.BookShowRequest;
import com.mega.carmaintenance.network.ServiceType;
import com.mega.carmaintenance.utils.DateUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookingFragment extends Fragment implements View.OnClickListener, PositionItemCallback,
        DateItemClickCallback {
    private static final String TAG = BookingFragment.class.getSimpleName();
    private View mRootView;
    private RecyclerView mDateListView;
    private RecyclerView mPositionsListView;
    private TextView mBookingButton;
    private TextView mCurrentPosition;
    private List<DateModel> mDateList = new ArrayList<>();
    //this list should order by distance from current location
    //and the distance should be computed by the lan and lon from current location
    private List<ShopListDateModel> mPositionList = new ArrayList<>();
    private BookShowRequest mBookRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.booking_fragment, null);
        mDateListView = mRootView.findViewById(R.id.date_list);
        mPositionsListView = mRootView.findViewById(R.id.positions_list);
        mCurrentPosition = mRootView.findViewById(R.id.current_position);
        mBookingButton = mRootView.findViewById(R.id.booking_button);
        mBookingButton.setOnClickListener(this);
        mBookRequest = new BookShowRequest();
        initData();
        LinearLayoutManager dateListManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mDateListView.setAdapter(new DateListAdapter(R.layout.booking_fragment_date_list_item,
                getActivity(), mDateList, this));
        mDateListView.setLayoutManager(dateListManager);

        LinearLayoutManager positionManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        PositionListAdapter positionAdapter = new PositionListAdapter(
                R.layout.booking_fragment_position_list_item, getActivity(), mPositionList);
        positionAdapter.setContext(getContext());
        positionAdapter.setCallback(this);
        mPositionsListView.setAdapter(positionAdapter);
        mPositionsListView.setLayoutManager(positionManager);
        mCurrentPosition.setText("北京市 顺北大街与来广营东路交汇处东北角");
        return mRootView;
    }

    private void initData() {
        for (int i = 0; i < 7; i++) {
            ShopListDateModel positionModel = new ShopListDateModel();
            positionModel.positionName = "address";
            positionModel.positionAddress = "jdejje . kdkoe";
            positionModel.shopId = String.valueOf(i);
            mPositionList.add(positionModel);
        }
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        mDateList.add(DateUtil.getDateModel(now, this.getResources()));
        for (int i = 1; i < 15; i++) {
            now.add(Calendar.DAY_OF_MONTH, 1);  //天数加1
            mDateList.add(DateUtil.getDateModel(now, this.getResources()));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.booking_button:
                // TODO: 21-11-18 send book request
                if (TextUtils.isEmpty(mBookRequest.type)) {
                    showToast("please select type", getContext());
                    return;
                }
                if (TextUtils.isEmpty(mBookRequest.date)) {
                    showToast("please select date", getContext());
                    return;
                }
                if (TextUtils.isEmpty(mBookRequest.timeBucket)) {
                    showToast("please select time", getContext());
                    return;
                }
                showToast("booking successfully", getContext());
                break;
            default:
                break;
        }
    }

    @Override
    public void onRepairClick() {
        mBookRequest.type = ServiceType.REPAIR.value;
    }

    @Override
    public void onMaintainClick() {
        mBookRequest.type = ServiceType.MAINTAIN.value;
    }

    @Override
    public void onTimeClick(String timeString) {
        Log.d(TAG, "selected time=" + timeString);
        mBookRequest.timeBucket = timeString;
    }

    @Override
    public void updateShopId(ShopListDateModel data) {
        mBookRequest.shopId = data.shopId;
        mBookRequest.province = data.province;
        mBookRequest.city = data.city;
        mBookRequest.type = null;
        mBookRequest.timeBucket = null;
        // TODO: 21-11-18 compute distance
    }

    public static void showToast(String text, Context context) {
        Toast toast = new Toast(context);
        TextView view = new TextView(context);
        view.setBackgroundResource(R.drawable.toast_background);
        view.setTextColor(context.getResources().getColor(R.color.color_app_name));
        view.setTextSize(28);
        view.setText(text);
        view.setWidth(480);
        view.setHeight(100);
        view.setGravity(Gravity.CENTER);
        toast.setGravity(Gravity.BOTTOM, 0, 40);
        toast.setView(view);
        toast.show();
    }

    @Override
    public void onDateClick(String date) {
       Log.d(TAG, "selected day is=" + date);
       mBookRequest.date = date;
    }
}
