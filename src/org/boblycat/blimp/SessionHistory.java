package org.boblycat.blimp;

import java.util.Vector;

class HistoryEntry {
    BlimpSession sessionCopy;
    public HistoryEntry(BlimpSession session) {
        // Note: sessionCopy must not be of class HistoryBlimpSession
        // since that would cause a recusion well, so clone() is not used.
        sessionCopy = BlimpSession.createCopy(session);
    }
}

public class SessionHistory {
    BlimpSession session;
    Vector<HistoryEntry> historyList;
    int currentIndex;
    HistoryEntry savedHistoryEntry;
    
    public SessionHistory(BlimpSession session) {
        this.session = session;
        historyList = new Vector<HistoryEntry>();
        // record initial state
        HistoryEntry newEntry = new HistoryEntry(session);
        historyList.add(newEntry);
        currentIndex = 0;
        savedHistoryEntry = new HistoryEntry(session);
    }
    
    public boolean canUndo() {
        return currentIndex > 0;
    }
    
    public boolean canRedo() {
        return currentIndex < historyList.size()-1;
    }
    
    public void record() {
        HistoryEntry currentEntry = historyList.get(currentIndex);
        if (session.sessionDataEquals(currentEntry.sessionCopy))
            // no changes to record
            return;
        historyList.setSize(currentIndex+1);
        HistoryEntry newEntry = new HistoryEntry(session);
        historyList.add(newEntry);
        currentIndex++;
    }
    
    /**
     * Record that the session has been saved in its current state.
     * It should not be dirty after this.
     */
    public void recordSaved() {
        savedHistoryEntry = new HistoryEntry(session);
    }
    
    public boolean isDirty() {
        HistoryEntry currentEntry = historyList.get(currentIndex);
        return !savedHistoryEntry.sessionCopy.sessionDataEquals(
                currentEntry.sessionCopy);
    }
    
    public void undo() {
        if (!canUndo())
            return;
        currentIndex--;
        HistoryEntry entry = historyList.get(currentIndex);
        session.synchronizeSessionData(entry.sessionCopy);
    }
    
    public void redo() {
        if (!canRedo())
            return;
        currentIndex++;
        HistoryEntry entry = historyList.get(currentIndex);
        session.synchronizeSessionData(entry.sessionCopy);
    }
    
    public int size() {
        return historyList.size();
    }
}
