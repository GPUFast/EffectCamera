package com.gpufast.recorder.muxer;

import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.gpufast.logger.ELog;
import com.gpufast.recorder.audio.EncodedAudio;
import com.gpufast.recorder.video.EncodedImage;

import java.io.IOException;

/**
 * 视频合成接口
 */
public class Mp4Muxer extends IMediaMuxer {
    private static final String TAG = Mp4Muxer.class.getSimpleName();
    private MediaMuxer mMediaMuxer;
    private int audioTrackIndex = -1;
    private int videoTrackIndex = -1;

    private boolean mediaMuxerStarted = false;
    private boolean videoTrackHasReady = false;
    private boolean audioTrackHasRead = false;
    private boolean hasRelease = false;

    private boolean enableVideo;
    private boolean enableAudio;

    Mp4Muxer(Setting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("setting is null object");
        }
        enableAudio = setting.enableAudio;
        enableVideo = setting.enableVideo;
        try {
            mMediaMuxer = new MediaMuxer(setting.savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            ELog.e(TAG, "Init IMediaMuxer:" + e.getMessage());
        }
    }

    @Override
    public void onUpdateVideoMediaFormat(MediaFormat format) {
        if (!enableVideo || videoTrackHasReady || hasRelease) return;
        ELog.i(TAG, "onUpdateVideoMediaFormat:" + format);
        videoTrackIndex = mMediaMuxer.addTrack(format);
        if (videoTrackIndex < 0) {
            ELog.e(TAG, "Add video track failed");
            return;
        }
        videoTrackHasReady = true;
        ELog.i(TAG, "video track has ready");
    }

    @Override
    public void onUpdateAudioMediaFormat(MediaFormat mediaFormat) {
        if (!enableAudio || audioTrackHasRead || hasRelease) return;
        ELog.i(TAG, "onUpdateAudioMediaFormat");
        audioTrackIndex = mMediaMuxer.addTrack(mediaFormat);
        if (audioTrackIndex < 0) {
            ELog.e(TAG, "Add audio track failed");
            return;
        }
        audioTrackHasRead = true;
    }

    @Override
    public void onEncodedFrame(EncodedImage frame) {
        if ((videoTrackHasReady && !enableAudio) || (videoTrackHasReady && audioTrackHasRead)) {
            start();
            mMediaMuxer.writeSampleData(videoTrackIndex, frame.buffer, frame.bufferInfo);
            ELog.i(TAG, "Write video data ，time=" + frame.bufferInfo.presentationTimeUs);
        }
    }


    @Override
    public void onEncodedAudio(EncodedAudio frame) {
        if ((audioTrackHasRead && !enableVideo) || (audioTrackHasRead && videoTrackHasReady)) {
            start();
            mMediaMuxer.writeSampleData(audioTrackIndex, frame.mBuffer, frame.mBufferInfo);
            ELog.i(TAG, "Write audio data，time=" + frame.mBufferInfo.presentationTimeUs);
        }
    }

    private void start() {
        if (mediaMuxerStarted) return;
        synchronized (Mp4Muxer.class) {
            if (!mediaMuxerStarted) {
                mediaMuxerStarted = true;
                mMediaMuxer.start();
            }
        }
    }

    public void release() {
        ELog.i(TAG, "Release IMediaMuxer");
        try {
            if (mMediaMuxer != null) {
                mMediaMuxer.release();
            }
        } catch (Exception e) {
            ELog.e(TAG, "Release IMediaMuxer:" + e.getMessage());
        }
        hasRelease = true;
        videoTrackIndex = -1;
        audioTrackIndex = -1;
        videoTrackHasReady = false;
        audioTrackHasRead = false;
    }

}
