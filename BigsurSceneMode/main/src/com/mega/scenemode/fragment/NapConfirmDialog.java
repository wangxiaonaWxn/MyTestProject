package com.mega.scenemode.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mega.scenemode.Constants;
import com.mega.scenemode.PreferenceUtil;
import com.mega.scenemode.R;
import com.mega.scenemode.view.ConfirmDialog;

import mega.log.MLog;

import static com.mega.scenemode.Constants.NAP_SETTING_DEFAULT_TIME;
import static com.mega.scenemode.Constants.NAP_SETTING_TOTAL_TIME;

public class NapConfirmDialog extends ConfirmDialog {
    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
          @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MLog.d("onCreateView()");
        View view = inflater.inflate(R.layout.nap_confirm_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
        }
        initView(view);
        initListener();
        return view;
    }

    public void initView(@NonNull View view) {
        super.initView(view);
        TextView subTip = view.findViewById(R.id.dialog_message_sub);
        int time = (int) PreferenceUtil.get(Constants.KEY_NAP_TOTAL_TIME, NAP_SETTING_DEFAULT_TIME);
        subTip.setText(String.format(getString(R.string.nap_count_down_total), time));
    }
}