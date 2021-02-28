package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public interface AnimatableValue<K, A> {
  /* 获取关键帧列表 */
  List<Keyframe<K>> getKeyframes();

  /* 是否静态 */
  boolean isStatic();

  /* 创建关键帧可绘制对象 */
  BaseKeyframeAnimation<K, A> createAnimation();
}
