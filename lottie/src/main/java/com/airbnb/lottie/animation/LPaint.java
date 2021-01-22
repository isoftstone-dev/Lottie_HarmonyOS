package com.airbnb.lottie.animation;


import ohos.agp.render.BlendMode;
import ohos.agp.render.Paint;

/**
 * Custom paint that doesn't set text locale.
 * It takes ~1ms on initialization and isn't needed so removing it speeds up
 * setComposition.
 */
public class LPaint extends Paint {
    public LPaint() {
        super();
    }

    public LPaint(Paint flags) {
        super(flags);
    }

    public LPaint(BlendMode porterDuffMode) {
        super();
        setBlendMode(porterDuffMode);
    }

    public LPaint(Paint flags, BlendMode porterDuffMode) {
        super(flags);
        setBlendMode(porterDuffMode);
    }
}
