package com.gpufast.recorder;

import android.opengl.EGLContext;

import com.gpufast.logger.ELog;
import com.gpufast.recorder.audio.AudioClient;
import com.gpufast.recorder.muxer.IMediaMuxer;
import com.gpufast.recorder.muxer.MediaMuxerFactory;
import com.gpufast.recorder.muxer.MuxerType;
import com.gpufast.recorder.video.VideoClient;

public class EffectRecorder extends BaseRecorder {
    private static final String TAG = EffectRecorder.class.getSimpleName();

    private volatile boolean isStartingRecorder = false;

    private volatile boolean recorderStarted = false;

    private IMediaMuxer mMediaMuxer;

    private VideoClient mVideoClient;

    private AudioClient mAudioClient;

    private RecorderListener mRecorderListener;

    private RecordParams mRecordParams;

    EffectRecorder() {
    }

    @Override
    public void setParams(RecordParams params) {
        if (params == null) {
            return;
        }
        if (params.isEnableVideo()) {
            initVideoRecorder(params);
        }
        if (params.isEnableAudio()) {
            initAudioRecorder(params);
        }
        mRecordParams = params;
    }

    @Override
    public void setShareContext(EGLContext shareContext) {
        setEGLShareContext(shareContext);
    }

    @Override
    public boolean isRecording() {
        return recorderStarted;
    }

    @Override
    public void startRecorder() {
        if (isStartingRecorder || recorderStarted) {
            return;
        }
        ELog.i(TAG, "startRecorder");
        isStartingRecorder = true;

        mMediaMuxer = MediaMuxerFactory.createMediaMuxer(mRecordParams, MuxerType.MP4);

        mVideoClient = createVideoClient(mMediaMuxer);

        if (mVideoClient != null) {
            mVideoClient.start();
        }

        mAudioClient = createAudioClient(mMediaMuxer);
        if (mAudioClient != null) {
            mAudioClient.start();
        }

        if (mRecorderListener != null) {
            mRecorderListener.onRecorderStart();
        }
        recorderStarted = true;
    }


    @Override
    public void sendVideoFrame(int textureId, int srcWidth, int srcHeight) {
        if (mVideoClient != null && recorderStarted) {
            mVideoClient.sendVideoFrame(textureId, srcWidth, srcHeight);
        }
    }


    @Override
    public void stopRecorder() {
        ELog.i(TAG, "stop recorder");
        if (mVideoClient != null) {
            mVideoClient.stop();
        }

        if (mAudioClient != null) {
            mAudioClient.stop();
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.release();
            mMediaMuxer = null;
        }

        isStartingRecorder = false;
        recorderStarted = false;

        if (mRecorderListener != null) {
            mRecorderListener.onRecorderStop();
        }
    }


    @Override
    public void jointVideo() {

    }


    @Override
    public void setRecorderListener(RecorderListener listener) {
        this.mRecorderListener = listener;
    }


    @Override
    public void release() {

        if (mAudioClient != null) {
            mAudioClient.release();
            mVideoClient = null;
        }
    }


}
