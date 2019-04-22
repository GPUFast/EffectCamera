package com.gpufast.effectcamera.recorder.ui;

import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.gpufast.effectcamera.BaseFragment;
import com.gpufast.effectcamera.R;
import com.gpufast.effectcamera.recorder.contract.RecorderContract;
import com.gpufast.effectcamera.recorder.presenter.RecorderPresenter;
import com.gpufast.recorder.RecorderParams;

public class RecorderFragment extends BaseFragment implements RecorderContract.View, View.OnClickListener {
    private static final String TAG = "RecorderFragment";
    private SurfaceView mPreview;
    private RecorderPresenter mPresenter;
    private ImageView mSwitchCameraBtn;
    private ImageView mStartRecoderBtn;


    @Override
    protected int getLayoutId() {
        return R.layout.recoder_fragment_layout;
    }

    @Override
    protected void onInitView() {
        mPreview = findViewById(R.id.id_camera_preview);
        mSwitchCameraBtn = findViewById(R.id.id_switch_camera);
        mSwitchCameraBtn.setOnClickListener(this);
        mStartRecoderBtn = findViewById(R.id.id_start_recorder_btn);
        mStartRecoderBtn.setOnClickListener(this);
        mPresenter = new RecorderPresenter();

        initRecorderParams();


        mPresenter.attachView(this);
        mPresenter.init();
    }

    private void initRecorderParams() {

        RecorderParams params = new RecorderParams();
        params.setVideoWidth(720);
        params.setVideoHeight(1280);
        params.setHwEncoder(true);

        mPresenter.setRecorderParameter(params);



    }


    @Override
    public SurfaceView getPreview() {
        return mPreview;
    }

    @Override
    public void onStartRecorder() {

    }

    @Override
    public void onRecorderProgress(int time_s) {

    }

    @Override
    public void onRecorderFinish() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_switch_camera:
                mPresenter.switchCamera();
                break;
            case R.id.id_start_recorder_btn:
                mPresenter.startRecorder();
                break;
        }
    }
}
