package com.airbnb.lottie.model.animatable;


import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ModifierContent;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.BaseLayer;
import ohos.agp.utils.Point;


public class AnimatableTransform implements ModifierContent, ContentModel {

  private final AnimatablePathValue anchorPoint;

  private final AnimatableValue<Point, Point> position;

  private final AnimatableScaleValue scale;

  private final AnimatableFloatValue rotation;

  private final AnimatableIntegerValue opacity;

  private final AnimatableFloatValue skew;

  private final AnimatableFloatValue skewAngle;

  // Used for repeaters

  private final AnimatableFloatValue startOpacity;

  private final AnimatableFloatValue endOpacity;

  public AnimatableTransform() {
    this(null, null, null, null, null, null, null, null, null);
  }

  public AnimatableTransform( AnimatablePathValue anchorPoint,
       AnimatableValue<Point, Point> position,  AnimatableScaleValue scale,
       AnimatableFloatValue rotation,  AnimatableIntegerValue opacity,
       AnimatableFloatValue startOpacity,  AnimatableFloatValue endOpacity,
       AnimatableFloatValue skew,  AnimatableFloatValue skewAngle) {
    this.anchorPoint = anchorPoint;
    this.position = position;
    this.scale = scale;
    this.rotation = rotation;
    this.opacity = opacity;
    this.startOpacity = startOpacity;
    this.endOpacity = endOpacity;
    this.skew = skew;
    this.skewAngle = skewAngle;
  }


  public AnimatablePathValue getAnchorPoint() {
    return anchorPoint;
  }


  public AnimatableValue<Point, Point> getPosition() {
    return position;
  }


  public AnimatableScaleValue getScale() {
    return scale;
  }


  public AnimatableFloatValue getRotation() {
    return rotation;
  }


  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }


  public AnimatableFloatValue getStartOpacity() {
    return startOpacity;
  }


  public AnimatableFloatValue getEndOpacity() {
    return endOpacity;
  }


  public AnimatableFloatValue getSkew() {
    return skew;
  }


  public AnimatableFloatValue getSkewAngle() {
    return skewAngle;
  }

  public TransformKeyframeAnimation createAnimation() {
    return new TransformKeyframeAnimation(this);
  }


  @Override
  public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return null;
  }
}
