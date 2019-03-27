package com.gpufast.recoder.video.encoder;

import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.Surface;

import java.nio.ByteBuffer;

interface MediaCodecWrapper {
  void configure(MediaFormat format, Surface surface, MediaCrypto crypto, int flags);

  void start();

  void flush();

  void stop();

  void release();

  int dequeueInputBuffer(long timeoutUs);

  void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags);

  int dequeueOutputBuffer(MediaCodec.BufferInfo info, long timeoutUs);

  void releaseOutputBuffer(int index, boolean render);

  MediaFormat getOutputFormat();

  ByteBuffer[] getInputBuffers();

  ByteBuffer[] getOutputBuffers();

  Surface createInputSurface();

  void setParameters(Bundle params);
}