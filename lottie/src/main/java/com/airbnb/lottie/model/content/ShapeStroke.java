package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.StrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import ohos.agp.render.Paint;

import java.util.List;

public class ShapeStroke implements ContentModel {
  public enum LineCapType {
    BUTT,
    ROUND,
    UNKNOWN;

    public Paint.StrokeCap toPaintCap() {
      switch (this) {
        case BUTT:
          return Paint.StrokeCap.BUTT_CAP;
        case ROUND:
          return Paint.StrokeCap.ROUND_CAP;
        case UNKNOWN:
        default:
          return Paint.StrokeCap.SQUARE_CAP;
      }
    }
  }

  public enum LineJoinType {
    MITER,
    ROUND,
    BEVEL;

    public Paint.Join toPaintJoin() {
      switch (this) {
        case BEVEL:
          return Paint.Join.BEVEL_JOIN;
        case MITER:
          return Paint.Join.MITER_JOIN;
        case ROUND:
          return Paint.Join.ROUND_JOIN;
      }
      return null;
    }
  }

  private final String name;
   private final AnimatableFloatValue offset;
  private final List<AnimatableFloatValue> lineDashPattern;
  private final AnimatableColorValue color;
  private final AnimatableIntegerValue opacity;
  private final AnimatableFloatValue width;
  private final LineCapType capType;
  private final LineJoinType joinType;
  private final float miterLimit;
  private final boolean hidden;

  public ShapeStroke(String name,  AnimatableFloatValue offset,
                     List<AnimatableFloatValue> lineDashPattern, AnimatableColorValue color,
                     AnimatableIntegerValue opacity, AnimatableFloatValue width, LineCapType capType,
                     LineJoinType joinType, float miterLimit, boolean hidden) {
    this.name = name;
    this.offset = offset;
    this.lineDashPattern = lineDashPattern;
    this.color = color;
    this.opacity = opacity;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
    this.miterLimit = miterLimit;
    this.hidden = hidden;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new StrokeContent(drawable, layer, this);
  }

  public String getName() {
    return name;
  }

  public AnimatableColorValue getColor() {
    return color;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public AnimatableFloatValue getWidth() {
    return width;
  }

  public List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  public AnimatableFloatValue getDashOffset() {
    return offset;
  }

  public LineCapType getCapType() {
    return capType;
  }

  public LineJoinType getJoinType() {
    return joinType;
  }

  public float getMiterLimit() {
    return miterLimit;
  }

  public boolean isHidden() {
    return hidden;
  }
}
