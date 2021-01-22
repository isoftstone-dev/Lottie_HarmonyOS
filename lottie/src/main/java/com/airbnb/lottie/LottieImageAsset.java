package com.airbnb.lottie;

import ohos.media.image.PixelMap;


/**
 * Data class describing an image asset exported by bodymovin.
 */
public class LottieImageAsset {
  private final int width;
  private final int height;
  private final String id;
  private final String fileName;
  private final String dirName;
  /** Pre-set a bitmap for this asset */

  private PixelMap bitmap;

  public LottieImageAsset(int width, int height, String id, String fileName, String dirName) {
    this.width = width;
    this.height = height;
    this.id = id;
    this.fileName = fileName;
    this.dirName = dirName;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  @SuppressWarnings("unused") public String getDirName() {
    return dirName;
  }

  /**
   * Returns the bitmap that has been stored for this image asset if one was explicitly set.
   */
   public PixelMap getBitmap() {
    return bitmap;
  }

  /**
   * TODO
   */
  public void setBitmap( PixelMap bitmap) {
    this.bitmap = bitmap;
  }

  /**
   * Returns whether this asset has an embedded Bitmap or whether the fileName is a base64 encoded bitmap.
   */
  public boolean hasBitmap() {
    return bitmap != null || (fileName.startsWith("data:") && fileName.indexOf("base64,") > 0);
  }
}
