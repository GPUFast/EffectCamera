package com.gpufast.recorder;

import android.opengl.EGLContext;

import com.gpufast.recorder.audio.AudioProcessor;

public interface IRecorder {

    /**
     * Set the video recording parameter
     *
     * @param params {@linkplain RecorderParams}
     */
    void setParams(RecorderParams params);

    /**
     * 设置EGL共享上下文
     *
     * @param shareContext
     */
    void setShareContext(EGLContext shareContext);


    /**
     * 传递图像数据信息
     *
     * @param textureId textureId
     * @param srcWidth  srcWidth
     * @param srcHeight srcHeight
     */
    void sendVideoFrame(int textureId, int srcWidth, int srcHeight);


    /**
     * 开始录制
     */
    void startRecorder();

    /**
     * 停止录制
     */
    void stopRecorder();


    /**
     * 是否正在录制
     *
     * @return true:正在录制
     */
    boolean isRecording();

    /**
     * 拼接视频
     */
    void jointVideo();

    void setRecorderListener(RecorderListener listener);

    void setAudioProcessor(AudioProcessor processor);

    void release();

    interface RecorderListener {

        void onRecorderStart();

        void onRecorderStop();
    }
}
