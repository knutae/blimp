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
package org.boblycat.blimp.tests;

import org.boblycat.blimp.Util;
import org.junit.runner.*;

public class RunTests {
    public static void main(String[] args) {
        // prevent console warnings
        Util.logger.setUseParentHandlers(false);
        JUnitCore.main("org.boblycat.blimp.tests.SessionTests",
                "org.boblycat.blimp.tests.CachedSessionTests",
                "org.boblycat.blimp.tests.HistoryTests",
                "org.boblycat.blimp.tests.SplineTests",
                "org.boblycat.blimp.tests.SerializationTests",
                "org.boblycat.blimp.tests.ZoomTests",
                "org.boblycat.blimp.tests.UtilTests",
                "org.boblycat.blimp.tests.ColorUtilTests",
                "org.boblycat.blimp.tests.LayerRearrangerTest",
                "org.boblycat.blimp.tests.ExifTests");
    }
}
