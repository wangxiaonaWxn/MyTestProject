package com.mega.scenemode;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mega.scenemode.manager.CameraOpenManager;
import com.mega.scenemode.manager.FloatingCameraService;

import mega.log.MLog;

public class BabyCareActivity extends AppCompatActivity implements View.OnClickListener,
        TextureView.SurfaceTextureListener {
    private static final String TAG = BabyCareActivity.class.getSimpleName();
    private TextureView mCameraPreview;
    private TextView mCameraError;
    private TextView mTitle;
    private ImageView mPipButton;
    private CameraOpenManager mManager;
    private static final int CAMERA_FACING = 0;

    private void showCameraErrorView() {
        if (mCameraError.getVisibility() == View.GONE) {
            mCameraError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.baby_care_activity);
        if (FloatingCameraService.sIsStarted) {
            stopService(new Intent(getApplicationContext(), FloatingCameraService.class));
            FloatingCameraService.sIsStarted = false;
        }
        mCameraError = findViewById(R.id.camera_error);
        mCameraPreview = findViewById(R.id.camera_view);
        mPipButton = findViewById(R.id.pip_button);
        mTitle = findViewById(R.id.title);
        mTitle.setOnClickListener(this);
        mPipButton.setOnClickListener(this);
        mManager = new CameraOpenManager(this, mCameraPreview, () -> {
            showCameraErrorView();
        });
        if (!isCameraAvailable()) {
            showCameraErrorView();
            return;
        }
        mCameraPreview.setSurfaceTextureListener(this);
    }

    @Override
    public void onClick(@Nullable View v) {
        switch (v.getId()) {
            case R.id.title:
                finish();
                break;
            case R.id.pip_button:
                Log.d(TAG, "====show pip=" + FloatingCameraService.sIsStarted);
                if (FloatingCameraService.sIsStarted) {
                    return;
                }
                mManager.closeCamera();
                finish();
                startService(new Intent(getApplicationContext(), FloatingCameraService.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MLog.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MLog.d(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        MLog.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        MLog.d(TAG, "onStop");
    }

    @Override
    public void onSurfaceTextureAvailable(@Nullable SurfaceTexture surface, int width, int height) {
       mManager.openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@Nullable SurfaceTexture surface,
                                            int width, int height) {
        MLog.d(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@Nullable SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@Nullable SurfaceTexture surface) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MLog.d(TAG, "onDestroy");
        mManager.stopPreview();
        mManager.closeCamera();
    }

    private boolean isCameraAvailable() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            for (String cameraId : manager.getCameraIdList()) {
//                MLog.d(TAG, "cameraID=" + cameraId);
//                CameraCharacteristics characteristics
//                        = manager.getCameraCharacteristics(cameraId);
//                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (facing != null && facing == CAMERA_FACING) {
//                    return true;
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
        return true;
    }
}
