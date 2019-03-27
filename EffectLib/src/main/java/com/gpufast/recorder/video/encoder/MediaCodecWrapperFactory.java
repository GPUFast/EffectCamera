package com.gpufast.recorder.video.encoder;


import java.io.IOException;

interface MediaCodecWrapperFactory {
  MediaCodecWrapper createByCodecName(String name) throws IOException;
}