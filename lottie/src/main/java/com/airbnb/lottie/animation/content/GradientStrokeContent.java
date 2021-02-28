package com.airbnb.lottie.animation.content;


import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.value.LottieValueCallback;
import ohos.agp.render.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;
import ohos.utils.LightweightMap;


public class GradientStrokeContent extends BaseStrokeContent {
    /**
     * Cache the gradients such that it runs at 30fps.
     */
    private static final int CACHE_STEPS_MS = 32;

    private final String name;
    private final boolean hidden;
    //LightweightMap<Long, LinearGradient>
    private final LightweightMap<Long, LinearShader> linearGradientCache = new LightweightMap<>();
    private final LightweightMap<Long, RadialShader> radialGradientCache = new LightweightMap<>();
    private final RectFloat boundsRect = new RectFloat();

    private final GradientType type;
    private final int cacheSteps = 1;
    private final BaseKeyframeAnimation<GradientColor, GradientColor> colorAnimation;
    private final BaseKeyframeAnimation<Point, Point> startPointAnimation;
    private final BaseKeyframeAnimation<Point, Point> endPointAnimation;
    private ValueCallbackKeyframeAnimation colorCallbackAnimation;

    public GradientStrokeContent(
            final LottieDrawable lottieDrawable, BaseLayer layer, GradientStroke stroke) {
        super(lottieDrawable, layer, stroke.getCapType().toPaintCap(),
                stroke.getJoinType().toPaintJoin(), stroke.getMiterLimit(), stroke.getOpacity(),
                stroke.getWidth(), stroke.getLineDashPattern(), stroke.getDashOffset());

        name = stroke.getName();
        type = stroke.getGradientType();
        hidden = stroke.isHidden();
        //cacheSteps = (int) (lottieDrawable.getComposition().getDuration() / CACHE_STEPS_MS);

        colorAnimation = stroke.getGradientColor().createAnimation();
        colorAnimation.addUpdateListener(this);
        layer.addAnimation(colorAnimation);

        startPointAnimation = stroke.getStartPoint().createAnimation();
        startPointAnimation.addUpdateListener(this);
        layer.addAnimation(startPointAnimation);

        endPointAnimation = stroke.getEndPoint().createAnimation();
        endPointAnimation.addUpdateListener(this);
        layer.addAnimation(endPointAnimation);
    }

    @Override
    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        if (hidden) {
            return;
        }
        getBounds(boundsRect, parentMatrix, false);

        Shader shader;
        if (type == GradientType.LINEAR)  {
            shader = getLinearGradient();
            paint.setShader(shader, Paint.ShaderType.LINEAR_SHADER);
        } else {
            shader = getRadialGradient();
            paint.setShader(shader, Paint.ShaderType.RADIAL_SHADER);
        }
        shader.setShaderMatrix(parentMatrix);

        super.draw(canvas, parentMatrix, parentAlpha);
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
        float x0 = startPoint.getPointX();
        float y0 = startPoint.getPointY();
        float x1 = endPoint.getPointX();
        float y1 = endPoint.getPointY();
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
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        super.addValueCallback(property, callback);
        if (property == LottieProperty.GRADIENT_COLOR) {
            if (colorCallbackAnimation != null) {
                layer.removeAnimation(colorCallbackAnimation);
            }

            if (callback == null) {
                colorCallbackAnimation = null;
            } else {
                colorCallbackAnimation = new ValueCallbackKeyframeAnimation<>(callback);
                colorCallbackAnimation.addUpdateListener(this);
                layer.addAnimation(colorCallbackAnimation);
            }
        }
    }
}
