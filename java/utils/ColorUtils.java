/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This file is automatically generated. Do not modify it.

package utils;

/**
 * Color science utilities.
 *
 * <p>Utility methods for color science constants and color space conversions that aren't HCT or
 * CAM16.
 */
public class ColorUtils {
  private ColorUtils() {}

  /** Converts a color from RGB components to ARGB format. */
  public static int argbFromRgb(int red, int green, int blue) {
    return (255 << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
  }

  /** Returns the alpha component of a color in ARGB format. */
  public static int alphaFromArgb(int argb) {
    return (argb >> 24) & 255;
  }

  /** Returns the red component of a color in ARGB format. */
  public static int redFromArgb(int argb) {
    return (argb >> 16) & 255;
  }

  /** Returns the green component of a color in ARGB format. */
  public static int greenFromArgb(int argb) {
    return (argb >> 8) & 255;
  }

  /** Returns the blue component of a color in ARGB format. */
  public static int blueFromArgb(int argb) {
    return argb & 255;
  }

  /** Returns whether a color in ARGB format is opaque. */
  public static boolean isOpaque(int argb) {
    return alphaFromArgb(argb) >= 255;
  }

  /** Returns the sRGB to XYZ transformation matrix. */
  public static double[][] srgbToXyz() {
    return new double[][] {
      new double[] {0.41233895, 0.35762064, 0.18051042},
      new double[] {0.2126, 0.7152, 0.0722},
      new double[] {0.01932141, 0.11916382, 0.95034478},
    };
  }

  /** Returns the XYZ to sRGB transformation matrix. */
  public static double[][] xyzToSrgb() {
    return new double[][] {
      new double[] {3.2406, -1.5372, -0.4986},
      new double[] {-0.9689, 1.8758, 0.0415},
      new double[] {0.0557, -0.204, 1.057},
    };
  }

  /** Converts a color from ARGB to XYZ. */
  public static int argbFromXyz(double x, double y, double z) {
    double[] linearRgb = MathUtils.matrixMultiply(new double[] {x, y, z}, xyzToSrgb());
    int r = delinearized(linearRgb[0]);
    int g = delinearized(linearRgb[1]);
    int b = delinearized(linearRgb[2]);
    return argbFromRgb(r, g, b);
  }

  /** Converts a color from XYZ to ARGB. */
  public static double[] xyzFromArgb(int argb) {
    double r = linearized(redFromArgb(argb));
    double g = linearized(greenFromArgb(argb));
    double b = linearized(blueFromArgb(argb));
    return MathUtils.matrixMultiply(new double[] {r, g, b}, srgbToXyz());
  }

  /** Converts a color represented in Lab color space into an ARGB integer. */
  public static int argbFromLab(double l, double a, double b) {
    double[] whitePoint = whitePointD65();
    double fy = (l + 16.0) / 116.0;
    double fx = a / 500.0 + fy;
    double fz = fy - b / 200.0;
    double xNormalized = labInvf(fx);
    double yNormalized = labInvf(fy);
    double zNormalized = labInvf(fz);
    double x = xNormalized * whitePoint[0];
    double y = yNormalized * whitePoint[1];
    double z = zNormalized * whitePoint[2];
    return argbFromXyz(x, y, z);
  }

  /**
   * Converts a color from ARGB representation to L*a*b* representation.
   *
   * @param argb the ARGB representation of a color
   * @return a Lab object representing the color
   */
  public static double[] labFromArgb(int argb) {
    double[] whitePoint = whitePointD65();
    double[] xyz = xyzFromArgb(argb);
    double xNormalized = xyz[0] / whitePoint[0];
    double yNormalized = xyz[1] / whitePoint[1];
    double zNormalized = xyz[2] / whitePoint[2];
    double fx = labF(xNormalized);
    double fy = labF(yNormalized);
    double fz = labF(zNormalized);
    double l = 116.0 * fy - 16;
    double a = 500.0 * (fx - fy);
    double b = 200.0 * (fy - fz);
    return new double[] {l, a, b};
  }

  /**
   * Converts an L* value to an ARGB representation.
   *
   * @param lstar L* in L*a*b*
   * @return ARGB representation of grayscale color with lightness matching L*
   */
  public static int argbFromLstar(double lstar) {
    double fy = (lstar + 16.0) / 116.0;
    double fz = fy;
    double fx = fy;
    double kappa = 24389.0 / 27.0;
    double epsilon = 216.0 / 24389.0;
    boolean lExceedsEpsilonKappa = lstar > 8.0;
    double y = lExceedsEpsilonKappa ? fy * fy * fy : lstar / kappa;
    boolean cubeExceedEpsilon = fy * fy * fy > epsilon;
    double x = cubeExceedEpsilon ? fx * fx * fx : lstar / kappa;
    double z = cubeExceedEpsilon ? fz * fz * fz : lstar / kappa;
    double[] whitePoint = whitePointD65();
    return argbFromXyz(x * whitePoint[0], y * whitePoint[1], z * whitePoint[2]);
  }

  /**
   * Computes the L* value of a color in ARGB representation.
   *
   * @param argb ARGB representation of a color
   * @return L*, from L*a*b*, coordinate of the color
   */
  public static double lstarFromArgb(int argb) {
    double y = xyzFromArgb(argb)[1] / 100.0;
    double e = 216.0 / 24389.0;
    if (y <= e) {
      return 24389.0 / 27.0 * y;
    } else {
      double yIntermediate = Math.pow(y, 1.0 / 3.0);
      return 116.0 * yIntermediate - 16.0;
    }
  }

  /**
   * Converts an L* value to a Y value.
   *
   * <p>L* in L*a*b* and Y in XYZ measure the same quantity, luminance.
   *
   * <p>L* measures perceptual luminance, a linear scale. Y in XYZ measures relative luminance, a
   * logarithmic scale.
   *
   * @param lstar L* in L*a*b*
   * @return Y in XYZ
   */
  public static double yFromLstar(double lstar) {
    double ke = 8.0;
    if (lstar > ke) {
      return Math.pow((lstar + 16.0) / 116.0, 3.0) * 100.0;
    } else {
      return lstar / 24389.0 / 27.0 * 100.0;
    }
  }

  /**
   * Linearizes an RGB component.
   *
   * @param rgbComponent 0 <= rgb_component <= 255, represents R/G/B channel
   * @return 0.0 <= output <= 100.0, color channel converted to linear RGB space
   */
  public static double linearized(int rgbComponent) {
    double normalized = rgbComponent / 255.0;
    if (normalized <= 0.040449936) {
      return normalized / 12.92 * 100.0;
    } else {
      return Math.pow((normalized + 0.055) / 1.055, 2.4) * 100.0;
    }
  }

  /**
   * Delinearizes an RGB component.
   *
   * @param rgbComponent 0.0 <= rgb_component <= 100.0, represents linear R/G/B channel
   * @return 0 <= output <= 255, color channel converted to regular RGB space
   */
  public static int delinearized(double rgbComponent) {
    double normalized = rgbComponent / 100.0;
    double delinearized = 0.0;
    if (normalized <= 0.0031308) {
      delinearized = normalized * 12.92;
    } else {
      delinearized = 1.055 * Math.pow(normalized, 1.0 / 2.4) - 0.055;
    }
    return MathUtils.clampInt(0, 255, (int) Math.round(delinearized * 255.0));
  }

  /**
   * Returns the standard white point; white on a sunny day.
   *
   * @return The white point
   */
  public static double[] whitePointD65() {
    return new double[] {95.047, 100.0, 108.883};
  }

  static double labF(double t) {
    double e = 216.0 / 24389.0;
    double kappa = 24389.0 / 27.0;
    if (t > e) {
      return Math.pow(t, 1.0 / 3.0);
    } else {
      return (kappa * t + 16) / 116;
    }
  }

  static double labInvf(double ft) {
    double e = 216.0 / 24389.0;
    double kappa = 24389.0 / 27.0;
    double ft3 = ft * ft * ft;
    if (ft3 > e) {
      return ft3;
    } else {
      return (116 * ft - 16) / kappa;
    }
  }

}

