package com.gpufast.recorder.muxer;

import android.media.MediaMuxer;

import com.gpufast.recorder.video.EncodedImage;
import com.gpufast.recorder.video.VideoEncoder;

/**
 * 视频合成接口
 */
public class Mp4Muxer implements VideoEncoder.VideoEncoderCallback {

    public MediaMuxer muxer;

    public Mp4Muxer() {

    }

    @Override
    public void onEncodedFrame(EncodedImage frame) {

    }
}
