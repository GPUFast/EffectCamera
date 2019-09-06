package com.gpufast.recorder;

public class RecorderParams {

    public enum SpeedType {
        hyperslow, slow, standard, fast, veryFast
    }

    /**
     * 录制视频的高度
     */
    private int videoWidth;
    /**
     * 录制视频的宽度
     */
    private int videoHeight;

    /**
     * 录制的总时长
     */
    private int allTime;

    /**
     *录制资源存放的路径
     */
    private String savePath;

    /**
     * 是否录制语音
     */
    private boolean enableAudio = true;

    /**
     * 是否录制视频
     */
    private boolean enableVideo = true;

    /**
     * 录制速度
     */
    private SpeedType speedType;

    /**
     * 背景音乐路径
     */
    private String backgroundMusicUrl;

    /**
     * 是否开启硬编码
     */
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

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isEnableAudio() {
        return enableAudio;
    }


    public void setEnableAudio(boolean enableAudio) {
        this.enableAudio = enableAudio;
    }

    public SpeedType getSpeedType() {
        return speedType;
    }

    public void setSpeedType(SpeedType speedType) {
        this.speedType = speedType;
    }

    public String getBackgroundMusicUrl() {
        return backgroundMusicUrl;
    }

    public void setBackgroundMusicUrl(String backgroundMusicUrl) {
        this.backgroundMusicUrl = backgroundMusicUrl;
    }

    public boolean isHwEncoder() {
        return hwEncoder;
    }

    public void setHwEncoder(boolean hwEncoder) {
        this.hwEncoder = hwEncoder;
    }

    public boolean isEnableVideo() {
        return enableVideo;
    }

    public void setEnableVideo(boolean enableVideo) {
        this.enableVideo = enableVideo;
    }
}