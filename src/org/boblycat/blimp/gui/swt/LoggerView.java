/*
 * Copyright (C) 2007, 2008 Knut Arild Erstad
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
package org.boblycat.blimp.gui.swt;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.boblycat.blimp.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * A logger view for status, warning and error messages.
 * 
 * @author Knut Arild Erstad
 */
public class LoggerView extends Composite {
    private List messages;

    /**
     * Construct a logger view.
     * @param parent
     * @param style
     */
    public LoggerView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());
        messages = new List(this, SWT.SINGLE | SWT.V_SCROLL);
        
        attachLogger(Util.logger);
    }
    
    private void directLogMessage(LogRecord record) {
        if (isDisposed())
            return;
        int level = record.getLevel().intValue();
        String prefix = "";
        if (level >= Level.SEVERE.intValue())
            prefix = "SEVERE: ";
        else if (level >= Level.WARNING.intValue())
            prefix = "WARNING: ";
        String message = prefix + record.getMessage();
        messages.add(message);
        // Scroll to the last item by selecting and deselecting it,
        // is there a better way to do this?
        messages.setSelection(messages.getItemCount()-1);
        messages.deselectAll();
    }
    
    private class AsyncLogMessage implements Runnable {
        LogRecord record;
        AsyncLogMessage(LogRecord record) {
            this.record = record;
        }
        public void run() {
            directLogMessage(record);
        }
    }
    
    /**
     * Thread-safe logging method.
     * @param record
     */
    public void logMessage(LogRecord record) {
        if (getDisplay().getThread() == Thread.currentThread()) {
            directLogMessage(record);
            return;
        }
        getDisplay().asyncExec(new AsyncLogMessage(record));
    }

    public void attachLogger(Logger logger) {
        logger.addHandler(new Handler() {
            public void publish(LogRecord record) {
                logMessage(record);
            }
            
            public void close() {
            }
            
            public void flush() {
            }
        });
    }
}
