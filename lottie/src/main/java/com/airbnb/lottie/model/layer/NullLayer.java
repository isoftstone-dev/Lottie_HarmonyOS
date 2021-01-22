package com.airbnb.lottie.model.layer;

import com.airbnb.lottie.LottieDrawable;
import ohos.agp.render.Canvas;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;

public class NullLayer extends BaseLayer {
  NullLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    // Do nothing.
  }

  @Override public void getBounds(RectFloat outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    outBounds.modify(0, 0, 0, 0);
  }
}
