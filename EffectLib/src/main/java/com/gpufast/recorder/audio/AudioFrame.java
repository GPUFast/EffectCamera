package com.gpufast.recorder.audio;

public class AudioFrame {

    public byte[] buf;
    public int len;
    public long timeStamp;

    public AudioFrame(byte[] buf, int len, long timeStamp) {
        this.buf = buf;
        this.len = len;
        this.timeStamp = timeStamp;
    }

}
