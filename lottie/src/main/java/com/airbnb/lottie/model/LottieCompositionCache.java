package com.airbnb.lottie.model;

import com.airbnb.lottie.LottieComposition;

import java.util.HashMap;

public class LottieCompositionCache {

  private static final LottieCompositionCache INSTANCE = new LottieCompositionCache();

  public static LottieCompositionCache getInstance() {
    return INSTANCE;
  }

  private final HashMap<String, LottieComposition> cache = new HashMap<>(20);

  LottieCompositionCache() {
  }


  public LottieComposition get( String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    return cache.get(cacheKey);
  }

  public void put( String cacheKey, LottieComposition composition) {
    if (cacheKey == null) {
      return;
    }
    cache.put(cacheKey, composition);
  }

  public void clear() {
    cache.clear();
  }

  /**
   * Set the maximum number of compositions to keep cached in memory.
   * This must be {@literal >} 0.
   */
  public void resize(int size) {
    //cache.resize(size);
  }
}
