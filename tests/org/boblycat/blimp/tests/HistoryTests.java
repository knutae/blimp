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
        input.setFilePath("new value");
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
        input.setFilePath("new value");
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
        input.setFilePath("initial value");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        input.setFilePath("new value 1");
        history.record();
        input.setFilePath("new value 2");
        history.record();

        assertEquals("new value 2", input.getFilePath());
        history.undo();
        assertEquals("new value 1", input.getFilePath());
        history.undo();
        assertEquals("initial value", input.getFilePath());
        history.undo();
        assertEquals("initial value", input.getFilePath());
    }

    @Test
    public void testRedo() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setFilePath("initial value");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        input.setFilePath("new value 1");
        history.record();
        input.setFilePath("new value 2");
        history.record();
        history.undo();
        history.undo();

        assertEquals("initial value", input.getFilePath());
        history.redo();
        assertEquals("new value 1", input.getFilePath());
        history.redo();
        assertEquals("new value 2", input.getFilePath());
        history.redo();
        assertEquals("new value 2", input.getFilePath());
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
        input.setFilePath("initial value");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);

        assertEquals(1, history.size());
        input.setFilePath("new value");
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
        input.setFilePath("initial path");
        input.invalidate();
        input.setFilePath("new path");
        input.invalidate();

        assertEquals("new path", input.getFilePath());
        session.undo();
        assertEquals("initial path", input.getFilePath());
        session.redo();
        assertEquals("new path", input.getFilePath());

        input.invalidate();
        session.undo();
        assertEquals("initial path", input.getFilePath());
    }

    @Test
    public void testDirtyFlag() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setFilePath("initial path");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        assertFalse(history.isDirty());

        input.setFilePath("new path");
        history.record();
        assertTrue(history.isDirty());

        history.undo();
        assertFalse(history.isDirty());

        input.setFilePath("new path 2");
        history.record();
        assertTrue(history.isDirty());

        input.setFilePath("initial path");
        history.record();
        assertFalse(history.isDirty());

        input.setFilePath("save value");
        history.record();
        assertTrue(history.isDirty());

        history.recordSaved(false);
        assertFalse(history.isDirty());

        input.setFilePath("another value");
        history.record();
        assertTrue(history.isDirty());

        history.undo();
        assertFalse(history.isDirty());

        history.undo();
        assertTrue(history.isDirty());

        history.redo();
        assertFalse(history.isDirty());
    }
    
    @Test
    public void testSessionNameAndHistory() {
        HistoryBlimpSession session = new HistoryBlimpSession();
        session.setName("initial name");
        session.setInput(new TestInput());
        session.triggerChangeEvent();
        session.addLayer(new TestLayer());
        assertTrue(session.isDirty());
        
        // simulate a "save as", changing the name
        session.setName("saved name");
        session.recordSaved(false);
        assertFalse(session.isDirty());
        assertTrue(session.getHistory().canUndo());
        
        session.undo();
        assertEquals("saved name", session.getName());
    }
}
