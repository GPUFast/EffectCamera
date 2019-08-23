package com.gpufast.recorder.audio.encoder;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.gpufast.logger.ELog;
import com.gpufast.recorder.audio.AudioFrame;
import com.gpufast.recorder.audio.EncodedAudio;
import com.gpufast.recorder.hardware.MediaCodecWrapper;
import com.gpufast.recorder.hardware.MediaCodecWrapperFactory;
import com.gpufast.utils.ThreadUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频硬编码器
 */
public class HwAudioEncoder implements AudioEncoder {

    private static final String TAG = "HwAudioEncoder";

    private MediaCodecWrapperFactory mediaCodecFactory;

    /**
     * 编码器
     */
    private MediaCodecWrapper codec;

    /**
     * 编码器名称
     */
    private final String codecName;

    /**
     * 音频编码类型
     */
    private AudioCodecType codecType;


    private MediaCodec.BufferInfo mBufferInfo;

    /**
     * 音频编码回到
     */
    private AudioEncoderCallback encoderCallback;
    /**
     * 声道数
     */
    private static final int CHANNEL = 2;

    private ThreadUtils.ThreadChecker checker;

    HwAudioEncoder(MediaCodecWrapperFactory mediaCodecFactory,
                   AudioCodecType codecType, String codecName) {
        this.mediaCodecFactory = mediaCodecFactory;
        this.codecType = codecType;
        this.codecName = codecName;
        checker = new ThreadUtils.ThreadChecker();
        checker.detachThread();
    }


    @Override
    public AudioCodecStatus initEncoder(AudioEncoder.Settings settings, AudioEncoderCallback callback) {
        checker.checkIsOnValidThread();

        if (settings == null) {
            return AudioCodecStatus.ERR_PARAMETER;
        }

        encoderCallback = callback;
        try {
            codec = mediaCodecFactory.createByCodecName(codecName);
        } catch (IOException | IllegalArgumentException e) {
            ELog.e(TAG, "Cannot create media encoder " + codecName);
            return AudioCodecStatus.FALLBACK_SOFTWARE;
        }


        ELog.e(TAG, "initEncoder bitrate = " + settings.bitrate + " sample rate = " + settings.sampleRate +
                " mimeType = " + codecType.mimeType());

        try {
            MediaFormat format = new MediaFormat();
            format.setInteger(MediaFormat.KEY_BIT_RATE, settings.bitrate);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, settings.sampleRate);
            format.setString(MediaFormat.KEY_MIME, codecType.mimeType());
            format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024);

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            if (encoderCallback != null) {
                MediaFormat outputFormat = codec.getOutputFormat();
                encoderCallback.onUpdateAudioFormat(outputFormat);
            }
            codec.start();

        } catch (IllegalStateException e) {
            ELog.e(TAG, "initEncoder failed:" + e.getLocalizedMessage());
            release();
            return AudioCodecStatus.FALLBACK_SOFTWARE;
        }
        mBufferInfo = new MediaCodec.BufferInfo();
        return AudioCodecStatus.OK;
    }

    @Override
    public void encodePcm(AudioFrame frame) {

        checker.checkIsOnValidThread();

        int inputBufferIndex = codec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
            if (inputBuffer == null) {
                ELog.e(TAG, "dequeue input buffer is null. inputBufferIndex = " + inputBufferIndex);
                return;
            }
            inputBuffer.clear();
            inputBuffer.put(frame.buf);
            inputBuffer.limit(frame.len);
            codec.queueInputBuffer(
                    inputBufferIndex, 0, frame.len, frame.timeStamp, 0);
        }

        int outputBufferIndex = codec.dequeueOutputBuffer(mBufferInfo, 0);

        while (outputBufferIndex >= 0) {

            ByteBuffer outputData = codec.getOutputBuffer(outputBufferIndex);

            EncodedAudio encodedAudio = new EncodedAudio.Builder()
                    .setBuffer(outputData)
                    .setBufferInfo(mBufferInfo)
                    .createEncodedAudio();
            if (encoderCallback != null) {
                encoderCallback.onEncodedAudio(encodedAudio);
            }

            codec.releaseOutputBuffer(outputBufferIndex, frame.timeStamp);
            outputBufferIndex = codec.dequeueOutputBuffer(mBufferInfo, 0);
        }
    }


    @Override
    public void release() {
        checker.checkIsOnValidThread();

        if (codec != null) {
            codec.stop();
        }
        if (codec != null) {
            codec.release();
        }
        checker.detachThread();
    }
}