package org.liaohailong.pdftestapp.widget.wave;

import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Describe as : 波浪控制工具
 * Created by LHL on 2018/5/14.
 */

public class WaveHelper {
    private final WaterWaveView mWaterWaveView;
    private final ObjectAnimator mProgressAnim;

    public WaveHelper(WaterWaveView waterWaveView) {
        mWaterWaveView = waterWaveView;

        mProgressAnim = ObjectAnimator.ofFloat(waterWaveView, "progress", 0f, 1.0f);
        mProgressAnim.setDuration(1000);
        mProgressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * @param progress [0.0f,1.0f]
     */
    public void setProgress(float progress) {
        if (mProgressAnim.isRunning()) {
            mProgressAnim.pause();
        }
        float oldProgress = mWaterWaveView.getProgress();
        mProgressAnim.setFloatValues(oldProgress, progress);
        mProgressAnim.start();
    }
}
