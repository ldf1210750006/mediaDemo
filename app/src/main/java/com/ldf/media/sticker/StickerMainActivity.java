package com.ldf.media.sticker;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ldf.media.R;
import com.ldf.media.util.PermissionUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 相机预览添加贴纸、水印
 */

public class StickerMainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SurfaceHolder surfaceHolder;

    private CameraTextureHelper textureHelper;
    //GLSurfaceView能够真正做到让Camera的数据和显示分离，我们就可以在此基础上对视频数据做一些处理，例如美图，增加特效等。
    private GLSurfaceView localSurfaceView;
    private SurfaceTexture surfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_main);
        textureHelper = new CameraTextureHelper();

        PermissionUtils.askPermission(this, new String[]{Manifest.permission.CAMERA, Manifest
                .permission.WRITE_EXTERNAL_STORAGE}, 10, initViewRunnable);

        localSurfaceView = findViewById(R.id.glsv_local);
        localSurfaceView.setRenderer(new Camera1Renderer());

    }


    private Runnable initViewRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            textureHelper.initCamera(StickerMainActivity.this);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        textureHelper.releaseCamera();
    }

    private class Camera1Renderer implements GLSurfaceView.Renderer {


        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //应用程序使用一个 SurfaceTexture 接收来自于 Camera 的帧，并把它们转为外部 GLES 纹理。
            int textureId = TextureUtil.createTextureID();
            surfaceTexture = new SurfaceTexture(textureId);
//            textureHelper.startPreview(localSurfaceView, surfaceTexture);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (surfaceTexture != null) {
                surfaceTexture.updateTexImage();
            }
        }
    }

}