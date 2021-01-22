package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;
import ohos.agp.utils.Point;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LottieRelativePointValueCallback extends LottieValueCallback<Point> {
  private final Point point = new Point();

  public LottieRelativePointValueCallback() {
  }

  public LottieRelativePointValueCallback(Point staticValue) {
    super(staticValue);
  }

  @Override
  public final Point getValue(LottieFrameInfo<Point> frameInfo) {
    point.modify(
        MiscUtils.lerp(
            frameInfo.getStartValue().getPointX(),
            frameInfo.getEndValue().getPointX(),
            frameInfo.getInterpolatedKeyframeProgress()),
        MiscUtils.lerp(
            frameInfo.getStartValue().getPointY(),
            frameInfo.getEndValue().getPointY(),
            frameInfo.getInterpolatedKeyframeProgress())
    );

    Point offset = getOffset(frameInfo);
    point.translate(offset.getPointX(), offset.getPointY());
    return point;
  }

  /**
   * Override this to provide your own offset on every frame.
   */
  public Point getOffset(LottieFrameInfo<Point> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
