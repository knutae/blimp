/*
 * Copyright (C) 2007 Knut Arild Erstad
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

import java.io.InputStream;
import java.util.Vector;

import org.boblycat.blimp.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * SWT-related utility functions.
 * 
 * @author Knut Arild Erstad
 */
public class SwtUtil {
    private static ImageLoader imageLoaderInstance;
    
    public static void errorDialog(Shell parentShell, String title,
            String message) {
        messageDialog(parentShell, title, message, SWT.ICON_ERROR);
    }
    
    /**
     * Show a message dialog with an OK button.
     * @param parentShell The parent window.
     * @param title The window title.
     * @param message The message text.
     * @param style The style, which should include one of the
     *              <code>SWT.ICON_</code> constants.
     */
    public static void messageDialog(Shell parentShell, String title,
            String message, int style) {
        MessageBox box = new MessageBox(parentShell, SWT.OK | style);
        box.setText(title);
        box.setMessage(message);
        box.open();
    }
    
    public static int confirmationDialog(Shell parentShell, String title,
            String message, int style) {
        MessageBox box = new MessageBox(parentShell, style);
        box.setText(title);
        box.setMessage(message);
        return box.open();
    }
    
    /**
     * Dispose the given widget if it is not <code>null</code>.
     * @param widget A widget, or <code>null</code>.
     */
    public static void dispose(Widget widget) {
        if (widget == null || widget.isDisposed())
            return;
        widget.dispose();
    }

    /**
     * Dispose the given resource if it is not <code>null</code>.
     * @param resource A graphics resource, or <code>null</code>.
     */
    public static void dispose(Resource resource) {
        if (resource == null || resource.isDisposed())
            return;
        resource.dispose();
    }
    
    /**
     * Get a platform-dependent list of extensions useful for SWT file dialogs.
     * @param names An array of extension names not including the dot, or "*" for all files.
     * @return A platform-dependent string of wildcards, separated by semicolons.
     */
    public static String getFilterExtensionList(String[] extensions) {
        StringBuilder builder = new StringBuilder();
    	if (Util.isWindowsOS()) {
            for (String ext : extensions) {
                builder.append(";*." + ext);
            }
        }
        else {
            // not windows: assume unix
            for (String ext : extensions) {
                builder.append(';');
                if (ext.equals("*"))
                    builder.append("*");
                else if (ext.toLowerCase().equals(ext.toUpperCase())) {
                    // no case
                    builder.append("*." + ext);
                }
                else {
                    // allow both cases
                    builder.append("*." + ext.toLowerCase());
                    builder.append(";*." + ext.toUpperCase());
                }
            }
        }
        if (builder.length() == 0)
            return "";
        // strip first separator
        return builder.substring(1);
    }
    
    public static void fillColorRect(GC gc, Rectangle rect,
            int red, int green, int blue) {
        Color color = new Color(gc.getDevice(), red, green, blue);
        gc.setBackground(color);
        gc.fillRectangle(rect);
        color.dispose();
    }
    
    public static void fillBlackRect(GC gc, Rectangle rect) {
        fillColorRect(gc, rect, 0, 0, 0);
    }
    
    public static void fillWhiteRect(GC gc, Rectangle rect) {
        fillColorRect(gc, rect, 255, 255, 255);
    }
    
    public static void drawColorLine(GC gc, int x1, int y1, int x2, int y2,
            int red, int green, int blue) {
        Color color = new Color(gc.getDevice(), red, green, blue);
        gc.setForeground(color);
        gc.drawLine(x1, y1, x2, y2);
        color.dispose();
    }
    
    /**
     * Show a confirmation dialog asking the if it is okay to overwrite
     * the given file.
     * @param parentShell The parent window.
     * @param filename The file to be overwritten.
     * @return <code>true</code> if the user confirmed the overwrite.
     */
    public static boolean confirmOverwrite(Shell parentShell,
            String filename) {
        MessageBox box = new MessageBox(parentShell,
                SWT.YES | SWT.NO | SWT.ICON_QUESTION);
        box.setText("Overwrite File");
        box.setMessage("Do you want to overwrite the existing file?\n"
                + filename);
        int result = box.open();
        return (result == SWT.YES);
    }
    
    private static ImageData[] loadResourceImageData(String filename) {
        if (imageLoaderInstance == null)
            imageLoaderInstance = new ImageLoader();
        String path = "resources/images/" + filename;
        InputStream stream = ClassLoader.getSystemResourceAsStream(path);
        if (stream == null)
            return null;
        return imageLoaderInstance.load(stream);
    }
    
    public static Image[] loadResourceImages(Device device, String filename) {
        ImageData[] data = loadResourceImageData(filename);
        if (data == null)
            return null;
        Image[] images = new Image[data.length];
        for (int i = 0; i < data.length; i++)
            images[i] = new Image(device, data[i]);
        return images;
    }
    
    public static Image loadResourceImage(Device device, String filename) {
        Image[] images = loadResourceImages(device, filename);
        if (images == null || images.length == 0)
            return null;
        return images[0];
    }
    
    public static void addResourceImages(Device device, String filename,
            Vector<Image> imageVector) {
        Image[] loadedImages = loadResourceImages(device, filename);
        if (loadedImages == null)
            return;
        for (Image image: loadedImages)
            imageVector.add(image);
    }
    
    public static void setImages(Shell shell, Vector<Image> images) {
        if (images == null || images.size() == 0)
            return;
        Image[] imageArray = new Image[images.size()];
        for (int i = 0; i < imageArray.length; i++)
            imageArray[i] = images.get(i);
        shell.setImages(imageArray);
    }
    
    public static Font copyFontWithHeight(Font font, int newHeight) {
        FontData[] fontDataArray = font.getFontData();
        for (FontData data: fontDataArray)
            data.setHeight(newHeight);
        return new Font(font.getDevice(), fontDataArray);
    }
    
    public static void modalLoop(Shell window) {
        Display display = window.getDisplay();
        while (!window.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();
    }
}
