package com.mega.scenemode.manager;

import android.app.Service;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.mega.scenemode.Constants;
import com.mega.scenemode.R;
import com.mega.scenemode.SceneModeApplication;

import java.io.IOException;

import mega.car.config.ParamsCommon;
import mega.log.MLog;

import static mega.car.config.Cabin.ID_THERMOSTATIC_COCKPIT;
import static mega.car.config.Dms.ID_SMOKE_MODE_SWITCH;
import static mega.car.config.Infotainment.ID_CAMPING_MODE;

public class SceneModeManager {
    private static final String TAG = "CarPropManager";
    private static SceneModeManager sInstance;
    private OnModeChangedListener mModeListener;
    private MediaPlayer mMediaPlayer;

    public static @NonNull SceneModeManager getInstance() {
        if (sInstance == null) {
            synchronized (CarPropManager.class) {
                if (sInstance == null) {
                    sInstance = new SceneModeManager();
                }
            }
        }
        return sInstance;
    }

    public SceneModeManager() {
        MLog.d(TAG, "SceneModeManager()");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioAttributes(new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        mMediaPlayer.setOnPreparedListener(mediaPlayer -> mediaPlayer.start());
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> mediaPlayer.start());
    }

    //获取车型，判断要显示哪些模式
     public void getCarModel() {
     }

    //抽烟模式
    public void switchSmokeMode(boolean isOn) {
        CarPropManager.getInstance().publish(ID_SMOKE_MODE_SWITCH, isOn ? ParamsCommon.OnOff.ON
                : ParamsCommon.OnOff.OFF);
    }

    //露营模式 -- 电源 On，档位 P，电量 >25%
    //电量低于 20% 时弹窗提示， 发送屏幕点亮请求， --- 铃声？
    //弹窗关闭改为无请求，并停止铃声
    public void switchCampMode(boolean isOn) {
        //发送 -- ID_CAMPING_MODE
        CarPropManager.getInstance().publish(ID_CAMPING_MODE, isOn ? ParamsCommon.OnOff.ON
                : ParamsCommon.OnOff.OFF);
    }

    public void onCampModeChanged(boolean isOn) {
        mModeListener.onModeChanged(Constants.MenuType.CAMPING, isOn);
    }

    //冬季模式  -- 只有电源 On 判断
    public void openWinterMode() {
    }

    public void closeWinterMode() {
    }

    //恒温座舱 -- 正常模式或低功耗模式
    //电源电量 > 20 %, 电量低于 10 退出
    public void switchTemperatureMode(int mode) {
        CarPropManager.getInstance().publish(ID_THERMOSTATIC_COCKPIT, mode);
    }

    public void onTemperatureModeChanged(int status) {
        mModeListener.onTemperatureModeChanged(status);
    }

    /*  ------- ---------------  只有可用性判断  ---------------------- */
    //小憩模式
    public void openNapMode() {
    }

    public void closeNapMode() {
    }

    public void saveNapModeSeatPosition(boolean isNeedSave) {
    }

    /*  ------- ---------------  没有信号交互  ---------------------- */
    //隐私模式
    public void openDisturbMode() {
    }

    public void closeDisturbMode() {
    }

    //视听联动 -- 只需要提供入口
    public void openAudioAndVideoMode() {
    }

    public void closeAudioAndVideoMode() {
    }

    //灯光秀
    public void openLightMode() {
    }

    //车内关怀
    public void openCareVideoMode() {
    }

    // ---- 车身信号回调 ----
    public void onLowEnergy(int soc) {
        mModeListener.lowEnergy(soc);
    }

    public interface OnModeChangedListener {
        void onModeChanged(@NonNull Constants.MenuType type, boolean isOn);

        void onTemperatureModeChanged(int status);

        void lowEnergy(int soc);
    }

    public void setModeChangedListener(@NonNull OnModeChangedListener listener) {
        mModeListener = listener;
    }

    private AudioManager mAudioManager;
    private AudioFocusRequest mAudioFocusRequest;

    //铃声播放相关
    public void processAudioFocus(
            int type, @NonNull AudioManager.OnAudioFocusChangeListener listener) {
        mAudioManager = (AudioManager) SceneModeApplication.getInstance()
                .getSystemService(Service.AUDIO_SERVICE);
        AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(type)
                        .build();

        mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(listener)
                .setWillPauseWhenDucked(false)
                .setAudioAttributes(audioAttributes)
                .build();

        mAudioManager.requestAudioFocus(mAudioFocusRequest);
    }

    public void abandonAudioFocus() {
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
    }

    public void playCampBell() {
        processAudioFocus(AudioManager.STREAM_MUSIC, new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    case AudioManager.AUDIOFOCUS_GAIN:
                        MLog.d("AudioManager gain");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        MLog.d("AudioManager loss");

                        break;
                    default:
                        MLog.e("unhandled audio focus type: " + focusChange);
                        break;
                }
            }
        });

        AssetFileDescriptor file = SceneModeApplication.getInstance()
                .getResources().openRawResourceFd(R.raw.bird);
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

    public void stopCampBell() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        abandonAudioFocus();
    }
}
