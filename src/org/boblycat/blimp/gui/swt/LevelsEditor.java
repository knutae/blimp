package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Histogram;
import org.boblycat.blimp.HistogramGeneratedTask;
import org.boblycat.blimp.layers.LevelsLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class LevelsEditor extends LayerEditor {
    LevelsLayer levels;
    HistogramView histogramView;
    ValueSlider blackSlider;
    ValueSlider whiteSlider;
    ValueSlider centerSlider;
    
    public LevelsEditor(Composite parent, int style) {
        super(parent, style);
        GridLayout grid = new GridLayout();
        grid.numColumns = 1;
        setLayout(grid);
        histogramView = new HistogramView(this, SWT.NONE);
        GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true);
        gdata.widthHint = 300;
        gdata.heightHint = 120;
        histogramView.setLayoutData(gdata);
        blackSlider = createSlider("Black Level");
        whiteSlider = createSlider("White Level");
        centerSlider = createSlider("Center");
        
        Listener blackAndWhiteListener = new Listener() {
            public void handleEvent(Event e) {
                if (levels == null)
                    return;
                // Update the center when changing black and white levels.
                // this is done by the GUI, not the model.
                double oldBlack = levels.getBlackLevel();
                double oldWhite = levels.getWhiteLevel();
                double oldCenter = levels.getCenter();
                double centerRatio = (oldCenter - oldBlack)
                    / (oldWhite - oldBlack);
                double newBlack = blackSlider.getSelectionAsDouble();
                double newWhite = whiteSlider.getSelectionAsDouble();
                double newCenter = newBlack
                    + (newWhite - newBlack) * centerRatio;
                updateLevels(newBlack, newCenter, newWhite);
                centerSlider.setSelectionAsDouble(newCenter);
            }
        };
        blackSlider.addListener(SWT.Selection, blackAndWhiteListener);
        whiteSlider.addListener(SWT.Selection, blackAndWhiteListener);
        centerSlider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (centerSlider.getSelection() <= blackSlider.getSelection() ||
                        centerSlider.getSelection() >= whiteSlider.getSelection()) {
                    e.doit = false;
                    return;
                }
                updateLevels(blackSlider.getSelectionAsDouble(),
                        centerSlider.getSelectionAsDouble(),
                        whiteSlider.getSelectionAsDouble());
            }
        });
    }
    
    ValueSlider createSlider(String caption) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE,
                caption, 0, 1000, 3);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        slider.setLayoutData(data);
        return slider;
    }
    
    private void updateLevels(double black, double center, double white) {
        if (levels == null)
            return;
        levels.setBlackLevel(black);
        levels.setCenter(center);
        levels.setWhiteLevel(white);
        levels.invalidate();
        histogramView.setLevels(black, center, white);
    }
    
    private void updateGui() {
        if (levels == null)
            return;
        blackSlider.setSelectionAsDouble(levels.getBlackLevel());
        whiteSlider.setSelectionAsDouble(levels.getWhiteLevel());
        centerSlider.setSelectionAsDouble(levels.getCenter());
        histogramView.setLevels(levels.getBlackLevel(), levels.getCenter(),
                levels.getWhiteLevel());
    }

    @Override
    protected void layerChanged() {
        levels = (LevelsLayer) layer;
        if (levels == null)
            return;
        workerThread.asyncGenerateHistogram(this, session, levels.getName(),
                new HistogramGeneratedTask() {
            public void handleHistogram(Histogram h) {
                if (!histogramView.isDisposed())
                    histogramView.setAllchannelsHistogram(h);
            }
        });
        updateGui();
    }

}
