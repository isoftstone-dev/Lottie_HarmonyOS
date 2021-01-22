package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.SplitDimensionPathKeyframeAnimation;
import com.airbnb.lottie.value.Keyframe;
import ohos.agp.utils.Point;

import java.util.ArrayList;
import java.util.List;

public class AnimatableSplitDimensionPathValue implements AnimatableValue<Point, Point> {
  private final AnimatableFloatValue animatableXDimension;
  private final AnimatableFloatValue animatableYDimension;

  public AnimatableSplitDimensionPathValue(
      AnimatableFloatValue animatableXDimension,
      AnimatableFloatValue animatableYDimension) {
    this.animatableXDimension = animatableXDimension;
    this.animatableYDimension = animatableYDimension;
  }

  @Override
  public List<Keyframe<Point>> getKeyframes() {
    throw new UnsupportedOperationException("Cannot call getKeyframes on AnimatableSplitDimensionPathValue.");
  }

  @Override
  public boolean isStatic() {
    return animatableXDimension.isStatic() && animatableYDimension.isStatic();
  }

  @Override public BaseKeyframeAnimation<Point, Point> createAnimation() {
    return new SplitDimensionPathKeyframeAnimation(
        animatableXDimension.createAnimation(), animatableYDimension.createAnimation());
  }
}
