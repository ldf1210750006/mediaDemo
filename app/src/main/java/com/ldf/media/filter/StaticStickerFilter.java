package com.ldf.media.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.ldf.media.R;
import com.xtc.log.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author：   ldf
 * date：      2021/1/20 & 14:46
 * version    1.0
 * description
 * modify by
 */
public class StaticStickerFilter extends AbstractFrameFilter {


    private final Context mContext;
    private Bitmap mBitmap;

    public StaticStickerFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
        this.mContext = context;
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
    }

    @Override
    public int onDrawFrame(int textureId) {

        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //不调用的话就是默认的操作glsurfaceview中的纹理了。显示到屏幕上了
        //这里我们还只是把它画到fbo中(缓存)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        GLES20.glUseProgram(mGLProgramId);

        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //画贴纸
        onDrawStick();
        //返回fbo的纹理id
        return mFrameBufferTextures[0];

    }

    private void onDrawStick() {

        //帖纸画上去
        //开启混合模式 ： 将多张图片进行混合(贴图)
        GLES20.glEnable(GLES20.GL_BLEND);
        //设置贴图模式
        // 1：src 源图因子 ： 要画的是源  (耳朵)
        // 2: dst : 已经画好的是目标  (从其他filter来的图像)
        //画耳朵的时候  GL_ONE:就直接使用耳朵的所有像素 原本是什么样子 我就画什么样子
        // 表示用1.0减去源颜色的alpha值来作为因子
        //  耳朵不透明 (0,0 （全透明）- 1.0（不透明）) 目标图对应位置的像素就被融合掉了 不见了
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //准备好Bitmap，类似于相机预览的yuv数据
        updateBitmap();

        //画画
        //不是画全屏 定位到相应的位置
        //设置显示窗口
        //起始的位置
        float x = 700;
        float y = 200;
        GLES20.glViewport((int) x, (int) y,
                mBitmap.getWidth(),
                mBitmap.getHeight());

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        //使用着色器
        GLES20.glUseProgram(mGLProgramId);
        //传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //这里需要重新创建一个新的纹理id
        int[] mTextureId = new int[1];
        OpenGLUtils.glGenTextures(mTextureId);
        //表示后续的操作 就是作用于这个纹理上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        // 将 Bitmap与纹理id 绑定起来
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //关闭
        GLES20.glDisable(GLES20.GL_BLEND);

//        mBitmap.recycle();


    }

    private void updateBitmap() {

        String aText = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());

        float aFontSize = 60;
        Paint textPaint = new Paint();
        textPaint.setTextSize(aFontSize);
        textPaint.setFakeBoldText(false);
        textPaint.setAntiAlias(true);
        textPaint.setARGB(255, 255, 255, 255);
        // If a hinting is available on the platform you are developing, you should enable it
        // (uncomment the line below).
        //textPaint.setHinting(Paint.HINTING_ON);
        textPaint.setSubpixelText(true);
        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        textPaint.setShadowLayer(2, 2, 2, Color.BLACK);

        float realTextWidth = textPaint.measureText(aText);

        // Creates a new mutable bitmap, with 128px of width and height
        int bitmapWidth = (int) (realTextWidth + 2.0f);
        int bitmapHeight = (int) aFontSize + 2;

        if (mBitmap == null) {
            LogUtil.d("TimeFilter", "updateBitmap: mBitmap == null");
            Drawable drawable = mContext.getResources().getDrawable(R.mipmap.ic_launcher);
            BitmapDrawable bd = (BitmapDrawable) drawable;
            mBitmap = bd.getBitmap();
        }

//        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
//        mBitmap.eraseColor(Color.argb(0, 255, 0, 0));
        // Creates a new canvas that will draw into a bitmap instead of rendering into the screen
//        Canvas bitmapCanvas = new Canvas(mBitmap);
        // Set start drawing position to [1, base_line_position]
        // The base_line_position may vary from one font to another but it usually is equal to
        // 75% of font size (height).
//        bitmapCanvas.drawText(aText, 1, 1.0f + aFontSize * 0.75f, textPaint);

    }
}
