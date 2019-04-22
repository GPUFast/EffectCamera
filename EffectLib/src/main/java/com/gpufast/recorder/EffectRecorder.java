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
    private static final String TAG = EffectRecorder.class.getSimpleName();

    private volatile boolean recorderStarted = false;

    private EGLContext shareContext;

    private VideoEncoderFactory videoEncoderFactory;
    private VideoCodecInfo videoCodecInfo;
    private VideoEncoder.VideoSettings videoSettings;
    private VideoClient mVideoClient;
    private Mp4Muxer mMp4Muxer;

    //开始码率
    public final int startBitrate = 4000; // Kilobits per second.
    //帧率
    public final int maxFrameRate = 30;


    EffectRecorder() {}


    @Override
    public void setParams(RecorderParams params) {

        if (params == null) return;

        videoSettings = new VideoEncoder.VideoSettings(params.getVideoWidth(),
                params.getVideoHeight(), startBitrate, maxFrameRate);

        if (params.isHwEncoder()) {
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
                ELog.e(TAG, "can't find a available codec :");
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
        return recorderStarted;
    }

    @Override
    public void startRecorder() {
        if (recorderStarted) return;
        recorderStarted = true;
        ELog.d(TAG, "videoEncoderFactory != null ?--->" + (videoEncoderFactory != null));
        if (videoEncoderFactory != null) {
            VideoEncoder videoEncoder = videoEncoderFactory.createEncoder(videoCodecInfo);
            if (videoEncoder == null) {
                ELog.e(TAG, "can't create video encoder");
                return;
            }
            mMp4Muxer = new Mp4Muxer();
            mVideoClient = new VideoClient(videoEncoder, videoSettings, mMp4Muxer);
            mVideoClient.start();
        }
    }


    @Override
    public void stitchVideo() {

    }


    @Override
    public void sendVideoFrame(int textureId, int srcWidth, int srcHeight, long timeStamp) {
        if (mVideoClient != null && recorderStarted) {
            mVideoClient.sendVideoFrame(textureId, srcWidth, srcHeight, timeStamp);
        }
    }

    @Override
    public int getFps() {
        return maxFrameRate;
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
