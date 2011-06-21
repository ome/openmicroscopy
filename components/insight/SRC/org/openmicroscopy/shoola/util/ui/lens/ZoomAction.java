/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is the action to zoom the lens, changing the magnification based on the
 * user input; index.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ZoomAction 
	extends AbstractAction
{
	
	/** Number of options in action. */
	final static int 		MAX = 11;

	/** Constant for zoom action to set the magnification of the lens to x1.*/
	final static int		ZOOMx1 = 0;
	
	/** Constant for zoom action to set the magnification of the lens to x2.*/
	final static int		ZOOMx2 = 1;
	
	/** Constant for zoom action to set the magnification of the lens to x3.*/
	final static int		ZOOMx3 = 2;
	
	/** Constant for zoom action to set the magnification of the lens to x4.*/
	final static int		ZOOMx4 = 3;
	
	/** Constant for zoom action to set the magnification of the lens to x5.*/
	final static int		ZOOMx5 = 4;
	
	/** Constant for zoom action to set the magnification of the lens to x6.*/
	final static int		ZOOMx6 = 5;
	
	/** Constant for zoom action to set the magnification of the lens to x7.*/
	final static int		ZOOMx7 = 6;
	
	/** Constant for zoom action to set the magnification of the lens to x8.*/
	final static int		ZOOMx8 = 7;
	
	/** Constant for zoom action to set the magnification of the lens to x9.*/
	final static int		ZOOMx9 = 8;
	
	/** Constant for zoom action to set the magnification of the lens to x10.*/
	final static int		ZOOMx10 = 9;
	
	/** Constant for zoom action to set the magnification of the lens to x10.*/
	final static int		MANUAL = 10;
	
	/** Parent component of the lens object. */
	private LensComponent	lens;
	
	/** Index used to determine which zoom action to perform on the lens. */
	private int				index;
	
	/** Names for each action associated with the change in magnification. */
	private static String[]	names;
	   
	static {
		names = new String[MAX];
		names[ZOOMx1] = "100%";
		names[ZOOMx2] = "200%";
		names[ZOOMx3] = "300%";
		names[ZOOMx4] = "400%";
		names[ZOOMx5] = "500%";
		names[ZOOMx6] = "600%";
		names[ZOOMx7] = "700%";
		names[ZOOMx8] = "800%";
		names[ZOOMx9] = "900%";
		names[ZOOMx10] = "1000%";
		names[MANUAL] = "Manual";
	}

	/** 
     * Controls if the specified index is valid.
     * 
     * @param i The index to check.
     */
	private void checkIndex(int i)
	{
		switch (i) {
			case ZOOMx1:
			case ZOOMx2:
			case ZOOMx3:
			case ZOOMx4:
			case ZOOMx5:
			case ZOOMx6:
			case ZOOMx7:
			case ZOOMx8:
			case ZOOMx9:
			case ZOOMx10:
			case MANUAL:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
   
	/** 
	 * Returns the index corresponding to the passed factor.
	 * 
	 * @param i The magnification factor.
	 * @return See above.
	 */
	static int factorToIndex(float i)
	{
		double d = Math.ceil(i);
		if ((i-d) != 0) return MANUAL;
		int v = (int) i;
		switch (v) {
			case ZOOMx1+1:
				return ZOOMx1;
			case ZOOMx2+1:
				return ZOOMx2;
			case ZOOMx3+1:
				return ZOOMx3;
			case ZOOMx4+1:
				return ZOOMx4;
			case ZOOMx5+1:
				return ZOOMx5;
			case ZOOMx6+1:
				return ZOOMx6;
			case ZOOMx7+1:
				return ZOOMx7;
			case ZOOMx8+1:
				return ZOOMx8;
			case ZOOMx9+1:
				return ZOOMx9;
			case ZOOMx10+1:
				return ZOOMx10;
			default:
				return MANUAL;
		}
	}
	
	/**
	 * Zoom action changes the magnification of the lens based on the parameter 
	 * zoomIndex. 
	 * 
	 * @param lens      The parent component.
	 * @param zoomIndex The index of the action.
	 */
	ZoomAction(LensComponent lens, int zoomIndex)
	{
		this.lens = lens;
		checkIndex(zoomIndex);
		index = zoomIndex;
	    putValue(Action.NAME, names[index]);
	}
	
	/**
	 * Returns the index of the action.
	 * 
	 * @return See above.
	 */
	int getIndex() { return index; }
	
	/**
     * Sets the magnification factor.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{ 
		if (index != MANUAL) lens.setZoomFactor(index+1); 
	}

}
