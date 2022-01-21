package com.mega.scenemode.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import mega.log.MLog;

public class CameraOpenManager {
    private String mCameraId = null;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Size mPreviewSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private static final int CAMERA_FACING = 0;
    private static final String CAMERA_ID = "5";
    private boolean mIsPreview = false;
    private Context mContext;
    private TextureView mCameraPreview;
    private static final String TAG = CameraOpenManager.class.getSimpleName();
    private CameraErrorCallback mCallback;

    public CameraOpenManager(@Nullable Context context, @Nullable TextureView preview,
                             @Nullable CameraErrorCallback callback) {
        mContext = context;
        mCameraPreview = preview;
        mCallback = callback;
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            MLog.d(TAG, "camera opened");
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            MLog.e(TAG, "camera open error=");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            MLog.e(TAG, "camera open error=" + error);
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            if (mCallback != null) {
                mCallback.onError();
            }
        }
    };

    private void startPreview() {
        if (null == mCameraDevice || !mCameraPreview.isAvailable() || null == mPreviewSize) {
            return;
        }
        MLog.d(TAG, "start preview 11111");
        try {
            mIsPreview = true;
            closePreviewSession();
            SurfaceTexture texture = mCameraPreview.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            MLog.d(TAG, "start preview 2222");
            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            java.util.List<Surface> targets = new ArrayList<>();
            targets.add(previewSurface);
            MLog.d(TAG, "start preview 3333");
            mCameraDevice.createCaptureSession(targets,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mPreviewSession = session;
                            MLog.d(TAG, "start preview 4444");
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            MLog.d(TAG, "start preview 55555");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            MLog.d(TAG, "start preview error");
            if (mCallback != null) {
                mCallback.onError();
            }
        }
    }

    private void updatePreview() {
        MLog.d(TAG, "start preview 555");
        if (null == mCameraDevice) {
            MLog.d(TAG, "start preview666");
            return;
        }
        try {
            MLog.d(TAG, "start preview 777");
            setUpCaptureRequestBuilder(mPreviewBuilder);
            MLog.d(TAG, "start preview 888");
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
            MLog.d(TAG, "start preview ok ");
        } catch (CameraAccessException e) {
            e.printStackTrace();
            MLog.d(TAG, "start preview error 2");
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    @SuppressLint("MissingPermission")
    public void openCamera(int width, int height) {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            MLog.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            for (String cameraId : manager.getCameraIdList()) {
                MLog.d(TAG, "cameraID=" + cameraId);
                if (CAMERA_ID.equals(cameraId)) {
                    mCameraId = cameraId;
                    break;
                }
//                CameraCharacteristics characteristics
//                        = manager.getCameraCharacteristics(cameraId);
//                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (facing != null && facing == CAMERA_FACING) {
//                    mCameraId = cameraId;
//                    break;
//                }
            }
            if (mCameraId == null) {
                return;
            }
            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            android.hardware.camera2.params.StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height);
            manager.openCamera(mCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            if (mCallback != null) {
                mCallback.onError();
            }
        } catch (NullPointerException e) {
            if (mCallback != null) {
                mCallback.onError();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    public void stopPreview() {
        MLog.d(TAG, "stop preview");
        if (!mIsPreview || mPreviewSession == null) {
            MLog.e(TAG, "preview has already stopped");
            return;
        }
        mIsPreview = false;
        try {
            mPreviewSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                    - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        java.util.List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            MLog.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    public void closeCamera() {
        try {
            MLog.e(TAG, "close camera start");
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                MLog.e(TAG, "close camera");
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            MLog.e(TAG, "close camera==" + e.toString());
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public interface CameraErrorCallback {
        void onError();
    }
}
