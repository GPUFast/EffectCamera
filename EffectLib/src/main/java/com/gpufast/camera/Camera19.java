package com.gpufast.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

import java.io.IOException;
import java.util.List;

/**
 * @author Sivin 2018/10/26
 * Description:使用旧版本camera类API实现实现,
 * TODO：该类没有实现数据回调的接口，目前有待测试，是否数据回调是否与帧数据同时返回
 */
public class Camera19 implements ICamera {
    private Camera mCamera = null;
    private int mCameraFace;
    private boolean isPreviewing = false;
    private CameraParams mParams;

    @Override
    public void setCameraParams(CameraParams params) {
        mParams = params;
    }

    @Override
    public boolean openFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                mCameraFace = Camera.CameraInfo.CAMERA_FACING_FRONT;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean openBackCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                mCameraFace = Camera.CameraInfo.CAMERA_FACING_BACK;
                return true;
            }
        }
        return false;
    }

    @Override
    public void switchCamera() {
        if (mCamera == null) return;
        stopCamera();
        if (mCameraFace == Camera.CameraInfo.CAMERA_FACING_BACK) {
            openFrontCamera();//打开当前选中的摄像头
        } else {
            openBackCamera();//打开当前选中的摄像头
        }
        initCamera();
        startPreview();
    }


    @Override
    public void startPreview() {
        if (mCamera == null) return;
        if (isPreviewing) {
            stopPreview();
        }
        if (mParams == null || mParams.getPreTexture() == null) return;
        try {
            mCamera.setPreviewTexture(mParams.getPreTexture());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        isPreviewing = false;
    }


    private void initCamera() {
        if (mCamera == null) return;
        Camera.Parameters parameters = mCamera.getParameters();
        //获取支持的预览尺寸
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size preViewSize = CameraUtils.chooseOptimalSize(supportedPreviewSizes,
                mParams.getPreViewWidth(), mParams.getPreViewHeight(), mParams.getRatio().ofFloat());
        parameters.setPreviewSize(preViewSize.width, preViewSize.height);
        // 设置摄像头为自动聚焦
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.setPictureFormat(ImageFormat.NV21);
        if (mCameraFace == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCamera.setDisplayOrientation(270);
        } else if (mCameraFace == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCamera.setDisplayOrientation(90);
        }

    }

    @Override
    public void stopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }
}
