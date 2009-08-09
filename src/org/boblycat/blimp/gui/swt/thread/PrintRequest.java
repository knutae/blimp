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
package org.boblycat.blimp.gui.swt.thread;

import java.io.IOException;

import org.boblycat.blimp.data.Bitmap;
import org.boblycat.blimp.gui.swt.ImageConverter;
import org.boblycat.blimp.gui.swt.SwtUtil;
import org.boblycat.blimp.gui.swt.thread.SwtImageWorkerThread.PrintTask;
import org.boblycat.blimp.layers.PrintLayer;
import org.boblycat.blimp.session.BlimpSession;
import org.boblycat.blimp.session.BlimpSession.PreviewQuality;
import org.boblycat.blimp.thread.ImageWorkerThread;
import org.boblycat.blimp.thread.Request;
import org.boblycat.blimp.util.Util;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

public class PrintRequest extends Request {
    private Printer printer;
    private PrintLayer printLayerCopy;
    private PrintTask printTask;
    private String printJobName;

    public PrintRequest(ImageWorkerThread thread, Object owner,
            BlimpSession session, PrintTask task, PrinterData printerData,
            PrintLayer printLayer) {
        super(thread, owner, session, null);
        printTask = task;
        printer = new Printer(printerData);
        printLayerCopy = (PrintLayer) sessionCopy.findLayer(printLayer.getName());
        assert (printLayerCopy != null);
        printLayerCopy.setActive(true);
        printLayerCopy.setPreview(false);
        sessionCopy.setPreviewQuality(PreviewQuality.Accurate);
    }

    @Override
    protected void execute() throws IOException {
        Bitmap bitmap = thread.getSession().getFullBitmap();
        ImageData imageData = ImageConverter.jiuToSwtImageData(bitmap.getImage());
        Image swtImage = new Image(printer, imageData);
        printJobName = "blimp_" + sessionCopy.getName();
        if (printer.startJob(printJobName)) {
            GC gc = new GC(printer);
            try {
                int left = (printLayerCopy.getPaperWidth() - imageData.width) / 2;
                int top = (printLayerCopy.getPaperHeight() - imageData.height) / 2;
                gc.drawImage(swtImage, left, top);
                printer.endPage();
                printer.endJob();
                thread.asyncExec(new Runnable() {
                    public void run() {
                        printTask.handleSuccess(printJobName);
                    }
                });
            }
            finally {
                gc.dispose();
            }
        }
        else {
            Util.err("Failed to start printer job " + printJobName);
            thread.asyncExec(new Runnable() {
                public void run() {
                    printTask.handleError(printJobName, "Failed to start print job (Printer.startJob() returned false)");
                }
            });
        }
    }

    @Override
    protected void dispose() {
        assert (printer != null && !printer.isDisposed());
        SwtUtil.dispose(printer);
    }

}