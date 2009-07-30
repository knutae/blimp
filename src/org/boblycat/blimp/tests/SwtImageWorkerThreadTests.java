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

import java.io.IOException;

import org.boblycat.blimp.BlimpSession;
import org.boblycat.blimp.gui.swt.SwtImageWorkerThread;
import org.boblycat.blimp.tests.TestableImageWorkerThread.TestRequest;
import org.eclipse.swt.widgets.Display;
import org.junit.*;
import static org.junit.Assert.*;

class TestableImageWorkerThread extends SwtImageWorkerThread {
    public class TestRequest extends Request {
        public volatile int executeCount;
        public volatile int disposeCount;
        
        public TestRequest(Object owner, BlimpSession session,
                Runnable runnable) {
            super(owner, session, runnable);
        }

        @Override
        protected void execute() throws IOException {
            executeCount++;
            if (runnable != null)
                asyncExec(runnable);
        }
        
        @Override
        protected void dispose() {
            disposeCount++;
        }
    }
    
    public class CallbackRequest extends Request {
        private Runnable callback;
        
        public CallbackRequest(Object owner, Runnable callback) {
            super(owner, null, null);
            this.callback = callback;
        }

        @Override
        protected void execute() throws IOException {
            callback.run();
        }
    }
    
    public TestableImageWorkerThread(Display display) {
        super(display);
    }

    public void asyncExec(Runnable runnable) {
        super.asyncExec(runnable);
    }
    
    public void putRequest(Request req) {
        super.putRequest(req);
    }
}

public class SwtImageWorkerThreadTests {
    private Display display;
    private TestableImageWorkerThread thread;
    private int counter;
    private boolean finished;
    private boolean timedOut;
    
    public SwtImageWorkerThreadTests() {
        display = new Display();
        thread = new TestableImageWorkerThread(display);
        thread.start();
    }
    
    private void joinThread() {
        thread.quit();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
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
        joinThread();
        display.dispose();
        assertFalse(timedOut);
    }

    private void runLoop() {
        runLoop(1000);
    }

    private TestRequest createFinishRequest(Object owner) {
        TestRequest req = thread.new TestRequest(owner, null, new Runnable() {
            public void run() {
                finished = true;
            }
        });
        return req;
    }
    
    private void putSleepRequest(Object owner, int milliseconds) {
        final int millis = milliseconds;
        thread.putRequest(thread.new CallbackRequest(owner, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
            }
        }));
    }
    
    @Test
    public void testAsyncExec() {
        thread.asyncExec(new Runnable() {
            public void run() {
                counter++;
                finished = true;
            }
        });
        assertEquals(0, counter);
        runLoop();
        assertEquals(1, counter);
    }
    
    @Test
    public void testRequestExecute() {
        TestRequest req = createFinishRequest(this);
        assertEquals(0, req.executeCount);
        thread.putRequest(req);
        runLoop();
        assertEquals(1, req.executeCount);
    }
    
    @Test
    public void testRequestDisposeNormal() {
        TestRequest req = createFinishRequest(this);
        assertEquals(0, req.disposeCount);
        thread.putRequest(req);
        runLoop();
        assertEquals(1, req.disposeCount);
    }
    
    @Test
    public void testCancelRequestsByOwner() {
        TestRequest req1 = createFinishRequest(this);
        assertEquals(0, req1.executeCount);
        assertEquals(0, req1.disposeCount);
        TestRequest req2 = createFinishRequest(null);
        assertEquals(0, req2.executeCount);
        assertEquals(0, req2.disposeCount);
        putSleepRequest(null, 10); // a short sleep is enough
        thread.putRequest(req1);
        thread.putRequest(req2);
        thread.cancelRequestsByOwner(this); // thread should be sleeping here
        runLoop();
        assertEquals(0, req1.executeCount);
        assertEquals(1, req1.disposeCount);
        assertEquals(1, req2.executeCount);
        assertEquals(1, req2.disposeCount);
    }
    
}
