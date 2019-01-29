package com.gpufast.recoder;

import android.opengl.EGLContext;

import com.gpufast.render.Render;

public class EffectRecorderEngine implements Render.FrameCallback{
    private static final String TAG = "EffectRecorderEngine";
    private static EffectRecorderEngine mRecorderEngine = null;

    private RecorderParams params;


    private EffectRecorderEngine(){}

    public static EffectRecorderEngine getInstance() {
        if (mRecorderEngine == null) {
            synchronized (EffectRecorderEngine.class) {
                if (mRecorderEngine == null) {
                    mRecorderEngine = new EffectRecorderEngine();
                }
            }
        }
        return mRecorderEngine;
    }


    /**
     * 开始录制
     */
    public void startRecorder(){

    }

    /**
     * 结束录制
     */
    public void stopRecorder(){

    }


    /**
     * 多段合成视频
     */
    public void jointVideo(){

    }


    @Override
    public int onFrameCallback(EGLContext context, int textureId, int width, int height) {


        return 0;
    }

    @Override
    public void onEglContextDestroy() {


    }
}
