package com.gpufast.recorder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.gpufast.logger.ELog;

public class AudioCollector {
    private static final String TAG = "AudioCollector";

   static class Settings {
        private final int sampleRate;
        Settings(int sampleRate) {
            this.sampleRate = sampleRate;
        }
    }

    private AudioRecord mAudioRecord;

    private AudioCollectThread collectThread;

    private OnAudioFrameCallback callback;


    public void init(Settings settings, OnAudioFrameCallback callback) {
        int minBufferSize = AudioRecord.getMinBufferSize(
                settings.sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            ELog.e(TAG, "AudioRecord.getMinBufferSize failed: " + minBufferSize);
        }

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, settings.sampleRate,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);


        this.callback = callback;
    }

    public void start() {
        collectThread = new AudioCollectThread(callback);
        collectThread.start();
        //检测等待录音器是否启动录制
        collectThread.waitUntilReady();
    }


    public void stop() {
        if (collectThread != null) {
            collectThread.stopThread();
            collectThread = null;
        }
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
    }

    public void release() {
        if (collectThread != null) {
            collectThread.stopThread();
            collectThread = null;
        }
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
    }

    /**
     * 音频采集线程
     */
    class AudioCollectThread extends Thread {
        private volatile boolean keepAlive = true;

        private final Object mStartLock = new Object();
        private boolean mReady = false;

        private OnAudioFrameCallback callback;

        AudioCollectThread(OnAudioFrameCallback callback) {
            super("AudioCollectThread");
            this.callback = callback;
        }

        @Override
        public void run() {
            //没有数据接收的地方，采集无意义
            if (callback == null || mAudioRecord == null) return;
            long start = System.currentTimeMillis();
            //等待检测录音器启动录制是否成功
            mAudioRecord.startRecording();
            while (keepAlive) {
                int state = mAudioRecord.getRecordingState();
                if (state == AudioRecord.RECORDSTATE_STOPPED) {
                    if (System.currentTimeMillis() - start < 500) {
                        yield();
                    } else {
                        //如果500毫秒内录音器未初始化成功，则放弃录音
                        synchronized (mStartLock) {
                            mReady = true;
                            mStartLock.notify();  //释放等待线程
                        }
                        //TODO:报告录音器开启失败
                        ELog.e(TAG, "audio AudioCollectThread start failed.");
                        return;
                    }
                } else {
                    break;
                }
            }
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();  //通知调用线程,准备工作已完成
            }
            byte[] buffer = new byte[1024];
            while (keepAlive) {
                int num = mAudioRecord.read(buffer, 0, buffer.length);
                if (num > 0) {
                    callback.onReceiveAudioFrame(new AudioFrame(buffer, num, System.nanoTime() - start));
                }
            }
        }

        void stopThread() {
            keepAlive = false;
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
    }

    public interface OnAudioFrameCallback {
        void onReceiveAudioFrame(AudioFrame frame);
    }


}
