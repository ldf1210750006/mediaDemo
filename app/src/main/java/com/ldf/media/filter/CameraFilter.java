package com.ldf.media.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.ldf.media.R;

/**
 * author：   ldf
 * date：      2021/1/19 & 17:16
 * version    1.0
 * description
 * modify by
 */
public class CameraFilter extends AbstractFrameFilter{


    private float[] matrix;

    public CameraFilter(Context context) {
        super(context, R.raw.camera_vertex, R.raw.camera_frag);
        //实现不同滤镜效果
        //super(context, R.raw.camera_vertex, R.raw.jiugongge);
    }

    @Override
    protected void initCoordinate() {
        mGLTextureBuffer.clear();
        //摄像头是颠倒的
//        float[] TEXTURE = {
//                0.0f, 0.0f,
////                1.0f, 0.0f,
////                0.0f, 1.0f,
////                1.0f, 1.0f
//        };
        //调整好了镜像
//        float[] TEXTURE = {
//                1.0f, 0.0f,
//                0.0f, 0.0f,
//                1.0f, 1.0f,
//                0.0f, 1.0f,
//        };
        //修复旋转 逆时针旋转90度
        float[] TEXTURE = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        mGLTextureBuffer.put(TEXTURE);
    }


    @Override
    public int onDrawFrame(int textureId) {
//        super.onDrawFrame(textureId);
        //复写 AbstractFilter 的onDrawFrame方法，区别在于glBindFramebuffer和
        //矩阵变换glUniformMatrix4fv

        //设置显示窗口
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        //问：OES纹理textureId，怎么转成自定义的FBO纹理，并绑定到自定义的FBO id上
        //答：glBindFramebuffer；
        //OES纹理textureId默认直接挂接在窗口系统的FBO，调用glBindFramebuffer后，会把OES纹理textureId切换到
        //自定义的FBO上

        //不调用的话就是默认的操作glsurfaceview中的纹理了。显示到屏幕上了
        //这里我们还只是把它画到fbo中(缓存)
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

        //变换矩阵
        //必现要加上，不然没画面，并且一直闪动
        //变换矩阵， 需要将原本的vCoord（01,11,00,10） 与矩阵matrix相乘 才能够得到 surfacetexure(特殊)的正确的采样坐标
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        //这里用GL_TEXTURE_2D也可以
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);

        //绘制，组装图元。还有其他命令绘制其他几何形状对象
        //glDrawElements、glDrawRangeElements、GLDrawArraysInstanced
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //返回fbo的纹理id
        return mFrameBufferTextures[0];
    }


    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

}
