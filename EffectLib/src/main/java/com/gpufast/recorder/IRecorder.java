package com.gpufast.recorder;

import android.opengl.EGLContext;

public interface IRecorder {

    void setParams(RecorderParams params);

    void setShareContext(EGLContext shareContext);

    boolean isRecording();

    void startRecorder();

    //合成多段mp4
    void jointVideo();

    void sendVideoFrame(int textureId, int srcWidth, int srcHeight, long timeStamp);

    int getFps();


    void stopRecorder();

    void stop();

    void release();
}
