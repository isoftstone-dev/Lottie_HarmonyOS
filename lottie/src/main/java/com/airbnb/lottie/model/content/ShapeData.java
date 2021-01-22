package com.airbnb.lottie.model.content;

import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.MiscUtils;
import ohos.agp.utils.Point;

import java.util.ArrayList;
import java.util.List;

public class ShapeData {
  private final List<CubicCurveData> curves;
  private Point initialPoint;
  private boolean closed;

  public ShapeData(Point initialPoint, boolean closed, List<CubicCurveData> curves) {
    this.initialPoint = initialPoint;
    this.closed = closed;
    this.curves = new ArrayList<>(curves);
  }

  public ShapeData() {
    curves = new ArrayList<>();
  }

  private void setInitialPoint(float x, float y) {
    if (initialPoint == null) {
      initialPoint = new Point();
    }
    initialPoint.modify(x, y);
  }

  public Point getInitialPoint() {
    return initialPoint;
  }

  public boolean isClosed() {
    return closed;
  }

  public List<CubicCurveData> getCurves() {
    return curves;
  }

  public void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2, float percentage) {
    if (percentage < 0 || percentage > 1) {
      throw  new NumberFormatException("percentage must be from 0 to 1");
    }
    if (initialPoint == null) {
      initialPoint = new Point();
    }
    closed = shapeData1.isClosed() || shapeData2.isClosed();


    if (shapeData1.getCurves().size() != shapeData2.getCurves().size()) {
      Logger.warning("Curves must have the same number of control points. Shape 1: " +
          shapeData1.getCurves().size() + "\tShape 2: " + shapeData2.getCurves().size());
    }
    
    int points = Math.min(shapeData1.getCurves().size(), shapeData2.getCurves().size());
    if (curves.size() < points) {
      for (int i = curves.size(); i < points; i++) {
        curves.add(new CubicCurveData());
      }
    } else if (curves.size() > points) {
      for (int i = curves.size() - 1; i >= points; i--) {
        curves.remove(curves.size() - 1);
      }
    }

    Point initialPoint1 = shapeData1.getInitialPoint();
    Point initialPoint2 = shapeData2.getInitialPoint();

    setInitialPoint(MiscUtils.lerp(initialPoint1.getPointX(), initialPoint2.getPointX(), percentage),
        MiscUtils.lerp(initialPoint1.getPointY(), initialPoint2.getPointY(), percentage));

    for (int i = curves.size() - 1; i >= 0; i--) {
      CubicCurveData curve1 = shapeData1.getCurves().get(i);
      CubicCurveData curve2 = shapeData2.getCurves().get(i);

      Point cp11 = curve1.getControlPoint1();
      Point cp21 = curve1.getControlPoint2();
      Point vertex1 = curve1.getVertex();

      Point cp12 = curve2.getControlPoint1();
      Point cp22 = curve2.getControlPoint2();
      Point vertex2 = curve2.getVertex();

      curves.get(i).setControlPoint1(
          MiscUtils.lerp(cp11.getPointX(), cp12.getPointX(), percentage), MiscUtils.lerp(cp11.getPointY(), cp12.getPointY(),
              percentage));
      curves.get(i).setControlPoint2(
          MiscUtils.lerp(cp21.getPointX(), cp22.getPointX(), percentage), MiscUtils.lerp(cp21.getPointY(), cp22.getPointY(),
              percentage));
      curves.get(i).setVertex(
          MiscUtils.lerp(vertex1.getPointX(), vertex2.getPointX(), percentage), MiscUtils.lerp(vertex1.getPointY(), vertex2.getPointY(),
              percentage));
    }
  }

  @Override public String toString() {
    return "ShapeData{" + "numCurves=" + curves.size() +
        "closed=" + closed +
        '}';
  }
}
