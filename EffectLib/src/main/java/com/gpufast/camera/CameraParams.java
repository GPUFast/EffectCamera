package com.gpufast.camera;

import android.graphics.SurfaceTexture;

/**
 * @author Sivin 2018/11/27
 * Description:Cameara配置类，该类对象将传递到ICameara实现类中，用于配置Camera
 */
class CameraParams {

    private SurfaceTexture mPreTexture;

    private int preViewWidth;

    private int preViewHeight;

    private AspectRatio ratio;

    private int fps;

    private CameraParams() {
    }

    private CameraParams(Builder builder) {
        mPreTexture = builder.texture;
        preViewWidth = builder.width;
        preViewHeight = builder.height;
        ratio = builder.ratio;
        fps = builder.fps;
    }

    SurfaceTexture getPreTexture() {
        return mPreTexture;
    }

    int getPreViewWidth() {
        return preViewWidth;
    }

    int getPreViewHeight() {
        return preViewHeight;
    }

    AspectRatio getRatio() {
        return ratio;
    }

    public int getFps() {
        return fps;
    }


    public static class Builder {
        private SurfaceTexture texture;
        private int width;
        private int height;
        private AspectRatio ratio;
        private int fps;

        public CameraParams build() {
            if (texture == null) {
                throw new RuntimeException("A CameraParams must be set a texture");
            }
            return new CameraParams(this);
        }

        public void setTexture(SurfaceTexture texture) {
            this.texture = texture;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setRatio(AspectRatio ratio) {
            this.ratio = ratio;
        }

        public void setFps(int fps) {
            this.fps = fps;
        }
    }
}
