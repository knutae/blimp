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
package org.boblycat.blimp.gui.swt.editors;

import org.boblycat.blimp.gui.swt.SwtUtil;
import org.boblycat.blimp.gui.swt.ValueSlider;
import org.boblycat.blimp.layers.PrintLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class PrintEditor extends GridBasedLayerEditor {
    private static final String NO_PRINTER = "No printer selected";

    private PrinterData printerData;
    private PrintLayer printLayer;
    private Text printerDescription;
    private ValueSlider borderSlider;

    public PrintEditor(Composite parent, int style) {
        super(parent, style);
        Button button = new Button(this, SWT.PUSH);
        button.setText("Select Printer and Options ...");
        button.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (isDisposed())
                    return;
                PrintDialog dlg = new PrintDialog(getShell());
                PrinterData data = dlg.open();
                updateWithPrinterData(data);
            }
        });

        Group group = createGroup("Printer Information");
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        printerDescription = new Text(group, SWT.READ_ONLY | SWT.MULTI | SWT.LEFT);
        printerDescription.setText(NO_PRINTER);

        borderSlider = createSlider("Border (%)", 0, 99, 0);
        button = new Button(this, SWT.PUSH);
        button.setText("Print Now");
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (isDisposed())
                    return;
                if (printerData == null) {
                    SwtUtil.messageDialog(getShell(), "Select Printer", "Please select a printer",
                            SWT.ICON_INFORMATION);
                    return;
                }
                Runnable testRunnable = new Runnable() {
                    public void run() {
                        SwtUtil.messageDialog(getShell(), "Test", "Hello", SWT.ICON_INFORMATION);
                    }
                };
                workerThread.asyncPrint(PrintEditor.this, session, testRunnable, printerData, printLayer);
            }
        });
    }

    private void updateWithPrinterData(PrinterData data) {
        if (data == null)
            return;
        printerData = data;
        Printer printer = new Printer(data);
        try {
            printLayer.setPaperWidth(printer.getClientArea().width);
            printLayer.setPaperHeight(printer.getClientArea().height);
        }
        finally {
            printer.dispose();
        }

        printLayer.invalidate(); // update image (preview)
        layerChanged(); // update GUI
    }

    @Override
    protected void layerChanged() {
        printLayer = (PrintLayer) layer;
        borderSlider.setSelectionAsDouble(printLayer.getBorderPercentage());

        if (printerData == null) {
            printerDescription.setText(NO_PRINTER);
            // Try to auto-select the default printer
            updateWithPrinterData(Printer.getDefaultPrinterData());
        }
        else {
            String orientation;
            if (printLayer.getPaperWidth() > printLayer.getPaperHeight())
                orientation = "Landscape";
            else
                orientation = "Portrait";
            printerDescription.setText(
                    "Printer: " + printerData.name + "\n" +
                    "Driver: " + printerData.driver + "\n" +
                    "Orientation: " + orientation + "\n" +
                    "Resolution: " + printLayer.getPaperWidth() + "x" + printLayer.getPaperHeight());
        }
    }

    @Override
    protected void updateLayer() {
        printLayer.setBorderPercentage(borderSlider.getSelectionAsDouble());
    }
}
