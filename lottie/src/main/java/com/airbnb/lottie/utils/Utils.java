package com.airbnb.lottie.utils;

import com.airbnb.lottie.L;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.content.TrimPathContent;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;

import java.io.Closeable;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.nio.channels.ClosedChannelException;

import javax.net.ssl.SSLException;

import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Path;
import ohos.agp.render.PathMeasure;
import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;
import ohos.app.Context;
import ohos.global.resource.Resource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
//import org.jetbrains.annotations.Nullable;

public final class Utils {
    public static final int SECOND_IN_NANOS = 1000000000;

    /**
     * Wrap in Local Thread is necessary for prevent race condition in multi-threaded mode
     */
    private static final ThreadLocal<PathMeasure> threadLocalPathMeasure = new ThreadLocal<PathMeasure>() {
        @Override
        protected PathMeasure initialValue() {
            //return null;
            return new PathMeasure(null, false);
        }
    };

    private static final ThreadLocal<Path> threadLocalTempPath = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            return new Path();
        }
    };

    private static final ThreadLocal<Path> threadLocalTempPath2 = new ThreadLocal<Path>() {
        @Override
        protected Path initialValue() {
            return new Path();
        }
    };

    private static final ThreadLocal<float[]> threadLocalPoints = new ThreadLocal<float[]>() {
        @Override
        protected float[] initialValue() {
            return new float[4];
        }
    };

    private static final float INV_SQRT_2 = (float) (Math.sqrt(2) / 2.0);
    private static float dpScale = -1;

    private Utils() {
    }

    public static Path createPath(Point startPoint, Point endPoint, Point cp1, Point cp2) {
        Path path = new Path();
        path.moveTo(startPoint.getPointX(), startPoint.getPointY());

        if (cp1 != null && cp2 != null && (Math.hypot(cp1.getPointX(), cp1.getPointY()) != 0 || Math.hypot(cp2.getPointX(), cp2.getPointY()) != 0)) {
            path.cubicTo(
                    new Point(startPoint.getPointX() + cp1.getPointX(), startPoint.getPointY() + cp1.getPointY()),
                    new Point(endPoint.getPointX() + cp2.getPointX(), endPoint.getPointY() + cp2.getPointY()),
                    new Point(endPoint.getPointX(), endPoint.getPointY())
            );
        } else {
            path.lineTo(endPoint.getPointX(), endPoint.getPointY());
        }
        return path;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static float getScale(Matrix matrix) {
        final float[] points = threadLocalPoints.get();

        points[0] = 0;
        points[1] = 0;
        // Use 1/sqrt(2) so that the hypotenuse is of length 1.
        points[2] = INV_SQRT_2;
        points[3] = INV_SQRT_2;
        matrix.mapPoints(points);
        float dx = points[2] - points[0];
        float dy = points[3] - points[1];

        return (float) Math.hypot(dx, dy);
    }

    public static boolean hasZeroScaleAxis(Matrix matrix) {
        final float[] points = threadLocalPoints.get();

        points[0] = 0;
        points[1] = 0;
        // Random numbers. The only way these should map to the same thing as 0,0 is if the scale is 0.
        points[2] = 37394.729378f;
        points[3] = 39575.2343807f;
        matrix.mapPoints(points);
        if (points[0] == points[2] || points[1] == points[3]) {
            return true;
        }
        return false;
    }

    public static void applyTrimPathIfNeeded(Path path,  TrimPathContent trimPath) {
        if (trimPath == null || trimPath.isHidden()) {
            return;
        }
        float start = ((FloatKeyframeAnimation) trimPath.getStart()).getFloatValue();
        float end = ((FloatKeyframeAnimation) trimPath.getEnd()).getFloatValue();
        float offset = ((FloatKeyframeAnimation) trimPath.getOffset()).getFloatValue();
        applyTrimPathIfNeeded(path, start / 100f, end / 100f, offset / 360f);
    }

    public static void applyTrimPathIfNeeded(
            Path path, float startValue, float endValue, float offsetValue) {
        L.beginSection("applyTrimPathIfNeeded");
        final PathMeasure pathMeasure = threadLocalPathMeasure.get();
        final Path tempPath = threadLocalTempPath.get();
        final Path tempPath2 = threadLocalTempPath2.get();

        pathMeasure.setPath(path, false);

        float length = pathMeasure.getLength();
        if (startValue == 1f && endValue == 0f) {
            L.endSection("applyTrimPathIfNeeded");
            return;
        }
        if (length < 1f || Math.abs(endValue - startValue - 1) < .01) {
            L.endSection("applyTrimPathIfNeeded");
            return;
        }
        float start = length * startValue;
        float end = length * endValue;
        float newStart = Math.min(start, end);
        float newEnd = Math.max(start, end);

        float offset = offsetValue * length;
        newStart += offset;
        newEnd += offset;

        // If the trim path has rotated around the path, we need to shift it back.
        if (newStart >= length && newEnd >= length) {
            newStart = MiscUtils.floorMod(newStart, length);
            newEnd = MiscUtils.floorMod(newEnd, length);
        }

        if (newStart < 0) {
            newStart = MiscUtils.floorMod(newStart, length);
        }
        if (newEnd < 0) {
            newEnd = MiscUtils.floorMod(newEnd, length);
        }

        // If the start and end are equals, return an empty path.
        if (newStart == newEnd) {
            path.reset();
            L.endSection("applyTrimPathIfNeeded");
            return;
        }

        if (newStart >= newEnd) {
            newStart -= length;
        }

        tempPath.reset();
        pathMeasure.getSegment(
                newStart,
                newEnd,
                tempPath,
                true);

        if (newEnd > length) {
            tempPath2.reset();
            pathMeasure.getSegment(
                    0,
                    newEnd % length,
                    tempPath2,
                    true);
            tempPath.addPath(tempPath2);
        } else if (newStart < 0) {
            tempPath2.reset();
            pathMeasure.getSegment(
                    length + newStart,
                    length,
                    tempPath2,
                    true);
            tempPath.addPath(tempPath2);
        }
        path.set(tempPath);
        L.endSection("applyTrimPathIfNeeded");
    }

    @SuppressWarnings("SameParameterValue")
    public static boolean isAtLeastVersion(int major, int minor, int patch, int minMajor, int minMinor, int
            minPatch) {
        if (major < minMajor) {
            return false;
        } else if (major > minMajor) {
            return true;
        }

        if (minor < minMinor) {
            return false;
        } else if (minor > minMinor) {
            return true;
        }

        return patch >= minPatch;
    }

    public static int hashFor(float a, float b, float c, float d) {
        int result = 17;
        if (a != 0) {
            result = (int) (31 * result * a);
        }
        if (b != 0) {
            result = (int) (31 * result * b);
        }
        if (c != 0) {
            result = (int) (31 * result * c);
        }
        if (d != 0) {
            result = (int) (31 * result * d);
        }
        return result;
    }

    public static float dpScale() {
        if (dpScale == -1) {
            dpScale = 1.8f;
            //dpScale = Resources.getSystem().getDisplayMetrics().density;
        }
        return dpScale;
    }

    public static float getAnimationScale(Context context) {
        dpScale = 1.8f;

        return dpScale;
        //return Settings.System.getFloat(context.getContentResolver(),
        //        Settings.System.ANIMATOR_DURATION_SCALE, 1.0f);
    }

    /**
     * Resize the bitmap to exactly the same size as the specified dimension, changing the aspect ratio if needed.
     * Returns the original bitmap if the dimensions already match.
     */
    public static PixelMap resizeBitmapIfNeeded(PixelMap bitmap, int width, int height) {
        if (bitmap.getImageInfo().size.width == width && bitmap.getImageInfo().size.height == height) {
            return bitmap;
        }
        PixelMap.InitializationOptions options = new PixelMap.InitializationOptions();
        options.size.width = width;
        options.size.height = height;
        PixelMap resizedBitmap = PixelMap.create(bitmap, options);
        bitmap.release();
        return resizedBitmap;
    }

    /**
     * From http://vaibhavblogs.org/2012/12/common-java-networking-exceptions/
     */
    public static boolean isNetworkException(Throwable e) {
        return e instanceof SocketException || e instanceof ClosedChannelException ||
                e instanceof InterruptedIOException || e instanceof ProtocolException ||
                e instanceof SSLException || e instanceof UnknownHostException ||
                e instanceof UnknownServiceException;
    }

    public static void saveLayerCompat(Canvas canvas, RectFloat rect, Paint paint) {
        saveLayerCompat(canvas,rect, paint, 0);
    }

    public static void saveLayerCompat(Canvas canvas, RectFloat rect, Paint paint, int flag) {
        L.beginSection("Utils#saveLayer");

        canvas.saveLayer(rect, paint);
        L.endSection("Utils#saveLayer");
    }
}
