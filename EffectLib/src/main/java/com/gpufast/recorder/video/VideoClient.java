package com.gpufast.recorder.video;

import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.gpufast.utils.ELog;

import java.lang.ref.WeakReference;

public class VideoClient {

    private EncoderThread mEncoderThread;

    public VideoClient(VideoEncoder encoder,
                       VideoEncoder.VideoSettings settings,
                       VideoEncoder.VideoEncoderCallback callback) {
        mEncoderThread = new EncoderThread(encoder, settings, callback);
    }


    public void start() {
        mEncoderThread.start();
        mEncoderThread.waitUntilReady();
    }


    public void sendVideoFrame(int textureId, int srcWidth, int srcHeight, long timeStamp) {
        if (mEncoderThread != null && mEncoderThread.isReady()) {

            VideoFrame videoFrame = new VideoFrame(
                    new TextureBufferImpl(textureId, srcWidth,
                            srcHeight, VideoFrame.TextureBuffer.TextureType.RGB),
                    0,timeStamp);

            mEncoderThread.getHandler().sendVideoFrame(videoFrame);
        }
    }


    private static class EncoderThread extends Thread {
        private static final String TAG = EncoderThread.class.getSimpleName();
        private final Object mStartLock = new Object();
        private boolean mReady = false;
        private EncoderHandler mEncoderHandler;


        private VideoEncoder mVideoEncoder;
        private VideoEncoder.VideoSettings mSettings;
        VideoEncoder.VideoEncoderCallback mCallback;

        EncoderThread(VideoEncoder encoder, VideoEncoder.VideoSettings settings,
                      VideoEncoder.VideoEncoderCallback callback) {
            mVideoEncoder = encoder;
            mSettings = settings;
            mCallback = callback;
        }

        public boolean isReady() {
            return mReady;
        }

        public EncoderHandler getHandler() {
            return mEncoderHandler;
        }

        @Override
        public void run() {
            Looper.prepare();
            mEncoderHandler = new EncoderHandler(this);
            initEncoder();
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();  //通知调用线程，渲染线程准备工作完成
            }
            Looper.loop();
        }

        void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        private void initEncoder() {
            if (mVideoEncoder != null) {
                mVideoEncoder.initEncoder(mSettings, mCallback);
            }
        }

        public void sendVideoFrame(VideoFrame frame) {
            if (mVideoEncoder != null && mReady) {
                mVideoEncoder.encode(frame);
            }
        }

        /**
         * 必须在当前线程调用
         */
        private void shutdown() {
            Log.d(TAG, "shutdown");
            Looper.myLooper().quit();
        }
    }


    private static class EncoderHandler extends Handler {
        private static final String TAG = EncoderHandler.class.getSimpleName();
        public static final int ON_FRAME_AVAILABLE = 0x001;

        private WeakReference<VideoClient.EncoderThread> mWeakEncoderThread;

        EncoderHandler(VideoClient.EncoderThread thread) {
            mWeakEncoderThread = new WeakReference<>(thread);
        }


        public void sendVideoFrame(VideoFrame frame) {
            sendMessage(obtainMessage(ON_FRAME_AVAILABLE, frame));
        }


        @Override
        public void handleMessage(Message msg) {

            EncoderThread encoderThread = mWeakEncoderThread.get();
            if (encoderThread == null) {
                ELog.e(TAG, "mWeakEncoderThread.get() == null");
                return;
            }
            switch (msg.what) {
                case ON_FRAME_AVAILABLE:
                    encoderThread.sendVideoFrame((VideoFrame) msg.obj);
                    break;
            }
        }
    }


    private static class TextureBufferImpl implements VideoFrame.TextureBuffer {

        private int width;
        private int height;
        private int textureId;
        private TextureType type;

        public TextureBufferImpl(int textureId, int width, int height, TextureType type) {
            this.width = width;
            this.height = height;
            this.textureId = textureId;
            this.type = type;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public VideoFrame.I420Buffer toI420() {
            return null;
        }

        @Override
        public void release() {

        }

        @Override
        public VideoFrame.Buffer cropAndScale(int cropX, int cropY, int cropWidth, int cropHeight, int scaleWidth, int scaleHeight) {
            return null;
        }

        @Override
        public TextureType getType() {
            return type;
        }

        @Override
        public int getTextureId() {
            return textureId;
        }

        @Override
        public Matrix getTransformMatrix() {
            return null;
        }

    }

}