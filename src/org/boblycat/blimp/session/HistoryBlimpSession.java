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
package org.boblycat.blimp.session;

import org.boblycat.blimp.event.LayerChangeListener;
import org.boblycat.blimp.event.LayerEvent;
import org.boblycat.blimp.event.LayerEventSource;
import org.boblycat.blimp.util.Util;

/**
 * A subclass of BlimpSession which has a SessionHistory member and
 * automatically records changes to it.
 *
 * @author Knut Arild Erstad
 */
public class HistoryBlimpSession extends BlimpSession {
    SessionHistory history;
    int autoRecordDisableLevel;
    LayerEventSource historyEventSource;

    public HistoryBlimpSession() {
        autoRecordDisableLevel = 0;
        history = null;
        addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent e) {
                if (autoRecordDisableLevel <= 0)
                    record();
            }
        });
        historyEventSource = new LayerEventSource();
    }

    public void addHistoryListener(LayerChangeListener listener) {
        historyEventSource.addListener(listener);
    }

    public void removeHistoryListener(LayerChangeListener listener) {
        historyEventSource.removeListener(listener);
    }

    private void tryEnsureHistoryExists() {
        if (history == null && getInput() != null && getInput().isActive())
            history = new SessionHistory(this);
    }

    private void record() {
        tryEnsureHistoryExists();
        if (history == null)
            return;
        history.record();
        triggerHistoryChange();
    }

    private void triggerHistoryChange() {
        historyEventSource.triggerChangeWithEvent(new LayerEvent(this));
    }

    public void undo() {
        if (history == null)
            return;
        beginDisableAutoRecord();
        try {
            history.undo();
            triggerChangeEvent();
            triggerHistoryChange();
        }
        finally {
            internalEndDisableAutoRecord();
        }
    }

    public void redo() {
        if (history == null)
            return;
        beginDisableAutoRecord();
        try {
            history.redo();
            triggerChangeEvent();
            triggerHistoryChange();
        }
        finally {
            internalEndDisableAutoRecord();
        }
    }

    /**
     * Returns the internal session history object.
     * This can return <code>null</code> if it has not yet been created.
     * @return a SessionHistory object, or <code>null</code>.
     */
    public SessionHistory getHistory() {
        return history;
    }

    public boolean isDirty() {
        if (history == null)
            return false;
        return history.isDirty();
    }

    /**
     * Record that the session has been saved in its current state.
     * It will not be dirty after this.
     * 
     * If the <code>eraseHistory</code> parameter is true, any previous
     * history will also be discarded, so that undo is not possible
     * until further changes are recorded.
     */
    public void recordSaved(boolean eraseHistory) {
        tryEnsureHistoryExists();
        if (history == null)
            return;
        history.recordSaved(eraseHistory);
        historyEventSource.triggerChangeWithEvent(new LayerEvent(this));
    }

    /**
     * Temporarily disable auto-recording to history.
     * This can be useful for user interations such as dialogs, where
     * multiple changes should be recorded as a single undoable operation.
     * <code>endDisableAutoRecord()</code> must be called afterwards to
     * reenable auto-recording.
     */
    public void beginDisableAutoRecord() {
        autoRecordDisableLevel++;
    }

    private void internalEndDisableAutoRecord() {
        autoRecordDisableLevel--;
        if (autoRecordDisableLevel < 0) {
            Util.err("internal error: autoRecordDisableLevel < 0");
            autoRecordDisableLevel = 0;
        }
    }

    /**
     * Reenable auto-recording to history.
     * <code>beginDisableAutoRecord()</code> must have been called first.
     * If is an error to call this function more times than
     * <code>beginDisableAutoRecord()</code>.
     */
    public void endDisableAutoRecord() {
        internalEndDisableAutoRecord();
        if (autoRecordDisableLevel == 0)
            record();
    }

    /**
     * Overridden to initialize the history after loading from file.
     */
    protected void beanLoaded(String filename) {
        super.beanLoaded(filename);
        recordSaved(true);
    }
}
