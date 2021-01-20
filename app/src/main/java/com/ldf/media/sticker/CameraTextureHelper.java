package com.ldf.media.sticker;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.ldf.media.util.ScreenUtil;
import com.ldf.media.util.YuvDataRotateUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * author：   ldf
 * date：      2021/1/14 & 20:31
 * version    1.0
 * description  通过内存复用来提高预览的效率
 * <p>
 * 拍照；保存一帧画面
 * <p>
 * modify by
 */
public class CameraTextureHelper implements Camera.PreviewCallback {

    private static final String TAG = "cameraHelper";

    private int width;
    private int height;
    private Camera camera;
    private int mCameraId;
    private ByteArrayOutputStream baos;
    private long lastTime;


    public void initCamera(Activity activity) {

        width = ScreenUtil.getPxWidth(activity);
        height = ScreenUtil.getPxHeight(activity);

        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        camera.setPreviewCallback(this);

        //相机预览角度，跟相机位置设计有关，一般是横放的，也有其他摆放方式，所以需要调节
        camera.setDisplayOrientation(calculateCameraPreviewOrientation(activity));

        Camera.Parameters parameters = camera.getParameters();

        // 后置摄像头自动对焦
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK
                && supportAutoFocusFeature(parameters)) {
            camera.cancelAutoFocus();
            //对焦方式还有FOCUS_MODE_CONTINUOUS_VIDEO使用视频录制，FOCUS_MODE_CONTINUOUS_PICTURE 用于拍照。
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        Camera.Size adapterSize = findBestPreviewSize(parameters);
        if (adapterSize != null) {
            Log.d(TAG,
                    "相机预览分辨率: " + "width:" + adapterSize.width + ",height:" + adapterSize.height);
            parameters.setPreviewSize(adapterSize.width, adapterSize.height);

            camera.setPreviewCallbackWithBuffer(this);
            //3/2是根据rgb转yuv格式NV21；4:1:1计算来的
            byte[] callbackBuffer = new byte[adapterSize.width * adapterSize.height * 3 / 2];
            camera.addCallbackBuffer(callbackBuffer);
        }
        //设置预览帧率
        parameters.setPreviewFrameRate(15);

        camera.setParameters(parameters);
    }


    private void takePicture() {
        //第一个参数，ShutterCallback接口，在拍摄瞬间瞬间被回调，通常用于播放“咔嚓”这样的音效；
        //第二个参数，PictureCallback接口，返回未经压缩的RAW类型照片；
        //第三个参数，PictureCallback接口，返回经过压缩的JPEG类型照片；
        camera.takePicture(null, null, takePictureCallback);
    }

    private Camera.PictureCallback takePictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //这里的data是picture data，
            //BitmapFactory.decodeByteArray( data, 0, data.length );

        }
    };

    /**
     * 判断是否支持自动对焦
     *
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


    //onPreviewFrame()方法跟Camera.open()是运行于同一个线程，
    // 所以为了防止onPreviewFrame()会阻塞UI线程，将Camera.open()放置在子线程中运行。
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 这里的data是NV21格式的yuv数据
        //先用YuvImage转成jpeg格式

        Log.d(TAG, "onPreviewFrame: ");

        //每隔5秒保存一帧画面
        if (System.currentTimeMillis() - lastTime > 5000) {

            Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到

            //默认被旋转90度，所以需要主动旋转回来
            byte[] afterRotate = YuvDataRotateUtil.rotateYUV420Degree90(data,previewSize.width, previewSize.height);
            //旋转后的宽高，跟原始的相反，所以需要调换,不然会出现重影
            int width = previewSize.height;
            int height = previewSize.width;

            YuvImage yuvimage = new YuvImage(
                    afterRotate,
                    ImageFormat.NV21,
                    width,
                    height,
                    null);
            baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0,width, height), 100,
                    baos);// 80--JPG图片的质量[0-100],100最高
            byte[] imageByte = baos.toByteArray();
            //将imageByte转换成bitmap;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length, options);
            // 保存至本地

            saveDataLocal(imageByte);
            lastTime = System.currentTimeMillis();
        }
        //因为前面setPreviewCallbackWithBuffer，所以需要调用setPreviewCallback，否则该回调只会触发一次
        camera.setPreviewCallback(this);
    }

    private void saveDataLocal(byte[] data) {

      Log.d(TAG, "saveDataLocal: ");

        File photo = new File(Environment.getExternalStorageDirectory(), "photo.jpg");

        if (photo.exists()) {
            photo.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());

            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
        Log.d(TAG, "saveDataLocal: success");
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

    public void startPreview(SurfaceTexture surfaceTexture) {
        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "setPreviewDisplay: error");
        }
    }


    public void releaseCamera() {
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
