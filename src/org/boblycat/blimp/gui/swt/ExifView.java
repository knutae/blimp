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
package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Util;
import org.boblycat.blimp.exif.ExifField;
import org.boblycat.blimp.exif.ExifTable;
import org.boblycat.blimp.exif.ExifTag;
import org.boblycat.blimp.exif.Rational;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
        
        addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                // Quick solution: all columns get the same width after resize.
                // Should ideally keep track of weights for each column...
                int colWidth = getClientArea().width / table.getColumnCount();
                for (TableColumn col: table.getColumns()) {
                    col.setWidth(colWidth);
                }
            }
        });
    }
    
    private static String splitTagNameIntoWords(String name) {
        StringBuilder result = new StringBuilder(name.length() + 5);
        for (int i=0; i<name.length(); ++i) {
            char c = name.charAt(i);
            if (i > 0 && i < name.length()-1) {
                char next = name.charAt(i+1);
                if (Character.isUpperCase(c) && Character.isLowerCase(next))
                    result.append(' ');
            }
            result.append(c);
        }
        return result.toString();
    }
    
    private static String describeTag(ExifTag tag) {
        switch (tag) {
        case Make:
            return "Camera Make";
        case Model:
            return "Camera Model";
        case ISOSpeedRatings:
            return "ISO";
        case FNumber:
            return "Aperture";
        default:
            return splitTagNameIntoWords(tag.toString());
        }
    }
    
    private static String describeFlashValue(ExifField field) {
        int value = (Integer) field.valueAt(0);
        StringBuilder result = new StringBuilder();
        // Bit 0: flash fired
        if ((value & 0x01) == 0)
            result.append("No");
        else
            result.append("Yes");
        // Bits 1-2: returned light
        int tmp = (value >> 1) & 0x03;
        if (tmp == 2)
            result.append(", return light not detected");
        else if (tmp == 3)
            result.append(", return light detected");
        // Bits 3-4: flash mode
        tmp = (value << 3) & 0x03;
        if (tmp == 1)
            result.append(", compulsory flash mode");
        else if (tmp == 2)
            result.append(", compulsory flash suppression mode");
        else if (tmp == 3)
            result.append(", auto mode");
        // Bit 5: flash function presence
        tmp = (value << 5) & 0x01;
        if (tmp == 1)
            result.append(", flash function not present");
        // Bit 6: red-eye mode
        tmp = (value << 6) & 0x01;
        if (tmp == 1)
            result.append(", red-eye reduction supported");
        
        return result.toString();
    }
    
    private static String describeExposureProgram(ExifField field) {
        int value = (Integer) field.valueAt(0);
        switch (value) {
        case 0:
            return "Not defined";
        case 1:
            return "Manual";
        case 2:
            return "Normal program";
        case 3:
            return "Aperture priority";
        case 4:
            return "Shutter priority";
        case 5:
            return "Creative program";
        case 6:
            return "Action program";
        case 7:
            return "Portrait mode";
        case 8:
            return "Landscape mode";
        default:
            return "Unknown (" + value + ")";
        }
    }
    
    private static String describeDecimal(ExifField field, String unit) {
        Rational value = (Rational) field.valueAt(0);
        String str = value.toDecimalString();
        if (unit != null && unit.length() > 0)
            str += " " + unit;
        return str;
    }
    
    private static String describeRational(ExifField field, String unit) {
        Rational value = (Rational) field.valueAt(0);
        String str = value.toSimpleString();
        if (unit != null && unit.length() > 0)
            str += " " + unit;
        return str;
    }
    
    private static String describeWhiteBalance(ExifField field) {
        int value = (Integer) field.valueAt(0);
        if (value == 0)
            return "Auto";
        else if (value == 1)
            return "Manual";
        else
            return "Unknown (" + value + ")";
    }
    
    private static String describeValue(ExifTag tag, ExifField field) {
        try {
            switch (tag) {
            case Flash:
                return describeFlashValue(field);
            case ExposureProgram:
                return describeExposureProgram(field);
            case FocalLength:
                return describeDecimal(field, "mm");
            case ExposureBiasValue:
                return describeRational(field, null);
            case FNumber:
                return describeDecimal(field, null);
            case WhiteBalance:
                return describeWhiteBalance(field);
            }
        }
        catch (IndexOutOfBoundsException e) {
            Util.err(tag.toString() + " error: " + e.getMessage());
        }
        catch (ClassCastException e) {
            Util.err(tag.toString() + " error: " + e.getMessage());
        }
        return field.toString();
    }
    
    private void addField(ExifTag tag) {
        ExifField field = data.get(tag);
        if (field == null) {
            //TableItem item = new TableItem(table, SWT.NONE);
            //item.setText(0, describeTag(tag));
            //item.setText(1, "<none>");
            return;
        }
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, describeTag(tag));
        item.setText(1, describeValue(tag, field));
    }

    private void addFields(ExifTag[] tags) {
        for (ExifTag tag: tags)
            addField(tag);
    }

    public void setData(ExifTable data) {
        this.data = data;
        table.removeAll();
        if (data == null)
            return;
        addFields(new ExifTag[] {
                ExifTag.Make,
                ExifTag.Model,
                ExifTag.Flash,
                ExifTag.FlashEnergy,
                ExifTag.FNumber,
                ExifTag.ExposureTime,
                ExifTag.ISOSpeedRatings,
                ExifTag.MeteringMode,
                ExifTag.ExposureProgram,
                ExifTag.ExposureBiasValue,
                ExifTag.FocalLength,
                ExifTag.WhiteBalance,
                ExifTag.DateTime,
                ExifTag.ImageDescription,
                ExifTag.Artist,
                ExifTag.Copyright,
                ExifTag.ImageUniqueID,
        });
    }
}
