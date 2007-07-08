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
}
