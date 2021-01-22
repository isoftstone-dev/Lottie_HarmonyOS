package com.airbnb.lottie.network;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * The result of the operation of obtaining a Lottie animation
 */
public interface LottieFetchResult extends Closeable {
  /**
   * @return Is the operation successful
   */
  boolean isSuccessful();

  /**
   *
   * @return Received content stream
   */
  InputStream bodyByteStream() throws IOException;

  /**
   *
   * @return Type of content received
   */

  String contentType();

  /**
   *
   * @return Operation error
   */

  String error();
}
