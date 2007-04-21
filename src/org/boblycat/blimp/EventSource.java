package org.boblycat.blimp;

import java.util.Vector;

/**
 * A generic event source implementation.  Mostly meant for change events
 * with only one handler function.
 * 
 * @author Knut Arild Erstad
 *
 * @param <L> A listener interface.
 * @param <E> An event type.
 */
public abstract class EventSource<L, E> {
    Vector<L> listenerList;

    public EventSource() {
        listenerList = new Vector<L>();
    }
    
    public void addListener(L listener) {
        int index = listenerList.indexOf(null);
        if (index >= 0)
            listenerList.setElementAt(listener, index);
        else
            listenerList.add(listener);
    }
    
    public void removeListener(L listener) {
        int index = listenerList.indexOf(listener);
        if (index >= 0)
            listenerList.setElementAt(null, index);
    }
    
    protected abstract void triggerListenerEvent(L listener, E event);
    
    public void triggerChangeWithEvent(E event) {
        for (L listener: listenerList) {
            if (listener == null)
                continue;
            triggerListenerEvent(listener, event);
        }
    }
    
    public int size() {
        return listenerList.size();
    }
}
