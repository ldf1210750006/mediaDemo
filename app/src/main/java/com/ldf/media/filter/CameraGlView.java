package com.ldf.media.filter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * author：   ldf
 * date：      2021/1/19 & 16:19
 * version    1.0
 * description
 * modify by
 */
public class CameraGlView extends GLSurfaceView {


    private CameraRenderer cameraRenderer;

    public CameraGlView(Context context) {
        super(context);
    }

    public CameraGlView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        cameraRenderer = new CameraRenderer(this);
        setRenderer(cameraRenderer);

        //设置按需渲染 当我们调用 requestRender 请求GLThread 回调一次 onDrawFrame
        // 连续渲染 就是自动的回调onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        cameraRenderer.onSurfaceDestroyed();
    }
}
