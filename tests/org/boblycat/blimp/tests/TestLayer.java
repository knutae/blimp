/*
 * Copyright (C) 2007, 2008, 2009 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.boblycat.blimp.tests;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.ColorRGB;
import org.boblycat.blimp.layers.AdjustmentLayer;

public class TestLayer extends AdjustmentLayer {
    public enum Enum {
        ONE, TWO, THREE
    };

    int intValue;
    double doubleValue;
    String stringValue;
    Enum enumValue;
    double[] doubleArrayValue;
    ColorRGB colorValue;

    public TestLayer() {
    }

    public TestLayer(String strValue) {
        stringValue = strValue;
    }

    public void setIntValue(int i) {
        intValue = i;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setDoubleValue(double d) {
        doubleValue = d;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setStringValue(String str) {
        stringValue = str;
    }

    public String getStringValue() {
        if (stringValue == null)
            return "";
        return stringValue;
    }

    public Enum getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(Enum e) {
        enumValue = e;
    }

    @Override
    public Bitmap applyLayer(Bitmap source) {
        if (!(source instanceof TestBitmap))
            return source;
        TestBitmap testSource = (TestBitmap) source;
        TestBitmap result = new TestBitmap(source.getImage());
        result.creator = "TestLayer";
        result.testValue = testSource.testValue + getStringValue();
        return result;
    }

    @Override
    public String getDescription() {
        return "Test Layer";
    }

    public void setDoubleArrayValue(double[] doubleArrayValue) {
        this.doubleArrayValue = doubleArrayValue;
    }

    public double[] getDoubleArrayValue() {
        return doubleArrayValue;
    }

    public void setColorValue(ColorRGB colorValue) {
        this.colorValue = colorValue;
    }

    public ColorRGB getColorValue() {
        return colorValue;
    }

}
