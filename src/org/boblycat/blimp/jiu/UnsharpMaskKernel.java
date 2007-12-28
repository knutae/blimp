package org.boblycat.blimp.jiu;

import net.sourceforge.jiu.filters.ConvolutionKernelData;
import net.sourceforge.jiu.filters.ConvolutionKernelFilter;

/**
 * This is a modified version of UnsharpMaskKernel which doesn't crash in the
 * constructor.
 * The changes were hacked by Knut Arild Erstad for the Blimp image editor.
 */

/*
 * UnsharpMaskKernel
 *
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

/**
 * An unsharp mask kernel to be used with {@link ConvolutionKernelFilter}.
 *
 * @author Marco Schmidt
 * @author Niels Donvil
 * @since 0.10.0
 */
public class UnsharpMaskKernel extends ConvolutionKernelData
{
    /**
     * Creates a new unsharp mask kernel.
     * @param level adjusts the amount of 'unsharpness', must be from 1 to 50
     */
    public UnsharpMaskKernel(int level)
    {
        super("Unsharp mask", new int[] {1}, 1, 1, 1, 0);
        if (level < 1 || level > 50)
        {
            throw new IllegalArgumentException("The level argument must be >= 1 and <= 50.");
        }
        level = ((51 - level) * 4 ) + 20;
        setDiv(level);
        int[] data =
        {
             0,   0,          -1,   0,  0,
             0,  -8,         -21,  -8,  0,
            -1, -21, level + 120, -21, -1,
             0,  -8,         -21,  -8,  0,
             0,   0,          -1,   0,  0
        };
        setWidth(5);
        setHeight(5);
        setData(data);
        check();
    }
}
