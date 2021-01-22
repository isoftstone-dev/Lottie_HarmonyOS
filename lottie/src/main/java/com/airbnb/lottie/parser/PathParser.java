package com.airbnb.lottie.parser;

import com.airbnb.lottie.parser.moshi.JsonReader;
import ohos.agp.utils.Point;

import java.io.IOException;

public class PathParser implements ValueParser<Point> {
  public static final PathParser INSTANCE = new PathParser();

  private PathParser() {}

  @Override public Point parse(JsonReader reader, float scale) throws IOException {
    return JsonUtils.jsonToPoint(reader, scale);
  }
}
