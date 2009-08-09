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

import org.boblycat.blimp.data.BitmapSize;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.util.Util;

public class SizeRequest extends Request {
    private BitmapSizeGeneratedTask sizeTask;
    private String layerName;

    SizeRequest(ImageWorkerThread thread, Object owner, BlimpSession session,
            BitmapSizeGeneratedTask task, String layerName) {
        super(thread, owner, session, task);
        this.sizeTask = task;
        this.layerName = layerName;
    }

    @Override
    protected void execute() throws IOException {
        assert(sizeTask != null && layerName != null);
        BitmapSize size = thread.getSession().getBitmapSizeBeforeLayer(layerName);
        if (size == null)
            Util.err("Failed to get size for layer " + layerName);
        sizeTask.setSize(size);
        thread.asyncExec(sizeTask);
    }
}