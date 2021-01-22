package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PointKeyframeAnimation;
import ohos.agp.utils.Point;

import java.util.List;

public class AnimatablePointValue extends BaseAnimatableValue<Point, Point> {
  public AnimatablePointValue(List<Keyframe<Point>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Point, Point> createAnimation() {
    return new PointKeyframeAnimation(keyframes);
  }
}
