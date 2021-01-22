package com.airbnb.lottie.model.layer;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;
import ohos.agp.render.Canvas;
import ohos.agp.render.ColorFilter;
import ohos.agp.render.Paint;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;
import ohos.media.image.PixelMap;

public class ImageLayer extends BaseLayer {

  private final Paint paint = new LPaint();
  private final RectFloat src = new RectFloat();
  private final RectFloat dst = new RectFloat();

  private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    paint.setAntiAlias(true);
  }

  @Override public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    PixelMap bitmap = getBitmap();
    if (bitmap == null || bitmap.isReleased()) {
      return;
    }
    float density = Utils.dpScale();

    paint.setAlpha(parentAlpha);
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }
    canvas.save();
    canvas.concat(parentMatrix);
    src.modify(0, 0, bitmap.getImageInfo().size.width, bitmap.getImageInfo().size.height);
    dst.modify(0, 0, (int) (bitmap.getImageInfo().size.width * density), (int) (bitmap.getImageInfo().size.height * density));
    PixelMapHolder pixelMapHolderBitmap = new PixelMapHolder(bitmap);
    canvas.drawPixelMapHolderRect(pixelMapHolderBitmap, src, dst , paint);
    canvas.restore();
  }

  @Override public void getBounds(RectFloat outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    PixelMap bitmap = getBitmap();
    if (bitmap != null) {
      outBounds.modify(0, 0, bitmap.getImageInfo().size.width * Utils.dpScale(), bitmap.getImageInfo().size.height * Utils.dpScale());
      boundsMatrix.mapRect(outBounds);
    }
  }


  private PixelMap getBitmap() {
    String refId = layerModel.getRefId();
    return null;//lottieDrawable.getImageAsset(refId);
  }

  @SuppressWarnings("SingleStatementInBlock")
  @Override
  public <T> void addValueCallback(T property,  LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
     if (property == LottieProperty.COLOR_FILTER) {
       if (callback == null) {
         colorFilterAnimation = null;
       } else {
         //noinspection unchecked
         colorFilterAnimation =
             new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
       }
    }
  }
}
