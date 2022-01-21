package com.mega.scenemode.manager;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mega.scenemode.BabyCareActivity;
import com.mega.scenemode.R;

import mega.log.MLog;

public class FloatingCameraService extends Service {
    private static final String TAG = FloatingCameraService.class.getSimpleName();
    public static boolean sIsStarted = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mDisplayView;
    private CameraOpenManager mCameraManager;
    private TextureView mPreviewView;
    private TextView mCameraError;
    private static final int PIP_WINDOW_X = 0;
    private static final int PIP_WINDOW_Y = 1080;
    private static final int PIP_WINDOW_WIDTH = 426;
    private static final int PIP_WINDOW_HEIGHT = 240;

    @Override
    public void onCreate() {
        super.onCreate();
        sIsStarted = true;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = PIP_WINDOW_WIDTH;
        mLayoutParams.height = PIP_WINDOW_HEIGHT;
        mLayoutParams.x = PIP_WINDOW_X;
        mLayoutParams.y = PIP_WINDOW_Y;
    }

    private void showCameraErrorView() {
        if (mCameraError.getVisibility() == View.GONE) {
            mCameraError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MLog.d(TAG, "onDestroy");
        mCameraManager.stopPreview();
        mCameraManager.closeCamera();
        mWindowManager.removeView(mDisplayView);
        sIsStarted = false;
    }

    @Nullable
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        MLog.d(TAG, "onStartCommand");
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            mDisplayView = layoutInflater.inflate(R.layout.baby_care_pip_window, null);
            mDisplayView.setOnTouchListener(new FloatingOnTouchListener());
            mCameraError = mDisplayView.findViewById(R.id.camera_error);
            mLayoutParams.windowAnimations = R.style.FloatWindowTheme;
            mWindowManager.addView(mDisplayView, mLayoutParams);
            mPreviewView = mDisplayView.findViewById(R.id.camera_view);
            mCameraManager = new CameraOpenManager(this, mPreviewView, () -> showCameraErrorView());
            mCameraManager.openCamera(PIP_WINDOW_WIDTH, PIP_WINDOW_HEIGHT);
            ImageView close = mDisplayView.findViewById(R.id.close_window);
            ImageView pip = mDisplayView.findViewById(R.id.pip_button);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     stopSelf();
                }
            });

            pip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopSelf();
                    Intent intent = new Intent(FloatingCameraService.this, BabyCareActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int mPositionX;
        private int mPositionY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPositionX = (int) event.getRawX();
                    mPositionY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - mPositionX;
                    int movedY = nowY - mPositionY;
                    mPositionX = nowX;
                    mPositionY = nowY;
                    mLayoutParams.x = mLayoutParams.x + movedX;
                    mLayoutParams.y = mLayoutParams.y + movedY;
                    mWindowManager.updateViewLayout(view, mLayoutParams);
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
