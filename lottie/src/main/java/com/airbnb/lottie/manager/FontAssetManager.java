package com.airbnb.lottie.manager;

import com.airbnb.lottie.FontAssetDelegate;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.MutablePair;

import java.util.HashMap;
import java.util.Map;

public class FontAssetManager {
  private final MutablePair<String> tempPair = new MutablePair<>();

  /** Pair is (fontName, fontStyle) */


  public FontAssetManager() {

  }

  public void setDelegate( FontAssetDelegate assetDelegate) {
  }

  /**
   * Sets the default file extension (include the `.`).
   *
   * e.g. `.ttf` `.otf`
   *
   * Defaults to `.ttf`
   */
  @SuppressWarnings("unused") public void setDefaultFontFileExtension(String defaultFontFileExtension) {

  }

  public Font getTypeface(String fontFamily, String style) {
    tempPair.set(fontFamily, style);

    return null        ;
  }

  private Font getFontFamily(String fontFamily) {
    return null;
  }

  private Font typefaceForStyle(Font typeface, String style) {


    return null;
  }
}
