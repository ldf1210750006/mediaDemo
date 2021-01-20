package com.ldf.media.filter;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author：   ldf
 * date：      2021/1/19 & 16:31
 * version    1.0
 * description
 * modify by
 */
public class CameraRenderer implements GLSurfaceView.Renderer, Camera.PreviewCallback,
        SurfaceTexture.OnFrameAvailableListener {


    private CameraGlView mView;
    private CameraHelper mCameraHelper;
    private int[] mTextures;
    private SurfaceTexture mSurfaceTexture;
    private CameraFilter mCameraFilter;
    private ScreenFilter mScreenFilter;
    private TimeFilter timeFilter;
    private StaticStickerFilter staticStickerFilter;
    private float[] mtx = new float[16];

    public CameraRenderer(CameraGlView cameraGlView) {
        mView = cameraGlView;

    }

    //Surface创建完成

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCameraHelper.setPreviewCallback(this);


        mTextures = new int[1];
        //  textureId = TextureUtil.createTextureID();
        //偷懒 这里可以不配置 （当然 配置了也可以）
        GLES20.glGenTextures(mTextures.length, mTextures, 0);

        //应用程序使用一个 SurfaceTexture 接收来自于 Camera 的帧，并把它们转为外部 OES 纹理。
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);

//surfaceTexture是BufferQueue 的拥有者和消费者。。从camera拿到数据缓存BufferQueue中，并转成OES外部纹理；
        //在OpenGL环境下,用GLSurfaceView.Render回调onDrawFrame将这个纹理绘制出来
        //onFrameAvailable收到回调，去通知requestRender，再触发onDrawFrame，通过surfaceTexture.updateTexImage();
        //拿到下一帧画面，如此循环。
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mCameraFilter = new CameraFilter(mView.getContext());
        mScreenFilter = new ScreenFilter(mView.getContext());
        timeFilter = new TimeFilter(mView.getContext());
        staticStickerFilter = new StaticStickerFilter(mView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        //开启预览
        mCameraHelper.startPreview(mSurfaceTexture);
        mCameraFilter.onReady(width, height);
        mScreenFilter.onReady(width, height);
        timeFilter.onReady(width, height);
        staticStickerFilter.onReady(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 配置屏幕
        //清理屏幕 :告诉opengl 需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 把摄像头的数据先输出来
        // 更新纹理，然后我们才能够使用opengl从SurfaceTexure当中获得新的一帧数据 进行渲染
        mSurfaceTexture.updateTexImage();

        //获得SurfaceTexture,赋值给mtx，再变换矩阵
        mSurfaceTexture.getTransformMatrix(mtx);
        mCameraFilter.setMatrix(mtx);

        //开始渲染。。。
        int id = mCameraFilter.onDrawFrame(mTextures[0]);

        id = timeFilter.onDrawFrame(id);

        id = staticStickerFilter.onDrawFrame(id);

        //加完之后再显示到屏幕中去
        mScreenFilter.onDrawFrame(id);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // data 可以做人脸识别
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mView.requestRender();
    }

    public void onSurfaceDestroyed() {
        mCameraHelper.stopPreview();
    }
}
