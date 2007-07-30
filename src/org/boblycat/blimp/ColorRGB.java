/*
 * Copyright (C) 2007 Knut Arild Erstad
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
package org.boblycat.blimp;

/**
 * A representation of a 24-bit RGB color triplet.
 * 
 * It can be used for specifying and serializing user-defined colors by
 * some layers.  The serialized format is compatible with HTML color codes.
 *   
 * Note that this class deliberately has limited scope.  It is <i>not</i>
 * meant to be used for individual pixels in a bitmap.  For that, use
 * JIU's RGBColor instead.
 * 
 * @see net.sourceforge.jiu.color.quantization.RGBColor
 * 
 * @author Knut Arild Erstad
 */
public class ColorRGB {
    public static class SyntaxException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        private SyntaxException(String s) {
            super(s);
        }
    }
    
    public static final int MAX = 255;
    
    public static final ColorRGB White = new ColorRGB(MAX, MAX, MAX);
    
    public static final ColorRGB Black = new ColorRGB(0, 0, 0);
    
    private int red;
    private int green;
    private int blue;
    
    public ColorRGB(int red, int green, int blue) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
    }
    
    private static int constrain(int value) {
        return Util.constrainedValue(value, 0, MAX);
    }
    
    public void setRed(int red) {
        this.red = constrain(red);
    }
    
    public int getRed() {
        return red;
    }
    
    public void setGreen(int green) {
        this.green = constrain(green);
    }
    
    public int getGreen() {
        return green;
    }
    
    public void setBlue(int blue) {
        this.blue = constrain(blue);
    }
    
    public int getBlue() {
        return blue;
    }
    
    /**
     * Returns the color in an HTML-compatible hexadecimal color code format.
     * E.g. #0040FF.
     */
    @Override
    public String toString() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }
    
    private static void throwSyntaxException(String input) {
        throw new SyntaxException("not a valid RGB color: " + input);
    }
    
    /**
     * Attempt to parse the string and return a new color.
     * The string must start with a hash character (#) followed by exactly
     * 6 hexadecimal digits (#RRGGBB).
     * @param input
     *      the string to parse.
     * @return
     *      a new ColorRGB instance.
     * @throws SyntaxException
     *      if the parsing failed.
     */
    public static ColorRGB parseColor(String input)
    throws SyntaxException {
        if (input.length() != 7 || input.charAt(0) != '#')
            throwSyntaxException(input);
        try {
            int r = Integer.parseInt(input.substring(1, 3), 16);
            int g = Integer.parseInt(input.substring(3, 5), 16);
            int b = Integer.parseInt(input.substring(5, 7), 16);
            return new ColorRGB(r, g, b);
        }
        catch (NumberFormatException e) {
            throwSyntaxException(input);
        }
        return null;
    }
}
