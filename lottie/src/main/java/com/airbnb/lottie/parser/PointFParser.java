package com.airbnb.lottie.parser;

import com.airbnb.lottie.parser.moshi.JsonReader;
import ohos.agp.utils.Point;

import java.io.IOException;

public class PointFParser implements ValueParser<Point> {
  public static final PointFParser INSTANCE = new PointFParser();

  private PointFParser() {
  }

  @Override
  public Point parse(JsonReader reader, float scale) throws IOException {
    JsonReader.Token token = reader.peek();
    if (token == JsonReader.Token.BEGIN_ARRAY) {
      return JsonUtils.jsonToPoint(reader, scale);
    } else if (token == JsonReader.Token.BEGIN_OBJECT) {
      return JsonUtils.jsonToPoint(reader, scale);
    } else if (token == JsonReader.Token.NUMBER) {
      // This is the case where the static value for a property is an array of numbers.
      // We begin the array to see if we have an array of keyframes but it's just an array
      // of static numbers instead.
      Point point = new Point((float) reader.nextDouble() * scale, (float) reader.nextDouble() * scale);
      while (reader.hasNext()) {
        reader.skipValue();
      }
      return point;
    } else {
      throw new IllegalArgumentException("Cannot convert json to point. Next token is " + token);
    }
  }
}
