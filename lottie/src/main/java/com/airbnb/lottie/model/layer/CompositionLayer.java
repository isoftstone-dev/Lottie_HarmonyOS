package com.airbnb.lottie.model.layer;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import ohos.agp.components.Component;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;


public class CompositionLayer extends BaseLayer {

  private BaseKeyframeAnimation<Float, Float> timeRemapping;
  private final List<BaseLayer> layers = new ArrayList<>();
  private final RectFloat rect = new RectFloat();
  private final RectFloat newClipRect = new RectFloat();
  private Paint layerPaint = new Paint();

   private Boolean hasMatte;
   private Boolean hasMasks;

  public CompositionLayer(LottieDrawable drawable, Layer layerModel, List<Layer> layerModels,
                          LottieComposition composition) {
    super(drawable, layerModel);

    AnimatableFloatValue timeRemapping = layerModel.getTimeRemapping();
    if (timeRemapping != null) {
      this.timeRemapping = timeRemapping.createAnimation();
      addAnimation(this.timeRemapping);
      //noinspection ConstantConditions
      this.timeRemapping.addUpdateListener(this);
    } else {
      this.timeRemapping = null;
    }

    HashMap<Long, BaseLayer> layerMap = new HashMap<>();

    BaseLayer mattedLayer = null;
    for (int i = layerModels.size() - 1; i >= 0; i--) {
      Layer lm = layerModels.get(i);
      BaseLayer layer = BaseLayer.forModel(lm, drawable, composition);
      if (layer == null) {
        continue;
      }
      layerMap.put(layer.getLayerModel().getId(), layer);
      if (mattedLayer != null) {
        mattedLayer.setMatteLayer(layer);
        mattedLayer = null;
      } else {
        layers.add(0, layer);
        switch (lm.getMatteType()) {
          case ADD:
          case INVERT:
            mattedLayer = layer;
            break;
        }
      }
    }

    Set<Long> keys = layerMap.keySet();
    for (Long key:keys) {
      BaseLayer layerView = layerMap.get(key);
      // This shouldn't happen but it appears as if sometimes on pre-lollipop devices when
      // compiled with d8, layerView is null sometimes.
      // https://github.com/airbnb/lottie-android/issues/524
      if (layerView == null) {
        continue;
      }
      BaseLayer parentLayer = layerMap.get(layerView.getLayerModel().getParentId());
      if (parentLayer != null) {
        layerView.setParentLayer(parentLayer);
      }
    }
  }

  @Override public void setOutlineMasksAndMattes(boolean outline) {
    super.setOutlineMasksAndMattes(outline);
    for (BaseLayer layer : layers) {
      layer.setOutlineMasksAndMattes(outline);
    }
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    L.beginSection("CompositionLayer#draw");
    newClipRect.modify(0, 0, layerModel.getPreCompWidth(), layerModel.getPreCompHeight());
    parentMatrix.mapRect(newClipRect);

    // Apply off-screen rendering only when needed in order to improve rendering performance.
    boolean isDrawingWithOffScreen = false;//lottieDrawable.isApplyingOpacityToLayersEnabled() && layers.size() > 1 && parentAlpha != 255;
    if (isDrawingWithOffScreen) {
      layerPaint.setAlpha(parentAlpha);
      Utils.saveLayerCompat(canvas, newClipRect, layerPaint, 0);
    } else {
      canvas.save();
    }

    int childAlpha = isDrawingWithOffScreen ? 255 : parentAlpha;
    for (int i = layers.size() - 1; i >= 0; i--) {
      boolean nonEmptyClip = true;
      if (!newClipRect.isEmpty()) {
        nonEmptyClip = canvas.quickReject(newClipRect.left,newClipRect.top,newClipRect.right,newClipRect.bottom);
      }
      //if (nonEmptyClip)
      {
        BaseLayer layer = layers.get(i);
        layer.draw(canvas, parentMatrix, childAlpha);
      }
    }
    canvas.restore();
    L.endSection("CompositionLayer#draw");
  }

  @Override public void getBounds(RectFloat outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    for (int i = layers.size() - 1; i >= 0; i--) {
      rect.modify(0, 0, 0, 0);
      layers.get(i).getBounds(rect, boundsMatrix, true);
      union(outBounds,rect.left,rect.top,rect.right,rect.bottom);
    }
  }

  @Override public void setProgress(float progress) {
    if (progress < 0 || progress > 1) {
      throw new NumberFormatException("progress must be from 0 to 1");
    }
    super.setProgress(progress);
    if (timeRemapping != null) {
      // The duration has 0.01 frame offset to show end of animation properly.
      // https://github.com/airbnb/lottie-android/pull/766
      // Ignore this offset for calculating time-remapping because time-remapping value is based on original duration.
      float durationFrames = 30;//lottieDrawable.getComposition().getDurationFrames() + 0.01f;
      float compositionDelayFrames = layerModel.getComposition().getStartFrame();
      float remappedFrames = timeRemapping.getValue() * 30 //layerModel.getComposition().getFrameRate()
            - compositionDelayFrames;
      progress = remappedFrames / durationFrames;
    }
    if (timeRemapping == null) {
      progress -= layerModel.getStartProgress();
    }
    if (layerModel.getTimeStretch() != 0) {
      progress /= layerModel.getTimeStretch();
    }
    for (int i = layers.size() - 1; i >= 0; i--) {
      layers.get(i).setProgress(progress);
    }
  }

  public boolean hasMasks() {
    if (hasMasks == null) {
      for (int i = layers.size() - 1; i >= 0; i--) {
        BaseLayer layer = layers.get(i);
        if (layer instanceof ShapeLayer) {
          if (layer.hasMasksOnThisLayer()) {
            hasMasks = true;
            return true;
          }
        } else if (layer instanceof CompositionLayer && ((CompositionLayer) layer).hasMasks()) {
          hasMasks = true;
          return true;
        }
      }
      hasMasks = false;
    }
    return hasMasks;
  }

  public boolean hasMatte() {
    if (hasMatte == null) {
      if (hasMatteOnThisLayer()) {
        hasMatte = true;
        return true;
      }

      for (int i = layers.size() - 1; i >= 0; i--) {
        if (layers.get(i).hasMatteOnThisLayer()) {
          hasMatte = true;
          return true;
        }
      }
      hasMatte = false;
    }
    return hasMatte;
  }

  @Override
  protected void resolveChildKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath) {
    for (int i = 0; i < layers.size(); i++) {
      layers.get(i).resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property,  LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);

    if (property == LottieProperty.TIME_REMAP) {
      if (callback == null) {
        if (timeRemapping != null) {
          timeRemapping.setValueCallback(null);
        }
      } else {
        timeRemapping = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback);
        timeRemapping.addUpdateListener(this);
        addAnimation(timeRemapping);
      }
    }
  }

  public void union(RectFloat rect,float left, float top, float right, float bottom) {
    if ((left < right) && (top < bottom)) {
      if ((rect.left < rect.right) && (rect.top < rect.bottom)) {
        if (rect.left > left)
          rect.left = left;
        if (rect.top > top)
          rect.top = top;
        if (rect.right < right)
          rect.right = right;
        if (rect.bottom < bottom)
          rect.bottom = bottom;
      } else {
        rect.left = left;
        rect.top = top;
        rect.right = right;
        rect.bottom = bottom;
      }
    }
  }
}
