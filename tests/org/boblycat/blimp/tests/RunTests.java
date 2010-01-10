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

import org.boblycat.blimp.data.ZoomTests;
import org.boblycat.blimp.exif.ExifTests;
import org.boblycat.blimp.gui.swt.SwtTests;
import org.boblycat.blimp.gui.swt.thread.SwtImageWorkerThreadTests;
import org.boblycat.blimp.io.SerializationTests;
import org.boblycat.blimp.session.CachedSessionTests;
import org.boblycat.blimp.session.HistoryTests;
import org.boblycat.blimp.session.LayerRearrangerTest;
import org.boblycat.blimp.session.SessionTests;
import org.boblycat.blimp.util.ColorUtilTests;
import org.boblycat.blimp.util.MathUtilTests;
import org.boblycat.blimp.util.Util;
import org.boblycat.blimp.util.UtilTests;
import org.junit.runner.*;

public class RunTests {
    private static void runClasses(Class<?>... classes)
    {
        String[] names = new String[classes.length];
        for (int i=0; i<classes.length; i++) {
            names[i] = classes[i].getName();
        }
        JUnitCore.main(names);
    }
    
    public static void main(String[] args) {
        // prevent console warnings
        Util.logger.setUseParentHandlers(false);
        runClasses(
                SessionTests.class,
                CachedSessionTests.class,
                HistoryTests.class,
                SerializationTests.class,
                ZoomTests.class,
                UtilTests.class,
                ColorUtilTests.class,
                LayerRearrangerTest.class,
                ExifTests.class,
                MathUtilTests.class,
                SwtTests.class,
                SwtImageWorkerThreadTests.class);
    }
}
