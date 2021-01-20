package com.ldf.media;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.ldf.media.util.ScreenUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * author：   ldf
 * date：      2021/1/14 & 20:31
 * version    1.0
 * description
 * modify by
 */
class CameraHelper implements Camera.PreviewCallback {

    private static final String TAG = "cameraHelper";

    private int width;
    private int height;
    private Camera camera;
    private int mCameraId;


    public void initCamera(Activity activity) {

        width = ScreenUtil.getPxWidth(activity);
        height = ScreenUtil.getPxHeight(activity);

        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        camera.setPreviewCallback(this);

//        try {
//            camera.setPreviewDisplay(surfaceHolder);
//        } catch (IOException e) {
//            Log.e(TAG, "setPreviewDisplay: error");
//        }

        //相机预览角度，跟相机位置设计有关，一般是横放的，也有其他摆放方式，所以需要调节
        camera.setDisplayOrientation(calculateCameraPreviewOrientation(activity));

        Camera.Parameters parameters = camera.getParameters();

        // 后置摄像头自动对焦
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK
                && supportAutoFocusFeature(parameters)) {
            camera.cancelAutoFocus();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        Camera.Size adapterSize = findBestPreviewSize(parameters);
        if (adapterSize != null) {
            Log.d(TAG,
                    "相机预览分辨率: " + "width:" + adapterSize.width + ",height:" + adapterSize.height);
            parameters.setPreviewSize(adapterSize.width, adapterSize.height);
        }
        //设置预览帧率
        parameters.setPreviewFrameRate(15);

        camera.setParameters(parameters);

    }

    /**
     * 判断是否支持自动对焦
     * @param parameters
     * @return
     */
    private boolean supportAutoFocusFeature(@NonNull Camera.Parameters parameters) {
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            return true;
        }
        return false;
    }


    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     *
     * @param activity
     */
    private int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.d(TAG, "相机调整后预览角度: " + result);
        return result;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {


    }


    /**
     * 找出最适合的预览分辨率
     */
    private Camera.Size findBestPreviewSize(Camera.Parameters parameters) {

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

        List<Camera.Size> newSizes = new ArrayList<>();
        for (Camera.Size size : sizes) {
            if (size.width * 3 == size.height * 4) {
                newSizes.add(size);
            }
        }

        Camera.Size resultSize = null;
        //surfaceView的分辨率
        int surViewSize = width * height * 4 / 3;

        int cameraSizeBase = Math.abs(newSizes.get(0).width * newSizes.get(0).height
                - surViewSize);

        for (Camera.Size size : newSizes) {
            int cameraSize = size.width * size.height;
            if (Math.abs(cameraSize - surViewSize) < cameraSizeBase) {
                cameraSizeBase = Math.abs(cameraSize - surViewSize);
                resultSize = size;
            }
        }
        if (resultSize == null) {
            return newSizes.get(0);
        }
        return resultSize;
    }

    public void startPreview(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "setPreviewDisplay: error");
        }
    }


    public void releaseCamera(){
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.setPreviewCallbackWithBuffer(null);
            camera.addCallbackBuffer(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

    }
}
