package com.gpufast.render;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.view.Surface;

import com.gpufast.effectlib.filter.CameraInputFilter;
import com.gpufast.effectlib.filter.ImageFilter;

public class CameraRender extends BaseRender implements BaseRender.RenderCallback, SurfaceTexture.OnFrameAvailableListener {

    private Surface mInputSurface;
    private int[] oesTextureId;
    private float[] textureMatrix;
    private SurfaceTexture swTexture;
    private int mCurrentWidth;
    private int mCurrentHeight;
    private CameraInputFilter cameraInputFilter;
    private ImageFilter imageFilter;
    private FrameCallback mFrameCallback;

    public CameraRender() {
        super(null, "cameraRender");
        setRenderCallback(this);
        textureMatrix = new float[16];
        oesTextureId = new int[1];
        swTexture = new SurfaceTexture(-1);
        swTexture.detachFromGLContext();
        swTexture.setOnFrameAvailableListener(this);
        cameraInputFilter = new CameraInputFilter();
        imageFilter = new ImageFilter();
    }

    /**
     * 这个函数必须在start调用之前调用
     *
     * @param surface 用户展示画面内容的surface
     */
    public void setInputSurface(Surface surface) {
        mInputSurface = surface;
    }

    public void setInputTexture(SurfaceTexture texture) {
        mInputSurface = new Surface(texture);
    }

    public void setFrameCallback(FrameCallback callback) {
        mFrameCallback = callback;
    }

    @Override
    public void onInit() {
        if (mFrameCallback != null) {
            mFrameCallback.onEGLContextCreate(getEGLContext());
        }
        cameraInputFilter.init();
        imageFilter.init();
    }

    @Override
    public Surface getInputSurface() {
        return mInputSurface;
    }

    @Override
    public void onSizeChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mCurrentWidth = width;
        mCurrentHeight = height;
        cameraInputFilter.onSizeChanged(width, height);
        imageFilter.onSizeChanged(width, height);
    }

    @Override
    public void onDraw(int textureId) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
        drawTexture();
    }

    private void drawTexture() {
        swTexture.updateTexImage();
        swTexture.getTransformMatrix(textureMatrix);
        int newTextureId = cameraInputFilter.drawTexture(oesTextureId[0], textureMatrix);
        int resId = -1;
        if (mFrameCallback != null) {
            resId = mFrameCallback.onFrameCallback(newTextureId, mCurrentWidth, mCurrentHeight);
        }
        if (resId > 0) {
            newTextureId = resId;
        }
        imageFilter.drawTexture(newTextureId);
    }

    @Override
    public void onDrawFinish() {

    }

    @Override
    public void onDestroy() {
        if (mFrameCallback != null) {
            mFrameCallback.onEGLContextDestroy();
        }
        GLES20.glDeleteTextures(1, oesTextureId, 0);
        swTexture.release();
        swTexture.setOnFrameAvailableListener(null);
        cameraInputFilter.destroy();
        imageFilter.destory();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        sendFrameAvaible(0);
    }

    public SurfaceTexture getPreViewTexture() {
        return swTexture;
    }

    public interface FrameCallback {
        void onEGLContextCreate(EGLContext eglContext);

        int onFrameCallback(int textureId, int width, int height);

        void onEGLContextDestroy();
    }
}
