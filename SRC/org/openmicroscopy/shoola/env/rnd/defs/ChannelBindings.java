/*
 * org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd.defs;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Tells which pixel intensity interval of a specified wavelength is affected
 * by quantization. Also associates the wavelength to a color and tells
 * whether or not it has to be mapped.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ChannelBindings
{
	/** Color range. */
	private static final int	COLOR_MIN = 0, COLOR_MAX = 255;
	
	/** Index of the wavelength. */
	private int 				index;
	
	/** The lower bound of the pixel intensity interval. */
	private Comparable 			inputStart;
	
	/** The upper bound of the pixel intensity interval. */
	private Comparable 			inputEnd;
	
	/** Color associated to the wavelength. */
	private int[] 				rgba;
	
	/** 
	 *<code>true</code> if the wavelength has to mapped, 
	 *<code>false</code> otherwise.
	 */
	private boolean				active;
	
	public ChannelBindings(int index, 
							Comparable inputStart, Comparable inputEnd,
							int red, int green, int blue, int alpha,
						  	boolean active)
	{
		this.index = index;
		setInputWindow(inputStart, inputEnd);
		rgba = new int[4];
		setRGBA(red, green, blue, alpha);
		this.active = active;
	}

	/** Private empty constructor. */
	private ChannelBindings() {}
	
	public boolean isActive() { return active; }

	public int getIndex() { return index; }

	public Comparable getInputEnd() { return inputEnd; }

	public Comparable getInputStart() { return inputStart; }

	public int[] getRGBA() 
	{
		int[] colors = new int[rgba.length];
		for (int i = 0; i < rgba.length; i++)
			colors[i] = rgba[i];
		return colors;
	}

	public void setActive(boolean active) { this.active = active; }

	//TODO: checks done in QuantumStrategy, where do they belong to?
	public void setInputWindow(Comparable start, Comparable end)
	{
		inputStart = start;
		inputEnd = end;
	}

	public void setRGBA(int red, int green, int blue, int alpha)
	{
		verifyColorComponent(red);
		verifyColorComponent(green);
		verifyColorComponent(blue);
		verifyColorComponent(alpha);
		rgba[0] = red;
		rgba[1] = green;
		rgba[2] = blue;
		rgba[3] = alpha;
	}
	
	public void setRGBA(int[] rgba)
	{
		if (rgba == null || rgba.length != 4)
			throw new IllegalArgumentException("Invalid rgba array.");
		verifyColorComponent(rgba[0]);
		verifyColorComponent(rgba[1]);
		verifyColorComponent(rgba[2]);
		verifyColorComponent(rgba[3]);
		this.rgba[0] = rgba[0];
		this.rgba[1] = rgba[1];
		this.rgba[2] = rgba[2];
		this.rgba[3] = rgba[3];
	}
	
	
	/** Make a copy of the object. */
	ChannelBindings copy()
	{
		ChannelBindings cb = new ChannelBindings();
		cb.index = this.index;
		cb.active = this.active;
		//Will work b/c the objects are read-only: Integer, Float, etc.
		cb.inputStart = this.inputStart;
		cb.inputEnd = this.inputEnd;
		cb.rgba = getRGBA();
		return cb;
	}
	
	/** Verify the color components. */
	private void verifyColorComponent(int c)
	{
		if (c < COLOR_MIN || COLOR_MAX < c)
			throw new IllegalArgumentException("Value must be in [0,255].");
	}

}
