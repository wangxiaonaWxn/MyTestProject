package com.mega.scenemode.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mega.scenemode.R;

import mega.log.MLog;

/*
 * 低功耗模式，且电源 OFF 时显示
 * 电量低于 10 时，显示 low energy 画面
 */
public class TemperatureOpenFragment extends Fragment {
    @Override
    public @NonNull
    View onCreateView(@NonNull LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.temperature_open_fragment, container, false);
        MLog.d("onCreateView");
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView(View view) {
    }
}
