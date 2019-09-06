package com.gpufast.recorder;

import android.opengl.EGLContext;
import android.os.Handler;

import com.gpufast.recorder.audio.AudioProcessor;

public abstract class BaseWorker implements IRecorder {

    protected static abstract class BaseWorkerThread extends Thread implements IRecorder{

        @Override
        public void setShareContext(EGLContext shareContext) {
        }

        @Override
        public void sendVideoFrame(int textureId, int srcWidth, int srcHeight) {
        }

        @Override
        public void setRecorderListener(RecorderListener listener) {

        }
        @Override
        public void setAudioProcessor(AudioProcessor callback) {

        }



    }


    protected static abstract class BaseWorkerHandler extends Handler implements IRecorder{

        @Override
        public void setShareContext(EGLContext shareContext) {
        }

        @Override
        public void sendVideoFrame(int textureId, int srcWidth, int srcHeight) {
        }

        @Override
        public void setRecorderListener(RecorderListener listener) {
        }
        @Override
        public void setAudioProcessor(AudioProcessor callback) {
        }

    }

}
