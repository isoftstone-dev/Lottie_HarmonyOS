package com.airbnb.lottie.utils;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieLogger;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.HashSet;
import java.util.Set;

/**
 * Default logger.
 * Warnings with same message will only be logged once.
 */
public class LogcatLogger implements LottieLogger {
  // 定义日志标签
  private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, L.TAG);
  /**
   * Set to ensure that we only log each message one time max.
   */
  private static final Set<String> loggedMessages = new HashSet<>();


  public void debug(String message) {
    debug(message, null);
  }

  public void debug(String message, Throwable exception) {
    if (L.DBG) {
      HiLog.debug(label, message, exception);
    }
  }

  public void warning(String message) {
    warning(message, null);
  }

  public void warning(String message, Throwable exception) {
    if (loggedMessages.contains(message)) {
      return;
    }

    HiLog.warn(label, message, exception);

    loggedMessages.add(message);
  }

  @Override public void error(String message, Throwable exception) {
    if (L.DBG) {
      HiLog.debug(label, message, exception);
    }
  }
}
