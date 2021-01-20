package com.ldf.media.sticker;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author：   ldf
 * date：      2021/1/16 & 14:54
 * version    1.0
 * description
 * modify by
 */
public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "CameraGLSurfaceView";
    private int textureId;
    private SurfaceTexture surfaceTexture;
    private CameraTextureHelper textureHelper;

    private Activity activity;

    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        activity = (Activity) context;

        //设置openGL ES 的版本为2.0
        setEGLContextClientVersion(2);
        // 设置与当前GLSurfaceView绑定的Renderer
        setRenderer(this);
        // 设置渲染的模式;当需要重绘时，调用
        //GLSurfaceView.requestRender()
        //https://blog.csdn.net/zh13544539220/article/details/45058945
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        textureHelper = new CameraTextureHelper();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        Log.d(TAG, "onSurfaceCreated: ");
        //应用程序使用一个 SurfaceTexture 接收来自于 Camera 的帧，并把它们转为外部 GLES 纹理。
        textureId = TextureUtil.createTextureID();
        surfaceTexture = new SurfaceTexture(textureId);
        //surfaceTexture是BufferQueue 的拥有者和消费者。。从camera拿到数据缓存BufferQueue中，并转成OES外部纹理；
        //在OpenGL环境下,用GLSurfaceView.Render回调onDrawFrame将这个纹理绘制出来
        //onFrameAvailable收到回调，去通知requestRender，再触发onDrawFrame，通过surfaceTexture.updateTexImage();
        //拿到下一帧画面，如此循环。

        //拿到OES纹理之后，因为opengL和手机屏幕坐标不一致，需要变换
        surfaceTexture.setOnFrameAvailableListener(this);
        textureHelper.initCamera(activity);
        textureHelper.startPreview(surfaceTexture);
        //打开相机
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(w,h)指定了视口的大小
        GLES20.glViewport(0, 0, width, height);
        //开始预览
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG,"onDrawFrame...");
        // 设置白色为清屏
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // 清除屏幕和深度缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 把摄像头的数据先输出来
        // 更新纹理，然后我们才能够使用opengl从SurfaceTexure当中获得数据 进行渲染
        surfaceTexture.updateTexImage();

//        mDirectDrawer.draw();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }
}
