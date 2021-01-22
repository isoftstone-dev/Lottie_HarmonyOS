package com.airbnb.lottie.animation.content;

import ohos.agp.render.Canvas;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;

public interface DrawingContent extends Content {
  void draw(Canvas canvas, Matrix parentMatrix, int alpha);
  void getBounds(RectFloat outBounds, Matrix parentMatrix, boolean applyParents);
}
