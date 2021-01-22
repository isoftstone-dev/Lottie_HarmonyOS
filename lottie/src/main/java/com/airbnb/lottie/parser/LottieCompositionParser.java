package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;
import ohos.agp.utils.RectFloat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LottieCompositionParser {
  static JsonReader.Options NAMES = JsonReader.Options.of(
      "w",
      "h",
      "ip",
      "op",
      "fr",
      "v",
      "layers",
      "assets",
      "fonts",
      "chars",
      "markers");

  public static LottieComposition parse(JsonReader reader) throws IOException {
    float scale = Utils.dpScale();
    float startFrame = 0f;
    float endFrame = 0f;
    float frameRate = 0f;
    final HashMap<Long, Layer> layerMap = new HashMap<>();
    final List<Layer> layers = new ArrayList<>();
    int width = 0;
    int height = 0;
    Map<String, List<Layer>> precomps = new HashMap<>();
    Map<String, LottieImageAsset> images = new HashMap<>();
    Map<String, Font> fonts = new HashMap<>();
    List<Marker> markers = new ArrayList<>();
    HashMap<Integer, FontCharacter> characters = new HashMap<>();

    LottieComposition composition = new LottieComposition();
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          width = reader.nextInt();
          break;
        case 1:
          height = reader.nextInt();
          break;
        case 2:
          startFrame = (float) reader.nextDouble();
          break;
        case 3:
          endFrame = (float) reader.nextDouble() - 0.01f;
          break;
        case 4:
          frameRate = (float) reader.nextDouble();
          break;
        case 5:
          String version = reader.nextString();
          String[] versions = version.split("\\.");
          int majorVersion = Integer.parseInt(versions[0]);
          int minorVersion = Integer.parseInt(versions[1]);
          int patchVersion = Integer.parseInt(versions[2]);
          if (!Utils.isAtLeastVersion(majorVersion, minorVersion, patchVersion,
              4, 4, 0)) {
            composition.addWarning("Lottie only supports bodymovin >= 4.4.0");
          }
          break;
        case 6:
          parseLayers(reader, composition, layers, layerMap);
        default:
          reader.skipValue();

      }
    }
    int scaledWidth = (int) (width * scale);
    int scaledHeight = (int) (height * scale);
    RectFloat bounds = new RectFloat(0, 0, scaledWidth, scaledHeight);

    composition.init(bounds, startFrame, endFrame, frameRate, layers, layerMap, precomps,
        images, characters, fonts, markers);

    return composition;
  }

  private static void parseLayers(JsonReader reader, LottieComposition composition,
                                  List<Layer> layers, HashMap<Long, Layer> layerMap) throws IOException {
    int imageCount = 0;
    reader.beginArray();
    while (reader.hasNext()) {
      Layer layer = LayerParser.parse(reader, composition);
      if (layer.getLayerType() == Layer.LayerType.IMAGE) {
        imageCount++;
      }
      layers.add(layer);
      layerMap.put(layer.getId(), layer);

      if (imageCount > 4) {
        Logger.warning("You have " + imageCount + " images. Lottie should primarily be " +
            "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
            " to shape layers.");
      }
    }
    reader.endArray();
  }
}
