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

    @Test
    public void testDirtyFlag() {
        BlimpSession session = new BlimpSession();
        TestInput input = new TestInput();
        input.setPath("initial path");
        session.setInput(input);
        SessionHistory history = new SessionHistory(session);
        assertFalse(history.isDirty());

        input.setPath("new path");
        history.record();
        assertTrue(history.isDirty());

        history.undo();
        assertFalse(history.isDirty());

        input.setPath("new path 2");
        history.record();
        assertTrue(history.isDirty());

        input.setPath("initial path");
        history.record();
        assertFalse(history.isDirty());

        input.setPath("save value");
        history.record();
        assertTrue(history.isDirty());

        history.recordSaved();
        assertFalse(history.isDirty());

        input.setPath("another value");
        history.record();
        assertTrue(history.isDirty());

        history.undo();
        assertFalse(history.isDirty());

        history.undo();
        assertTrue(history.isDirty());

        history.redo();
        assertFalse(history.isDirty());
    }
}
