package com.gpufast.recorder;

public class RecorderFactory {

    private static IRecorder recorder;

    public static IRecorder getRecorderInstance() {
        if (recorder == null) {
            recorder = new EffectRecorder();
        }
        return recorder;
    }

}
