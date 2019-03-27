package com.gpufast.render;

public class PresentationTime {

    private int fps;
    long presentationTimeUs;
    private long timestamp;

    public PresentationTime(int fps) {
        this.fps = fps;
        this.presentationTimeUs = 0L;
        this.timestamp = 0L;
    }

    void start() {
        timestamp = 0;
    }

    void record() {
        long timeTmp = System.nanoTime();
        if (0L != timestamp) {
            presentationTimeUs += (timeTmp - timestamp);
        } else {
            presentationTimeUs += (1000000000L / fps);
        }
        timestamp = timeTmp;
    }


    void reset() {
        presentationTimeUs = 0;
        timestamp = 0;
    }
}
