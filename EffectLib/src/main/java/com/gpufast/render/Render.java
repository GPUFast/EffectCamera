package com.gpufast.render;


import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.view.Surface;

import com.gpufast.effect.filter.ImageFilter;
import com.gpufast.effect.filter.OesToRgbFilter;
import com.gpufast.utils.ELog;

public class Render extends BaseRender implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = Render.class.getSimpleName();

    private int srcTexWidth = 720;
    private int srcTexHeight = 1280;

    //预览窗口尺寸
    private int mWidth;
    private int mHeight;

    private int[] textures;
    private float[] textureMatrix;
    private SurfaceTexture swTexture;

    //预处理滤镜
    private OesToRgbFilter mOesToRgbFilter;
    //最终显示的滤镜
    private ImageFilter imageFilter;

    private FrameCallback mCallback = null;

    public Render(Surface surface) {
        super(surface);
        //不要做任何操作，请在onRenderInit里做子类相关的初始化
    }

    @Override
    protected void onRenderInit() { //该函数为主线程

        textureMatrix = new float[16];
        textures = new int[1];

        mOesToRgbFilter = new OesToRgbFilter();
        imageFilter = new ImageFilter();

        swTexture = new SurfaceTexture(-1);
        swTexture.detachFromGLContext();
        swTexture.setOnFrameAvailableListener(this);
    }



    private void setupTexture() {
        // 生成textureId
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("Texture generate");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("Texture bind");
    }

    @Override
    protected RenderCallback getRenderCallback() {
        return new RenderCallback() {
            @Override
            public void onInit() {
                setupTexture();
                swTexture.attachToGLContext(textures[0]);
                mOesToRgbFilter.init();
                imageFilter.init();
            }

            @Override
            public void onSizeChanged(int width, int height) {
                mWidth = width;
                mHeight = height;
                mOesToRgbFilter.onSizeChanged(srcTexWidth, srcTexHeight);
            }

            @Override
            public void onDraw() {
                swTexture.updateTexImage();
                swTexture.getTransformMatrix(textureMatrix);
                int srcRgbId = mOesToRgbFilter.drawTexture(textures[0], textureMatrix);

                int newTexId = 0;
                if(mCallback != null){
                    newTexId = mCallback.onFrameCallback(getEGLContext(),srcRgbId,srcTexWidth,srcTexHeight);
                }

                if(newTexId == 0){
                    newTexId = srcRgbId;
                }

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                GLES20.glViewport(0,0,mWidth,mHeight);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                imageFilter.drawTexture(newTexId);
            }

            @Override
            public void onDestroy() {
                if(mCallback != null){
                    mCallback.onEglContextDestroy();
                }
                GLES20.glDeleteTextures(1, textures, 0);
                swTexture.release();
                swTexture.setOnFrameAvailableListener(null);
                mOesToRgbFilter.destroy();
                imageFilter.destroy();
            }
        };
    }

    @Override
    public SurfaceTexture getVideoTexture() {
        return swTexture;
    }


    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            ELog.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        onFrameAvailable();
    }

    public void setFrameCallback(FrameCallback callback) {
        mCallback = callback;
    }

    public interface FrameCallback {

        int onFrameCallback(EGLContext context, int textureId, int width, int height);

        void onEglContextDestroy();
    }
}
