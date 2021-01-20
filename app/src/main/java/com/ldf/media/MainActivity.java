package com.ldf.media;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ldf.media.util.PermissionUtils;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "MainActivity";
    private SurfaceHolder surfaceHolder;

    private CameraBufferHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraHelper = new CameraBufferHelper();

        PermissionUtils.askPermission(this, new String[]{Manifest.permission.CAMERA, Manifest
                .permission.WRITE_EXTERNAL_STORAGE}, 10, initViewRunnable);

        SurfaceView localSurfaceView = findViewById(R.id.sv_local);

        surfaceHolder = localSurfaceView.getHolder();

        surfaceHolder.addCallback(this);

    }


    private Runnable initViewRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            cameraHelper.initCamera(MainActivity.this);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cameraHelper.startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHelper.releaseCamera();
    }
}