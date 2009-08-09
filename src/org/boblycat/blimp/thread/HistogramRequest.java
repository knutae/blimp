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

import org.boblycat.blimp.Debug;
import org.boblycat.blimp.HistogramGeneratedTask;
import org.boblycat.blimp.RGBHistograms;
import org.boblycat.blimp.session.BlimpSession;

public class HistogramRequest extends Request {
    private HistogramGeneratedTask histogramTask;
    private String layerName;

    public HistogramRequest(ImageWorkerThread thread, Object owner, BlimpSession session,
            HistogramGeneratedTask task, String layerName) {
        super(thread, owner, session, task);
        this.histogramTask = task;
        this.layerName = layerName;
    }

    @Override
    protected void execute() throws IOException {
        assert(histogramTask != null && layerName != null);
        Debug.print(this, "generating histogram for layer " + layerName);
        RGBHistograms histograms = thread.getSession().getHistogramsBeforeLayer(layerName, true);
        Debug.print(this, "finished generating histogram");
        // Note: the following should work without synchronization problems,
        // because the histogram task is only used by one thread at a time.
        histogramTask.setHistograms(histograms);
        thread.asyncExec(histogramTask);
    }
}