package com.gpufast.recorder.muxer;

import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.gpufast.logger.ELog;
import com.gpufast.recorder.audio.EncodedAudio;
import com.gpufast.recorder.audio.encoder.AudioEncoder;
import com.gpufast.recorder.video.EncodedImage;
import com.gpufast.recorder.video.VideoEncoder;

import java.io.IOException;

/**
 * 视频合成接口
 */
public class Mp4Muxer implements VideoEncoder.VideoEncoderCallback, AudioEncoder.AudioEncoderCallback {
    private static final String TAG = Mp4Muxer.class.getSimpleName();
    private MediaMuxer mMediaMuxer;
    private int audioTrackIndex = -1;
    private int videoTrackIndex = -1;

    private boolean mediaMuxerStarted = false;
    private boolean videoTrackHasReady = false;
    private boolean audioTrackHasRead = false;

    private boolean hasRelease = false;

    public Mp4Muxer(String outputPath) {
        try {
            mMediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            ELog.e(TAG, "Init MediaMuxer:" + e.getMessage());
        }
    }

    @Override
    public void onUpdateVideoMediaFormat(MediaFormat format) {
        if (videoTrackHasReady || hasRelease) return;
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
        if (audioTrackHasRead || hasRelease) return;
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
        if (videoTrackHasReady) {
            start();
            mMediaMuxer.writeSampleData(videoTrackIndex, frame.buffer, frame.bufferInfo);
            ELog.i(TAG, "Write video data ，time=" + frame.bufferInfo.presentationTimeUs);
        }
    }


    @Override
    public void onEncodedAudio(EncodedAudio frame) {
        if (videoTrackHasReady && audioTrackHasRead) {
            start();
            mMediaMuxer.writeSampleData(audioTrackIndex, frame.mBuffer, frame.mBufferInfo);
            ELog.i(TAG, "Write audio data，time=" + frame.mBufferInfo.presentationTimeUs);
        }
    }

    private void start() {
        if (mediaMuxerStarted) {
            return;
        }
        synchronized (Mp4Muxer.class) {
            if (!mediaMuxerStarted) {
                mediaMuxerStarted = true;
                mMediaMuxer.start();
            }
        }
    }

    public void release() {
        ELog.i(TAG, "Release MediaMuxer");
        try {
            if (mMediaMuxer != null) {
                mMediaMuxer.release();
            }
        } catch (Exception e) {
            ELog.e(TAG, "Release MediaMuxer:" + e.getMessage());
        }
        hasRelease = true;
        videoTrackIndex = -1;
        audioTrackIndex = -1;
        videoTrackHasReady = false;
        audioTrackHasRead = false;
    }

}
