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
package org.boblycat.blimp;

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
        Debug.print(this, "recorded, history size is now " + history.size());
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
    
    public void recordSaved() {
        tryEnsureHistoryExists();
        if (history == null)
            return;
        history.recordSaved();
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
        Debug.print(this, "beginDisableAutoRecord " + autoRecordDisableLevel);
    }
    
    private void internalEndDisableAutoRecord() {
        autoRecordDisableLevel--;
        Debug.print(this, "endDisableAutoRecord " + autoRecordDisableLevel);
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
        recordSaved();
    }
}
