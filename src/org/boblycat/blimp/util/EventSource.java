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
package org.boblycat.blimp.util;

import java.util.ArrayList;
import java.util.List;

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
    List<L> listenerList;

    public EventSource() {
        listenerList = new ArrayList<L>();
    }

    public void addListener(L listener) {
        int index = listenerList.indexOf(null);
        if (index >= 0)
            listenerList.set(index, listener);
        else
            listenerList.add(listener);
    }

    public void removeListener(L listener) {
        int index = listenerList.indexOf(listener);
        if (index >= 0)
            listenerList.set(index, null);
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
