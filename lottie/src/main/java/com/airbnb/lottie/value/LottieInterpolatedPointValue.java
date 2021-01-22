package com.airbnb.lottie.value;

import com.airbnb.lottie.animation.keyframe.Interpolator;
import com.airbnb.lottie.utils.MiscUtils;
import ohos.agp.utils.Point;

@SuppressWarnings("unused")
public class LottieInterpolatedPointValue extends LottieInterpolatedValue<Point> {
  private final Point point = new Point();

  public LottieInterpolatedPointValue(Point startValue, Point endValue) {
    super(startValue, endValue);
  }

  public LottieInterpolatedPointValue(Point startValue, Point endValue, Interpolator interpolator) {
    super(startValue, endValue, interpolator);
  }

  @Override Point interpolateValue(Point startValue, Point endValue, float progress) {
    point.modify(
        MiscUtils.lerp(startValue.getPointX(), endValue.getPointX(), progress),
        MiscUtils.lerp(startValue.getPointY(), endValue.getPointY(), progress)
    );
    return point;
  }
}
