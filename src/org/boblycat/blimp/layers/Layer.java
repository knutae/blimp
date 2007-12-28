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
package org.boblycat.blimp.layers;

import org.boblycat.blimp.BlimpBean;
import org.boblycat.blimp.LayerChangeListener;
import org.boblycat.blimp.LayerEvent;
import org.boblycat.blimp.LayerEventSource;
import org.boblycat.blimp.ProgressEvent;
import org.boblycat.blimp.ProgressEventSource;
import org.boblycat.blimp.ProgressListener;

/**
 * Abstract base class for all layers in Blimp, which includes adjustment layers
 * (for image modification) and input layers. In Blimp, all image operations are
 * based upon adjustment layers which must extend the AdjustmentLayer subclass.
 */
public abstract class Layer extends BlimpBean {
    boolean active;

    LayerEventSource eventSource;

    ProgressEventSource progressEventSource;

    String name;

    public abstract String getDescription();

    public String getProgressDescription() {
        return getDescription();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Layer() {
        active = true;
        eventSource = new LayerEventSource();
        name = null;
        progressEventSource = new ProgressEventSource();
    }

    public void addChangeListener(LayerChangeListener listener) {
        eventSource.addListener(listener);
    }

    public void removeChangeListener(LayerChangeListener listener) {
        eventSource.removeListener(listener);
    }

    public void triggerChangeEvent() {
        eventSource.triggerChangeWithEvent(new LayerEvent(this));
    }

    public void addProgressListener(ProgressListener listener) {
        progressEventSource.addListener(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressEventSource.removeListener(listener);
    }

    protected void triggerProgress(String message, double progress) {
        progressEventSource.triggerChangeWithEvent(
                new ProgressEvent(this, message, progress));
    }

    protected void triggerProgress(double progress) {
        triggerProgress(getProgressDescription(), progress);
    }

    public void invalidate() {
        triggerChangeEvent();
    }

    /**
     * Overridden to return "layer" for serialization. Overriding this function
     * any further can break serialization, so be careful. (Some classes like
     * BlimpSession needs to override it, so it is not final.)
     */
    public String elementName() {
        return "layer";
    }

    /**
     * Get a name which can be used as an identifier for the layer.
     * Within a session all names should be unique.
     * @return A name.
     */
    public String getName() {
        if (name == null || name.length() == 0)
            name = generateName(this, 1);
        return name;
    }

    public void setName(String name) {
        this.name = name;
        // TODO: make sure it is unique within a session...?
    }

    protected String getBaseName() {
        String className = getClass().getSimpleName();
        if (className.endsWith("Layer"))
            className = className.substring(0, className.length()-5);
        return className;
    }

    protected static String generateName(Layer layer, int suffixNumber) {
        return layer.getBaseName() + Integer.toString(suffixNumber);
    }
}
