package com.gpufast.recorder;

import android.opengl.EGLContext;

import com.gpufast.logger.ELog;
import com.gpufast.recorder.audio.AudioProcessor;
import com.gpufast.recorder.muxer.MediaMuxer;
import com.gpufast.recorder.video.EncoderType;
import com.gpufast.recorder.video.VideoClient;
import com.gpufast.recorder.video.VideoEncoder;
import com.gpufast.recorder.video.VideoEncoderFactory;
import com.gpufast.recorder.video.encoder.VideoCodecInfo;

public abstract class BaseRecorder implements IRecorder {
    private static final String TAG = "BaseRecorder";

    //视频录制参数
    private RecorderParams recorderParams;

    //EGL上下文，使用openGL编码时需要
    private EGLContext shareContext;
    //视频编码器factory
    private VideoEncoderFactory videoEncoderFactory;
    //编码视频的设置
    private VideoEncoder.Settings videoSettings;

    //视频编码客户端
    private VideoClient mVideoClient;

    //封装器
    private MediaMuxer mMuxer;

    protected AudioProcessor mAudioFrameCallback;


    private void initRecorder() {
        if (recorderParams.isHwEncoder()) {
            videoEncoderFactory = EncoderFactory.getVideoEncoderFactory(EncoderType.HW_VIDEO_ENCODER);
        } else {
            videoEncoderFactory = EncoderFactory.getVideoEncoderFactory(EncoderType.HW_VIDEO_ENCODER);
        }

        if (videoEncoderFactory == null) {
            ELog.e(TAG, "can't find a available video encoder factory");
            return;
        }

        //设置EGL上下文
        if (shareContext != null) {
            videoEncoderFactory.setShareContext(shareContext);
        }

        VideoCodecInfo videoCodecInfo = null;
        //配置视频编码器信息
        VideoCodecInfo[] supportedCodecs = videoEncoderFactory.getSupportedCodecs();
        //设置视频编码器信息
        if (supportedCodecs != null && supportedCodecs.length > 0) {
            videoCodecInfo = supportedCodecs[0];
            ELog.d(TAG, "find a codec :" + videoCodecInfo.name);
        } else {
            ELog.e(TAG, "don't find a available codec :");
        }
    }

    @Override
    public void setAudioProcessor(AudioProcessor callback) {
        mAudioFrameCallback = callback;
    }

}
