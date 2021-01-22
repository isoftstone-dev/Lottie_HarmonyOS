package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
import ohos.agp.render.Path;
import ohos.agp.utils.Point;

public class PathKeyframe extends Keyframe<Point> {

  private Path path;

  private final Keyframe<Point> pointKeyFrame;

  public PathKeyframe(LottieComposition composition, Keyframe<Point> keyframe) {
    super(composition, keyframe.startValue, keyframe.endValue, keyframe.interpolator, keyframe.xInterpolator, keyframe.yInterpolator,
        keyframe.startFrame, keyframe.endFrame);
    this.pointKeyFrame = keyframe;
    createPath();
  }

  public void createPath() {
    // This must use equals(float, float) because PointF didn't have an equals(PathF) method
    // until KitKat...
    boolean equals = endValue != null && startValue != null &&
        startValue.equals(endValue.getPointX(), endValue.getPointY());
    if (startValue != null && endValue != null && !equals) {
      path = Utils.createPath(startValue, endValue, pointKeyFrame.pathCp1, pointKeyFrame.pathCp2);
    }
  }

  /** This will be null if the startValue and endValue are the same. */
   Path getPath() {
    return path;
  }
}
