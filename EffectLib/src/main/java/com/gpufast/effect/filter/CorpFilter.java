package com.gpufast.effect.filter;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.gpufast.gles.GlUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class CorpFilter {

    private static final String TAG = CorpFilter.class.getSimpleName();

    private static final String VERTEX_SHADER = "\n" +
            "attribute vec4 vPosition;\n" +
            "attribute vec4 vTexCoordinate;\n" +
            "uniform mat4 corpMatrix;\n" +
            "varying vec2 v_TexCoord;\n" +
            "\n" +
            "void main () {\n" +
            "    v_TexCoord = vTexCoordinate.xy;\n" +
            "    gl_Position = corpMatrix * vPosition;\n" +
            "}";
    private static final String FRAGMENT_SHADER = "\n" +
            "precision mediump float;\n" +
            "uniform sampler2D texture;\n" +
            "varying vec2 v_TexCoord;\n" +
            "\n" +
            "void main () {\n" +
            "    vec4 color = texture2D(texture, v_TexCoord);\n" +
            "    gl_FragColor = color;\n" +
            "}";


    private static float squareSize = 1.0f;

    private static float squareCoords[] = {
            -squareSize, squareSize,
            -squareSize, -squareSize,
            squareSize, -squareSize,
            squareSize, squareSize};

    private float textureCoords[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private static short drawOrder[] = {0, 1, 2, 0, 2, 3};

    private int mProgram = 0;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer drawListBuffer;

    //顶点属性索引
    private int positionLoc;
    private int textureCoordinateLoc;
    private int mCorpMatrixLoc;
    //纹理采样单元索引
    private int textureHandle;


    private int mFrameBufferTextureId = 0;
    private int mFramebuffer = 0;


    private int mWidth = 0;
    private int mHeight = 0;

    private int mSrcWidth = 0;
    private int mSrcHeight = 0;

    private float transX = 0.0f;
    private float transY = 0.0f;


    private float[] mCorpMatrix = new float[16];

    //private CanvasFilter mCanvasFilter;

    public CorpFilter() {
        setupBuffer();
    }

    public void addFilter(CanvasFilter filter) {
        // mCanvasFilter = filter;
    }

    private void setupBuffer() {
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);
        //初始化成单位矩阵
        Matrix.setIdentityM(mCorpMatrix, 0);
    }

    /**
     * 这个函数调用在openGL线程中
     */
    public void init() {

        mProgram =GlUtil.createProgram(VERTEX_SHADER,FRAGMENT_SHADER);
        GLES20.glUseProgram(mProgram);

        positionLoc = GLES20.glGetAttribLocation(mProgram, "vPosition");
        textureCoordinateLoc = GLES20.glGetAttribLocation(mProgram, "vTexCoordinate");
        mCorpMatrixLoc = GLES20.glGetUniformLocation(mProgram, "corpMatrix");
        textureHandle = GLES20.glGetUniformLocation(mProgram, "texture");

    }


    /**
     * 这个代码必须在setCorpSize之后掉用
     *
     * @param width
     * @param height
     */
    public void setSrcSize(int width, int height,boolean reversalY) {
        if (width == 0 || height == 0) {
            Log.d(TAG, "setSrcSize: width or height is 0");
            return;
        }
        if (mWidth == 0 || mHeight == 0) {
            Log.e(TAG, "you must call setCorpSize first before setSrcSize");
            return;
        }
        if (mSrcWidth != width || mSrcHeight != height) {
            mSrcWidth = width;
            mSrcHeight = height;

            Matrix.setIdentityM(mCorpMatrix, 0);

            transX = 0;
            transY = 0;

            float scaleX = 1.0f;
            float scaleY = 1.0f;

            float srcRatio = mSrcHeight * 1.0f / mSrcWidth;
            float targetRatio = mHeight * 1.0f / mWidth;
            float diff = Math.abs(srcRatio - targetRatio);
            if (diff > 0.001f) { //
                if (srcRatio > 1.0f && targetRatio > 1.0f) { //双方都是h>w
                    if (srcRatio > targetRatio) { //16:9--> 4:3 h:w

                        float scaleFactor = mWidth * 1.0f / mSrcWidth;
                        float scaleHeight = mSrcHeight * scaleFactor;
                        float offset = scaleHeight - mHeight;
                        if (offset < 0) {
                            throw new RuntimeException("algorithmic error: setSrcSize: scaleHeight < mHeight ");
                        }
                        //放大高度
                        scaleY = scaleHeight / mHeight;
                        transY = offset * 1.0f / 2;
                        transX = 0.0f;
                    } else { //16:12 --> 16:9 h:w
                        float scaleFactor = mHeight * 1.0f / mSrcHeight;
                        float mScaleWidth = mSrcWidth * scaleFactor;
                        float offset = mScaleWidth - mWidth;
                        if (offset < 0) {
                            throw new RuntimeException("algorithmic error: setSrcSize: scaleHeight < mHeight ");
                        }
                        //放大宽度
                        scaleX = mScaleWidth / mWidth;
                        transX = offset * 1.0f / 2;
                        transY = 0.0f;
                    }
                } else if (srcRatio < 1.0f && targetRatio < 1.0f) { //w>h:算法咱不考虑

                } else { //一个是w>h一个w<h //不裁剪直接缩放

                }

            } else {//尺寸相差不大：直接缩放不会产生明显的变形

            }

            float transXFactor = transX / mWidth;
            float transYFactor = transY / mHeight;
          if(reversalY){
              scaleY = scaleY * -1.0f;
          }
          Matrix.scaleM(mCorpMatrix,0,scaleX,scaleY,1.0f);
          Matrix.translateM(mCorpMatrix, 0, transXFactor, transYFactor, 0.0f);
        }
    }

    /**
     * 设置裁剪尺寸
     * 必须在OpenGL线程中进行
     *
     * @param width  width
     * @param height height
     */
    public void setCorpSize(int width, int height) {
        if (width == 0 || height == 0) {
            Log.d(TAG, "setCorpSize: width or height is 0");
            return;
        }
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            destroyFrameBuffer();
            prepareFramebuffer(mWidth, mHeight);
        }
    }

    /**
     * 准备离屏缓冲区
     */
    private void prepareFramebuffer(int width, int height) {

        GlUtil.checkGlError("prepareFramebuffer start");
        int[] values = new int[1];
        // 创建颜色缓冲区
        GLES20.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mFrameBufferTextureId = values[0];   // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextureId);
        GlUtil.checkGlError("glBindTexture " + mFrameBufferTextureId);
        // 设置texture存储
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        //设置缓冲区参数
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        // 创建帧缓冲区
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);


        //将颜色缓冲区绑定到帧缓冲区中
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextureId, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        //切换回默认的缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GlUtil.checkGlError("prepareFramebuffer done");
    }


    private void destroyFrameBuffer() {

        if (mFrameBufferTextureId != 0) {
            int[] value = new int[]{mFrameBufferTextureId};
            GLES20.glDeleteTextures(1, value, 0);
            mFrameBufferTextureId = 0;
        }

        if (mFramebuffer != 0) {
            int[] value = new int[]{mFramebuffer};
            GLES20.glDeleteFramebuffers(1, value, 0);
            mFramebuffer = 0;
        }

    }


    /**
     * 绘制纹理
     *
     * @param textureId 传入的纹理
     * @return 返回新的纹理id
     */
    public int drawTexture(int textureId) {

        //绑定当前缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        //视口应该和帧缓冲区大小保持一致
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //开启混合算法
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(mProgram);

        //设置pos顶点属性
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        //设置纹理顶点属性
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glEnableVertexAttribArray(textureCoordinateLoc);
        GLES20.glVertexAttribPointer(textureCoordinateLoc, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        //设置裁剪矩阵
        GLES20.glUniformMatrix4fv(mCorpMatrixLoc, 1, false, mCorpMatrix, 0);
        //开始绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionLoc);
        GLES20.glDisableVertexAttribArray(textureCoordinateLoc);
        GLES20.glUseProgram(0);
        //切换回默认缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBufferTextureId;
    }


    /**
     * 绘制纹理
     *
     * @param textureId 传入的纹理
     * @return 返回新的纹理id
     */
    public Buffer captureRgbBuffer(int textureId) {

        //绑定当前缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        //视口应该和帧缓冲区大小保持一致
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //开启混合算法
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glUseProgram(mProgram);

        //设置pos顶点属性
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        //设置纹理顶点属性
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glEnableVertexAttribArray(textureCoordinateLoc);
        GLES20.glVertexAttribPointer(textureCoordinateLoc, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        //设置裁剪矩阵
        GLES20.glUniformMatrix4fv(mCorpMatrixLoc, 1, false, mCorpMatrix, 0);
        //开始绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        ByteBuffer buf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        buf.position(0);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT,1);
        GLES20.glReadPixels(0,0,mWidth,mHeight,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buf);

        GLES20.glDisableVertexAttribArray(positionLoc);
        GLES20.glDisableVertexAttribArray(textureCoordinateLoc);
        GLES20.glUseProgram(0);
        //切换回默认缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return buf;
    }



    public void destroy() {
        destroyFrameBuffer();
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
        }
        mProgram = 0;
    }
}