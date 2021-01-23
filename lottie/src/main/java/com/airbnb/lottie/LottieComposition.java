package com.airbnb.lottie;


import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.LayerParser;
import com.airbnb.lottie.parser.LottieCompositionMoshiParser;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;
import ohos.agp.render.Canvas;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Rect;
import ohos.agp.utils.RectFloat;
import ohos.app.Context;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static okio.Okio.buffer;
import static okio.Okio.source;

/**
 * After Effects/Bodymovin composition model. This is the serialized model from which the
 * animation will be created.
 *
 * To create one, use {}.
 *
 * It can be used with a {} or
 * {}.
 */
public class LottieComposition {

  private final PerformanceTracker performanceTracker = new PerformanceTracker();
  private final HashSet<String> warnings = new HashSet<>();
  private Map<String, List<Layer>> precomps;
  private Map<String, LottieImageAsset> images;
  /** Map of font names to fonts */
  private Map<String, Font> fonts;
  private List<Marker> markers;
  private HashMap<Integer, FontCharacter> characters;
  private HashMap<Long, Layer> layerMap;
  public List<Layer> layers;
  // This is stored as a set to avoid duplicates.
  private RectFloat bounds;
  private float startFrame;
  private float endFrame;
  private float frameRate;
  /**
   * Used to determine if an animation can be drawn with hardware acceleration.
   */
  private boolean hasDashPattern;
  /**
   * Counts the number of mattes and masks. Before Android switched to SKIA
   * for drawing in Oreo (API 28), using hardware acceleration with mattes and masks
   * was only faster until you had ~4 masks after which it would actually become slower.
   */
  private int maskAndMatteCount = 0;

  public void init(RectFloat bounds, float startFrame, float endFrame, float frameRate,
                   List<Layer> layers, HashMap<Long, Layer> layerMap, Map<String,
          List<Layer>> precomps, Map<String, LottieImageAsset> images,
                   HashMap<Integer, FontCharacter> characters, Map<String, Font> fonts,
                   List<Marker> markers) {
    this.bounds = bounds;
    this.startFrame = startFrame;
    this.endFrame = endFrame;
    this.frameRate = frameRate;
    this.layers = layers;
    this.layerMap = layerMap;
    this.precomps = precomps;
    this.images = images;
    this.characters = characters;
    this.fonts = fonts;
    this.markers = markers;
  }
  public float getStartFrame() {
    return startFrame;
  }

  public float getEndFrame() {
    return endFrame;
  }

  public float getFrameRate() {return frameRate;}

  public float getDurationFrames() {
    return endFrame - startFrame;
  }

  public void addWarning(String warning) {
    Logger.warning(warning);
    warnings.add(warning);
  }

  public void setHasDashPattern(boolean hasDashPattern) {
    this.hasDashPattern = hasDashPattern;
  }

  public RectFloat getBounds() {
    return bounds;
  }

  public void incrementMatteOrMaskCount(int amount)  {
    maskAndMatteCount += amount;
  }

  public Layer layerModelForId(long id)  {
    return layerMap.get(id);
  }

  public List<Layer> getPrecomps(String id) {return precomps.get(id);
  }

  public HashMap<Integer, FontCharacter> getCharacters() {
    return characters;
  }

  public Map<String, Font> getFonts() {
    return fonts;
  }

  public void parse(Context context, String filename) throws IOException {
    // 获取资源管理器
    ResourceManager resourceManager = context.getResourceManager();
    RawFileEntry rawFileEntry = resourceManager.getRawFileEntry("resources/rawfile/bullseye.json");
    Resource resource = rawFileEntry.openRawFile();

    JsonReader reader = JsonReader.of(buffer(source(resource)));

    LottieComposition composition = LottieCompositionMoshiParser.parse(reader);

  }

  
  private final Matrix matrix = new Matrix();
  private int alpha = 255;

  private void draw(Canvas canvas) {


    matrix.reset();

    //compositionLayer.draw(canvas, matrix, alpha);

  }

  public  List<Layer>  getLayers()  {
    return layers;
  }
}
