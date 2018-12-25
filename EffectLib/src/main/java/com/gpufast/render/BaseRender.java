package com.gpufast.render;

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;


import com.gpufast.gles.EglCore;
import com.gpufast.gles.GLESUtil;
import com.gpufast.gles.WindowSurface;

import java.lang.ref.WeakReference;


public class BaseRender {

    private int mWidth;
    private int mHeight;
    private RenderThread mRenderThread;

    public BaseRender(EGLContext shareContext,String name) {
        mRenderThread = new RenderThread(shareContext);
        if(!"".equals(name)){
            mRenderThread.setName(name);
        }
    }

    public void setRenderCallback(RenderCallback callback) {
        mRenderThread.setRenderCallback(callback);
    }

    public void startRender() {
        mRenderThread.start();
        mRenderThread.waitUntilReady();
    }

    public void sendSurfaceChanged(int width, int height) {
        mRenderThread.getHandler().sendSurfaceChanged(width, height);
    }

    public void sendFrameAvaible(int textureId) {
        mRenderThread.getHandler().sendDoFrame(textureId);
    }
    public EGLContext getEGLContext(){
        return mRenderThread.getEGLContext();
    }


    public void setSize(int width, int height) {
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
            mRenderThread.getHandler().sendSurfaceChanged(width, height);
        }
    }

    public void stopRender() {
        mRenderThread.getHandler().sendShutdown();
    }

    private static class RenderThread extends Thread {
        private static final String TAG = "RenderThread";
        private volatile RenderHandler mHandler;
        private final Object mStartLock = new Object();
        private boolean mReady = false;
        private EglCore mEglCore;
        private WindowSurface mInputWindowSurface;
        private WeakReference<EGLContext> mEGLShareContext;
        private RenderCallback callback;

        public RenderThread(EGLContext shareContext) {
            mEGLShareContext = new WeakReference<>(shareContext);
        }

        public void setRenderCallback(RenderCallback cb) {
            callback = cb;
        }

        public EGLContext getEGLContext(){
            return mEglCore.getEGLContext();
        }

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new RenderHandler(this);
            mEglCore = new EglCore(mEGLShareContext.get(), EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);

            if (callback == null) {
                throw new RuntimeException("you must provide a renderCallback,so EGL can get an available surface");
            }
            Surface inputSurface = callback.getInputSurface();
            if (inputSurface == null) {
                throw new RuntimeException("inputSurface must be available");
            }
            mInputWindowSurface = new WindowSurface(mEglCore, inputSurface, true);
            mInputWindowSurface.makeCurrent();
            callback.onInit();
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();
            }
            Looper.loop();

            Log.d(TAG, "Render thread start destroy");

            callback.onDestroy();
            callback = null;
            releaseGl();
            mEglCore.release();
            synchronized (mStartLock) {
                mReady = false;
            }
        }

        /**
         * 开始离屏渲染
         */
        private void draw(int textureId) {
            callback.onDraw(textureId);
            mInputWindowSurface.swapBuffers();
            callback.onDrawFinish();
        }

        /**
         * 函数渲染线程调用，使渲染线程等待编码渲染宣城准备完毕
         */
        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        /**
         * 必须在当前线程中调用
         */
        private void shutdown() {
            Looper.myLooper().quit();
        }


        public RenderHandler getHandler() {
            return mHandler;
        }

        private void surfaceChanged(int width, int height) {
            callback.onSizeChanged(width, height);
        }

        private void releaseGl() {
            GLESUtil.checkGlError("releaseGl done");
            if (mInputWindowSurface != null) {
                mInputWindowSurface.release();
                mInputWindowSurface = null;
            }
            callback = null;
            mEGLShareContext = null;
            mEglCore.makeNothingCurrent();
        }
    }


    interface RenderCallback {

        void onInit();

        Surface getInputSurface();

        void onSizeChanged(int width, int height);

        void onDraw(int textureId);

        void onDrawFinish();

        void onDestroy();

    }


    private static class RenderHandler extends Handler {
        private static final String TAG = "RenderHandler";

        private static final int MSG_SURFACE_CREATED = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_DO_FRAME = 2;
        private static final int MSG_SHUTDOWN = 3;


        private WeakReference<RenderThread> mWeakRenderThread;


        public RenderHandler(RenderThread rt) {
            mWeakRenderThread = new WeakReference<>(rt);
        }


        public void sendSurfaceCreated() {
            sendMessage(obtainMessage(RenderHandler.MSG_SURFACE_CREATED));
        }

        public void sendSurfaceChanged(int width, int height) {
            sendMessage(obtainMessage(RenderHandler.MSG_SURFACE_CHANGED, width, height));
        }


        public void sendDoFrame(int textureId) {
            sendMessage(obtainMessage(RenderHandler.MSG_DO_FRAME, textureId, 0));
        }


        public void sendShutdown() {
            sendMessage(obtainMessage(RenderHandler.MSG_SHUTDOWN));
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_SURFACE_CREATED:
                    break;
                case MSG_SURFACE_CHANGED:
                    renderThread.surfaceChanged(msg.arg1, msg.arg2);
                    break;
                case MSG_DO_FRAME:
                    renderThread.draw(msg.arg1);
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }

}
