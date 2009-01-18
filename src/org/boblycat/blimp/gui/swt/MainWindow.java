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

import org.boblycat.blimp.*;
import org.boblycat.blimp.exif.ExifTable;
import org.boblycat.blimp.layers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Image;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

class ImageTab {
    CTabItem item;
    ImageView imageView;
    LayerEditorEnvironment editorEnv;

    ImageTab(CTabItem item, ImageView imageView) {
        this.item = item;
        this.imageView = imageView;
        
        editorEnv = new LayerEditorEnvironment();
        editorEnv.session = getSession();
        editorEnv.workerThread = imageView.workerThread;
        editorEnv.layerWasJustAdded = true;
    }

    HistoryBlimpSession getSession() {
        return imageView.getSession();
    }

    void dispose() {
        item.dispose();
        imageView.dispose();
        editorEnv.workerThread.cancelRequestsByOwner(this);
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
    MenuItem menuFileSaveSessionAs;
    MenuItem menuFileExportImage;
    MenuItem menuHelpAbout;
    MenuItem menuHelpSystemInfo;
    MenuItem menuUndo;
    MenuItem menuRedo;
    CTabFolder mainTabFolder;
    CTabFolder rightTabFolder;
    LayersView layers;
    ArrayList<ImageTab> imageTabs;
    ImageTab currentImageTab;
    LayerRegistry layerRegistry;
    HistogramView histogramView;
    ArrayList<Image> appImages;
    ExifView exifView;

    class MenuArmListener implements Listener {
        String helpText;

        MenuArmListener(String description) {
            helpText = description;
        }

        public void handleEvent(Event event) {
            status(helpText);
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
                doMenuSaveSessionDirect();
            }
            else if (event.widget == menuFileSaveSessionAs) {
                doMenuSaveSessionAs();
            }
            else if (event.widget == menuFileExportImage) {
                doMenuExportImage();
            }
            else if (event.widget == menuHelpAbout) {
                doMenuAbout();
            }
            else if (event.widget == menuHelpSystemInfo) {
                SystemInfoDialog.show(shell, appImages);
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
        imageTabs = new ArrayList<ImageTab>();
        display = new Display(deviceData);
        shell = new Shell(display);
        shell.setText("Blimp " + Version.versionString);
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
        appImages = new ArrayList<Image>();
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

        // Listener used to enable all menu items, which is used
        // as a simple way to ensure that accelerators are enabled.
        Listener enableAllMenuItems = new Listener() {
            public void handleEvent(Event e) {
                if (!(e.widget instanceof Menu))
                    return;
                Menu menu = (Menu) e.widget;
                for (MenuItem item: menu.getItems())
                    item.setEnabled(true);
            }
        };

        Menu fileMenu = addMenu(bar, "&File");
        menuFileOpenImage = addMenuItem(fileMenu, "Open &Image",
                "Open an image");
        menuFileOpenSession = addMenuItem(fileMenu, "Open &Project",
                "Open a blimp project");
        menuFileSaveSession = addMenuItem(fileMenu, "&Save Project\tCtrl+S",
                "Save the current project");
        menuFileSaveSession.setAccelerator(SWT.CONTROL | 'S');
        menuFileSaveSessionAs = addMenuItem(fileMenu, "Save Project &As",
                "Save the current project in a new file");
        menuFileExportImage = addMenuItem(fileMenu, "&Export Image",
                "Export the current image");
        menuFileExit = addMenuItem(fileMenu, "E&xit", "Exit the program");
        fileMenu.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event e) {
                boolean canSave = (currentImageTab != null);
                menuFileSaveSession.setEnabled(canSave);
                menuFileSaveSessionAs.setEnabled(canSave);
                menuFileExportImage.setEnabled(canSave);
            }
        });
        fileMenu.addListener(SWT.Hide, enableAllMenuItems);

        Menu editMenu = addMenu(bar, "&Edit");
        menuUndo = addMenuItem(editMenu, "&Undo\tCtrl+Z", "Undo a change");
        menuUndo.setAccelerator(SWT.CONTROL | 'Z');
        menuRedo = addMenuItem(editMenu, "&Redo\tCtrl+Y", "Redo a change");
        menuRedo.setAccelerator(SWT.CONTROL | 'Y');
        editMenu.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event e) {
                SessionHistory history = null;
                if (currentImageTab != null) {
                    history = currentImageTab.getSession().getHistory();
                }
                menuUndo.setEnabled(history != null && history.canUndo());
                menuRedo.setEnabled(history != null && history.canRedo());
            }
        });
        editMenu.addListener(SWT.Hide, enableAllMenuItems);

        Menu layerMenu = addMenu(bar, "Add &Layer");
        layerRegistry = LayerRegistry.createDefaultRegistry();
        for (LayerRegistry.Category category : layerRegistry) {
            Menu catMenu = addMenu(layerMenu, category.label);
            for (LayerRegistry.LayerInfo info : category) {
                MenuItem item = addMenuItem(catMenu, info.label, info.description);
                item.setData(info);
            }
        }

        Menu helpMenu = addMenu(bar, "&Help");
        menuHelpSystemInfo = addMenuItem(helpMenu, "&System Information", "System Information");
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
        SashForm mainSash = new SashForm(shell, SWT.HORIZONTAL);
        FormData formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.top = new FormAttachment(0);
        // formData.bottom = new FormAttachment(100, -bottomHeight);
        formData.bottom = new FormAttachment(statusLabel, -2);
        mainSash.setLayoutData(formData);

        SashForm leftSash = new SashForm(mainSash, SWT.VERTICAL);
        mainTabFolder = new CTabFolder(leftSash, SWT.TOP | SWT.BORDER
                | SWT.CLOSE);
        
        CTabFolder bottomTabs = new CTabFolder(leftSash, SWT.TOP | SWT.BORDER);
        LoggerView loggerView = new LoggerView(bottomTabs, SWT.NONE);
        Util.info("Welcome to the Blimp photo editor " + Version.versionString + "!");
        CTabItem tabItem = new CTabItem(bottomTabs, SWT.NONE);
        tabItem.setText("Messages");
        tabItem.setControl(loggerView);
        bottomTabs.setSelection(tabItem);
        
        exifView = new ExifView(bottomTabs, SWT.NONE);
        tabItem = new CTabItem(bottomTabs, SWT.NONE);
        tabItem.setText("Exif Metadata");
        tabItem.setControl(exifView);

        SashForm rightSash = new SashForm(mainSash, SWT.VERTICAL);

        CTabFolder histogramTabFolder = new CTabFolder(rightSash,
                SWT.TOP | SWT.BORDER);
        histogramView = new HistogramView(histogramTabFolder, SWT.NONE);
        CTabItem histogramTabItem = new CTabItem(histogramTabFolder, SWT.NONE);
        histogramTabItem.setText("Histogram");
        histogramTabItem.setControl(histogramView);
        histogramTabFolder.setSelection(histogramTabItem);
        rightTabFolder = new CTabFolder(rightSash, SWT.TOP | SWT.BORDER);

        mainSash.setWeights(new int[] { 4, 1 });
        leftSash.setWeights(new int[] { 5, 1 });
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
                        asyncOpenFile(filename);
                    }
                }
            }
        });

        shell.open();
    }

    public void mainLoop() {
        SwtUtil.modalLoop(shell);
        display.dispose();
    }

    ImageView addImageViewWithSession(HistoryBlimpSession session, boolean dirty) {
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
        if (!dirty)
            session.recordSaved(true);
        return imageView;
    }

    ImageView addImageView(String imageFilename) {
        InputLayer input = Util.getInputLayerFromFile(imageFilename);
        if (input instanceof RawFileInputLayer)
            input.setActive(false); // TODO: quick hack for raw input, improve
        HistoryBlimpSession session = new HistoryBlimpSession();
        session.setNameFromFilename(imageFilename);
        session.setInput(input);
        return addImageViewWithSession(session, false);
    }

    void updateCurrentImageTab(ImageTab newImageTab) {
        currentImageTab = newImageTab;
        updateLayersView();
        updateExifView();
        if (currentImageTab == null) {
            histogramView.setBitmap(null);
        }
        else {
            currentImageTab.imageView.triggerBitmapChange();
        }
    }
    
    void updateExifView() {
        exifView.setData(null);
        if (currentImageTab == null)
            return;

        LayerEditorEnvironment env = currentImageTab.editorEnv;
        env.workerThread.getExifData(currentImageTab, env.session,
                new ExifQueryTask() {
            public void handleExifData(ExifTable table) {
                if (!exifView.isDisposed())
                    exifView.setData(table);
            }
        });
    }

    ImageTab findImageTab(BlimpSession session) {
        for (ImageTab tab: imageTabs) {
            if (tab.getSession() == session)
                return tab;
        }
        return null;
    }

    private void removeImageTab(ImageTab tab) {
        // Disposing an image tab will automatically close it and
        // select a new one.  The only special case to consider
        // is when the last tab is closed.
        tab.editorEnv.workerThread.cancelRequestsByOwner(tab);
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
            layers.updateWithEnvironment(currentImageTab.editorEnv);
    }

    private void showLayerEditor(Layer layer, LayerEditorCallback callback) {
        if (currentImageTab == null) {
            Util.err("Attempted to show editor without an active image tab");
            return;
        }
        LayerEditorEnvironment env = currentImageTab.editorEnv;
        env.layer = layer;
        env.editorCallback = callback;
        layers.updateWithEnvironment(env);
    }

    boolean tryEnsureInputFileExists(BlimpSession session)
    throws FileNotFoundException {
    	String strPath = session.inputFilePath();
    	if (strPath == null)
    		return false;
    	File filePath = new File(strPath);
    	if (filePath.exists())
    		return false;
        // filePath is given, but does not exist
        String newPath = FileSearchView.showDialog(shell, filePath.getName());
        if (newPath == null)
            throw new FileNotFoundException(String.format(
                    "The input image %s was not found.",
                    filePath.getAbsolutePath()));
        if (newPath.equals(filePath.getAbsolutePath()))
            return false;
        BlimpBean.Property prop = session.getInput().findProperty("filePath");
        assert(prop != null);
        prop.setValue(newPath);
        return true;
    }

    void openProjectOrImageFile(String filename) {
        if (filename.toLowerCase().endsWith(".blimp")) {
            // open a saved session
            try {
                HistoryBlimpSession historySession =
                    Serializer.loadHistorySessionFromFile(filename);
                boolean dirty = false;
                try {
                    dirty = tryEnsureInputFileExists(historySession);
                }
                catch (FileNotFoundException e) {
                    SwtUtil.errorDialog(shell, "Input File Error",
                            e.getMessage());
                    return;
                }
                addImageViewWithSession(historySession, dirty);
                updateLayersView();
                updateExifView();
                historySession.triggerChangeEvent();
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
            public void editingFinished(LayerEditorEnvironment env,
                    boolean cancelled) {
                if (cancelled) {
                    ImageTab tab = findImageTab(env.session);
                    if (tab != null)
                        removeImageTab(tab);
                }
                else if (env.layer instanceof RawFileInputLayer) {
                    RawFileInputLayer rawInput = (RawFileInputLayer) env.layer;
                    if (rawInput.getColorDepth() == ColorDepth.Depth16Bit) {
                        // Automatically add a gamma layer for 16-bit raw input,
                        // because dcraw 16-bit output is not gamma corrected
                        // (linear color mapping).
                        GammaLayer gamma = new GammaLayer();
                        gamma.setGamma(2.2);
                        env.session.addLayer(gamma);
                        env.session.recordSaved(true);
                    }
                }
                updateExifView();
            }
        });
        updateExifView();
    }

    void asyncOpenFile(String fileName) {
        if (display.isDisposed())
            return;
        final String fname = fileName;
        display.asyncExec(new Runnable() {
            public void run() {
                openProjectOrImageFile(fname);
            }
        });
    }

    void doMenuExit() {
        shell.close();
    }

    void saveCurrentSession(String filename) {
        if (currentImageTab == null)
            return;
        HistoryBlimpSession session = currentImageTab.getSession();
        String oldName = session.getName();
        try {
            session.setNameFromFilename(filename);
            Serializer.saveBeanToFile(session, filename);
            session.recordSaved(false);
            currentImageTab.item.setText(session.getName());
            Util.info("Project saved to " + filename);
            //SwtUtil.messageDialog(shell, "Project Saved",
            //        "The project was saved:\n" + filename, SWT.ICON_INFORMATION);
        }
        catch (IOException e) {
            session.setName(oldName); // restore old name if the save failed
            SwtUtil.errorDialog(shell, "Save Error",
                    "An I/O error occured: " + e.getMessage());
        }
    }

    /**
     * Save -- overwrite an existing project without confirmation.
     * If the session has not previously been saved, trigger a "Save As".
     */
    void doMenuSaveSessionDirect() {
        if (currentImageTab == null)
            return;
        HistoryBlimpSession session = currentImageTab.getSession();
        if (session.getProjectFilePath() == null)
            doMenuSaveSessionAs();
        else
            saveCurrentSession(session.getProjectFilePath());
    }

    /**
     * Save As -- show a save dialog, ask for overwrite confirmation if an
     * existing file is selected, then save.
     */
    void doMenuSaveSessionAs() {
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
        saveCurrentSession(filename);
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
        double quality = 0.9;
        if (ext.equals("jpg") || ext.equals("jpeg"))
            quality = JpegQualityDialog.queryJpegQuality(shell);

        // Export the image on the worker thread, and handle the result on the
        // main thread.
        currentImageTab.editorEnv.workerThread.asyncExportBitmap(
                this, session, new File(filename), quality,
                new ImageWorkerThread.FileExportTask() {
                    public void handleSuccess(File file) {
                        SwtUtil.messageDialog(shell, "Image Exported",
                                "The image was exported to:\n" + file,
                                SWT.ICON_INFORMATION);
                    }
                    public void handleError(File file, String errorMessage) {
                        SwtUtil.errorDialog(shell, "Image Export Error",
                                "An I/O error occurred: " + errorMessage);
                    }
                });
    }

    void doMenuAbout() {
        AboutDialog.show(shell, appImages);
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
            // New layers are always inactive before the editing starts
            layer.setActive(false);
            session.addLayer(layer);
            tab.editorEnv.layerWasJustAdded = true;
            showLayerEditor(layer, new LayerEditorCallback() {
                public void editingFinished(LayerEditorEnvironment env,
                        boolean cancelled) {
                    if (cancelled) {
                        env.session.removeLayer(env.layer);
                    }
                }
            });
        }
        finally {
            tab.editorEnv.layerWasJustAdded = false;
            session.endDisableAutoRecord();
        }
    }

    public static void main(String[] args) {
        ArgumentSocketServer server = new ArgumentSocketServer();
        try {
            ArgumentSocketServer.startServerOrQuit(server, args);
            final MainWindow window = new MainWindow();
            server.addListener(new ArgumentSocketServer.Listener() {
                public void handleArgument(String arg) {
                    window.asyncOpenFile(arg);
                }
            });
            for (String filename: args)
                window.openProjectOrImageFile(filename);
            window.mainLoop();
        }
        finally {
            server.close();
        }
    }
}