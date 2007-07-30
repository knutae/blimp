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
package org.boblycat.blimp;

/**
 * Task used when asynchronously querying the size of a bitmap from the
 * image worker thread.
 * 
 * @see ImageWorkerThread
 * 
 * @author Knut Arild Erstad
 */
public abstract class BitmapSizeGeneratedTask implements Runnable {
    private BitmapSize size;
    
    public void setSize(BitmapSize size) {
        this.size = size;
    }
    
    public void run() {
        handleSize(size);
    }

    protected abstract void handleSize(BitmapSize size);
}
