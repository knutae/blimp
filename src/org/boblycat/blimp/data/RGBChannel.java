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
package org.boblycat.blimp.data;

import net.sourceforge.jiu.data.RGBIndex;

/**
 * An enumeration for specifying one or all of the red, green or blue color
 * channels.
 *
 * @author Knut Arild Erstad
 */
public enum RGBChannel {
    Red,
    Green,
    Blue,
    All;

    public int toJiuIndex() {
        switch (this) {
        case Red:
            return RGBIndex.INDEX_RED;
        case Green:
            return RGBIndex.INDEX_GREEN;
        case Blue:
            return RGBIndex.INDEX_BLUE;
        default:
            return -1;
        }
    }

    /**
     * An array of all RGB channels (Red, Green and Blue).
     * This does not include special enum values like "All".
     */
    public static final RGBChannel[] COLORS = { Red, Green, Blue };
}
