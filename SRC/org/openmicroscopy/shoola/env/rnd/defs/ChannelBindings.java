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
	private static final int	COLOR_MIN = 0;
	private static final int	COLOR_MAX = 255;
	
	/** Index of the wavelength. */
	private int 				index;
	
	/** The lower bound of the pixel intensity interval. */
	private int 				inputStart;
	
	/** The upper bound of the pixel intensity interval. */
	private int 				inputEnd;
	
	/** Color associated to the wavelength. */
	private int[] 				rgba;
	
	/** 
	*<code>true</code> if the wavelength has to mapped, 
	*<code>false</code> otherwise.
	*/
	private boolean				active;
	
	public ChannelBindings(int index, int inputStart, int inputEnd, int[] rgba,
						  boolean active)
	{
		this.index = index;
		this.inputStart = inputStart;
		this.inputEnd = inputEnd;
		this.active = active;
		setRGBA(rgba);
	}

	public boolean isActive()
	{
		return active;
	}

	public int getIndex()
	{
		return index;
	}

	public int getInputEnd()
	{
		return inputEnd;
	}

	public int getInputStart()
	{
		return inputStart;
	}

	public int[] getRGBA() 
	{
		return rgba;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	//TODO: checks done in QuantumStrategy.
	public void setInputWindow(int start, int end)
	{
		inputStart = start;
		inputEnd = end;
	}

	public void setRGBA(int[] rgba)
	{
		verifyColorInput(rgba);
		this.rgba = rgba;
	}
	
	/** Verify the color components. */
	private void verifyColorInput(int[] rgba)
	{
		if (rgba == null)
			throw new IllegalArgumentException("no color specified " +
												"for this channel");
		if (rgba.length != 4)
			throw new IllegalArgumentException("4 components must be " +
									"specified");
									
		for (int i = 0; i < rgba.length; i++) {
			if (rgba[i] < COLOR_MIN || rgba[i] > COLOR_MAX)
				throw new IllegalArgumentException("Value must be in the " +
												"range 0 255");
		}
	}
	
}
