package com.ldf.media.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author：   ldf
 * date：      2021/1/19 & 17:17
 * version    1.0
 * description
 * modify by
 */
public abstract class AbstractFilter {


    protected FloatBuffer mGLVertexBuffer;
    protected FloatBuffer mGLTextureBuffer;

    //顶点着色
    protected int mVertexShaderId;
    //片段着色
    protected int mFragmentShaderId;


    protected int mGLProgramId;
    /**
     * 顶点着色器
     * attribute vec4 position;
     * 赋值给gl_Position(顶点)
     */
    protected int vPosition;

    /**
     * varying vec2 textureCoordinate;
     */
    protected int vCoord;


    /**
     * uniform mat4 vMatrix;
     */
    protected int vMatrix;

    /**
     * 片元着色器
     * Samlpe2D 扩展 samplerExternalOES
     */
    protected int vTexture;

    protected int mOutputWidth;
    protected int mOutputHeight;


    public AbstractFilter(Context context, int vertexShaderId, int fragmentShaderId) {

        initBuffer();

        this.mVertexShaderId = vertexShaderId;
        this.mFragmentShaderId = fragmentShaderId;

        initilize(context);
        initCoordinate();
    }


    protected void initilize(Context context) {
        String vertexSharder = OpenGLUtils.readRawTextFile(context, mVertexShaderId);
        String framentShader = OpenGLUtils.readRawTextFile(context, mFragmentShaderId);
        mGLProgramId = OpenGLUtils.loadProgram(vertexSharder, framentShader);
        //着色器（Shader）是在GPU上运行的小程序
        // 获得着色器中的 attribute 变量 position 的索引值
        //获取着色器glsl程序中的定义的变量
        vPosition = GLES20.glGetAttribLocation(mGLProgramId, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mGLProgramId,
                "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mGLProgramId,
                "vMatrix");
        // 获得Uniform变量的索引值
        vTexture = GLES20.glGetUniformLocation(mGLProgramId,
                "vTexture");
    }


    /**
     * 设置窗口大小
     */
    public void onReady(int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    //draw step
    public int onDrawFrame(int textureId) {
        //step0 clear
        onClear();
        //step1 设置显示窗口
        onViewPort();
        //step2 use program
        onUseProgram();
        //step3 active and bind custom data
        onSetExpandData();
        //step4 bind texture
        onBindTexture(textureId);
        //step5 normal draw
        onDraw();
        return textureId;
    }


    /**
     * Draw step0 :清除画布
     */
    private void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }


    //设置显示窗口
    private void onViewPort() {
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
    }

    /**
     * step1
     * <p>
     * 使用openGL 对象
     */
    private void onUseProgram() {
        GLES20.glUseProgram(mGLProgramId);
    }


    /**
     * step2
     * 变换矩阵
     */
    protected void onSetExpandData() {

    }

    /**
     * step3
     * <p>
     * 绑定纹理
     */
    private void onBindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //设置OES没有画面，，因为FBO已经把OES转成了2D
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);
    }

    /**
     * step 4
     * 绘制图像的话，同之前相同，只需要绘制一个长方形就可以了。
     * 启用顶点坐标和纹理坐标进行绘制。
     * <p>
     * 主要就是激活和使用定义的varying和uniform 变量
     */
    private void onDraw() {
        //传递坐标
        mGLVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vCoord);
    }

    /**
     * Buffer初始化
     */
    private void initBuffer() {
        //每个float占用4个字节

        float[] VERTEX = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLVertexBuffer = ByteBuffer
                .allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLVertexBuffer.clear();
        mGLVertexBuffer.put(VERTEX);


        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };
        mGLTextureBuffer = ByteBuffer
                .allocateDirect(TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(TEXTURE);
    }


    //修改顶点或者纹理坐标

    protected void initCoordinate() {

    }

    public void release() {
        GLES20.glDeleteProgram(mGLProgramId);
    }

}
