
package com.gpufast.recorder.video.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;

/**
 * Container class for static constants and helpers used with MediaCodec.
 */
class MediaCodecUtils {
    private static final String TAG = "MediaCodecUtils";

    //支持的Encoder/decoder 组件名字前缀
    static final String EXYNOS_PREFIX = "OMX.Exynos."; //三星猎户座
    static final String INTEL_PREFIX = "OMX.Intel.";   //Intel
    static final String NVIDIA_PREFIX = "OMX.Nvidia.";
    static final String QCOM_PREFIX = "OMX.qcom."; //高通

    //软件实现的编解码器
    static final String[] SOFTWARE_IMPLEMENTATION_PREFIXES = {"OMX.google.", "OMX.SEC."};

    // NV12 color format supported by QCOM codec, but not declared in MediaCodec -
    // 高通处理器支持的NV12的颜色格式，但是MediaCodec中没有声明
    // see /hardware/qcom/media/mm-core/inc/OMX_QCOMExtns.h
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar32m4ka = 0x7FA30C01;
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar16m4ka = 0x7FA30C02;
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar64x32Tile2m8ka = 0x7FA30C03;
    static final int COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m = 0x7FA30C04;

    // Color formats supported by hardware decoder - in order of preference.
    static final int[] DECODER_COLOR_FORMATS = new int[]{
            CodecCapabilities.COLOR_FormatYUV420Planar,
            CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
            MediaCodecUtils.COLOR_QCOM_FORMATYVU420PackedSemiPlanar32m4ka,
            MediaCodecUtils.COLOR_QCOM_FORMATYVU420PackedSemiPlanar16m4ka,
            MediaCodecUtils.COLOR_QCOM_FORMATYVU420PackedSemiPlanar64x32Tile2m8ka,
            MediaCodecUtils.COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m};

    // Color formats supported by hardware encoder - in order of preference.
    static final int[] ENCODER_COLOR_FORMATS = {
            CodecCapabilities.COLOR_FormatYUV420Planar,
            CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
            CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
            MediaCodecUtils.COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m};

    // Color formats supported by texture mode encoding - in order of preference.
    static final int[] TEXTURE_COLOR_FORMATS = getTextureColorFormats();

    private static int[] getTextureColorFormats() {
        return new int[]{CodecCapabilities.COLOR_FormatSurface};
    }

    static Integer selectColorFormat(int[] supportedColorFormats, CodecCapabilities capabilities) {
        for (int supportedColorFormat : supportedColorFormats) {
            for (int codecColorFormat : capabilities.colorFormats) {
                if (codecColorFormat == supportedColorFormat) {
                    return codecColorFormat;
                }
            }
        }
        return null;
    }

    static boolean codecSupportsType(MediaCodecInfo info, VideoCodecType type) {
        for (String mimeType : info.getSupportedTypes()) {
            if (type.mimeType().equals(mimeType)) {
                return true;
            }
        }
        return false;
    }


    private MediaCodecUtils() {
    }
}
