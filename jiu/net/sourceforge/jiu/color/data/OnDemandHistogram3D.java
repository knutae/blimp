/*
 * OnDemandHistogram3D
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

import net.sourceforge.jiu.color.data.Histogram3D;

/**
 * A data class for a three-dimensional histogram, creating counters on demand only,
 * not allocating counters for all possible entries at the beginning.
 * The creation on demand happens to save space.
 * A naive implementation can become huge - for eight bits per component,
 * you'd need 2<sup>(8 + 8 + 8)</sup> = 2<sup>24</sup> = 16,777,216 
 * int values (64 MB), while a typical 24 bit image uses only a fraction of
 * these possible colors.
 *
 * @author Marco Schmidt
 */
public class OnDemandHistogram3D implements Histogram3D
{
	private int c1;
	private int c2;
	private int c3;
	private int[] compOrder;
	private int maxValue1;
	private int maxValue2;
	private int maxValue3;
	private int[] maxValue;
	private int[] vector;
	private Object[] top;

	/**
	 * Creates a new object of this class with specified maximum values
	 * for all three indexes.
	 * @param maxValue1 the maximum value for the first index
	 * @param maxValue2 the maximum value for the second index
	 * @param maxValue3 the maximum value for the third index
	 * @param c1 the top level component for the internal tree
	 * @param c2 the second-level component for the internal tree
	 * @param c3 the third-level component for the internal tree
	 */
	public OnDemandHistogram3D(
		int maxValue1, int maxValue2, int maxValue3,
		int c1, int c2, int c3) throws IllegalArgumentException
	{
		if (maxValue1 < 1 || maxValue2 < 1 || maxValue3 < 1)
		{
			throw new IllegalArgumentException("The three maximum value arguments must all be larger than zero.");
		}
		this.maxValue1 = maxValue1;
		this.maxValue2 = maxValue2;
		this.maxValue3 = maxValue3;
		maxValue = new int[3];
		maxValue[0] = maxValue1;
		maxValue[1] = maxValue2;
		maxValue[2] = maxValue3;
		if (c1 < 0 || c1 > 2 || c2 < 0 || c2 > 2 || c3 < 0 || c3 > 2)
		{
			throw new IllegalArgumentException("Arguments for components must be from 0..2.");
		}
		if (c1 == c2 || c1 == c3 || c2 == c3)
		{
			throw new IllegalArgumentException("No two arguments for components are allowed to be equal.");
		}
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		compOrder = new int[3];
		compOrder[0] = c1;
		compOrder[1] = c2;
		compOrder[2] = c3;
		vector = new int[3];
		clear();
	}

	/**
	 * Creates a new object of this class with maximum values as specified
	 * by the arguments and the component values 0, 1, 2.
	 * Simply calls <code>this(maxValue1, maxValue2, maxValue3, 0, 1, 2);</code>
	 * @param maxValue1 the maximum value for the first index
	 * @param maxValue2 the maximum value for the second index
	 * @param maxValue3 the maximum value for the third index
	 */
	public OnDemandHistogram3D(int maxValue1, int maxValue2, int maxValue3)
	{
		this(maxValue1, maxValue2, maxValue3, 0, 1, 2);
	}

	/**
	 * Creates a new object of this class with the argument as maximum value for
	 * all three index positions and the component values 0, 1, 2.
	 * Simply calls <code>this(maxValue, maxValue, maxValue);</code>
	 * @param maxValue the maximum value for all indexes
	 */
	public OnDemandHistogram3D(int maxValue)
	{
		this(maxValue, maxValue, maxValue);
	}

	/**
	 * Resets all counters to zero.
	 * As the Java VM's garbage collector is responsible for releasing unused
	 * memory, it is unclear when memory allocated for the histogram will be
	 * available again.
	 */
	public void clear()
	{
		top = createObjectArray(maxValue[c1] + 1);
	}

	/**
	 * Creates an array of int values, initializes all values to 0 and returns
	 * the newly-allocated array.
	 * @param LENGTH the number of entries in the array to be allocated
	 * @return the resulting array
	 */
	private int[] createIntArray(final int LENGTH)
	{
		int[] result = new int[LENGTH];
		for (int i = 0; i < LENGTH; i++)
		{
			result[i] = 0;
		}
		return result;
	}

	/**
	 * Creates an array of objects, initializes all values to null and
	 * returns this new array.
	 * @param LENGTH the number of entries of the new int array to be returned
	 */
	private Object[] createObjectArray(final int LENGTH)
	{
		Object[] result = new Object[LENGTH];
		for (int i = 0; i < LENGTH; i++)
		{
			result[i] = null;
		}
		return result;
	}

	/**
	 * Returns counter for the argument color given by its red, green and
	 * blue intensity.
	 * @param index1 the first value of the index
	 * @param index2 
	 * @param index3 
	 * @throws IllegalArgumentException this exception is thrown if the 
	 *  argument color is not in the valid interval, i.e., if at least one
	 *  of the components is not from 0 .. max
	 */
	public int getEntry(int index1, int index2, int index3) throws IllegalArgumentException
	{
		if (index1 < 0 && index1 > maxValue1 &&
		    index2 < 0 && index2 > maxValue2 &&
		    index3 < 0 && index3 > maxValue3)
		{
			throw new IllegalArgumentException("Invalid value " + index1 + " " + index2 + " " + index3);
		}
		vector[0] = index1;
		vector[1] = index2;
		vector[2] = index3;
		Object[] second = (Object[])top[vector[compOrder[0]]];
		if (second == null)
		{
			return 0;
		}
		int[] counters = (int[])second[vector[compOrder[1]]];
		if (counters == null)
		{
			return 0;
		}
		return counters[vector[compOrder[2]]];
	}

	public int getMaxValue(int index) throws IllegalArgumentException
	{
		if (index >= 0 && index <= 2)
		{
			return maxValue[index];
		}
		else
		{
			throw new IllegalArgumentException("The index argument must be " +
				"from 0 to 2; got " + index);
		}
	}

	/**
	 * Returns the number of entries in this histogram with a counter value of one
	 * or higher (in other words: the number of colors that are in use).
	 * @return the number of unique colors
	 */
	public int getNumUsedEntries()
	{
		int result = 0;
		for (int i1 = 0; i1 <= maxValue[c1]; i1++)
		{
			if (top[i1] != null)
			{
				Object[] second = (Object[])top[i1];
				if (second != null)
				{
					for (int i2 = 0; i2 <= maxValue[c2]; i2++)
					{
						if (second[i2] != null)
						{
							int[] third = (int[])second[i2];
							for (int i3 = 0; i3 <= maxValue[c3]; i3++)
							{
								if (third[i3] != 0)
								{
									result++;
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Increases the counter for the color given by the arguments red, green and blue.
	 * This method could be implemented by the following code snippet:
	 * <pre>
	 *  setEntry(red, green, blue, getEntry(red, green, blue) + 1);
	 * </pre>However, this is not done to avoid slow-downs by accessing a counter value twice.
	 *
	 * @param red the red intensity of the color entry whose counter will be increased
	 * @param green the green intensity of the color entry whose counter will be increased
	 * @param blue the blue intensity of the color entry whose counter will be increased
	 * @throws IllegalArgumentException if the argument color is not valid 
	 * (minimum is 0, maximum is defined for each component independently)
	 */
	public void increaseEntry(int red, int green, int blue) throws IllegalArgumentException
	{
		if (red < 0 || red > maxValue1 ||
		    green < 0 || green > maxValue2 ||
		    blue < 0 || blue > maxValue3)
		{
			throw new IllegalArgumentException("Invalid color value in increaseEntry(): " +
				red + " " + green + " " + blue);
		}
		vector[0] = red;
		vector[1] = green;
		vector[2] = blue;
		int index1 = vector[compOrder[0]];
		Object[] second = (Object[])(top[index1]);
		if (second == null)
		{
			int num = maxValue[compOrder[1]] + 1;
			top[index1] = createObjectArray(num);
			second = (Object[])(top[index1]);
		}
		int index2 = vector[compOrder[1]];
		int[] counters = (int[])(second[index2]);
		if (counters == null)
		{
			int num = maxValue[compOrder[2]] + 1;
			second[index2] = createIntArray(num);
			counters = (int[])(second[index2]);
		}
		int index3 = vector[compOrder[2]];
		counters[index3]++;
	}

	/**
	 * Sets one counter to a new value.
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param newValue the new value for the counter
	 * @throws IllegalArgumentException if the components do not form a valid color 
	 */
	public void setEntry(int r, int g, int b, int newValue) throws IllegalArgumentException
	{
		if (r < 0 || r > maxValue1 ||
		    g < 0 || g > maxValue2 ||
		    b < 0 || b > maxValue3)
		{
			throw new IllegalArgumentException("Invalid index triplet: " +
				r + " " + g + " " + b);
		}
		vector[0] = r;
		vector[1] = g;
		vector[2] = b;
		int index1 = vector[compOrder[0]];
		Object[] second = (Object[])(top[index1]);
		if (second == null)
		{
			int num = maxValue[compOrder[1]] + 1;
			top[index1] = createObjectArray(num);
			second = (Object[])(top[index1]);
		}
		int index2 = vector[compOrder[1]];
		int[] counters = (int[])(second[index2]);
		if (counters == null)
		{
			int num = maxValue[compOrder[2]] + 1;
			second[index2] = createIntArray(num);
			counters = (int[])(second[index2]);
		}
		int index3 = vector[compOrder[2]];
		counters[index3] = newValue;
	}
}
