package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.ShapeFill;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;
import ohos.agp.render.Canvas;
import ohos.agp.render.ColorFilter;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;

import java.util.ArrayList;
import java.util.List;

import static com.airbnb.lottie.utils.MiscUtils.clamp;

public class FillContent
    implements DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
  private final Path path = new Path();
  private final Paint paint = new LPaint();
  private final BaseLayer layer;
  private final String name;
  private final boolean hidden;
  private final List<PathContent> paths = new ArrayList<>();
  private final BaseKeyframeAnimation<Integer, Integer> colorAnimation;
  private final BaseKeyframeAnimation<Integer, Integer> opacityAnimation;

  private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
  private final LottieDrawable lottieDrawable;

  public FillContent(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeFill fill) {
    this.layer = layer;
    name = fill.getName();
    hidden = fill.isHidden();
    this.lottieDrawable = lottieDrawable;
    this.paint.setAntiAlias(true);
    if (fill.getColor() == null || fill.getOpacity() == null ) {
      colorAnimation = null;
      opacityAnimation = null;
      return;
    }

    path.setFillType(fill.getFillType());

    colorAnimation = fill.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
    opacityAnimation = fill.getOpacity().createAnimation();
    opacityAnimation.addUpdateListener(this);
    layer.addAnimation(opacityAnimation);
  }

  @Override public void onValueChanged() {
    //lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content content = contentsAfter.get(i);
      if (content instanceof PathContent) {
        paths.add((PathContent) content);
      }
    }
  }

  @Override public String getName() {
    return name;
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    if (hidden) {
      return;
    }
    L.beginSection("FillContent#draw");
    paint.setColor(new Color(((ColorKeyframeAnimation) colorAnimation).getIntValue()));
    int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
    paint.setAlpha(clamp(alpha, 0, 255));

    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }

    path.reset();

    float[] data = parentMatrix.getData();
    String str = data.toString();
    //matrix.translate(500,500);
    //data = matrix.getData();
    for (int i = 0; i < paths.size(); i++) {
      path.addPath(paths.get(i).getPath(), parentMatrix,null);
      //path.addPath(paths.get(i).getPath(), matrix,null);
    }

    canvas.drawPath(path, paint);

    L.endSection("FillContent#draw");
  }

  @Override public void getBounds(RectFloat outBounds, Matrix parentMatrix, boolean applyParents) {
    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      this.path.addPath(paths.get(i).getPath(), parentMatrix,null);
    }
    path.computeBounds(outBounds);
    // Add padding to account for rounding errors.
    outBounds.modify(
        outBounds.left - 1,
        outBounds.top - 1,
        outBounds.right + 1,
        outBounds.bottom + 1
    );
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property,  LottieValueCallback<T> callback) {
    if (property == LottieProperty.COLOR) {
      colorAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.OPACITY) {
      opacityAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.COLOR_FILTER) {
      if (colorFilterAnimation != null) {
        layer.removeAnimation(colorFilterAnimation);
      }

      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
        colorFilterAnimation.addUpdateListener(this);
        layer.addAnimation(colorFilterAnimation);
      }
    }
  }
}
