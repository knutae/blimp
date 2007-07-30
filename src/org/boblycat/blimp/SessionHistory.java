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
