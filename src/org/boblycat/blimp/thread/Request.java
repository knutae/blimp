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
/**
 * 
 */
package org.boblycat.blimp.thread;

import java.io.IOException;

import org.boblycat.blimp.session.BlimpSession;

public abstract class Request {
    Object owner;
    protected Runnable runnable;
    protected BlimpSession sessionCopy;
    protected ImageWorkerThread thread;

    protected Request(ImageWorkerThread thread, Object owner,
            BlimpSession session, Runnable runnable) {
        this.thread = thread;
        this.owner = owner;
        this.runnable = runnable;
        if (session != null)
            this.sessionCopy = BlimpSession.createCopy(session);
    }

    protected abstract void execute() throws IOException;

    // Will be called once, either after execute or during cancel.
    // Override to dispose of (non-memory) resources.
    protected void dispose() {
    }
}