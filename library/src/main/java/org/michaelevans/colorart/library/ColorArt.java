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

import org.apache.commons.collections.bag.HashBag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ColorArt {
    private double COLOR_THRESHOLD_MINIMUM_PERCENTAGE = .01;
    private static String LOG_TAG = ColorArt.class.getSimpleName();

    private Bitmap mBitmap;

    private HashBag imageColors;
    private int backgroundColor;
    private Integer primaryColor = null;
    private Integer secondaryColor = null;
    private Integer detailColor = null;


    public ColorArt(Bitmap bitmap) {
        mBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
        analyzeImage();
    }

    private void analyzeImage() {
        backgroundColor = findEdgeColor();
        findTextColors(imageColors);
        boolean darkBackground = isDarkColor(backgroundColor);

        if (primaryColor == null) {
            Log.d(LOG_TAG, "missed primary");
            if (darkBackground)
                primaryColor = Color.WHITE;
            else
                primaryColor = Color.BLACK;
        }

        if (secondaryColor == null) {
            Log.d(LOG_TAG, "missed secondary");
            if (darkBackground)
                secondaryColor = Color.WHITE;
            else
                secondaryColor = Color.BLACK;
        }

        if (detailColor == null) {
            Log.d(LOG_TAG, "missed detail");
            if (darkBackground)
                detailColor = Color.WHITE;
            else
                detailColor = Color.BLACK;
        }
    }

    private int findEdgeColor() {
        int height = mBitmap.getHeight();
        int width = mBitmap.getWidth();

        imageColors = new HashBag();
        HashBag leftImageColors = new HashBag();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0) {
                    leftImageColors.add(Integer.valueOf(mBitmap.getPixel(x, y)));
                }
                imageColors.add(Integer.valueOf(mBitmap.getPixel(x, y)));
            }
        }

        ArrayList<CountedColor> sortedColors = new ArrayList<CountedColor>();

        Iterator iterator = leftImageColors.iterator();
        while (iterator.hasNext()) {
            Integer color = (Integer) iterator.next();
            int colorCount = leftImageColors.getCount(color);
            int randomColorThreshold = (int) (height * COLOR_THRESHOLD_MINIMUM_PERCENTAGE);
            if (colorCount < randomColorThreshold)
                continue;
            CountedColor container = new CountedColor(color, colorCount);
            sortedColors.add(container);
        }

        Collections.sort(sortedColors);

        CountedColor proposedEdgeColor = null;
        if (sortedColors.size() > 0) {
            proposedEdgeColor = sortedColors.get(0);
            if (proposedEdgeColor.isBlackOrWhite()) {
                for (int i = 1; i < sortedColors.size(); i++) {
                    CountedColor nextProposedColor = sortedColors.get(i);
                    if ((double) nextProposedColor.getCount() / proposedEdgeColor.getCount() > .3) {
                        if (!nextProposedColor.isBlackOrWhite()) {
                            proposedEdgeColor = nextProposedColor;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        if (proposedEdgeColor == null)
            return Color.BLACK;
        else
            return proposedEdgeColor.getColor();
    }

    private void findTextColors(HashBag colors) {
        Iterator iterator = colors.iterator();
        int currentColor;
        ArrayList<CountedColor> sortedColors = new ArrayList<CountedColor>();
        boolean findDarkTextColor = !isDarkColor(backgroundColor);

        while (iterator.hasNext()) {
            currentColor = (Integer) iterator.next();
            currentColor = colorWithMinimumSaturation(currentColor, .15f);
            if (isDarkColor(currentColor) == findDarkTextColor) {
                int colorCount = colors.getCount(currentColor);
                CountedColor container = new CountedColor(currentColor, colorCount);
                sortedColors.add(container);
            }
        }

        Collections.sort(sortedColors);

        for (CountedColor currentContainer : sortedColors) {
            currentColor = currentContainer.getColor();
            if (primaryColor == null) {
                if (isContrastingColor(currentColor, backgroundColor)) {
                    primaryColor = currentColor;
                }
            } else if (secondaryColor == null) {
                if (!isDistinctColor(primaryColor, currentColor) || !isContrastingColor(currentColor, backgroundColor)) {
                    continue;
                } else {
                    secondaryColor = currentColor;
                }
            } else if (detailColor == null) {
                if (!isDistinctColor(secondaryColor, currentColor) || !isDistinctColor(primaryColor, currentColor) || !isContrastingColor(currentColor, backgroundColor)) {
                    continue;
                } else {
                    detailColor = currentColor;
                    break;
                }
            }
        }
    }


    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public int getDetailColor() {
        return detailColor;
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

        if (lum < .5) {
            return true;
        }
        return false;
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

        if (bLum > fLum)
            contrast = (bLum + 0.05) / (fLum + 0.05);
        else
            contrast = (fLum + 0.05) / (bLum + 0.05);

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

            if (Math.abs(r - g) < .03 && Math.abs(r - b) < .03) {
                if (Math.abs(r1 - g1) < .03 && Math.abs(r1 - b1) < .03)
                    return false;
            }

            return true;
        }

        return false;
    }

    private String intToColor(int intColor) {
        return String.format("#%08X", (0xFFFFFF & intColor));
    }

    private class CountedColor implements Comparable<CountedColor> {

        private int mColor;
        private int mCount;

        public CountedColor(int color, int count) {
            mColor = color;
            mCount = count;
        }

        @Override
        public int compareTo(CountedColor another) {
            return getColor() < another.getCount() ? -1 : (getCount() == another.getCount() ? 0 : 1);
        }

        public boolean isBlackOrWhite() {
            return false;//TODO
        }

        public int getCount() {
            return mCount;
        }

        public int getColor() {
            return mColor;
        }

    }
}
