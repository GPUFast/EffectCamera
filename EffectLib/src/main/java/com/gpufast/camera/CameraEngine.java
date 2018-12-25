package com.gpufast.camera;

import android.os.Build;
import android.view.SurfaceView;

import com.gpufast.utils.LogUtils;

/**
 * @author Sivin 2018/10/26
 * Description:摄像头控制引擎
 */
public class CameraEngine {

    private static final String TAG = "CameraEngine";

    private static CameraEngine mInstance = null;

    private ICamera mICamera;

    //本地预览的surfaceView
    private SurfaceView mLocalPreview = null;

    private CameraEngine() {
        if(Build.VERSION.SDK_INT >Build.VERSION_CODES.LOLLIPOP){
            mICamera = new Camera19();
        }else{
            mICamera = new Camera19();
        }
    }

    public static CameraEngine getInstance() {
        if (mInstance == null) {
            synchronized (CameraEngine.class) {
                if (mInstance == null) {
                    mInstance = new CameraEngine();
                }
            }
        }
        return mInstance;
    }

    public void setLocalPreview(SurfaceView view){
        mLocalPreview = view;
    }

    public void openCamera(){
        mICamera.openFrontCamera();
    }

    /**
     * 开始预览摄像头数据
     */
    public void startPreview(){
        if(mLocalPreview == null){
            LogUtils.e(TAG,"localPreview == null");
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera(){

    }


    public void release(){

    }

}
