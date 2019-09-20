package com.gpufast.recorder.video.encoder;

import com.gpufast.recorder.video.VideoEncoder;
import com.gpufast.recorder.video.VideoFrame;

public class X264VideoEncoder implements VideoEncoder {
    @Override
    public VideoCodecStatus init(Settings settings, VideoEncoderCallback encodeCallback) {
        return null;
    }

    @Override
    public VideoCodecStatus encode(VideoFrame frame) {
        return null;
    }

    @Override
    public String getImplementationName() {
        return null;
    }

    @Override
    public VideoCodecStatus deInit() {
        return null;
    }

    public native int nativeInit(Settings settings, VideoEncoderCallback encodeCallback);

}
