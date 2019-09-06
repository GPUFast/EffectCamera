package com.gpufast.recorder;


import android.opengl.EGLContext;
import android.os.Looper;

import com.gpufast.recorder.audio.AudioProcessor;

class Worker extends BaseWorker {

    private IRecorder recorder;
    private WorkerHandler mWorkHandler;

    Worker() {
        WorkerThread workerThread = new WorkerThread();
        workerThread.start();
        workerThread.waitUntilReady();
        recorder = workerThread.getRecorder();
        mWorkHandler = workerThread.getHandler();
    }

    @Override
    public void setParams(RecorderParams params) {
        if (mWorkHandler != null) {
            mWorkHandler.setParams(params);
        }
    }

    @Override
    public void setShareContext(EGLContext shareContext) {
        //不用转发到同一个线程
        if (recorder != null) {
            recorder.setShareContext(shareContext);
        }
    }

    @Override
    public void sendVideoFrame(int textureId, int srcWidth, int srcHeight) {
        //不用转发到同一个线程
        if (recorder != null) {
            recorder.sendVideoFrame(textureId, srcWidth, srcHeight);
        }
    }


    @Override
    public void startRecorder() {
        if (mWorkHandler != null) {
            mWorkHandler.startRecorder();
        }
    }

    @Override
    public void stopRecorder() {
        if (mWorkHandler != null) {
            mWorkHandler.stopRecorder();
        }
    }

    @Override
    public boolean isRecording() {
        return mWorkHandler != null && mWorkHandler.isRecording();
    }

    @Override
    public void jointVideo() {
        if (mWorkHandler != null) {
            mWorkHandler.jointVideo();
        }
    }

    @Override
    public void setRecorderListener(RecorderListener listener) {
        //不需要做线程同步
        if (recorder != null) {
            recorder.setRecorderListener(listener);
        }
    }

    @Override
    public void setAudioProcessor(AudioProcessor processor) {
        if (recorder != null) {
            recorder.setAudioProcessor(processor);
        }
    }

    @Override
    public void release() {
        if (mWorkHandler != null) {
            mWorkHandler.release();
        }
        mWorkHandler = null;
        recorder = null;
    }

    private static class WorkerThread extends BaseWorkerThread {
        private static final String TAG = WorkerThread.class.getSimpleName();
        private final Object mStartLock = new Object();
        private boolean mReady = false;

        private WorkerHandler mHandler;
        private IRecorder mRecorder;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new WorkerHandler(this);
            mRecorder = new EffectRecorder();
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();
            }
            Looper.loop();
            release();

        }

        @Override
        public void setParams(RecorderParams params) {

        }

        @Override
        public void startRecorder() {

        }

        @Override
        public void stopRecorder() {

        }

        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public void jointVideo() {

        }

        @Override
        public void release() {

        }

        void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        WorkerHandler getHandler() {
            return mHandler;
        }

        public IRecorder getRecorder() {
            return mRecorder;
        }
    }


    private static class WorkerHandler extends BaseWorkerHandler {

        WorkerHandler(WorkerThread thread) {

        }

        @Override
        public void setParams(RecorderParams params) {

        }

        @Override
        public void startRecorder() {

        }

        @Override
        public void stopRecorder() {

        }

        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public void jointVideo() {

        }

        @Override
        public void release() {

        }
    }
}
