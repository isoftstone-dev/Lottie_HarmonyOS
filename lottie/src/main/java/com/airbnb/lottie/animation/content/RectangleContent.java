package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.RectangleShape;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;
import ohos.agp.render.Path;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;

import java.util.List;


public class RectangleContent
        implements BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, PathContent {
    private final Path path = new Path();
    private final RectFloat rect = new RectFloat();

    private final String name;
    private final boolean hidden;
    private final LottieDrawable lottieDrawable;
    private final BaseKeyframeAnimation<?, Point> positionAnimation;
    private final BaseKeyframeAnimation<?, Point> sizeAnimation;
    private final BaseKeyframeAnimation<?, Float> cornerRadiusAnimation;

    private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();
    private boolean isPathValid;

    public RectangleContent(LottieDrawable lottieDrawable, BaseLayer layer, RectangleShape rectShape) {
        name = rectShape.getName();
        hidden = rectShape.isHidden();
        this.lottieDrawable = lottieDrawable;
        positionAnimation = rectShape.getPosition().createAnimation();
        sizeAnimation = rectShape.getSize().createAnimation();
        cornerRadiusAnimation = rectShape.getCornerRadius().createAnimation();

        layer.addAnimation(positionAnimation);
        layer.addAnimation(sizeAnimation);
        layer.addAnimation(cornerRadiusAnimation);

        positionAnimation.addUpdateListener(this);
        sizeAnimation.addUpdateListener(this);
        cornerRadiusAnimation.addUpdateListener(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onValueChanged() {
        invalidate();
    }

    private void invalidate() {
        isPathValid = false;
        //lottieDrawable.invalidateSelf();
    }

    @Override
    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        for (int i = 0; i < contentsBefore.size(); i++) {
            Content content = contentsBefore.get(i);
            if (content instanceof TrimPathContent &&
                    ((TrimPathContent) content).getType() == ShapeTrimPath.Type.SIMULTANEOUSLY) {
                TrimPathContent trimPath = (TrimPathContent) content;
                trimPaths.addTrimPath(trimPath);
                trimPath.addListener(this);
            }
        }
    }

    @Override
    public Path getPath() {
        if (isPathValid) {
            return path;
        }

        path.reset();

        if (hidden) {
            isPathValid = true;
            return path;
        }

        Point size = sizeAnimation.getValue();
        float halfWidth = size.getPointX() / 2f;
        float halfHeight = size.getPointY() / 2f;
        float radius = cornerRadiusAnimation == null ?
                0f : ((FloatKeyframeAnimation) cornerRadiusAnimation).getFloatValue();
        float maxRadius = Math.min(halfWidth, halfHeight);
        if (radius > maxRadius) {
            radius = maxRadius;
        }

        // Draw the rectangle top right to bottom left.
        Point position = positionAnimation.getValue();

        path.moveTo(position.getPointX() + halfWidth, position.getPointY() - halfHeight + radius);

        path.lineTo(position.getPointX() + halfWidth, position.getPointY() + halfHeight - radius);

        if (radius > 0) {
            rect.modify(position.getPointX() + halfWidth - 2 * radius,
                    position.getPointY() + halfHeight - 2 * radius,
                    position.getPointX() + halfWidth,
                    position.getPointY() + halfHeight);
            path.arcTo(rect, 0, 90, false);
        }

        path.lineTo(position.getPointX() - halfWidth + radius, position.getPointY() + halfHeight);

        if (radius > 0) {
            rect.modify(position.getPointX() - halfWidth,
                    position.getPointY() + halfHeight - 2 * radius,
                    position.getPointX() - halfWidth + 2 * radius,
                    position.getPointY() + halfHeight);
            path.arcTo(rect, 90, 90, false);
        }

        path.lineTo(position.getPointX() - halfWidth, position.getPointY() - halfHeight + radius);

        if (radius > 0) {
            rect.modify(position.getPointX() - halfWidth,
                    position.getPointY() - halfHeight,
                    position.getPointX() - halfWidth + 2 * radius,
                    position.getPointY() - halfHeight + 2 * radius);
            path.arcTo(rect, 180, 90, false);
        }

        path.lineTo(position.getPointX() + halfWidth - radius, position.getPointY() - halfHeight);

        if (radius > 0) {
            rect.modify(position.getPointX() + halfWidth - 2 * radius,
                    position.getPointY() - halfHeight,
                    position.getPointX() + halfWidth,
                    position.getPointY() - halfHeight + 2 * radius);
            path.arcTo(rect, 270, 90, false);
        }
        path.close();

        trimPaths.apply(path);

        isPathValid = true;
        return path;
    }

    @Override
    public void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
                               KeyPath currentPartialKeyPath) {
        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
    }

    @Override
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        if (property == LottieProperty.RECTANGLE_SIZE) {
            sizeAnimation.setValueCallback((LottieValueCallback<Point>) callback);
        } else if (property == LottieProperty.POSITION) {
            positionAnimation.setValueCallback((LottieValueCallback<Point>) callback);
        } else if (property == LottieProperty.CORNER_RADIUS) {
            cornerRadiusAnimation.setValueCallback((LottieValueCallback<Float>) callback);
        }
    }
}
