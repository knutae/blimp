/*
 * Contrast
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.adjustment;

import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Adjusts the contrast of an image.
 * The amount of adjustment is given to the constructor as a percentage value between -100 and 100.
 * -100 will make the resulting image middle-gray, 0 will leave it unchanged, 100 will map it to 
 * the eight corners of the color cube.
 * <h3>Usage example</h3>
 * This code snippet will reduce <code>image</code>'s contrast by 40 percent.
 * <pre>
 * Contrast contrast = new Contrast();
 * contrast.setInputImage(image);
 * contrast.setContrast(-40);
 * contrast.process();
 * PixelImage adjustedImage = contrast.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class Contrast extends LookupTableOperation
{
	private int contrast;

	private int[] createLookupTable(int numSamples, int contrast)
	{
		int[] result = new int[numSamples];
		final int MAX = numSamples - 1;
		final float MID = MAX / 2.0f;
		for (int i = 0; i < numSamples; i++)
		{
			if (contrast < 0)
			{
				if (i < MID)
				{
					result[i] = (int)(i + (MID - i) * (- contrast) / 100.0f);
				}
				else
				{
					result[i] = (int)(MID + (i - MID) * (100.0f + contrast) / 100.0f);
				}
			}
			else
			{
				if (i < MID)
				{
					result[i] = (int)(i * (100.0f - contrast) / 100.0f);
				}
				else
				{
					result[i] = (int)(i + (MAX - i) * contrast / 100.0f);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the contrast adjustment value associated with this opperation.
	 * The value lies between -100 and 100 (including both values).
	 * @return contrast adjustment
	 * @see #setContrast
	 */
	public int getContrast()
	{
		return contrast;
	}

	private void process(Paletted8Image in, Paletted8Image out)
	{
		if (out == null)
		{
			out = (Paletted8Image)in.createCompatibleImage(in.getWidth(), in.getHeight());
		}
		Palette palette = out.getPalette();
		int numSamples = palette.getMaxValue() + 1;
		final int[] LUT = createLookupTable(numSamples, contrast);
		for (int c = 0; c < 3; c++)
		{
			for (int i = 0; i < palette.getNumEntries(); i++)
			{
				palette.putSample(c, i, LUT[palette.getSample(c, i)]);
			}
		}
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y));
			}
			setProgress(y, in.getHeight());
		}
		setOutputImage(out);
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		prepareImages();
		IntegerImage in = (IntegerImage)getInputImage();
		if (in instanceof GrayIntegerImage || in instanceof RGBIntegerImage)
		{
			setNumTables(in.getNumChannels());
			for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
			{
				setTable(channelIndex, createLookupTable(in.getMaxSample(channelIndex) + 1, getContrast()));
			}
			super.process();
		}
		else
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)in, (Paletted8Image)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Contrast operation cannot operate on input image type: " + in.getClass());
		}
	}

	/**
	 * Sets the value for contrast adjustment to be used within this operation.
	 * @param newContrast new contrast, between -100 and 100 (including both values)
	 * @throws IllegalArgumentException if the new contrast value is not in the above mentioned interval
	 * @see #getContrast
	 */
	public void setContrast(int newContrast)
	{
		if (newContrast < -100)
		{
			throw new IllegalArgumentException("Contrast must be at least -100: " + newContrast);
		}
		if (newContrast > 100)
		{
			throw new IllegalArgumentException("Contrast must be at most 100: " + newContrast);
		}
		contrast = newContrast;
	}
}
