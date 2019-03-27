package com.gpufast.recorder;

import android.opengl.EGLContext;

import com.gpufast.recorder.muxer.Mp4Muxer;
import com.gpufast.recorder.video.EncoderType;
import com.gpufast.recorder.video.VideoClient;
import com.gpufast.recorder.video.VideoEncoder;
import com.gpufast.recorder.video.VideoEncoderFactory;
import com.gpufast.recorder.video.encoder.VideoCodecInfo;
import com.gpufast.utils.ELog;

public class EffectRecorder implements IRecorder {
    private static final String TAG = "EffectRecorder";

    private boolean startRecorder = false;

    private EGLContext shareContext;

    private VideoEncoderFactory videoEncoderFactory;
    private VideoCodecInfo videoCodecInfo;
    private VideoEncoder.VideoSettings videoSettings;
    private VideoClient mVideoClient;

    private RecorderParams recorderParams;

    private Mp4Muxer mMp4Muxer;


    EffectRecorder() {
    }


    @Override
    public void setParams(RecorderParams params) {
        recorderParams = params;
        if (recorderParams.isHwEncoder()) {
            videoEncoderFactory = EncoderFactory.getVideoEncoderFactory(EncoderType.HW_VIDEO_ENCODER);
        }
        if (videoEncoderFactory != null) {

            if (shareContext != null) {
                videoEncoderFactory.setShareContext(shareContext);
            }

            VideoCodecInfo[] supportedCodecs = videoEncoderFactory.getSupportedCodecs();
            if (supportedCodecs != null && supportedCodecs.length > 0) {
                videoCodecInfo = supportedCodecs[0];
                ELog.d(TAG, "find a codec :" + videoCodecInfo.name);
            } else {
                ELog.e(TAG, "don't find a available codec :");
            }
        }
    }

    @Override
    public void setShareContext(EGLContext shareContext) {
        this.shareContext = shareContext;
        if (videoEncoderFactory != null) {
            videoEncoderFactory.setShareContext(shareContext);
        }
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public void startRecorder() {
        if (videoEncoderFactory != null) {
            VideoEncoder videoEncoder = videoEncoderFactory.createEncoder(videoCodecInfo);
            if (videoEncoder == null) {
                ELog.e(TAG, "can't create video encoder");
                return;
            }
            mMp4Muxer = new Mp4Muxer();
            mVideoClient = new VideoClient(videoEncoder,videoSettings,mMp4Muxer);
            mVideoClient.start();


        }
    }


    @Override
    public void jointVideo() {

    }


    @Override
    public void sendVideoFrame(int textureId, int srcWidth, int srcHeight, long timeStamp) {
        mVideoClient.sendVideoFrame(textureId,srcWidth,srcHeight,timeStamp);
    }

    @Override
    public int getFps() {
        return 0;
    }


    @Override
    public void stopRecorder() {

    }


    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

}
