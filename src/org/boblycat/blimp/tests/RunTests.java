package org.boblycat.blimp.tests;

import org.junit.runner.*;

public class RunTests {
    public static void main(String[] args) {
        JUnitCore.main("org.boblycat.blimp.tests.SessionTests",
                "org.boblycat.blimp.tests.SplineTests",
                "org.boblycat.blimp.tests.SerializationTests",
                "org.boblycat.blimp.tests.ZoomTests",
                "org.boblycat.blimp.tests.UtilTests");
    }
}
