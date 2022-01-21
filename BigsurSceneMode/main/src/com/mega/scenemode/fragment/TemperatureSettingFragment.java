package com.mega.scenemode.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mega.scenemode.R;

import mega.log.MLog;

public class TemperatureSettingFragment extends Fragment implements View.OnClickListener {
    private IFragmentPresenter mPresenter;
    private ImageView mLightSetting;

    @Override
    public @NonNull
    View onCreateView(@NonNull LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.temperature_setting_fragment, container, false);
        MLog.d("onCreateView");
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView(View view) {
        RelativeLayout back = view.findViewById(R.id.scene_setting_back);
        back.setOnClickListener(this);

        mLightSetting = view.findViewById(R.id.temperature_light);
        mLightSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        switch (id) {
            case R.id.scene_setting_back:
                mPresenter.backToActivity();
                break;

            case R.id.temperature_light:
                //开关双闪
                break;
            default:
                break;
        }
    }

    public void setPresenter(@NonNull IFragmentPresenter presenter) {
        mPresenter = presenter;
    }
}
