package com.gpufast.recorder.video;

import com.gpufast.recorder.video.encoder.VideoCodecStatus;

public interface VideoEncoder {

    class VideoSettings {
        //宽度高度
        public final int width;
        public final int height;
        //开始码率
        public final int startBitrate; // Kilobits per second.
        //帧率
        public final int maxFrameRate;

        public VideoSettings(int width, int height,
                             int startBitrate, int maxFrameRate) {
            this.width = width;
            this.height = height;
            this.startBitrate = startBitrate;
            this.maxFrameRate = maxFrameRate;
        }
    }

    interface VideoEncoderCallback {
        void onEncodedFrame(EncodedImage frame);
    }

    default boolean isHardwareEncoder() {
        return true;
    }

    //初始化编码器
    VideoCodecStatus initEncoder(VideoSettings settings, VideoEncoderCallback encodeCallback);

    VideoCodecStatus encode(VideoFrame frame);

    String getImplementationName();

    VideoCodecStatus release();


}
