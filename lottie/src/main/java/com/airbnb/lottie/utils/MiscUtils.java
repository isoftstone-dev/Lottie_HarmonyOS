package com.airbnb.lottie.utils;

import com.airbnb.lottie.animation.content.KeyPathElementContent;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.ShapeData;
import ohos.agp.render.Path;
import ohos.agp.utils.Point;

import java.util.List;

public class MiscUtils {
  private static Point pathFromDataCurrentPoint = new Point();

  public static Point addPoints(Point p1, Point p2) {
    return new Point(p1.getPointX() + p2.getPointX(), p1.getPointY() + p2.getPointY());
  }

  public static void getPathFromData(ShapeData shapeData, Path outPath) {
    outPath.reset();
    Point initialPoint = shapeData.getInitialPoint();
    outPath.moveTo(initialPoint.getPointX(), initialPoint.getPointY());
    //outPath.moveTo(0, 0);
    pathFromDataCurrentPoint.modify(initialPoint.getPointX(), initialPoint.getPointY());
    for (int i = 0; i < shapeData.getCurves().size(); i++) {
      CubicCurveData curveData = shapeData.getCurves().get(i);
      Point cp1 = curveData.getControlPoint1();
      Point cp2 = curveData.getControlPoint2();
      Point vertex = curveData.getVertex();

      if (cp1.equals(pathFromDataCurrentPoint) && cp2.equals(vertex)) {
        // On some phones like Samsung phones, zero valued control points can cause artifacting.
        // https://github.com/airbnb/lottie-android/issues/275
        //
        // This does its best to add a tiny value to the vertex without affecting the final
        // animation as much as possible.
        // outPath.rMoveTo(0.01f, 0.01f);
        outPath.lineTo(vertex.getPointX(), vertex.getPointY());
        //outPath.lineTo(300, 300);
      } else {
        outPath.cubicTo(new Point(cp1.getPointX(), cp1.getPointY()),new Point(cp2.getPointX(), cp2.getPointY()),new Point(vertex.getPointX(), vertex.getPointY()));
      }
      pathFromDataCurrentPoint.modify(vertex.getPointX(), vertex.getPointY());
    }
    if (shapeData.isClosed()) {
      outPath.close();
    }
  }

  public static float lerp(float a, float b, float percentage) {
    if (percentage < 0 || percentage > 1) {
      percentage = 0;
      //throw new NumberFormatException("percentage must be from 0 to 1");
    }
    return a + percentage * (b - a);
  }

  public static double lerp(double a, double b, double percentage) {
    if (percentage < 0 || percentage > 1) {
      throw new NumberFormatException("percentage must be from 0 to 1");
    }
    return a + percentage * (b - a);
  }

  public static int lerp(int a, int b, float percentage) {
    if (percentage < 0 || percentage > 1) {
      throw new NumberFormatException("percentage must be from 0 to 1");
    }
    return (int) (a + percentage * (b - a));
  }

  static int floorMod(float x, float y) {
    return floorMod((int) x, (int) y);
  }

  private static int floorMod(int x, int y) {
    return x - y * floorDiv(x, y);
  }

  private static int floorDiv(int x, int y) {
    int r = x / y;
    boolean sameSign = (x ^ y) >= 0;
    int mod = x % y;
    if (!sameSign && mod != 0) {
      r--;
    }
    return r;
  }

  public static int clamp(int number, int min, int max) {
    return Math.max(min, Math.min(max, number));
  }

  public static float clamp(float number, float min, float max) {
    return Math.max(min, Math.min(max, number));
  }

  public static double clamp(double number, double min, double max) {
    return Math.max(min, Math.min(max, number));
  }

  public static boolean contains(float number, float rangeMin, float rangeMax) {
    return number >= rangeMin && number <= rangeMax;
  }

  /**
   * Helper method for any {@link KeyPathElementContent} that will check if the content
   * fully matches the keypath then will add itself as the final key, resolve it, and add
   * it to the accumulator list.
   *
   * Any {@link KeyPathElementContent} should call through to this as its implementation of
   * {@link KeyPathElementContent#resolveKeyPath(KeyPath, int, List, KeyPath)}.
   */
  public static void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath, KeyPathElementContent content) {
    if (keyPath.fullyResolvesTo(content.getName(), depth)) {
      currentPartialKeyPath = currentPartialKeyPath.addKey(content.getName());
      accumulator.add(currentPartialKeyPath.resolve(content));
    }
  }
}
