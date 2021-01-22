package com.airbnb.lottie.model;


import ohos.agp.utils.Point;


public class CubicCurveData {
  private final Point controlPoint1;
  private final Point controlPoint2;
  private final Point vertex;

  public CubicCurveData() {
    controlPoint1 = new Point();
    controlPoint2 = new Point();
    vertex = new Point();
  }

  public CubicCurveData(Point controlPoint1, Point controlPoint2, Point vertex) {
    this.controlPoint1 = controlPoint1;
    this.controlPoint2 = controlPoint2;
    this.vertex = vertex;
  }

  public void setControlPoint1(float x, float y) {
    controlPoint1.modify(x, y);
  }

  public Point getControlPoint1() {
    return controlPoint1;
  }

  public void setControlPoint2(float x, float y) {
    controlPoint2.modify(x, y);
  }

  public Point getControlPoint2() {
    return controlPoint2;
  }

  public void setVertex(float x, float y) {
    vertex.modify(x, y);
  }

  public Point getVertex() {
    return vertex;
  }
}
