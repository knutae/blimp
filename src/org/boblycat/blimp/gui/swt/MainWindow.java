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

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Vector;

class ImageTab {
    CTabItem item;

    ImageView imageView;

    ImageTab(CTabItem item, ImageView imageView) {
        this.item = item;
        this.imageView = imageView;
    }
    
    HistoryBlimpSession getSession() {
        return imageView.getSession();
    }
    
    LayerEditorEnvironment getEditorEnv() {
        LayerEditorEnvironment env = new LayerEditorEnvironment();
        env.session = getSession();
        env.workerThread = imageView.workerThread;
        env.layerWasJustAdded = true;
        return env;
    }
    
    void dispose() {
        item.dispose();
        imageView.dispose();
    }
    
    int tryClose(Shell shell) {
        if (!getSession().isDirty())
            return SWT.YES;
        return SwtUtil.confirmationDialog(shell, "Close Tab",
                getSession().getName() + " has unsaved changes.\n" +
                "Close it without saving?",
                SWT.YES | SWT.NO | SWT.ICON_QUESTION);
    }
}

public class MainWindow {
    Display display;
    Shell shell;
    Label statusLabel;
    Listener menuHideListener;
    Listener menuItemListener;
    MenuItem menuFileOpenImage;
    MenuItem menuFileOpenSession;
    MenuItem menuFileExit;
    MenuItem menuFileSaveSession;
    MenuItem menuFileExportImage;
    MenuItem menuHelpAbout;
    MenuItem menuUndo;
    MenuItem menuRedo;
    CTabFolder mainTabFolder;
    CTabFolder rightTabFolder;
    LayersView layers;
    Vector<ImageTab> imageTabs;
    ImageTab currentImageTab;
    LayerRegistry layerRegistry;
    HistogramView histogramView;
    Vector<Image> appImages;

    class MenuArmListener implements Listener {
        String helpText;

        MenuArmListener(String description) {
            helpText = description;
        }

        public void handleEvent(Event event) {
            statusLabel.setText(helpText);
        }
    }

    class MenuItemListener implements Listener {
        public void handleEvent(Event event) {
            if (event.widget == menuFileOpenImage) {
                doMenuOpen(false);
            }
            else if (event.widget == menuFileOpenSession) {
                doMenuOpen(true);
            }
            else if (event.widget == menuFileExit) {
                doMenuExit();
            }
            else if (event.widget == menuFileSaveSession) {
                doMenuSaveSession();
            }
            else if (event.widget == menuFileExportImage) {
                doMenuExportImage();
            }
            else if (event.widget == menuHelpAbout) {
                doMenuAbout();
            }
            else if (event.widget == menuUndo) {
                doUndo();
            }
            else if (event.widget == menuRedo) {
                doRedo();
            }
            else if (event.widget instanceof MenuItem) {
                MenuItem item = (MenuItem) event.widget;
                if (item.getData() instanceof LayerRegistry.LayerInfo) {
                    LayerRegistry.LayerInfo info = (LayerRegistry.LayerInfo) item
                            .getData();
                    try {
                        addLayer(info.layerClass.newInstance());
                    }
                    catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    MenuItem addMenuItem(Menu menu, String text, String description) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        item.addListener(SWT.Arm, new MenuArmListener(description));
        item.addListener(SWT.Selection, menuItemListener);
        return item;
    }

    Menu addMenu(Menu parentMenu, String text) {
        MenuItem item = new MenuItem(parentMenu, SWT.CASCADE);
        item.setText(text);
        Menu subMenu = new Menu(shell, SWT.DROP_DOWN);
        subMenu.addListener(SWT.Hide, menuHideListener);
        item.setMenu(subMenu);
        return subMenu;
    }
    
    public MainWindow() {
        this(null);
    }

    public MainWindow(DeviceData deviceData) {
        imageTabs = new Vector<ImageTab>();
        display = new Display(deviceData);
        shell = new Shell(display);
        shell.setText("Blimp");
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
        shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event e) {
                for (ImageTab tab: imageTabs) {
                    if (tab.getSession().isDirty()) {
                        int ret = SwtUtil.confirmationDialog(shell,
                                "Exit Blimp",
                                "There are unsaved changes.\n" +
                                "Quit without saving?",
                                SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                        if (ret != SWT.YES)
                            e.doit = false;
                        break;
                    }
                }
            }
        });
        
        // Application images
        appImages = new Vector<Image>();
        SwtUtil.addResourceImages(display, "blimp-logo-16.png", appImages);
        SwtUtil.addResourceImages(display, "blimp-logo-32.png", appImages);
        SwtUtil.addResourceImages(display, "blimp-logo-48.png", appImages);
        if (appImages.size() == 0)
            Util.warn("Failed to load any icon resources");
        else
            SwtUtil.setImages(shell, appImages);
        
        // Menus
        Menu bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);

        menuHideListener = new Listener() {
            public void handleEvent(Event event) {
                status("");
            }
        };
        menuItemListener = new MenuItemListener();

        Menu fileMenu = addMenu(bar, "&File");
        menuFileOpenImage = addMenuItem(fileMenu, "Open &Image",
                "Open an image");
        menuFileOpenSession = addMenuItem(fileMenu, "Open &Project",
                "Open a blimp project");
        menuFileSaveSession = addMenuItem(fileMenu, "&Save Project",
                "Save the current project");
        menuFileExportImage = addMenuItem(fileMenu, "&Export Image",
                "Export the current image");
        menuFileExit = addMenuItem(fileMenu, "E&xit", "Exit the program");
        fileMenu.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event e) {
                boolean canSave = (currentImageTab != null);
                menuFileSaveSession.setEnabled(canSave);
                menuFileExportImage.setEnabled(canSave);
            }
        });
        
        Menu editMenu = addMenu(bar, "&Edit");
        menuUndo = addMenuItem(editMenu, "&Undo", "Undo a change");
        menuUndo.setAccelerator(SWT.CONTROL | 'Z');
        menuRedo = addMenuItem(editMenu, "&Redo", "Redo a change");
        menuRedo.setAccelerator(SWT.CONTROL | 'Y');

        Menu layerMenu = addMenu(bar, "Add &Layer");
        layerRegistry = LayerRegistry.createDefaultRegister();
        for (LayerRegistry.LayerInfo info : layerRegistry) {
            MenuItem item = addMenuItem(layerMenu, info.label, info.description);
            item.setData(info);
        }

        Menu helpMenu = addMenu(bar, "&Help");
        menuHelpAbout = addMenuItem(helpMenu, "&About", "About Blimp...");

        // Bottom status line
        statusLabel = new Label(shell, SWT.BORDER);
        FormData statusLabelData = new FormData();
        statusLabelData.left = new FormAttachment(0);
        statusLabelData.right = new FormAttachment(100);
        statusLabelData.bottom = new FormAttachment(100);
        statusLabel.setLayoutData(statusLabelData);

        // Main GUI with two notebooks
        // int bottomHeight = statusLabel.getSize().y;
        // int bottomHeight = 20;
        SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);
        FormData formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.top = new FormAttachment(0);
        // formData.bottom = new FormAttachment(100, -bottomHeight);
        formData.bottom = new FormAttachment(statusLabel, -2);
        sashForm.setLayoutData(formData);

        mainTabFolder = new CTabFolder(sashForm, SWT.TOP | SWT.BORDER
                | SWT.CLOSE);
        SashForm rightSash = new SashForm(sashForm, SWT.VERTICAL);

        CTabFolder histogramTabFolder = new CTabFolder(rightSash,
                SWT.TOP | SWT.BORDER);
        histogramView = new HistogramView(histogramTabFolder, SWT.NONE);
        CTabItem histogramTabItem = new CTabItem(histogramTabFolder, SWT.NONE);
        histogramTabItem.setText("Histogram");
        histogramTabItem.setControl(histogramView);
        histogramTabFolder.setSelection(histogramTabItem);
        
        rightTabFolder = new CTabFolder(rightSash, SWT.TOP | SWT.BORDER);
        sashForm.setWeights(new int[] { 4, 1 });
        rightSash.setWeights(new int[] { 1, 4 });
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                for (int i = 0; i < imageTabs.size(); i++)
                    if (imageTabs.get(i).item == e.item) {
                        updateCurrentImageTab(imageTabs.get(i));
                        return;
                    }
            }
        });
        mainTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
           public void close(CTabFolderEvent e) {
               for (ImageTab tab: imageTabs) {
                   if (tab.item == e.item) {
                       if (tab.tryClose(shell) != SWT.YES) {
                           e.doit = false;
                           return;
                       }
                       removeImageTab(tab);
                       break;
                   }
               }
               // Trigger a GC to shrink the memory usage
               System.gc();
           }
        });

        // Layers view
        layers = new LayersView(rightTabFolder);
        layers.setLayout(new FillLayout());
        CTabItem tmpItem = new CTabItem(rightTabFolder, SWT.NONE);
        tmpItem.setText("Layers");
        tmpItem.setControl(layers);
        rightTabFolder.setSelection(0);
        
        // Drag and drop from external programs
        DropTarget target = new DropTarget(shell, DND.DROP_COPY);
        target.setTransfer(new Transfer[] {FileTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent e) {
                if (FileTransfer.getInstance().isSupportedType(e.currentDataType))
                    e.detail = DND.DROP_COPY;
            }

            public void drop(DropTargetEvent e) {
                Object obj = FileTransfer.getInstance().nativeToJava(
                        e.currentDataType);
                if (obj instanceof String[]) {
                    e.detail = DND.DROP_COPY;
                    for (String filename: (String[]) obj) {
                        openProjectOrImageFile(filename);
                        // TODO: support more than one file
                        break;
                    }
                }
            }
        });

        shell.open();
    }

    public void mainLoop() {
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    ImageView addImageViewWithSession(HistoryBlimpSession session) {
        ImageView imageView = new ImageView(mainTabFolder, SWT.NONE, session);
        CTabItem item = new CTabItem(mainTabFolder, SWT.CLOSE);
        item.setText(imageView.getSession().getDescription());
        item.setControl(imageView);
        mainTabFolder.setSelection(item);
        currentImageTab = new ImageTab(item, imageView);
        imageTabs.add(currentImageTab);
        imageView.addBitmapListener(new BitmapChangeListener() {
            public void handleChange(BitmapEvent e) {
                if (currentImageTab == null
                        || currentImageTab.imageView != e.getSource())
                    return;
                histogramView.setBitmap(e.getBitmap());
            }
        });
        session.addHistoryListener(new LayerChangeListener() {
            public void handleChange(LayerEvent e) {
                if (currentImageTab == null)
                    return;
                HistoryBlimpSession session = currentImageTab.getSession();
                String name = session.getName();
                if (session.isDirty())
                    name = name + "*";
                currentImageTab.item.setText(name);
            }
        });
        session.recordSaved();
        return imageView;
    }

    ImageView addImageView(String imageFilename) {
        InputLayer input = Util.getInputLayerFromFile(imageFilename);
        if (input instanceof RawFileInputLayer)
            input.setActive(false); // TODO: quick hack for raw input, improve
        HistoryBlimpSession session = new HistoryBlimpSession();
        session.setNameFromFilename(imageFilename);
        session.setInput(input);
        return addImageViewWithSession(session);
    }
    
    void updateCurrentImageTab(ImageTab newImageTab) {
        currentImageTab = newImageTab;
        updateLayersView();
        if (currentImageTab == null) {
            histogramView.setBitmap(null);
        }
        else {
            currentImageTab.imageView.triggerBitmapChange();
        }
    }
    
    private void removeImageTab(ImageTab tab) {
        // Disposing an image tab will automatically close it and
        // select a new one.  The only special case to consider
        // is when the last tab is closed.
        tab.dispose();
        imageTabs.remove(tab);
        if (imageTabs.size() == 0)
            updateCurrentImageTab(null);
    }

    private void fileOpenError(String filename, String errorType, Exception e) {
        SwtUtil.errorDialog(shell, errorType + " error", errorType
                + " error while opening file: " + filename + "\n"
                + e.getMessage());
    }

    void doMenuOpen(boolean openSessionByDefault) {
        // status("File->Open");
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        String imageFilterNames =
            "Images (jpeg, tiff, png, gif, bmp, raw, dng, crw, cr2)";
        String imageExtensionList =
            SwtUtil.getFilterExtensionList(new String[] {
                    "jpg", "jpeg", "tiff", "tif", "png", "gif", "bmp",
                    "raw", "dng", "crw", "cr2"});
        String projectFilterNames =
            "Blimp projects (blimp)";
        String projectExtensionList =
            SwtUtil.getFilterExtensionList(new String[] { "blimp" });
        String allFilesFilterNames = "All Files";
        String allFilesExtensionList =
            SwtUtil.getFilterExtensionList(new String[] { "*" });
        if (openSessionByDefault) {
            dialog.setFilterNames(new String[] {
                    projectFilterNames, imageFilterNames, allFilesFilterNames
            });
            dialog.setFilterExtensions(new String[] {
                    projectExtensionList, imageExtensionList, allFilesExtensionList
            });
        }
        else {
            dialog.setFilterNames(new String[] {
                    imageFilterNames, projectFilterNames, allFilesFilterNames
            });
            dialog.setFilterExtensions(new String[] {
                    imageExtensionList, projectExtensionList, allFilesExtensionList
            });
        }
        String filename = dialog.open();
        if (filename != null)
            openProjectOrImageFile(filename);
    }
    
    private void updateLayersView() {
        if (currentImageTab == null)
            layers.updateWithEnvironment(null);
        else
            layers.updateWithEnvironment(currentImageTab.getEditorEnv());
    }
    
    private void showLayerEditor(Layer layer, LayerEditorCallback callback) {
        if (currentImageTab == null) {
            Util.err("Attempted to show editor without an active image tab");
            return;
        }
        LayerEditorEnvironment env = currentImageTab.getEditorEnv();
        env.layer = layer;
        env.editorCallback = callback;
        layers.updateWithEnvironment(env);
    }

    void openProjectOrImageFile(String filename) {
        if (filename.toLowerCase().endsWith(".blimp")) {
            // open a saved session
            try {
                BlimpSession session = (BlimpSession) Serializer
                        .loadBeanFromFile(filename);
                HistoryBlimpSession historySession = new HistoryBlimpSession();
                historySession.synchronizeSessionData(session);
                historySession.setNameFromFilename(filename);
                addImageViewWithSession(historySession);
                updateLayersView();
            }
            catch (ClassCastException e) {
                fileOpenError(filename, "Class cast", e);
            }
            catch (ClassNotFoundException e) {
                fileOpenError(filename, "Class not found", e);
            }
            catch (IOException e) {
                fileOpenError(filename, "I/O", e);
            }
            catch (SAXException e) {
                fileOpenError(filename, "XML parse", e);
            }
            return;
        }

        // open an image
        ImageView imageView = addImageView(filename);
        HistoryBlimpSession session = imageView.getSession();
        showLayerEditor(session.getInput(), new LayerEditorCallback() {
            public void editingFinished(Layer layer, boolean cancelled) {
                ImageTab tab = currentImageTab;
                if (cancelled) {
                    removeImageTab(tab);
                }
                else if (layer instanceof RawFileInputLayer) {
                    RawFileInputLayer rawInput = (RawFileInputLayer) layer;
                    if (rawInput.getColorDepth() == ColorDepth.Depth16Bit) {
                        // Automatically add a gamma layer for 16-bit raw input,
                        // because dcraw 16-bit output is not gamma corrected
                        // (linear color mapping).
                        GammaLayer gamma = new GammaLayer();
                        gamma.setGamma(2.2);
                        tab.getSession().addLayer(gamma);
                        tab.getSession().recordSaved();
                        layers.refresh();
                    }
                }
            }
        });
        imageView.invalidateImage(); // TODO: is this needed at all?
    }

    void doMenuExit() {
        shell.close();
    }

    void doMenuSaveSession() {
        if (currentImageTab == null)
            return;
        HistoryBlimpSession session = currentImageTab.getSession();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterNames(new String[] { "Blimp projects (*.blimp)" });
        dialog.setFilterExtensions(new String[] {
                SwtUtil.getFilterExtensionList(new String[] { "blimp" }) });
        dialog.setFileName(Util.changeFileExtension(session.getName(), "blimp"));
        String filename = dialog.open();
        if (filename == null)
            return;
        filename = Util.changeFileExtension(filename, "blimp");
        if (Util.fileExists(filename) &&
                !SwtUtil.confirmOverwrite(shell, filename)) {
            SwtUtil.messageDialog(shell, "Aborted",
                    "The project was not saved.", SWT.ICON_WARNING);
            return;
        }
        try {
            Serializer.saveBeanToFile(session, filename);
            session.recordSaved();
            session.setNameFromFilename(filename);
            currentImageTab.item.setText(session.getName());
            status("Project saved to " + filename);
            //SwtUtil.messageDialog(shell, "Project Saved",
            //        "The project was saved:\n" + filename, SWT.ICON_INFORMATION);
        }
        catch (IOException e) {
            SwtUtil.errorDialog(shell, "Save Error",
                    "An I/O error occured: " + e.getMessage());
        }
    }

    void doMenuExportImage() {
        if (currentImageTab == null)
            return;
        BlimpSession session = currentImageTab.getSession();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterNames(
                new String[] { "Exportable image formats (jpeg, png, bmp)" });
        dialog.setFilterExtensions(new String[] {
                SwtUtil.getFilterExtensionList(new String[] {
                        "jpeg", "jpg",  "png", "bmp" }) });
        dialog.setFileName(Util.changeFileExtension(session.getName(), "jpg"));
        String filename = dialog.open();
        if (filename == null)
            return;
        String ext = Util.getFileExtension(filename);
        if (!BitmapUtil.canSaveToFormat(ext)) {
            SwtUtil.errorDialog(shell, "Image export",
                    "Unsupported file type: " + ext);
            return;
        }
        if (Util.fileExists(filename) && !SwtUtil.confirmOverwrite(shell, filename)) {
            SwtUtil.messageDialog(shell, "Aborted Export",
                    "No image was exported.", SWT.ICON_WARNING);
            return;
        }
        try {
            BitmapUtil.writeBitmap(session.getFullBitmap(), ext, filename, 0.9);
            SwtUtil.messageDialog(shell, "Image Exported",
                    "The image was exported to:\n" + filename, SWT.ICON_INFORMATION);
        }
        catch (IOException e) {
            SwtUtil.errorDialog(shell, "Image Export", "An I/O error occured: "
                    + e.getMessage());
        }
    }

    void doMenuAbout() {
        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        SwtUtil.setImages(dialog, appImages);
        dialog.setText("About Blimp");
        GridLayout layout = new GridLayout();
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        layout.verticalSpacing = 20;
        dialog.setLayout(layout);

        final Image aboutImage = SwtUtil.loadResourceImage(display,
                "blimp-about.png");
        if (aboutImage == null)
            Util.warn("Failed to load blimp-about.png");
        else {
            Label label = new Label(dialog, SWT.NONE);
            label.setImage(aboutImage);
            label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        }
        
        Label heading = new Label(dialog, SWT.NONE);
        heading.setText("Blimp, a layered photo editor.");
        final Font bigFont = SwtUtil.copyFontWithHeight(heading.getFont(), 18);
        heading.setFont(bigFont);
        
        Link linkText = new Link(dialog, SWT.NONE);
        linkText.setText(
                "Copyright 2006-2007 Knut Arild Erstad\n"
                + "\n"
                + "Blimp is <a href=\"http://www.gnu.org/philosophy/free-sw.html\">free software</a>"
                + " distributed under the <a href=\"http://www.gnu.org/licenses/info/GPLv2.html\">GNU General Public License, version 2</a>.\n"
                + "\n"
                + "Credits:\n"
                + "<a href=\"http://schmidt.devlib.org/jiu/\">Java Imaging Utilities</a> by Marco Schmidt and others\n"
                + "<a href=\"http://cybercom.net/~dcoffin/dcraw/\">dcraw</a> (Raw input) by Dave Coffin\n"
                + "\n"
                + "Please visit the <a href=\"http://projects.boblycat.org/blimp/\">Blimp project page</a>"
                + " for more information and resources.\n");
        linkText.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                Util.openLinkInBrowser(e.text);
            }
        });
        Button button = new Button(dialog, SWT.PUSH);
        button.setText("Close");
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                dialog.close();
            }
        });
        button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        
        dialog.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event e) {
                SwtUtil.dispose(aboutImage);
                SwtUtil.dispose(bigFont);
            }
        });
        
        dialog.pack();
        dialog.open();
    }
    
    void doUndo() {
        if (currentImageTab == null)
            return;
        currentImageTab.getSession().undo();
        layers.refresh();
    }
    
    void doRedo() {
        if (currentImageTab == null)
            return;
        currentImageTab.getSession().redo();
        layers.refresh();
    }

    void status(String msg) {
        statusLabel.setText(msg);
    }

    void addLayer(AdjustmentLayer layer) {
        // ImageTab tab = getCurrentTab();
        ImageTab tab = currentImageTab;
        if (tab == null)
            return;
        HistoryBlimpSession session = tab.getSession();
        session.beginDisableAutoRecord();
        try {
            // New layers are always inactive initially
            layer.setActive(false);
            session.addLayer(layer);
            tab.imageView.invalidateImage();
            showLayerEditor(layer, new LayerEditorCallback() {
                public void editingFinished(Layer layer, boolean cancelled) {
                    if (cancelled) {
                        BlimpSession session = currentImageTab.imageView
                                .getSession();
                        session.removeLayer(layer);
                    }
                    layers.refresh();
                }
            });
        }
        finally {
            session.endDisableAutoRecord();
        }
    }

    public static void main(String[] args) {
        MainWindow window = new MainWindow();
        window.mainLoop();
    }
}