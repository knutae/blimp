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

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.session.BlimpSession.PreviewQuality;

public class BitmapRequest extends Request {
    private int viewWidth;
    private int viewHeight;
    private PreviewQuality previewQuality;

    public BitmapRequest(ImageWorkerThread thread, Object owner, BlimpSession session, Runnable runnable,
            int viewWidth, int viewHeight, PreviewQuality previewQuality) {
        super(thread, owner, session, runnable);
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.previewQuality = previewQuality;
    }

    @Override
    protected void execute() throws IOException {
        assert(runnable != null);
        // Generate the bitmap on this thread.  It should not be transferred
        // to other threads.
        Bitmap bitmap;
        if (viewWidth > 0 && viewHeight > 0)
            bitmap = thread.getSession().getSizedBitmap(viewWidth, viewHeight, previewQuality);
        else
            bitmap = thread.getSession().getBitmap();
        thread.bitmapGenerated(runnable, bitmap);
    }
}