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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SystemInfoDialog {
	private static void addPropertyToTable(Table table, String property) {
		String value = System.getProperty(property);
		if (value == null)
			value = "<null>";
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setText(0, property);
		tableItem.setText(1, value);
	}
	
    public static void show(Shell parentShell, List<Image> appImages) {
        final Shell dialog = new Shell(parentShell,
                SWT.APPLICATION_MODAL | SWT.CLOSE);
        SwtUtil.setImages(dialog, appImages);
        dialog.setText("Blimp System Information");
        GridLayout layout = new GridLayout();
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        layout.verticalSpacing = 20;
        dialog.setLayout(layout);

        Table table = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn tableColumn = new TableColumn(table, SWT.LEAD);
		tableColumn.setText("Java property");
		tableColumn = new TableColumn(table, SWT.LEAD | SWT.FILL);
		tableColumn.setText("Value");
		//table.setSize(500, 500);
		addPropertyToTable(table, "java.home");
		addPropertyToTable(table, "java.version");
		addPropertyToTable(table, "java.vendor");
		addPropertyToTable(table, "java.vm.version");
		addPropertyToTable(table, "java.vm.vendor");
		addPropertyToTable(table, "java.vm.name");
		addPropertyToTable(table, "java.class.path");
		addPropertyToTable(table, "java.compiler");
		addPropertyToTable(table, "os.name");
		addPropertyToTable(table, "os.arch");
		
		for (TableColumn col: table.getColumns()) {
			col.pack();
			if (col.getWidth() > 500)
				col.setWidth(500);
		}

        dialog.pack();
		//dialog.setSize(500, 500);
        dialog.open();
    }
}
