package com.mega.scenemode.fragment;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.mega.scenemode.Constants;
import com.mega.scenemode.PreferenceUtil;
import com.mega.scenemode.R;
import com.mega.scenemode.manager.SceneModeManager;
import com.mega.scenemode.view.CircleSeekBar;
import com.mega.scenemode.view.ViewPagerAdapter;

import java.io.IOException;
import java.util.ArrayList;

import mega.log.MLog;

import static com.mega.scenemode.Constants.NAP_AUDIO_FIRST;
import static com.mega.scenemode.Constants.NAP_AUDIO_SECOND;
import static com.mega.scenemode.Constants.NAP_AUDIO_THIRD;
import static com.mega.scenemode.Constants.NAP_SETTING_DEFAULT_TIME;
import static com.mega.scenemode.Constants.NAP_SETTING_TOTAL_TIME;

public class NapFragment extends Fragment implements View.OnClickListener,
        ViewPager.OnPageChangeListener, CircleSeekBar.OnSeekBarChangeListener {
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ArrayList<Integer> mImageList;
    private CircleSeekBar mNapSeekBar;
    private TextView mTimeText;
    private Button mBack;
    private ImageView mEdit;

    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private IFragmentPresenter mPresenter;

    private int mDelaySeconds;
    private int mTotalTime;
    private boolean mIsEditMode;

    @Override
    public @NonNull
    View onCreateView(@NonNull LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nap_fragment, container, false);
        MLog.d("onCreateView");
        initView(view);
        return view;
    }

    private void initView(View view) {
        mIsEditMode = false;
        mViewPager = view.findViewById(R.id.view_pager);
        setViewPager();
        mTimeText = view.findViewById(R.id.nap_progress_text);

        mTotalTime = (int) PreferenceUtil
                .get(Constants.KEY_NAP_TOTAL_TIME, NAP_SETTING_DEFAULT_TIME);
        mNapSeekBar = view.findViewById(R.id.seek_bar_circular);

        mDelaySeconds = mTotalTime * 60;
        mNapSeekBar.setMaxProcess(mTotalTime);
        mNapSeekBar.setOnSeekBarChangeListener(this);
        mNapSeekBar.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //获取width和height
                        //注意取消监听，因为该方法会调用多次！
                        mNapSeekBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mNapSeekBar.setClickable(false);
                        mNapSeekBar.setCurProcess(mTotalTime);
                    }
                });

        mBack = view.findViewById(R.id.mode_close);
        mBack.setOnClickListener(this);
        mEdit = view.findViewById(R.id.nap_edit);
        mEdit.setOnClickListener(this);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioAttributes(new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        mMediaPlayer.setOnPreparedListener(mediaPlayer -> mediaPlayer.start());
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> mediaPlayer.start());

        mHandler = new Handler();
        mHandler.post(() -> refreshCountdown());
    }

    @Override
    public void onResume() {
        super.onResume();
        initSelectViewPager();
    }

    private void initSelectViewPager() {
        SceneModeManager.getInstance()
                .processAudioFocus(AudioManager.STREAM_MUSIC, mAudioFocusChangeListener);
        int media = (int) PreferenceUtil.get(Constants.KEY_NAP_AUDIO, NAP_AUDIO_FIRST);
        mViewPager.setCurrentItem(media);
        AssetFileDescriptor file;
        switch (media) {
            case NAP_AUDIO_FIRST:
                file = getResources().openRawResourceFd(R.raw.bird);
                break;

            case NAP_AUDIO_SECOND:
                file = getResources().openRawResourceFd(R.raw.spring_rain);
                break;

            case NAP_AUDIO_THIRD:
                file = getResources().openRawResourceFd(R.raw.thunder);
                break;

            default:
                file = getResources().openRawResourceFd(R.raw.bird);
                break;
        }

        try {
            if (mMediaPlayer == null) {
                return;
            }

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }

            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(file.getFileDescriptor(),
                    file.getStartOffset(), file.getLength());
            file.close();
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        SceneModeManager.getInstance().abandonAudioFocus();
    }

    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        switch (id) {
            case R.id.mode_close:
                if (!mIsEditMode) {
                    mHandler.removeCallbacksAndMessages(null);
                    mPresenter.backToActivity();
                } else {
                    mIsEditMode = false;
                    mEdit.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.VISIBLE);
                    mBack.setText(R.string.mode_close);
                    mNapSeekBar.setClickable(false);
                    mDelaySeconds = mNapSeekBar.getCurProcess() * 60;
                    mNapSeekBar.setMaxProcess(mNapSeekBar.getCurProcess());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshCountdown();
                        }
                    });
                }
                break;

            case R.id.nap_edit:
                mIsEditMode = true;
                mEdit.setVisibility(View.GONE);
                mViewPager.setVisibility(View.INVISIBLE);
                mBack.setText(R.string.save);
                mHandler.removeCallbacksAndMessages(null);
                mNapSeekBar.setMaxProcess(NAP_SETTING_TOTAL_TIME);
                mDelaySeconds = 45 * 60;
                mNapSeekBar.setCurProcess(45);
                mNapSeekBar.setClickable(true);
                break;
            default:
                break;
        }
    }

    private void setViewPager() {
        mImageList = new ArrayList<>();

        mImageList.add(R.mipmap.bird);
        mImageList.add(R.mipmap.rain);
        mImageList.add(R.mipmap.thunder);

        mViewPagerAdapter = new ViewPagerAdapter(getContext(), mImageList);
        mViewPager.setAdapter(mViewPagerAdapter);
    }

    public void setPresenter(@NonNull IFragmentPresenter presenter) {
        mPresenter = presenter;
    }

    private void refreshCountdown() {
        mNapSeekBar.setCurProcess(mDelaySeconds / 60);
        if (mDelaySeconds != 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshCountdown();
                }
            }, 1000);
        }
        String time = getTimeString(mDelaySeconds);
        mTimeText.setText(String.format(getString(R.string.nap_progress_time), time));
        mDelaySeconds--;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        PreferenceUtil.put(Constants.KEY_NAP_AUDIO, position);
        initSelectViewPager();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onChanged(@NonNull CircleSeekBar seekbar, int curValue) {
        String time = getTimeString(curValue * 60);
        mTimeText.setText(String.format(getString(R.string.nap_progress_time), time));
    }

    private String getTimeString(int curValue) {
        String time;
        if (curValue % 60 > 9 && curValue >= 600) {
            time = String.format("%d : %d ", curValue / 60, curValue % 60);
        } else if (curValue >= 600) {
            time = String.format("%d : 0%d ", curValue / 60, curValue % 60);
        } else if (mDelaySeconds % 60 > 9) {
            time = String.format("0%d : %d ", curValue / 60, curValue % 60);
        } else {
            time = String.format("0%d : 0%d ", curValue / 60, curValue % 60);
        }
        return time;
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
            case AudioManager.AUDIOFOCUS_GAIN:
                MLog.d("AudioManager gain");
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                MLog.d("AudioManager loss");
                mPresenter.backToActivity();
                break;
            default:
                MLog.e("unhandled audio focus type: " + focusChange);
                break;
        }
    };
}
