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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class PrintEditor extends GridBasedLayerEditor {
    private PrintLayer printLayer;
    private Label sizeLabel;
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
        sizeLabel = createLabel("No printer selected");
        borderSlider = createSlider("Border (%)", 0, 99, 0);
    }

    private Label createLabel(String initialText) {
        Label label = new Label(this, SWT.NONE);
        label.setText(initialText);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return label;
    }

    private void updateWithPrinterData(PrinterData data) {
        if (data == null)
            return;
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
        if (printLayer.getPaperWidth() <= 0 || printLayer.getPaperHeight() <= 0)
            sizeLabel.setText("No printer selected");
        else
            sizeLabel.setText(String.format("Printing resolution: %s x %s",
                    printLayer.getPaperWidth(), printLayer.getPaperHeight()));
        borderSlider.setSelectionAsDouble(printLayer.getBorderPercentage());
    }

    @Override
    protected void updateLayer() {
        printLayer.setBorderPercentage(borderSlider.getSelectionAsDouble());
    }
}
