package com.mega.scenemode.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mega.scenemode.PreferenceUtil;
import com.mega.scenemode.R;
import com.mega.scenemode.view.CircleSeekBar;
import com.mega.scenemode.view.ConfirmDialog;

import mega.log.MLog;

import static com.mega.scenemode.Constants.KEY_NAP_AUDIO;
import static com.mega.scenemode.Constants.KEY_NAP_TOTAL_TIME;
import static com.mega.scenemode.Constants.NAP_AUDIO_FIRST;
import static com.mega.scenemode.Constants.NAP_AUDIO_SECOND;
import static com.mega.scenemode.Constants.NAP_AUDIO_THIRD;
import static com.mega.scenemode.Constants.NAP_SETTING_DEFAULT_TIME;
import static com.mega.scenemode.Constants.NAP_SETTING_TOTAL_TIME;

public class NapSettingFragment extends Fragment implements View.OnClickListener,
        CircleSeekBar.OnSeekBarChangeListener, ConfirmDialog.OnClickListener {
    private CircleSeekBar mRoundSeekBar;
    private RelativeLayout mNapAudio1;
    private RelativeLayout mNapAudio2;
    private RelativeLayout mNapAudio3;
    private TextView mProgressTime;

    private IFragmentPresenter mPresenter;
    private NapConfirmDialog mConfirmDialog;
    private Handler mHandler;
    private int mCountDownTime;

    @Override
    public @NonNull
    View onCreateView(@NonNull LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nap_setting_fragment, container, false);
        MLog.d("onCreateView");
        initView(view);
        mHandler = new Handler();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initSettings();
    }

    private void initView(View view) {
        mRoundSeekBar = view.findViewById(R.id.seek_bar_circular);
        mRoundSeekBar.setMaxProcess(NAP_SETTING_TOTAL_TIME);
        mProgressTime = view.findViewById(R.id.progress_time);
        RelativeLayout back = view.findViewById(R.id.scene_setting_back);
        back.setOnClickListener(this);
        Button startButton = view.findViewById(R.id.nap_start_button);
        startButton.setOnClickListener(this);

        mNapAudio1 = view.findViewById(R.id.nap_audio_1);
        mNapAudio2 = view.findViewById(R.id.nap_audio_2);
        mNapAudio3 = view.findViewById(R.id.nap_audio_3);
        mNapAudio1.setOnClickListener(this);
        mNapAudio2.setOnClickListener(this);
        mNapAudio3.setOnClickListener(this);

        mRoundSeekBar.setOnSeekBarChangeListener(this);
    }

    private void initSettings() {
        mCountDownTime = 5;
        int totalTime = (int) PreferenceUtil.get(KEY_NAP_TOTAL_TIME, NAP_SETTING_DEFAULT_TIME);
        int audio = (int) PreferenceUtil.get(KEY_NAP_AUDIO, NAP_AUDIO_FIRST);
        selectAudio(audio);

        mRoundSeekBar.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获取width和height
                //注意取消监听，因为该方法会调用多次！
                mRoundSeekBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mRoundSeekBar.setCurProcess(totalTime);
                mProgressTime.setText(String.valueOf(totalTime));
            }
        });
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        switch (id) {
            case R.id.scene_setting_back:
                mPresenter.backToActivity();
                break;

            case R.id.nap_audio_1:
                selectAudio(NAP_AUDIO_FIRST);
                break;
            case R.id.nap_audio_2:
                selectAudio(NAP_AUDIO_SECOND);
                break;
            case R.id.nap_audio_3:
                selectAudio(NAP_AUDIO_THIRD);
                break;

            case R.id.nap_start_button:
                showNapCountDownDialog();
                break;
            default:
                break;
        }
    }

    private void selectAudio(int audio) {
        mNapAudio1.setSelected(audio == NAP_AUDIO_FIRST);
        mNapAudio2.setSelected(audio == NAP_AUDIO_SECOND);
        mNapAudio3.setSelected(audio == NAP_AUDIO_THIRD);
        PreferenceUtil.put(KEY_NAP_AUDIO, audio);
    }

    private void showNapCountDownDialog() {
        mConfirmDialog = new NapConfirmDialog();
        mConfirmDialog.setHint(
                String.format(getString(R.string.nap_count_down_tip), mCountDownTime--));
        mConfirmDialog.setPositiveVisible(View.GONE);
        mConfirmDialog.setOnClickListener(this);
        mConfirmDialog.show(getChildFragmentManager(), "nap_confirm");

        mHandler.postDelayed(mCountDownRunnable, 1000);
    }

    public void setPresenter(@NonNull IFragmentPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onChanged(@NonNull CircleSeekBar seekbar, int curValue) {
        int total = curValue * NAP_SETTING_TOTAL_TIME / (mRoundSeekBar.getMaxProcess());
        mProgressTime.setText(String.valueOf(total));
        PreferenceUtil.put(KEY_NAP_TOTAL_TIME, total);
    }

    @Override
    public void onPositiveClick() {
    }

    @Override
    public void onNegativeClick() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private final Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            mConfirmDialog.setHint(
                    String.format(getString(R.string.nap_count_down_tip), mCountDownTime--));
            if (mCountDownTime == -1) {
                mConfirmDialog.dismiss();
                //开启小憩模式
                mPresenter.startNapMode();
            } else {
                mHandler.postDelayed(mCountDownRunnable, 1000);
            }
        }
    };
}
