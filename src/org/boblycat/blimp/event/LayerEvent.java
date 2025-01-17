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
package org.boblycat.blimp.event;

import java.util.EventObject;

import org.boblycat.blimp.layers.Layer;

public class LayerEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    // TODO: make sure layer is really serializable using normal Java serialization?
    private Layer layer;

    public LayerEvent(Layer layer) {
        super(layer);
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }
}