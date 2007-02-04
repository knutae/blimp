package org.boblycat.blimp;

/**
 * A subclass of BlimpSession which has a SessionHistory member and
 * automatically records changes to it.
 * 
 * @author Knut Arild Erstad
 */
public class HistoryBlimpSession extends BlimpSession {
    private static final boolean DEBUG = false;
    
    SessionHistory history;
    int autoRecordDisableLevel;
    
    public HistoryBlimpSession() {
        autoRecordDisableLevel = 0;
        history = null;
        addChangeListener(new LayerChangeListener() {
            public void handleChange(LayerEvent e) {
                if (autoRecordDisableLevel <= 0)
                    record();
            }
        });
    }
    
    private void debug(String message) {
        if (DEBUG)
            System.out.println(message);
    }

    private void record() {
        if (history == null && getInput() != null && getInput().isActive())
            history = new SessionHistory(this);
        if (history == null)
            return;
         history.record();
         debug("recorded, history size is now " + history.size());
    }

    public void undo() {
        if (history == null)
            return;
        beginDisableAutoRecord();
        try {
            history.undo();
            triggerChangeEvent();
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
        }
        finally {
            internalEndDisableAutoRecord();
        }
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
        debug("beginDisableAutoRecord " + autoRecordDisableLevel);
    }
    
    private void internalEndDisableAutoRecord() {
        autoRecordDisableLevel--;
        debug("endDisableAutoRecord " + autoRecordDisableLevel);
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
