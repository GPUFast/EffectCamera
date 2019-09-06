package com.gpufast.recorder.audio;

public class AudioSetting {
    final int bitrate; // Kilobits per second.
    final int sampleRate;

    public AudioSetting(int sampleRate, int bitrate) {
        this.sampleRate = sampleRate;
        this.bitrate = bitrate;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getSampleRate() {
        return sampleRate;
    }
}