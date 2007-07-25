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
                "org.boblycat.blimp.tests.LayerRearrangerTest");
    }
}
