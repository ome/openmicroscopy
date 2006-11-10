/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomAction 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
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
public class ZoomAction 
	extends AbstractAction
{
	
	/** Constant for zoom action to reset the magnification of the lens to x2.*/
	final static int		ZOOMDEFAULT = 0;
	
	/** Constant for zoom action to set the magnification of the lens to x1.*/
	final static int		ZOOMx1 = 1;
	
	/** Constant for zoom action to set the magnification of the lens to x2.*/
	final static int		ZOOMx2 = 2;
	
	/** Constant for zoom action to set the magnification of the lens to x3.*/
	final static int		ZOOMx3 = 3;
	
	/** Constant for zoom action to set the magnification of the lens to x4.*/
	final static int		ZOOMx4 = 4;
	
	/** Constant for zoom action to set the magnification of the lens to x5.*/
	final static int		ZOOMx5 = 5;
	
	/** Constant for zoom action to set the magnification of the lens to x6.*/
	final static int		ZOOMx6 = 6;
	
	/** Constant for zoom action to set the magnification of the lens to x7.*/
	final static int		ZOOMx7 = 7;
	
	/** Constant for zoom action to set the magnification of the lens to x8.*/
	final static int		ZOOMx8 = 8;
	
	/** Constant for zoom action to set the magnification of the lens to x9.*/
	final static int		ZOOMx9 = 9;
	
	/** Constant for zoom action to set the magnification of the lens to x10.*/
	final static int		ZOOMx10 = 10;
	
	/** Parent component of the lens object. */
	private LensComponent	lens;
	
	/** Index used to determine which zoom action to perform on the lens. */
	private int				index;
	
	/** Names for each action associated with the change in magnification. */
	private static String[]     names;
	   
	static {
	        names = new String[11];
	        names[ZOOMDEFAULT] = "Set Zoom to Default magnification";
	        names[ZOOMx1] = "Set Magnification x1";
	        names[ZOOMx2] = "Set Magnification x2";
	        names[ZOOMx3] = "Set Magnification x3";
	        names[ZOOMx4] = "Set Magnification x4";
	        names[ZOOMx5] = "Set Magnification x5";
	        names[ZOOMx6] = "Set Magnification x6";
	        names[ZOOMx7] = "Set Magnification x7";
	        names[ZOOMx8] = "Set Magnification x8";
	        names[ZOOMx9] = "Set Magnification x9";
	        names[ZOOMx10] = "Set Magnification x10";
	}

	/**
	 * Zoom action changes the magnification of the lens based on the parameter 
	 * zoomIndex. 
	 * 
	 * @param lens parent component.
	 * @param zoomIndex Action.
	 */
	ZoomAction(LensComponent lens, int zoomIndex)
	{
		this.lens = lens;
		checkIndex(zoomIndex);
		index = zoomIndex;
	    putValue(Action.NAME, names[index]);
	}
	
	/** (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if( index == 0 )
			lens.setZoomFactor(LensComponent.DEFAULT_ZOOM);
		else
			lens.setZoomFactor(index);
	}
	
	 /** 
     * Controls if the specified index is valid.
     * 
     * @param i The index to check.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case ZOOMDEFAULT:
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
            	return;
            default:
                throw new IllegalArgumentException("Index not supported.");
        }
    }
    
}
