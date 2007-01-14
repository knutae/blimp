package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.*;
import org.boblycat.blimp.layers.AdjustmentLayer;
import org.boblycat.blimp.layers.GammaLayer;
import org.boblycat.blimp.layers.InputLayer;
import org.boblycat.blimp.layers.Layer;
import org.boblycat.blimp.layers.RawFileInputLayer;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
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
}

public class MainWindow {
    Display display;
    Shell shell;
    Label statusLabel;
    Listener menuHideListener;
    Listener menuItemListener;
    MenuItem menuFileOpen;
    MenuItem menuFileExit;
    MenuItem menuFileSaveSession;
    MenuItem menuFileExportImage;
    MenuItem menuHelpAbout;
    CTabFolder mainTabFolder;
    CTabFolder rightTabFolder;
    LayersView layers;
    Vector<ImageTab> imageTabs;
    ImageTab currentImageTab;
    LayerRegistry layerRegistry;

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
            if (event.widget == menuFileOpen) {
                doMenuOpen();
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
        imageTabs = new Vector<ImageTab>();
        display = new Display();
        shell = new Shell(display);
        shell.setText("Blimp");
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);

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
        menuFileOpen = addMenuItem(fileMenu, "&Open",
                "Open an image or a project");
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
        rightTabFolder = new CTabFolder(sashForm, SWT.TOP | SWT.BORDER);
        int[] weights = { 4, 1 };
        sashForm.setWeights(weights);
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                for (int i = 0; i < imageTabs.size(); i++)
                    if (imageTabs.get(i).item == e.item) {
                        currentImageTab = imageTabs.get(i);
                        layers.updateWithSession(currentImageTab.imageView
                                .getSession(), null, null);
                        return;
                    }
            }
        });

        // Layers view
        layers = new LayersView(rightTabFolder);
        layers.setLayout(new FillLayout());
        CTabItem tmpItem = new CTabItem(rightTabFolder, SWT.NONE);
        tmpItem.setText("Layers");
        tmpItem.setControl(layers);
        rightTabFolder.setSelection(0);

        shell.open();
    }

    public void mainLoop() {
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    ImageView addImageViewWithSession(BlimpSession session) {
        ImageView imageView = new ImageView(mainTabFolder, SWT.NONE, session);
        CTabItem item = new CTabItem(mainTabFolder, SWT.CLOSE);
        item.setText(imageView.getSession().getDescription());
        item.setControl(imageView);
        mainTabFolder.setSelection(item);
        currentImageTab = new ImageTab(item, imageView);
        imageTabs.add(currentImageTab);
        return imageView;
    }

    ImageView addImageView(String imageFilename) {
        InputLayer input = Util.getInputLayerFromFile(imageFilename);
        if (input instanceof RawFileInputLayer)
            input.setActive(false); // TODO: quick hack for raw input, improve
        BlimpSession session = new BlimpSession();
        session.setInput(input);
        return addImageViewWithSession(session);
    }

    private void fileOpenError(String filename, String errorType, Exception e) {
        SwtUtil.errorDialog(shell, errorType + " error", errorType
                + " error while opening file: " + filename + "\n"
                + e.getMessage());
    }

    void doMenuOpen() {
        // status("File->Open");
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterNames(new String[] {
                "Images (jpeg, tiff, png, gif, bmp, raw, dng, crw, cr2)",
                "Blimp projects (blimp)", "All Files" });
        // the following is MS windows-specific
        dialog
                .setFilterExtensions(new String[] {
                        "*.jpg;*.jpeg;*.tiff;*.tif;*.png;*.gif;*.bmp;*.raw;*.dng;*.crw;*.cr2",
                        "*.blimp", "*.*" });
        String filename = dialog.open();
        if (filename == null)
            return;

        if (filename.toLowerCase().endsWith(".blimp")) {
            // open a saved session
            try {
                BlimpSession session = (BlimpSession) Serializer
                        .loadBeanFromFile(filename);
                addImageViewWithSession(session);
                layers.updateWithSession(session, null, null);
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
        BlimpSession session = imageView.getSession();
        layers.updateWithSession(session, session.getInput(),
                new LayerEditorCallback() {
                    public void editingFinished(Layer layer, boolean cancelled) {
                        ImageTab tab = currentImageTab;
                        if (cancelled) {
                            tab.item.dispose();
                        }
                        else if (layer instanceof RawFileInputLayer) {
                            RawFileInputLayer rawInput = (RawFileInputLayer) layer;
                            if (rawInput.getColorDepth() == ColorDepth.Depth16Bit) {
                                // Automatically add a gamma layer for 16-bit
                                // raw input,
                                // because dcraw 16-bit output is not gamma
                                // corrected
                                // (linear color mapping).
                                GammaLayer gamma = new GammaLayer();
                                gamma.setGamma(2.2);
                                tab.imageView.getSession().addLayer(gamma);
                                layers.refresh();
                            }
                        }
                    }
                });
        // layers.updateWithSession(session, null);
        imageView.invalidateImage();
    }

    void doMenuExit() {
        shell.close();
    }

    void doMenuSaveSession() {
        if (currentImageTab == null)
            return;
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterNames(new String[] { "Blimp projects (*.blimp)" });
        dialog.setFilterExtensions(new String[] { "*.blimp" });
        String filename = dialog.open();
        if (filename == null)
            return;
        BlimpSession session = currentImageTab.imageView.getSession();
        try {
            Serializer.saveBeanToFile(session, filename);
        }
        catch (IOException e) {
            System.err.println("An I/O error occured: " + e.getMessage());
        }
    }

    void doMenuExportImage() {
        if (currentImageTab == null)
            return;
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog
                .setFilterNames(new String[] { "Exportable image formats (jpeg, bmp)" });
        dialog.setFilterExtensions(new String[] { "*.jpeg;*.jpg;*.bmp" });
        String filename = dialog.open();
        if (filename == null)
            return;
        String ext = Util.getFileExtension(filename);
        if (!BitmapUtil.canSaveToFormat(ext)) {
            SwtUtil.errorDialog(shell, "Image export",
                    "Unsupported file type: " + ext);
            return;
        }
        BlimpSession session = currentImageTab.imageView.getSession();
        try {
            BitmapUtil.writeBitmap(session.getFullBitmap(), ext, filename, 0.9);
        }
        catch (IOException e) {
            SwtUtil.errorDialog(shell, "Image Export", "An I/O error occured: "
                    + e.getMessage());
        }
    }

    void doMenuAbout() {
        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        dialog.setText("About Blimp");
        GridLayout layout = new GridLayout();
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        layout.verticalSpacing = 20;
        dialog.setLayout(layout);
        Link linkText = new Link(dialog, SWT.NONE);
        linkText
                .setText("Blimp, a layered photo editor.\n"
                        + "Copyright 2006-2007 Knut Arild Erstad\n"
                        + "\n"
                        + "Credits:\n"
                        + "<a href=\"http://schmidt.devlib.org/jiu/\">Java Imaging Utilities</a> by Marco Schmidt and others\n"
                        + "<a href=\"http://cybercom.net/~dcoffin/dcraw/\">dcraw</a> (Raw input) by Dave Coffin");
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
        dialog.pack();
        dialog.open();
    }

    void status(String msg) {
        statusLabel.setText(msg);
    }

    void addLayer(AdjustmentLayer layer) {
        // ImageTab tab = getCurrentTab();
        ImageTab tab = currentImageTab;
        if (tab == null)
            return;
        tab.imageView.getSession().addLayer(layer);
        tab.imageView.invalidateImage();
        layers.updateWithSession(tab.imageView.getSession(), layer,
                new LayerEditorCallback() {
                    public void editingFinished(Layer layer, boolean cancelled) {
                        if (cancelled) {
                            BlimpSession session = currentImageTab.imageView
                                    .getSession();
                            session.removeLayer(layer);
                            layers.refresh();
                        }

                    }
                });
    }

    public static void main(String[] args) {
        MainWindow window = new MainWindow();
        window.mainLoop();
    }
}