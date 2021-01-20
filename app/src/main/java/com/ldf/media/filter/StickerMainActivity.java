package com.ldf.media.filter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.ldf.media.R;
import com.xtc.log.LogUtil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 相机预览添加贴纸、水印
 */

public class StickerMainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //GLSurfaceView能够真正做到让Camera的数据和显示分离，我们就可以在此基础上对视频数据做一些处理，例如美图，增加特效等。
    private CameraGlView localSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_main);
        localSurfaceView = findViewById(R.id.sv_local);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}