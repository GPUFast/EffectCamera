package com.gpufast.recorder.muxer;

import com.gpufast.recorder.RecordParams;

public class Mp4MuxerCreator {

    public static Mp4Muxer create(RecordParams params) {
        IMediaMuxer.Setting muxerSetting = createMuxerSetting(params);
        return new Mp4Muxer(muxerSetting);
    }


    private static IMediaMuxer.Setting createMuxerSetting(RecordParams params) {
        if (params == null) return null;
        String savePath = params.getSavePath();
        boolean enableVideo = params.isEnableVideo();
        boolean enableAudio = params.isEnableAudio();
        return new IMediaMuxer.Setting(savePath, enableVideo, enableAudio);
    }

}
