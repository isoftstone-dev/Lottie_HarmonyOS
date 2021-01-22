package com.airbnb.lottie.animation.keyframe;


import com.airbnb.lottie.value.Keyframe;
import ohos.agp.render.Path;
import ohos.agp.render.PathMeasure;
import ohos.agp.utils.Point;

import java.util.List;

public class PathKeyframeAnimation extends KeyframeAnimation<Point> {
  private final Point point = new Point();
  private final float[] pos = new float[2];
  private final PathMeasure pathMeasure = new PathMeasure(null,false);
  private PathKeyframe pathMeasureKeyframe;

  public PathKeyframeAnimation(List<? extends Keyframe<Point>> keyframes) {
    super(keyframes);
  }

  @Override public Point getValue(Keyframe<Point> keyframe, float keyframeProgress) {
    PathKeyframe pathKeyframe = (PathKeyframe) keyframe;
    Path path = pathKeyframe.getPath();
    if (path == null) {
      return keyframe.startValue;
    }

    if (valueCallback != null) {
      Point value = valueCallback.getValueInternal(pathKeyframe.startFrame, pathKeyframe.endFrame,
              pathKeyframe.startValue, pathKeyframe.endValue, getLinearCurrentKeyframeProgress(),
              keyframeProgress, getProgress());
      if (value != null) {
        return value;
      }
    }

    if (pathMeasureKeyframe != pathKeyframe) {
      pathMeasure.setPath(path, false);
      pathMeasureKeyframe = pathKeyframe;
    }

    pathMeasure.getPosTan(keyframeProgress * pathMeasure.getLength(), pos, null);
    point.modify(pos[0], pos[1]);
    return point;
  }
}
