package org.boblycat.blimp.tests;

import org.boblycat.blimp.BlimpSession;
import org.boblycat.blimp.CachedBlimpSession;

public class CachedSessionTests extends SessionTests {
    // Note: all tests in SessionTests will be executed
    @Override
    protected BlimpSession newSession() {
        return new CachedBlimpSession();
    }
}
