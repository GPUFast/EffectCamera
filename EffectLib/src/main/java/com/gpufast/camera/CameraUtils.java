package com.gpufast.camera;


import android.hardware.Camera;
import android.util.Log;

import com.gpufast.utils.ELog;

import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sivin 2018/3/24
 * @Description
 */
class CameraUtils {

    private static final String TAG = "CameraUtils";

    /**
     * 根据要显示的宽度和告诉，找一个大于改尺寸最小组合。
     * 如果发现尺寸对不上，就进行裁剪。
     *
     * @param list         相机支持的尺寸列表
     * @param targetWidth  预览视频view的宽度
     * @param targetHeight 预览视频的高度
     * @return Camera.Size
     */
    public static Camera.Size chooseOptimalSize(List<Camera.Size> list, int targetWidth, int targetHeight) {
        Collections.sort(list, new CameraSizeComparator());
        float rate = targetWidth * 1.0f / targetHeight;
        Camera.Size resultSize = null;
        for (Camera.Size size : list) {
            if ((size.height >= targetWidth) && equalRate(size, rate)) {
                resultSize = size;
                break;
            }
        }
        if(resultSize == null){
            throw new RuntimeException("can not find target size :targetWidth ="+targetWidth);
        }else{
            ELog.i(TAG,"find targetSize :width="+resultSize.height +" height="+resultSize.width);
        }

        return resultSize;
    }


    /**
     * 从小到大排序
     */
    static class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    private static boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.height) / (float) (s.width);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

}
