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
    ValueSlider blackPointSlider;
    ValueSlider whitePointSlider;
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
        blackPointSlider = createSlider("Black Level", 0, 100);
        whitePointSlider = createSlider("White Level", 0, 100);
        centerSlider = createSlider("Center", -90, 90);
    }
    
    ValueSlider createSlider(String caption, int min, int max) {
        ValueSlider slider = new ValueSlider(this, SWT.NONE,
                caption, min, max, 2);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        slider.setLayoutData(data);
        slider.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                updateLayer();
            }
        });
        return slider;
    }
    
    void updateLayer() {
        if (levels == null)
            return;
        levels.setBlackLevel(blackPointSlider.getSelectionAsDouble());
        levels.setWhiteLevel(whitePointSlider.getSelectionAsDouble());
        levels.setCenter(centerSlider.getSelectionAsDouble());
        levels.invalidate();
        histogramView.setLevels(levels.getBlackLevel(), levels.getWhiteLevel());
    }
    
    void updateGui() {
        if (levels == null)
            return;
        blackPointSlider.setSelection((int) (levels.getBlackLevel() * 100.0));
        whitePointSlider.setSelection((int) (levels.getWhiteLevel() * 100.0));
        centerSlider.setSelection((int) (levels.getCenter() * 100.0));
        histogramView.setLevels(levels.getBlackLevel(), levels.getWhiteLevel());
    }

    @Override
    protected void layerChanged() {
        levels = (LevelsLayer) layer;
        if (levels == null)
            return;
        workerThread.asyncGenerateHistogram(session, levels.getName(),
                new HistogramGeneratedTask() {
            public void handleHistogram(Histogram h) {
                if (!histogramView.isDisposed())
                    histogramView.setAllchannelsHistogram(h);
            }
        });
        updateGui();
    }

}
