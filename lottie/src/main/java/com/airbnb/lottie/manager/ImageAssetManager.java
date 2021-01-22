package com.airbnb.lottie.manager;


import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;
import ohos.agp.components.Component;
import ohos.app.Context;
import ohos.media.image.PixelMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ImageAssetManager {
  private static final Object bitmapHashLock = new Object();

  private final Context context;
  private String imagesFolder;
   private ImageAssetDelegate delegate;
  private final Map<String, LottieImageAsset> imageAssets;

  public ImageAssetManager(Component component, String imagesFolder,
                           ImageAssetDelegate delegate, Map<String, LottieImageAsset> imageAssets) {
    this.imagesFolder = imagesFolder;
    if (!(imagesFolder.isEmpty()) &&
        this.imagesFolder.charAt(this.imagesFolder.length() - 1) != '/') {
      this.imagesFolder += '/';
    }



    context = component.getContext();
    this.imageAssets = imageAssets;
    setDelegate(delegate);
  }

  public void setDelegate( ImageAssetDelegate assetDelegate) {
    this.delegate = assetDelegate;
  }

  /**
   * Returns the previously set bitmap or null.
   */
   public PixelMap updateBitmap(String id,  PixelMap bitmap) {
    if (bitmap == null) {
      LottieImageAsset asset = imageAssets.get(id);
      PixelMap ret = asset.getBitmap();
      asset.setBitmap(null);
      return ret;
    }
     PixelMap prevBitmap = imageAssets.get(id).getBitmap();
    putBitmap(id, bitmap);
    return prevBitmap;
  }

   public PixelMap bitmapForId(String id) {
    LottieImageAsset asset = imageAssets.get(id);
    if (asset == null) {
      return null;
    }
     PixelMap bitmap = asset.getBitmap();
    if (bitmap != null) {
      return bitmap;
    }

    if (delegate != null) {
      bitmap = delegate.fetchBitmap(asset);
      if (bitmap != null) {
        putBitmap(id, bitmap);
      }
      return bitmap;
    }

    String filename = asset.getFileName();

    //PixelMap.InitializationOptions opts = new BitmapFactory.Options();
    //opts.inScaled = true;
    //opts.inDensity = 160;

    if (filename.startsWith("data:") && filename.indexOf("base64,") > 0) {
      // Contents look like a base64 data URI, with the format data:image/png;base64,<data>.
      byte[] data;
      try {
        //data = Base64.Decoder(filename.substring(filename.indexOf(',') + 1), Base64.DEFAULT);
      } catch (IllegalArgumentException e) {
        Logger.warning("data URL did not have correct base64 format.", e);
        return null;
      }
      //bitmap = new PixelMap();
      return putBitmap(id, bitmap);
    }

    InputStream is;
    try {
      if (imagesFolder.isEmpty()) {
        throw new IllegalStateException("You must set an images folder before loading an image." +
            " Set it with LottieComposition#setImagesFolder or LottieDrawable#setImagesFolder");
      }
      //is = context.getAssets().open(imagesFolder + filename);
    } catch (Exception e) {
      Logger.warning("Unable to open asset.", e);
      return null;
    }
    //bitmap = BitmapFactory.decodeStream(is, null, opts);
    bitmap = Utils.resizeBitmapIfNeeded(bitmap, asset.getWidth(), asset.getHeight());
    return bitmap;
  }

  public boolean hasSameContext(Context context) {
    return context == null && this.context == null || this.context.equals(context);
  }

  private PixelMap putBitmap(String key,  PixelMap bitmap) {
    synchronized (bitmapHashLock) {
      imageAssets.get(key).setBitmap(bitmap);
      return bitmap;
    }
  }
}
