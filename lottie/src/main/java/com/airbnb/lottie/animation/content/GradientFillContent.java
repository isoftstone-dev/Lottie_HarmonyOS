package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;
import ohos.agp.render.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;
import ohos.utils.LightweightMap;

import java.util.ArrayList;
import java.util.List;

import static com.airbnb.lottie.utils.MiscUtils.clamp;

public class GradientFillContent
        implements DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
    /**
     * Cache the gradients such that it runs at 30fps.
     */
    private static final int CACHE_STEPS_MS = 32;
    private final String name;
    private final boolean hidden;
    private final BaseLayer layer;
    private final LightweightMap<Long, LinearShader> linearGradientCache = new LightweightMap<>();
    private final LightweightMap<Long, RadialShader> radialGradientCache = new LightweightMap<>();
    private final Path path = new Path();
    private final Paint paint = new LPaint();
    private final RectFloat boundsRect = new RectFloat();
    private final List<PathContent> paths = new ArrayList<>();
    private final GradientType type;
    private final BaseKeyframeAnimation<GradientColor, GradientColor> colorAnimation;
    private final BaseKeyframeAnimation<Integer, Integer> opacityAnimation;
    private final BaseKeyframeAnimation<Point, Point> startPointAnimation;
    private final BaseKeyframeAnimation<Point, Point> endPointAnimation;
    private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

    private ValueCallbackKeyframeAnimation colorCallbackAnimation;
    private final LottieDrawable lottieDrawable;
    private final int cacheSteps = 1;

    public GradientFillContent(final LottieDrawable lottieDrawable, BaseLayer layer, GradientFill fill) {
        this.layer = layer;
        name = fill.getName();
        hidden = fill.isHidden();
        this.lottieDrawable = lottieDrawable;
        type = fill.getGradientType();
        paint.setAntiAlias(true);
        path.setFillType(fill.getFillType());
        //cacheSteps = (int) (lottieDrawable.getComposition().getDuration() / CACHE_STEPS_MS);

        colorAnimation = fill.getGradientColor().createAnimation();
        colorAnimation.addUpdateListener(this);
        layer.addAnimation(colorAnimation);

        opacityAnimation = fill.getOpacity().createAnimation();
        opacityAnimation.addUpdateListener(this);
        layer.addAnimation(opacityAnimation);

        startPointAnimation = fill.getStartPoint().createAnimation();
        startPointAnimation.addUpdateListener(this);
        layer.addAnimation(startPointAnimation);

        endPointAnimation = fill.getEndPoint().createAnimation();
        endPointAnimation.addUpdateListener(this);
        layer.addAnimation(endPointAnimation);
    }

    @Override
    public void onValueChanged() {
        //lottieDrawable.invalidateSelf();
    }

    @Override
    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        for (int i = 0; i < contentsAfter.size(); i++) {
            Content content = contentsAfter.get(i);
            if (content instanceof PathContent) {
                paths.add((PathContent) content);
            }
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        if (hidden) {
            return;
        }
        L.beginSection("GradientFillContent#draw");
        path.reset();
        for (int i = 0; i < paths.size(); i++) {
            path.addPath(paths.get(i).getPath(), parentMatrix, Path.AddPathMode.APPEND_ADD_PATH_MODE);
        }

        path.computeBounds(boundsRect);


        Shader shader;
        if (type == GradientType.LINEAR) {
            shader = getLinearGradient();
            paint.setShader(shader, Paint.ShaderType.LINEAR_SHADER);
        } else {
            shader = getRadialGradient();
            paint.setShader(shader, Paint.ShaderType.RADIAL_SHADER);
        }
        shader.setShaderMatrix(parentMatrix);

        if (colorFilterAnimation != null) {
            paint.setColorFilter(colorFilterAnimation.getValue());
        }

        int alpha = (int) ((parentAlpha / 255f * opacityAnimation.getValue() / 100f) * 255);
        paint.setAlpha(clamp(alpha, 0, 255));

        canvas.drawPath(path, paint);
        L.endSection("GradientFillContent#draw");
    }

    @Override
    public void getBounds(RectFloat outBounds, Matrix parentMatrix, boolean applyParents) {
        path.reset();
        for (int i = 0; i < paths.size(); i++) {
            path.addPath(paths.get(i).getPath(), parentMatrix, Path.AddPathMode.APPEND_ADD_PATH_MODE);
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

    @Override
    public String getName() {
        return name;
    }

  private LinearShader getLinearGradient() {
      int gradientHash = getGradientHash();
      LinearShader gradient = linearGradientCache.get(gradientHash);
      if (gradient != null) {
          return gradient;
      }
      Point startPoint = startPointAnimation.getValue();
      Point endPoint = endPointAnimation.getValue();
      GradientColor gradientColor = colorAnimation.getValue();
      Point[] points = new Point[2];
      points[0] = startPoint;
      points[1] = endPoint;

      int[] colors = applyDynamicColorsIfNeeded(gradientColor.getColors());
      Color[] colorss = new Color[colors.length];
      colorss[0] = new Color(colors[0]);
      colorss[1] = new Color(colors[0]);

      float[] positions = gradientColor.getPositions();
      gradient = new LinearShader(points, positions, colorss, Shader.TileMode.CLAMP_TILEMODE);
      linearGradientCache.put(new Long(gradientHash), gradient);
      return gradient;
  }

    private RadialShader getRadialGradient() {
        int gradientHash = getGradientHash();
        RadialShader gradient = radialGradientCache.get(gradientHash);
        if (gradient != null) {
            return gradient;
        }
        Point startPoint = startPointAnimation.getValue();
        Point endPoint = endPointAnimation.getValue();
        GradientColor gradientColor = colorAnimation.getValue();
        int[] colors = applyDynamicColorsIfNeeded(gradientColor.getColors());
        Color[] colorss = new Color[colors.length];
        colorss[0] = new Color(colors[0]);
        colorss[1] = new Color(colors[0]);
        float[] positions = gradientColor.getPositions();
        float x0 = startPoint.getPointX();
        float y0 = startPoint.getPointY();
        float x1 = endPoint.getPointX();
        float y1 = endPoint.getPointY();
        float r = (float) Math.hypot(x1 - x0, y1 - y0);
        if (r <= 0) {
            r = 0.001f;
        }
        gradient = new RadialShader(startPoint, r, positions, colorss, Shader.TileMode.CLAMP_TILEMODE);
        radialGradientCache.put(new Long(gradientHash), gradient);
        return gradient;
    }

    private int getGradientHash() {
        int startPointProgress = Math.round(startPointAnimation.getProgress() * cacheSteps);
        int endPointProgress = Math.round(endPointAnimation.getProgress() * cacheSteps);
        int colorProgress = Math.round(colorAnimation.getProgress() * cacheSteps);
        int hash = 17;
        if (startPointProgress != 0) {
            hash = hash * 31 * startPointProgress;
        }
        if (endPointProgress != 0) {
            hash = hash * 31 * endPointProgress;
        }
        if (colorProgress != 0) {
            hash = hash * 31 * colorProgress;
        }
        return hash;
    }

    private int[] applyDynamicColorsIfNeeded(int[] colors) {
        if (colorCallbackAnimation != null) {
            Integer[] dynamicColors = (Integer[]) colorCallbackAnimation.getValue();
            if (colors.length == dynamicColors.length) {
                for (int i = 0; i < colors.length; i++) {
                    colors[i] = dynamicColors[i];
                }
            } else {
                colors = new int[dynamicColors.length];
                for (int i = 0; i < dynamicColors.length; i++) {
                    colors[i] = dynamicColors[i];
                }
            }
        }
        return colors;
    }

    @Override
    public void resolveKeyPath(
            KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        if (property == LottieProperty.OPACITY) {
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
        } else if (property == LottieProperty.GRADIENT_COLOR) {
            if (colorCallbackAnimation != null) {
                layer.removeAnimation(colorCallbackAnimation);
            }

            if (callback == null) {
                colorCallbackAnimation = null;
            } else {
                //noinspection rawtypes
                //linearGradientCache.clear();
                //radialGradientCache.clear();
                colorCallbackAnimation = new ValueCallbackKeyframeAnimation<>(callback);
                colorCallbackAnimation.addUpdateListener(this);
                layer.addAnimation(colorCallbackAnimation);
            }
        }
    }
}
