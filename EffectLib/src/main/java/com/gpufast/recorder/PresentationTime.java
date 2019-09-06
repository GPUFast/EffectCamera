package com.gpufast.recorder;

public class PresentationTime {

    private int fps;
    public long presentationTimeNs;
    private long timestamp;

    public PresentationTime(int fps) {
        this.fps = fps;
        this.presentationTimeNs = 0L;
        this.timestamp = 0L;
    }

    public void start() {
        timestamp = 0;
    }

    public void record() {
        long timeTmp = System.nanoTime();
        if (0L != timestamp) {
            presentationTimeNs += (timeTmp - timestamp);
        } else {
            presentationTimeNs += (1000000000L / fps);
        }
        timestamp = timeTmp;
    }


   public void reset() {
       presentationTimeNs = 0;
        timestamp = 0;
    }
}
