package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.CircleShape;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;
import ohos.agp.render.Path;
import ohos.agp.utils.Point;


import java.util.List;

public class EllipseContent
    implements PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
  private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

  private final Path path = new Path();

  private final String name;
  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, Point> sizeAnimation;
  private final BaseKeyframeAnimation<?, Point> positionAnimation;
  private final CircleShape circleShape;

  private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();
  private boolean isPathValid;

  public EllipseContent(LottieDrawable lottieDrawable, BaseLayer layer, CircleShape circleShape) {
    name = circleShape.getName();
    this.lottieDrawable = lottieDrawable;
    sizeAnimation = circleShape.getSize().createAnimation();
    positionAnimation = circleShape.getPosition().createAnimation();
    this.circleShape = circleShape;

    layer.addAnimation(sizeAnimation);
    layer.addAnimation(positionAnimation);

    sizeAnimation.addUpdateListener(this);
    positionAnimation.addUpdateListener(this);
  }

  @Override public void onValueChanged() {
    invalidate();
  }

  private void invalidate() {
    isPathValid = false;
    //lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent && ((TrimPathContent) content).getType() == ShapeTrimPath.Type.SIMULTANEOUSLY) {
        TrimPathContent trimPath = (TrimPathContent) content;
        trimPaths.addTrimPath(trimPath);
        trimPath.addListener(this);
      }
    }
  }

  @Override public String getName() {
    return name;
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    if (circleShape.isHidden()) {
      isPathValid = true;
      return path;
    }

    Point size = sizeAnimation.getValue();
    float halfWidth = size.getPointX() / 2f;
    float halfHeight = size.getPointY() / 2f;
    // TODO: handle bounds

    float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
    float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;

    path.reset();
    if (circleShape.isReversed()) {
      path.moveTo(0, -halfHeight);
      path.cubicTo(new Point(0 - cpW,-halfHeight),new Point(-halfWidth,0 - cpH),new Point( -halfWidth, 0));
      path.cubicTo(new Point(-halfWidth, 0 + cpH),new Point(0 - cpW, halfHeight),new Point(0, halfHeight));
      path.cubicTo(new Point(0 + cpW, halfHeight),new Point(halfWidth, 0 + cpH),new Point(halfWidth, 0));
      path.cubicTo(new Point(halfWidth, 0 - cpH),new Point( 0 + cpW, -halfHeight),new Point(0, -halfHeight));
    } else {
      path.moveTo(0, -halfHeight);
      path.cubicTo(new Point(0 + cpW, -halfHeight),new Point( halfWidth, 0 - cpH),new Point(halfWidth, 0));
      path.cubicTo(new Point(halfWidth, 0 + cpH),new Point(0 + cpW, halfHeight),new Point(0, halfHeight));
      path.cubicTo(new Point(0 - cpW, halfHeight),new Point( -halfWidth, 0 + cpH),new Point(-halfWidth, 0));
      path.cubicTo(new Point(-halfWidth, 0 - cpH),new Point(0 - cpW, -halfHeight),new Point(0, -halfHeight));
    }

    Point position = positionAnimation.getValue();
    path.offset(position.getPointX(), position.getPointY());

    path.close();

    trimPaths.apply(path);

    isPathValid = true;
    return path;
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property,  LottieValueCallback<T> callback) {
    if (property == LottieProperty.ELLIPSE_SIZE) {
      sizeAnimation.setValueCallback((LottieValueCallback<Point>) callback);
    } else if (property == LottieProperty.POSITION) {
      positionAnimation.setValueCallback((LottieValueCallback<Point>) callback);
    }
  }
}
