package com.android.grafika.helper;


import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.android.grafika.CameraUtils;

import java.io.IOException;

public class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();

    private final SurfaceTexture surfaceTexture;

    private Camera camera;

    private float cameraAspect;

    public CameraHelper(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    /**
     * Tries to open the front camera by default. If not found then tries to open the default one.
     */
    private void openCamera() {
        Log.d(TAG, "Acquiring camera...");

        if (camera != null) {
            throw new RuntimeException("Camera already initialized");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                camera = Camera.open(i);
                break;
            }
        }
        if (camera == null) {
            Log.d(TAG, "No front-facing camera found; opening default");
            camera = Camera.open();    // opens first back-facing camera
        }
        if (camera == null) {
            throw new RuntimeException("Unable to open camera");
        }

        Camera.Parameters mCameraParameters = camera.getParameters();
        CameraUtils.choosePreviewSize(mCameraParameters, 1280, 720);
        camera.setParameters(mCameraParameters);

        Camera.Size mCameraPreviewSize = mCameraParameters.getPreviewSize();
        cameraAspect = (float) mCameraPreviewSize.width / mCameraPreviewSize.height;
    }

    /**
     * Opens the camera and starts the preview
     */
    public void startCameraPreview() {
        openCamera();

        Log.d(TAG, "starting camera preview");
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        camera.startPreview();
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    public void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;

            Log.d(TAG, "Camera release done");
        }
    }

    /**
     * @return camera preview's aspect ratio
     */
    public float getCameraAspect() {
        return cameraAspect;
    }
}
