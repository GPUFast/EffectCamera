package com.gpufast.recorder.video;

public class VideoClient {


    public VideoClient(VideoEncoder encoder,
                       VideoEncoder.VideoSettings settings,
                       VideoEncoder.VideoEncoderCallback callback) {

    }

    public void start(){

    }

    public void sendVideoFrame(int textureId, int srcWidth, int srcHeight, long timeStamp) {


    }


    private static class EncoderThread extends Thread {

        private VideoEncoder mEncoder;


    }

}
