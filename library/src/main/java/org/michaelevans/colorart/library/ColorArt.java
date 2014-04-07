/*
 * Copyright 2013 Michael Evans <michaelcevans10@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.michaelevans.colorart.library;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ColorArt {
    private final double COLOR_THRESHOLD_MINIMUM_PERCENTAGE = 0.01;
    private final double EDGE_COLOR_DISCARD_THRESHOLD = 0.3;
    private final float MINIMUM_SATURATION_THRESHOLD = 0.15f;
    private static final String LOG_TAG = ColorArt.class.getSimpleName();

    private final Bitmap mBitmap;

    private HashBag<Integer> mImageColors;
    private int mBackgroundColor;
    private Integer mPrimaryColor = null;
    private Integer mSecondaryColor = null;
    private Integer mDetailColor = null;


    public ColorArt(Bitmap bitmap) {
        mBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
        analyzeImage();
    }

    private void analyzeImage() {
        mBackgroundColor = findEdgeColor();
        findTextColors(mImageColors);
        boolean hasDarkBackground = isDarkColor(mBackgroundColor);

        if (mPrimaryColor == null) {
            Log.d(LOG_TAG, "Unable to detect primary color in image");
            if (hasDarkBackground) {
                mPrimaryColor = Color.WHITE;
            } else {
                mPrimaryColor = Color.BLACK;
            }
        }

        if (mSecondaryColor == null) {
            Log.d(LOG_TAG, "Unable to detect secondary in image");
            if (hasDarkBackground) {
                mSecondaryColor = Color.WHITE;
            } else {
                mSecondaryColor = Color.BLACK;
            }
        }

        if (mDetailColor == null) {
            Log.d(LOG_TAG, "Unable to detect detail color in image");
            if (hasDarkBackground) {
                mDetailColor = Color.WHITE;
            } else {
                mDetailColor = Color.BLACK;
            }
        }
    }

    private int findEdgeColor() {
        int height = mBitmap.getHeight();
        int width = mBitmap.getWidth();

        mImageColors = new HashBag<Integer>();
        HashBag<Integer> leftImageColors = new HashBag<Integer>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0) {
                    leftImageColors.add(mBitmap.getPixel(x, y));
                }
                mImageColors.add(mBitmap.getPixel(x, y));
            }
        }

        ArrayList<CountedColor> sortedColors = new ArrayList<CountedColor>();

        int randomColorThreshold = (int) (height * COLOR_THRESHOLD_MINIMUM_PERCENTAGE);
        Iterator<Integer> iterator = leftImageColors.iterator();
        while (iterator.hasNext()) {
            Integer color = iterator.next();
            int colorCount = leftImageColors.getCount(color);
            if (colorCount < randomColorThreshold) {
                continue;
            }

            CountedColor container = new CountedColor(color, colorCount);
            sortedColors.add(container);
        }

        Collections.sort(sortedColors);

        Iterator<CountedColor> sortedColorIterator = sortedColors.iterator();
        if (!sortedColorIterator.hasNext()) {
            return Color.BLACK;
        }

        CountedColor proposedEdgeColor = sortedColorIterator.next();
        if (!proposedEdgeColor.isBlackOrWhite()) {
            return proposedEdgeColor.getColor();
        }

        while (sortedColorIterator.hasNext()) {
            CountedColor nextProposedColor = sortedColorIterator.next();
            double edgeColorRatio = (double) nextProposedColor.getCount() / proposedEdgeColor.getCount();
            if (edgeColorRatio <= EDGE_COLOR_DISCARD_THRESHOLD) {
                break;
            }

            if (!nextProposedColor.isBlackOrWhite()) {
                proposedEdgeColor = nextProposedColor;
                break;
            }
        }

        return proposedEdgeColor.getColor();
    }

    private void findTextColors(HashBag<Integer> colors) {
        Iterator<Integer> iterator = colors.iterator();
        int currentColor;
        ArrayList<CountedColor> sortedColors = new ArrayList<CountedColor>();
        boolean findDarkTextColor = !isDarkColor(mBackgroundColor);

        while (iterator.hasNext()) {
            currentColor = iterator.next();
            currentColor = colorWithMinimumSaturation(currentColor, MINIMUM_SATURATION_THRESHOLD);
            if (isDarkColor(currentColor) == findDarkTextColor) {
                int colorCount = colors.getCount(currentColor);
                CountedColor container = new CountedColor(currentColor, colorCount);
                sortedColors.add(container);
            }
        }

        Collections.sort(sortedColors);

        for (CountedColor currentContainer : sortedColors) {
            currentColor = currentContainer.getColor();
            if (mPrimaryColor == null) {
                if (isContrastingColor(currentColor, mBackgroundColor)) {
                    mPrimaryColor = currentColor;
                }
            } else if (mSecondaryColor == null) {
                if (!isDistinctColor(mPrimaryColor, currentColor) ||
                        !isContrastingColor(currentColor, mBackgroundColor)) {
                    continue;
                }
                mSecondaryColor = currentColor;
            } else if (mDetailColor == null) {
                if (!isDistinctColor(mSecondaryColor, currentColor) ||
                        !isDistinctColor(mPrimaryColor, currentColor) ||
                        !isContrastingColor(currentColor, mBackgroundColor)) {
                    continue;
                }
                mDetailColor = currentColor;
                break;
            }
        }
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    public int getSecondaryColor() {
        return mSecondaryColor;
    }

    public int getDetailColor() {
        return mDetailColor;
    }

    //helpers
    private int colorWithMinimumSaturation(int color, float minSaturation) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (hsv[1] < minSaturation) {
            return Color.HSVToColor(new float[]{hsv[0], minSaturation, hsv[2]});
        }

        return color;
    }

    private boolean isDarkColor(int color) {
        double r = (double) Color.red(color) / 255;
        double g = (double) Color.green(color) / 255;
        double b = (double) Color.blue(color) / 255;

        double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;

        return lum < 0.5;
    }

    private boolean isContrastingColor(int backgroundColor, int foregroundColor) {
        double br = (double) Color.red(backgroundColor) / 255;
        double bg = (double) Color.green(backgroundColor) / 255;
        double bb = (double) Color.blue(backgroundColor) / 255;

        double fr = (double) Color.red(foregroundColor) / 255;
        double fg = (double) Color.green(foregroundColor) / 255;
        double fb = (double) Color.blue(foregroundColor) / 255;


        double bLum = 0.2126 * br + 0.7152 * bg + 0.0722 * bb;
        double fLum = 0.2126 * fr + 0.7152 * fg + 0.0722 * fb;

        double contrast;

        if (bLum > fLum) {
            contrast = (bLum + 0.05) / (fLum + 0.05);
        } else {
            contrast = (fLum + 0.05) / (bLum + 0.05);
        }

        return contrast > 1.6;
    }

    private boolean isDistinctColor(int colorA, int colorB) {
        double r = (double) Color.red(colorA) / 255;
        double g = (double) Color.green(colorA) / 255;
        double b = (double) Color.blue(colorA) / 255;
        double a = (double) Color.alpha(colorA) / 255;

        double r1 = (double) Color.red(colorB) / 255;
        double g1 = (double) Color.green(colorB) / 255;
        double b1 = (double) Color.blue(colorB) / 255;
        double a1 = (double) Color.alpha(colorB) / 255;

        double threshold = .25; //.15

        if (Math.abs(r - r1) > threshold ||
                Math.abs(g - g1) > threshold ||
                Math.abs(b - b1) > threshold ||
                Math.abs(a - a1) > threshold) {
            // check for grays, prevent multiple gray colors

            if (Math.abs(r - g) < .03 && Math.abs(r - b) < .03 &&
                    (Math.abs(r1 - g1) < .03 && Math.abs(r1 - b1) < .03)) {
                    return false;
            }

            return true;
        }

        return false;
    }

    private class CountedColor implements Comparable<CountedColor> {

        private final int mColor;
        private final int mCount;

        public CountedColor(int color, int count) {
            mColor = color;
            mCount = count;
        }

        @Override
        public int compareTo(CountedColor another) {
            return getCount() < another.getCount() ? -1 : (getCount() == another.getCount() ? 0 : 1);
        }

        public boolean isBlackOrWhite() {
            double r = (double) Color.red(mColor) / 255;
            double g = (double) Color.green(mColor) / 255;
            double b = (double) Color.blue(mColor) / 255;

            if ((r > .91 && g > .91 && b > .91) || (r < .09 && g < .09 && b < .09)) // color is white or black
                return true;

            return false;
        }

        public int getCount() {
            return mCount;
        }

        public int getColor() {
            return mColor;
        }

    }
}
