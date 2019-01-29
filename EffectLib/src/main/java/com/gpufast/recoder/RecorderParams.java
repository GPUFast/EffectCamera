package com.gpufast.recoder;

public class RecorderParams {

    public enum SpeedType{
        hyperslow,slow,standard,fast,veryFast
    }

    //是否将音频一起录制进去
    private boolean isAuido = true;

    private int videoWidth;

    private int videoHeight;

    //视频总录制时长
    private int allTime;

    private SpeedType speedType;

    //背景音乐
    private String backgoundMuisc;
}
