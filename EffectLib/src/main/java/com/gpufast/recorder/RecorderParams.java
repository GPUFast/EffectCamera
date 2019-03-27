package com.gpufast.recorder;

public class RecorderParams {

    public enum SpeedType {
        hyperslow, slow, standard, fast, veryFast
    }

    //录制视频的宽度和高度
    private int videoWidth;

    private int videoHeight;

    //视频总录制时长
    private int allTime;

    //视频保存的位置
    private String videoPath;

    //是否将音频一起录制进去
    private boolean hasAudio = true;

    private SpeedType speedType;

    //背景音乐
    private String backgroundMuiscUrl;

    private boolean hwEncoder = true;

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getAllTime() {
        return allTime;
    }

    public void setAllTime(int allTime) {
        this.allTime = allTime;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public SpeedType getSpeedType() {
        return speedType;
    }

    public void setSpeedType(SpeedType speedType) {
        this.speedType = speedType;
    }

    public String getBackgroundMuiscUrl() {
        return backgroundMuiscUrl;
    }

    public void setBackgroundMuiscUrl(String backgroundMuiscUrl) {
        this.backgroundMuiscUrl = backgroundMuiscUrl;
    }

    public boolean isHwEncoder() {
        return hwEncoder;
    }

    public void setHwEncoder(boolean hwEncoder) {
        this.hwEncoder = hwEncoder;
    }
}