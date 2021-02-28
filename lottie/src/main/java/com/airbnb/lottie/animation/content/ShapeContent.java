package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapePath;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import ohos.agp.render.Path;
import ohos.agp.render.PathMeasure;
import ohos.agp.utils.Point;

import java.util.List;

public class ShapeContent implements PathContent, BaseKeyframeAnimation.AnimationListener {
    private final Path path = new Path();

    private final String name;
    private final boolean hidden;
    private final LottieDrawable lottieDrawable;
    private final BaseKeyframeAnimation<?, Path> shapeAnimation;

    private boolean isPathValid;
    private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();

    public ShapeContent(LottieDrawable lottieDrawable, BaseLayer layer, ShapePath shape) {
        name = shape.getName();
        hidden = shape.isHidden();
        this.lottieDrawable = lottieDrawable;
        shapeAnimation = shape.getShapePath().createAnimation();
        layer.addAnimation(shapeAnimation);
        shapeAnimation.addUpdateListener(this);
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
                // Trim path individually will be handled by the stroke where paths are combined.
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


        path.set(shapeAnimation.getValue());
//        path.reset();
//        path.moveTo(new Point(0, 0));
//        path.lineTo(new Point(300, 300));


        path.setFillType(Path.FillType.EVEN_ODD);

//        // 使用PathMeasure获取path总长度
//        PathMeasure pathMeasure = new PathMeasure(null, false);
//        pathMeasure.setPath(path, false);
//        float length = pathMeasure.getLength();
//
//        // 截取path的前半段，保存在tempPath中
//        Path tempPath = new Path();
//        tempPath.reset();
//        pathMeasure.getSegment(0,length / 2, tempPath,true);
//
//        // 获取tempPath的长度，发现长度为0
//        pathMeasure.setPath(tempPath, false);
//        length = pathMeasure.getLength();

        //trimPaths.apply(path);
        //path.set(tempPath);

        isPathValid = true;
        return path;
    }

    @Override
    public String getName() {
        return name;
    }
}
