package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.FillContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import ohos.agp.render.Path;

public class ShapeFill implements ContentModel {
  private final boolean fillEnabled;
  private final Path.FillType fillType;
  private final String name;
   private final AnimatableColorValue color;
   private final AnimatableIntegerValue opacity;
  private final boolean hidden;

  public ShapeFill(String name, boolean fillEnabled, Path.FillType fillType,
                    AnimatableColorValue color,  AnimatableIntegerValue opacity, boolean hidden) {
    this.name = name;
    this.fillEnabled = fillEnabled;
    this.fillType = fillType;
    this.color = color;
    this.opacity = opacity;
    this.hidden = hidden;
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

  public Path.FillType getFillType() {
    return fillType;
  }

  public boolean isHidden() {
    return hidden;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new FillContent(drawable, layer, this);
  }

  @Override
  public String toString() {
    return "ShapeFill{" + "color=" +
        ", fillEnabled=" + fillEnabled +
        '}';
  }
}
