package com.airbnb.lottie;


import com.airbnb.lottie.manager.FontAssetManager;
import com.airbnb.lottie.manager.ImageAssetManager;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.parser.LayerParser;

import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.SimpleLottieValueCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import ohos.agp.animation.AnimatorValue;
import ohos.agp.utils.Matrix;
import ohos.media.image.PixelMap;


/**
 * This can be used to show an lottie animation in any place that would normally take a drawable.
 *
 * @see <a href="http://airbnb.io/lottie">Full Documentation</a>
 */
@SuppressWarnings({"WeakerAccess"})
public class LottieDrawable {
  public void invalidateSelf() {
    // invalidate();
  }

  private interface LazyCompositionTask {
    void run(LottieComposition composition);
  }

  private final Matrix matrix = new Matrix();
  private LottieComposition composition;
  //private final LottieValueAnimator animator = new LottieValueAnimator();
  private float scale = 1f;
  private boolean systemAnimationsEnabled = true;
  private boolean safeMode = false;

  private final ArrayList<LazyCompositionTask> lazyCompositionTasks = new ArrayList<>();
  private final AnimatorValue.ValueUpdateListener  progressUpdateListener = new AnimatorValue.ValueUpdateListener() {
    @Override
    public void onUpdate(AnimatorValue var1, float var2) {
      if (compositionLayer != null) {
        compositionLayer.setProgress(var2);
      }
    }
  };

  private ImageAssetManager imageAssetManager;

  private String imageAssetsFolder;

  private ImageAssetDelegate imageAssetDelegate;

  private FontAssetManager fontAssetManager;

  FontAssetDelegate fontAssetDelegate;

  TextDelegate textDelegate;
  private boolean enableMergePaths;

  private CompositionLayer compositionLayer;
  private int alpha = 255;
  private boolean performanceTrackingEnabled;
  private boolean outlineMasksAndMattes;
  private boolean isApplyingOpacityToLayersEnabled;
  private boolean isExtraScaleEnabled = true;
  /**
   * True if the drawable has not been drawn since the last invalidateSelf.
   * We can do this to prevent things like bounds from getting recalculated
   * many times.
   */
  private boolean isDirty = false;

  @Retention(RetentionPolicy.SOURCE)
  public @interface RepeatMode {
  }

  /**
   * When the animation reaches the end and <code>repeatCount</code> is INFINITE
   * or a positive value, the animation restarts from the beginning.
   */
  //public static final int RESTART = AnimatorValue.RESTART;
  /**
   * When the animation reaches the end and <code>repeatCount</code> is INFINITE
   * or a positive value, the animation reverses direction on every iteration.
   */
  //public static final int REVERSE = ValueAnimator.REVERSE;
  /**
   * This value used used with the {@link (int)} property to repeat
   * the animation indefinitely.
   */
  //public static final int INFINITE = ValueAnimator.INFINITE;

  public LottieDrawable() {

  }

  /**
   * Returns whether or not any layers in this composition has masks.
   */
  public boolean hasMasks() {
    return compositionLayer != null && compositionLayer.hasMasks();
  }

  /**
   * Returns whether or not any layers in this composition has a matte layer.
   */
  public boolean hasMatte() {
    return compositionLayer != null && compositionLayer.hasMatte();
  }

  public boolean enableMergePathsForKitKatAndAbove() {
    return enableMergePaths;
  }


  public boolean isMergePathsEnabledForKitKatAndAbove() {
    return enableMergePaths;
  }

  /**
   * If you use image assets, you must explicitly specify the folder in assets/ in which they are
   * located because bodymovin uses the name filenames across all compositions (img_#).
   * Do NOT rename the images themselves.
   * <p>
   * If your images are located in src/main/assets/airbnb_loader/ then call
   * `setImageAssetsFolder("airbnb_loader/");`.
   * <p>
   * <p>
   * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
   * from After Effects. If your images look like they could be represented with vector shapes,
   * see if it is possible to convert them to shape layers and re-export your animation. Check
   * the documentation at http://airbnb.io/lottie for more information about importing shapes from
   * Sketch or Illustrator to avoid this.
   */
  public void setImagesAssetsFolder( String imageAssetsFolder) {
    this.imageAssetsFolder = imageAssetsFolder;
  }


  public String getImageAssetsFolder() {
    return imageAssetsFolder;
  }

  /**
   * Create a composition with {@link}
   *
   * @return True if the composition is different from the previously set composition, false otherwise.
   */
  public boolean setComposition(LottieComposition composition) {
    if (this.composition == composition) {
      return false;
    }

    isDirty = false;

    this.composition = composition;
    //buildCompositionLayer();
    //animator.setComposition(composition);
    //setProgress(animator.getAnimatedFraction());
    //setScale(scale);

    // We copy the tasks to a new ArrayList so that if this method is called from multiple threads,
    // then there won't be two iterators iterating and removing at the same time.
    Iterator<LazyCompositionTask> it = new ArrayList<>(lazyCompositionTasks).iterator();
    while (it.hasNext()) {
      LazyCompositionTask t = it.next();
      // The task should never be null but it appears to happen in rare cases. Maybe it's an oem-specific or ART bug.
      // https://github.com/airbnb/lottie-android/issues/1702
      if (t != null) {
        t.run(composition);
      }
      it.remove();
    }
    lazyCompositionTasks.clear();

    //composition.setPerformanceTrackingEnabled(performanceTrackingEnabled);

    // Ensure that ImageView updates the drawable width/height so it can
    // properly calculate its drawable matrix.
//    Callback callback = getCallback();
//    if (callback instanceof ImageView) {
//      ((ImageView) callback).setImageDrawable(null);
//      ((ImageView) callback).setImageDrawable(this);
//    }

    return true;
  }
}
