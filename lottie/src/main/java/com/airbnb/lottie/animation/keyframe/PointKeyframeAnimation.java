package com.airbnb.lottie.animation.keyframe;


import com.airbnb.lottie.value.Keyframe;
import ohos.agp.utils.Point;

import java.util.List;

public class PointKeyframeAnimation extends KeyframeAnimation<Point> {
  private final Point point = new Point();

  public PointKeyframeAnimation(List<Keyframe<Point>> keyframes) {
    super(keyframes);
  }

  @Override public Point getValue(Keyframe<Point> keyframe, float keyframeProgress) {
    return getValue(keyframe, keyframeProgress, keyframeProgress, keyframeProgress);
  }

  @Override protected Point getValue(Keyframe<Point> keyframe, float linearKeyframeProgress, float xKeyframeProgress, float yKeyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    Point startPoint = keyframe.startValue;
    Point endPoint = keyframe.endValue;

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Point value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, startPoint,
          endPoint, linearKeyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    point.modify(startPoint.getPointX() + xKeyframeProgress * (endPoint.getPointX() - startPoint.getPointX()),
        startPoint.getPointY() + yKeyframeProgress * (endPoint.getPointY() - startPoint.getPointY()));
    return point;
  }
}
