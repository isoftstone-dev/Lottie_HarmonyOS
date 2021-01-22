package com.airbnb.lottie.animation.keyframe;


import ohos.agp.components.AttrSet;
import ohos.app.Context;

public class LinearInterpolator implements Interpolator {

    public LinearInterpolator() {
    }

    public LinearInterpolator(Context context, AttrSet attrs) {
    }

    public float getInterpolation(float input) {
        return input;
    }
}