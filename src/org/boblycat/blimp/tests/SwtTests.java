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

import org.eclipse.swt.widgets.Display;
import org.junit.*;
import static org.junit.Assert.*;

class RunCounter implements Runnable {
    public int count = 0;
    public void run() {
        count++;
    }
}

public class SwtTests {
    private static final int DEFAULT_TEST_TIMEOUT = 1000;
    
    private Display display;
    private boolean finished;
    private boolean timedOut;
    private Runnable asyncFinish;
    
    public SwtTests() {
        display = new Display();
        asyncFinish = new Runnable() {
            public void run() {
                finished = true;
            }
        };
    }
    
    private void runLoop(int timeoutMillisec) {
        finished = false;
        timedOut = false;
        display.timerExec(timeoutMillisec, new Runnable() {
            public void run() {
                timedOut = true;
            }
        });
        while (!finished && !timedOut) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
        assertFalse(timedOut);
    }
    
    private void runLoop() {
        runLoop(DEFAULT_TEST_TIMEOUT);
    }
    
    @Test
    public void testSimpleAsyncExec() {
        // basic test to ensure the framework is OK
        display.asyncExec(asyncFinish);
        runLoop();
    }
    
    @Test
    public void testTimeout() {
        // ensure that test failures will occur on timeouts
        try {
            runLoop(5); // use a short timeout
            fail("control should not reach this point");
        }
        catch (AssertionError e) {
            // everything OK
        }
    }
    
    @Test
    public void testTimerExecTwiceOnSameRunnable() {
        // Multiple timerExec() calls on the same Runnable should only cause one run.
        // ImageView depends on this (undocumented?) behavior.
        RunCounter counter = new RunCounter();
        display.timerExec(1, counter);
        display.timerExec(3, counter);
        display.timerExec(5, counter);
        display.timerExec(10, asyncFinish);
        runLoop();
        assertEquals(1, counter.count);
    }
}
