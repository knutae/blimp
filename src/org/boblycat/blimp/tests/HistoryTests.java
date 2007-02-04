package org.boblycat.blimp.tests;

import org.boblycat.blimp.BlimpSession;
import org.boblycat.blimp.HistoryBlimpSession;
import org.boblycat.blimp.SessionHistory;
import org.junit.*;
import static org.junit.Assert.*;

public class HistoryTests {
    @Test
    public void testCanUndo() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        assertFalse(history.canUndo());
        input.setPath("new value");
        assertFalse(history.canUndo());
        history.record();
        assertTrue(history.canUndo());
        history.undo();
        assertFalse(history.canUndo());
    }
    
    @Test
    public void testCanRedo() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        assertFalse(history.canRedo());
        input.setPath("new value");
        history.record();
        assertFalse(history.canRedo());
        history.undo();
        assertTrue(history.canRedo());
        history.redo();
        assertFalse(history.canRedo());
    }
    
    @Test
    public void testUndo() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setPath("initial value");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        input.setPath("new value 1");
        history.record();
        input.setPath("new value 2");
        history.record();
        
        assertEquals("new value 2", input.getPath());
        history.undo();
        assertEquals("new value 1", input.getPath());
        history.undo();
        assertEquals("initial value", input.getPath());
        history.undo();
        assertEquals("initial value", input.getPath());
    }

    @Test
    public void testRedo() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setPath("initial value");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        input.setPath("new value 1");
        history.record();
        input.setPath("new value 2");
        history.record();
        history.undo();
        history.undo();
        
        assertEquals("initial value", input.getPath());
        history.redo();
        assertEquals("new value 1", input.getPath());
        history.redo();
        assertEquals("new value 2", input.getPath());
        history.redo();
        assertEquals("new value 2", input.getPath());
    }
    
    @Test
    public void testUndoAndRedoAddLayer() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        TestLayer layer = new TestLayer();
        layer.setStringValue("some value");
        session.addLayer(layer);
        history.record();
        
        assertEquals(2, session.layerCount());
        history.undo();
        assertEquals(1, session.layerCount());
        history.redo();
        assertEquals(2, session.layerCount());
        layer = (TestLayer) session.getLayer(1);
        assertEquals("some value", layer.getStringValue());
    }
    
    @Test
    public void testRecordNoChanges() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setPath("initial value");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        
        assertEquals(1, history.size());
        input.setPath("new value");
        history.record();
        assertEquals(2, history.size());
        history.record(); // should be rejected
        assertEquals(2, history.size());
    }
    
    @Test
    public void testAutoRecordSession() {
        HistoryBlimpSession session = new HistoryBlimpSession();
        TestInput input = new TestInput();
        session.setInput(input);
        input.setPath("initial path");
        input.invalidate();
        input.setPath("new path");
        input.invalidate();
        
        assertEquals("new path", input.getPath());
        session.undo();
        assertEquals("initial path", input.getPath());
        session.redo();
        assertEquals("new path", input.getPath());
        
        input.invalidate();
        session.undo();
        assertEquals("initial path", input.getPath());
    }
}
