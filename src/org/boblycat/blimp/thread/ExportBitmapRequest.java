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

import java.io.File;
import java.io.IOException;

import org.boblycat.blimp.Bitmap;
import org.boblycat.blimp.BitmapUtil;
import org.boblycat.blimp.Util;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.thread.ImageWorkerThread.FileExportTask;

public class ExportBitmapRequest extends Request {
    private File file;
    private double exportQuality;
    private FileExportTask exportTask;
    private String errorMessage;

    ExportBitmapRequest(ImageWorkerThread thread, Object owner, BlimpSession session,
            FileExportTask task, File file, double quality) {
        // runnable can be null because IOException is handled internally
        super(thread, owner, session, null);
        this.file = file;
        this.exportQuality = quality;
        this.exportTask = task;
    }

    @Override
    protected void execute() throws IOException {
        assert(file != null);
        assert(exportTask != null);
        try {
            Bitmap bitmap = thread.getSession().getFullBitmap();
            String ext = Util.getFileExtension(file);
            BitmapUtil.writeBitmap(bitmap, ext, file, exportQuality);
            thread.asyncExec(new Runnable() {
                public void run() {
                    exportTask.handleSuccess(file);
                }
            });
        }
        catch (IOException e) {
            // special handling of IOException during export
            errorMessage = e.getMessage();
            thread.asyncExec(new Runnable() {
                public void run() {
                    exportTask.handleError(file, errorMessage);
                }
            });
        }
    }
}