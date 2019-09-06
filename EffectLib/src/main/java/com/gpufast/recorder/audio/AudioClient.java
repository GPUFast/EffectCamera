package com.gpufast.recorder.audio;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.gpufast.logger.ELog;
import com.gpufast.recorder.audio.encoder.AudioEncoder;

import java.lang.ref.WeakReference;

public class AudioClient implements AudioCollector.OnAudioFrameCallback {

    //音频编码线程
    private EncoderThread mEncoderThread;

    private EncoderHandler mEncoderHandler;

    //音频采集器
    private AudioCollector mAudioCollector;

    //音频预处理器
    private AudioProcessor mAudioPreprocessor;


    public AudioClient(AudioEncoder encoder,
                       AudioSetting settings,
                       AudioEncoder.AudioEncoderCallback callback) {

        if(encoder == null || settings == null) return;

        mEncoderThread = new EncoderThread(encoder,
                new AudioEncoder.Settings(settings.getSampleRate(),settings.getBitrate()),
                callback);

        mAudioCollector = new AudioCollector();
        mAudioCollector.init(new AudioCollector.Settings(settings.getSampleRate()),this);
    }


    public void start() {
        //启动音频编码线程
        mEncoderThread.start();
        mEncoderThread.waitUntilReady();
        //启动音频采集器
        mAudioCollector.start();
        mEncoderHandler = mEncoderThread.getHandler();
    }

    /**
     * 停止编码线程
     */
    public void stop() {
        if (mEncoderHandler != null) {
            mEncoderHandler.sendToStop();
        }
        if(mAudioCollector != null){
            mAudioCollector.stop();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        stop();
    }

    /**
     * 设置音频预处理器
     *
     * @param preprocessor 处理器对象
     */
    public void setAudioPreprocessor(AudioProcessor preprocessor) {
        mAudioPreprocessor = preprocessor;
    }

    @Override
    public void onReceiveAudioFrame(AudioFrame frame) {
        AudioFrame newFrame = null;
        if (mAudioPreprocessor != null) {
            newFrame = mAudioPreprocessor.onReceiveAudioFrame(frame);
        }
        if (newFrame != null) {
            frame = newFrame;
        }
        //编码音频
        mEncoderThread.getHandler().sendAudioFrame(frame);
    }


    private static class EncoderThread extends Thread {
        private static final String TAG = EncoderThread.class.getSimpleName();
        private final Object mStartLock = new Object();
        private boolean mReady = false;
        private EncoderHandler mEncoderHandler;


        private AudioEncoder mAudioEncoder;
        private AudioEncoder.Settings mSettings;

        AudioEncoder.AudioEncoderCallback mCallback;

        EncoderThread(AudioEncoder encoder, AudioEncoder.Settings settings,
                      AudioEncoder.AudioEncoderCallback callback) {
            super("audio_Encoder_Thread");
            mAudioEncoder = encoder;
            mSettings = settings;
            mCallback = callback;
        }

        boolean isReady() {
            return mReady;
        }

        EncoderHandler getHandler() {
            return mEncoderHandler;
        }

        @Override
        public void run() {
            Looper.prepare();
            mEncoderHandler = new EncoderHandler(this);
            initEncoder();
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();
            }
            Looper.loop();
            release();
        }

        void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException e) { /* not expected */ }
                }
            }
        }

        private void initEncoder() {
            if (mAudioEncoder != null) {
                mAudioEncoder.initEncoder(mSettings, mCallback);
            }
        }

        void sendAudioFrame(AudioFrame frame) {
            if (mAudioEncoder != null && mReady) {
                mAudioEncoder.encodePcm(frame);
            }
        }

        /**
         * 必须在当前线程调用
         */
        private void shutdown() {
            ELog.d(TAG, "shutdown");
            Looper.myLooper().quit();
        }

        private void release() {
            if (mAudioEncoder != null) {
                mAudioEncoder.release();
                mAudioEncoder = null;
            }
        }
    }


    private static class EncoderHandler extends Handler {

        private static final String TAG = EncoderHandler.class.getSimpleName();

        private static final int ON_FRAME_AVAILABLE = 0x001;
        private static final int ON_STOP = 0x002;

        private WeakReference<AudioClient.EncoderThread> mWeakEncoderThread;

        EncoderHandler(EncoderThread thread) {
            mWeakEncoderThread = new WeakReference<>(thread);
        }


        void sendAudioFrame(AudioFrame frame) {
            sendMessage(obtainMessage(ON_FRAME_AVAILABLE, frame));
        }

        private void sendToStop() {
            sendMessage(obtainMessage(ON_STOP));
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
                    encoderThread.sendAudioFrame((AudioFrame) msg.obj);
                    break;
                case ON_STOP:
                    encoderThread.shutdown();
                    break;
            }
        }
    }
}
