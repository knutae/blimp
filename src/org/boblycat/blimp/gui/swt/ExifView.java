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

import org.boblycat.blimp.exif.ExifField;
import org.boblycat.blimp.exif.ExifTable;
import org.boblycat.blimp.exif.ExifTag;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A view showing some Exif data for a photograph.
 *
 * @author Knut Arild Erstad
 */
public class ExifView extends Composite {

    private Table table;
    private ExifTable data;

    /**
     * Constructs an ExifView
     * @param parent
     * @param style
     */
    public ExifView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());
        table = new Table(this, SWT.NONE);
        table.setHeaderVisible(true);
        TableColumn col = new TableColumn(table, SWT.LEFT);
        col.setText("Name");
        col.setWidth(200);
        col = new TableColumn(table, SWT.LEFT);
        col.setText("Value");
        col.setWidth(200);
    }

    private void addField(ExifTag tag) {
        System.out.println("tag: " + tag);
        ExifField field = data.get(tag);
        if (field == null)
            return;
        System.out.println("value: " + field);
        TableItem item = new TableItem(table, SWT.NONE);
        //item.setText(tag.toString());
        item.setText(0, tag.toString());
        item.setText(1, field.toString());
    }

    private void addFields(ExifTag[] tags) {
        for (ExifTag tag: tags)
            addField(tag);
    }

    public void setData(ExifTable data) {
        this.data = data;
        table.clearAll();
        if (data == null)
            return;
        addFields(new ExifTag[] {
                ExifTag.XResolution,
                ExifTag.YResolution,
                ExifTag.ResolutionUnit,
                ExifTag.Make,
                ExifTag.Model,
                //ExifTag.ShutterSpeedValue,
                //ExifTag.ApertureValue,
                ExifTag.Flash,
                ExifTag.FlashEnergy,
                ExifTag.FNumber,
                ExifTag.ExposureTime,
                ExifTag.ISOSpeedRatings,
                ExifTag.MeteringMode,
                ExifTag.ExposureProgram,
                ExifTag.ExposureBiasValue,
                ExifTag.FocalLength,
                ExifTag.FocalLengthIn35mmFilm,
                ExifTag.SubjectDistance,
                ExifTag.LightSource,
                ExifTag.WhiteBalance,
                ExifTag.DateTime,
                ExifTag.DateTimeOriginal,
                ExifTag.DateTimeDigitized,
                ExifTag.SubsecTime,
                ExifTag.SubsecTimeOriginal,
                ExifTag.SubsecTimeDigitized,
                ExifTag.ImageDescription,
                ExifTag.Artist,
                ExifTag.Copyright,
                ExifTag.ImageUniqueID,
        });
    }

    public static void showInDialog(Shell parentShell, ExifTable data) {
        Shell dialog = new Shell(parentShell,
                SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.RESIZE);
        dialog.setText("Exif Metadata");
        dialog.setLayout(new FillLayout());
        ExifView view = new ExifView(dialog, SWT.NONE);
        view.setData(data);
        dialog.pack();
        dialog.open();
    }
}
