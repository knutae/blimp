package org.boblycat.blimp;

import java.util.Vector;

public abstract class BitmapSource {
    private Vector<BitmapSourceListener> listenerList;
    
    public BitmapSource() {
        listenerList = new Vector<BitmapSourceListener>();
    }
    
    public abstract Bitmap getBitmap();
    public abstract String getDescription();
    
    public void addChangeListener(BitmapSourceListener listener) {
        // set listener to a null slot or append it
        if (listenerList.indexOf(listener) >= 0)
            return;
        for (int i=0; i<listenerList.size(); i++)
            if (listenerList.get(i) == null) {
                listenerList.set(i, listener);
                return;
            }
        listenerList.add(listener);
    }
    
    public void removeChangeListener(BitmapSourceListener listener) {
        // set the slot to null instead of removing it,
        // in case this happens during notifyListeners()
        int index = listenerList.indexOf(listener);
        if (index < 0)
            return;
        listenerList.set(index, null);
    }
    
    public void notifyChangeListeners() {
        for (int i=0; i<listenerList.size(); i++) {
            BitmapSourceListener listener = listenerList.get(i);
            if (listener != null)
                listener.handleChange(this);
        }
    }
}