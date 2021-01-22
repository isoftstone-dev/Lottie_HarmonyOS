package com.airbnb.lottie;

import ohos.media.image.PixelMap;

/**
 * Delegate to handle the loading of bitmaps that are not packaged in the assets of your app.
 *
 * @see LottieDrawable#setImageAssetDelegate(ImageAssetDelegate)
 */
public interface ImageAssetDelegate {
    PixelMap fetchBitmap(LottieImageAsset asset);
}
