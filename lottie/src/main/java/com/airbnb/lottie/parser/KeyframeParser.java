package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.keyframe.Interpolator;
import com.airbnb.lottie.animation.keyframe.LinearInterpolator;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import ohos.agp.utils.Point;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

class KeyframeParser {
  /**
   * Some animations get exported with insane cp values in the tens of thousands.
   * PathInterpolator fails to create the interpolator in those cases and hangs.
   * Clamping the cp helps prevent that.
   */
  private static final float MAX_CP_VALUE = 100;
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  private static HashMap<Integer,WeakReference<Interpolator>> pathInterpolatorCache;

  static JsonReader.Options NAMES = JsonReader.Options.of(
      "t",  // 1
      "s",  // 2
      "e",  // 3
      "o",  // 4
      "i",  // 5
      "h",  // 6
      "to", // 7
      "ti"  // 8
  );
  static JsonReader.Options INTERPOLATOR_NAMES = JsonReader.Options.of(
      "x",  // 1
      "y"   // 2
  );

  // https://github.com/airbnb/lottie-android/issues/464
  private static HashMap<Integer, WeakReference<Interpolator>> pathInterpolatorCache() {
    if (pathInterpolatorCache == null) {
      pathInterpolatorCache = new HashMap<>();
    }
    return pathInterpolatorCache;
  }


  private static WeakReference<Interpolator> getInterpolator(int hash) {
    // This must be synchronized because get and put isn't thread safe because
    // SparseArrayCompat has to create new sized arrays sometimes.
    synchronized (KeyframeParser.class) {
      return pathInterpolatorCache().get(hash);
    }
  }

  private static void putInterpolator(int hash, WeakReference<Interpolator> interpolator) {
    // This must be synchronized because get and put isn't thread safe because
    // SparseArrayCompat has to create new sized arrays sometimes.
    synchronized (KeyframeParser.class) {
      pathInterpolatorCache.put(hash, interpolator);
    }
  }

  /**
   * @param multiDimensional When true, the keyframe interpolators can be independent for the X and Y axis.
   */
  static <T> Keyframe<T> parse(JsonReader reader, LottieComposition composition,
                               float scale, ValueParser<T> valueParser, boolean animated, boolean multiDimensional) throws IOException {

    if (animated && multiDimensional) {
      return parseMultiDimensionalKeyframe(composition, reader, scale, valueParser);
    } else if (animated) {
      return parseKeyframe(composition, reader, scale, valueParser);
    } else {
      return parseStaticValue(reader, scale, valueParser);
    }
  }

  /**
   * beginObject will already be called on the keyframe so it can be differentiated with
   * a non animated value.
   */
  private static <T> Keyframe<T> parseKeyframe(LottieComposition composition, JsonReader reader,
      float scale, ValueParser<T> valueParser) throws IOException {
    Point cp1 = null;
    Point cp2 = null;

    float startFrame = 0;
    T startValue = null;
    T endValue = null;
    boolean hold = false;
    Interpolator interpolator = null;

    // Only used by PathKeyframe
    Point pathCp1 = null;
    Point pathCp2 = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0: // t
          startFrame = (float) reader.nextDouble();
          break;
        case 1: // s
          startValue = valueParser.parse(reader, scale);
          break;
        case 2: // e
          endValue = valueParser.parse(reader, scale);
          break;
        case 3: // o
          cp1 = JsonUtils.jsonToPoint(reader, 1f);
          break;
        case 4: // i
          cp2 = JsonUtils.jsonToPoint(reader, 1f);
          break;
        case 5: // h
          hold = reader.nextInt() == 1;
          break;
        case 6: // to
          pathCp1 = JsonUtils.jsonToPoint(reader, scale);
          break;
        case 7: // ti
          pathCp2 = JsonUtils.jsonToPoint(reader, scale);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    if (hold) {
      endValue = startValue;
      // TODO: create a HoldInterpolator so progress changes don't invalidate.
      interpolator = LINEAR_INTERPOLATOR;
    } else if (cp1 != null && cp2 != null) {
      interpolator = interpolatorFor(cp1, cp2);
    } else {
      interpolator = LINEAR_INTERPOLATOR;
    }

    Keyframe<T> keyframe = new Keyframe<>(composition, startValue, endValue, interpolator, startFrame, null);

    keyframe.pathCp1 = pathCp1;
    keyframe.pathCp2 = pathCp2;
    return keyframe;
  }

  private static <T> Keyframe<T> parseMultiDimensionalKeyframe(LottieComposition composition, JsonReader reader,
      float scale, ValueParser<T> valueParser) throws IOException {
    Point cp1 = null;
    Point cp2 = null;

    Point xCp1 = null;
    Point xCp2 = null;
    Point yCp1 = null;
    Point yCp2 = null;

    float startFrame = 0;
    T startValue = null;
    T endValue = null;
    boolean hold = false;
    Interpolator interpolator = null;
    Interpolator xInterpolator = null;
    Interpolator yInterpolator = null;

    // Only used by PathKeyframe
    Point pathCp1 = null;
    Point pathCp2 = null;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0: // t
          startFrame = (float) reader.nextDouble();
          break;
        case 1: // s
          startValue = valueParser.parse(reader, scale);
          break;
        case 2: // e
          endValue = valueParser.parse(reader, scale);
          break;
        case 3: // o
          if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject();
            float xCp1x = 0f;
            float xCp1y = 0f;
            float yCp1x = 0f;
            float yCp1y = 0f;
            while (reader.hasNext()) {
              switch (reader.selectName(INTERPOLATOR_NAMES)) {
                case 0: // x
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp1x = (float) reader.nextDouble();
                    yCp1x = xCp1x;
                  } else {
                    reader.beginArray();
                    xCp1x = (float) reader.nextDouble();
                    yCp1x = (float) reader.nextDouble();
                    reader.endArray();
                  }
                  break;
                case 1: // y
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp1y = (float) reader.nextDouble();
                    yCp1y = xCp1y;
                  } else {
                    reader.beginArray();
                    xCp1y = (float) reader.nextDouble();
                    yCp1y = (float) reader.nextDouble();
                    reader.endArray();
                  }
                  break;
                default:
                  reader.skipValue();
              }
            }
            xCp1 = new Point(xCp1x, xCp1y);
            yCp1 = new Point(yCp1x, yCp1y);
            reader.endObject();
          } else {
            cp1 = JsonUtils.jsonToPoint(reader, scale);
          }
          break;
        case 4: // i
          if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject();
            float xCp2x = 0f;
            float xCp2y = 0f;
            float yCp2x = 0f;
            float yCp2y = 0f;
            while (reader.hasNext()) {
              switch (reader.selectName(INTERPOLATOR_NAMES)) {
                case 0: // x
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp2x = (float) reader.nextDouble();
                    yCp2x = xCp2x;
                  } else {
                    reader.beginArray();
                    xCp2x = (float) reader.nextDouble();
                    yCp2x = (float) reader.nextDouble();
                    reader.endArray();
                  }
                  break;
                case 1: // y
                  if (reader.peek() == JsonReader.Token.NUMBER) {
                    xCp2y = (float) reader.nextDouble();
                    yCp2y = xCp2y;
                  } else {
                    reader.beginArray();
                    xCp2y = (float) reader.nextDouble();
                    yCp2y = (float) reader.nextDouble();
                    reader.endArray();
                  }
                  break;
                default:
                  reader.skipValue();
              }
            }
            xCp2 = new Point(xCp2x, xCp2y);
            yCp2 = new Point(yCp2x, yCp2y);
            reader.endObject();
          } else {
            cp2 = JsonUtils.jsonToPoint(reader, scale);
          }
          break;
        case 5: // h
          hold = reader.nextInt() == 1;
          break;
        case 6: // to
          pathCp1 = JsonUtils.jsonToPoint(reader, scale);
          break;
        case 7: // ti
          pathCp2 = JsonUtils.jsonToPoint(reader, scale);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    if (hold) {
      endValue = startValue;
      // TODO: create a HoldInterpolator so progress changes don't invalidate.
      //interpolator = LINEAR_INTERPOLATOR;
    } else if (cp1 != null && cp2 != null) {
      interpolator = interpolatorFor(cp1, cp2);
    } else if (xCp1 != null && yCp1 != null && xCp2 != null && yCp2 != null) {
      xInterpolator = interpolatorFor(xCp1, xCp2);
      yInterpolator = interpolatorFor(yCp1, yCp2);
    } else {
      interpolator = LINEAR_INTERPOLATOR;
    }

    Keyframe<T> keyframe;
    if (xInterpolator != null && yInterpolator != null) {
      keyframe = new Keyframe<>(composition, startValue, endValue, xInterpolator, yInterpolator, startFrame, null);
    } else {
      keyframe = new Keyframe<>(composition, startValue, endValue, interpolator, startFrame, null);
    }

    keyframe.pathCp1 = pathCp1;
    keyframe.pathCp2 = pathCp2;
    return keyframe;
  }

  private static Interpolator interpolatorFor(Point cp1, Point cp2) {
    Interpolator interpolator = null;
    float fxcp1 = cp1.getPointX();
    float fycp1 = cp1.getPointY();
    float fxcp2 = cp2.getPointX();
    float fycp2 = cp2.getPointY();
    fxcp1 = MiscUtils.clamp(fxcp1, -1f, 1f);
    fycp1 = MiscUtils.clamp(fycp1, -MAX_CP_VALUE, MAX_CP_VALUE);
    fxcp2 = MiscUtils.clamp(fxcp2, -1f, 1f);
    fycp2 = MiscUtils.clamp(fycp2, -MAX_CP_VALUE, MAX_CP_VALUE);
    int hash = Utils.hashFor(fxcp1, fycp1, fxcp2, fycp2);
    WeakReference<Interpolator> interpolatorRef = getInterpolator(hash);
    if (interpolatorRef != null) {
      interpolator = interpolatorRef.get();
    }
    if (interpolatorRef == null || interpolator == null) {

      // We failed to create the interpolator. Fall back to linear.
      interpolator = new LinearInterpolator();
      //interpolator = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);

      try {
        putInterpolator(hash, new WeakReference<>(interpolator));
      } catch (ArrayIndexOutOfBoundsException e) {
        // It is not clear why but SparseArrayCompat sometimes fails with this:
        //     https://github.com/airbnb/lottie-android/issues/452
        // Because this is not a critical operation, we can safely just ignore it.
        // I was unable to repro this to attempt a proper fix.
      }
    }
    return interpolator;
  }

  private static <T> Keyframe<T> parseStaticValue(JsonReader reader,
      float scale, ValueParser<T> valueParser) throws IOException {
    T value = valueParser.parse(reader, scale);
    return new Keyframe<>(value);
  }
}
