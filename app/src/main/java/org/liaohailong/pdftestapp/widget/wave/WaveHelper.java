package org.liaohailong.pdftestapp.widget.wave;

import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Describe as : 波浪控制工具
 * Created by LHL on 2018/5/14.
 */

public class WaveHelper {

    private final ObjectAnimator mProgressAnim;
    private final ObjectAnimator mAmplitudeAnim;
    private final ObjectAnimator mWaveLengthAnim;

    public WaveHelper(WaterWaveView waterWaveView) {
        mProgressAnim = ObjectAnimator.ofFloat(waterWaveView, "progress");
        mAmplitudeAnim = ObjectAnimator.ofFloat(waterWaveView, "amplitude");
        mWaveLengthAnim = ObjectAnimator.ofFloat(waterWaveView, "waveLength");

        initAnim(mProgressAnim);
        initAnim(mAmplitudeAnim);
        initAnim(mWaveLengthAnim);
    }

    private void initAnim(ObjectAnimator objectAnimator) {
        objectAnimator.setDuration(1000);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * @param progress [0.0f,1.0f]
     */
    public void setProgress(float progress) {
        if (mProgressAnim.isRunning()) {
            mProgressAnim.pause();
        }
        mProgressAnim.setFloatValues(progress);
        mProgressAnim.start();
    }
}
