/*
 * org.openmicroscopy.shoola.util.ui.lens.LensAction 
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
 * Lens Action is called to change the size of the lens based on the index, this
 * is called from the popupmenu in the lensUI and menubar in the zoomWindowUI. 
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
class LensAction 
		extends AbstractAction
{

	/** Number of options in action. */
	final static int MAX = 9;
	
	/** Default size of lens. (20x20). */
    final static int		LENSDEFAULTSIZE = 0;
	
	/** Set lens to 40x40. */
	final static int		LENS40x40 = 1;
	
	/** Set lens to 50x50. */
	final static int		LENS50x50 = 2;
	
	/** Set lens to 60x60. */
	final static int		LENS60x60 = 3;
	
	/** Set lens to 70x70. */
	final static int		LENS70x70 = 4;
	
	/** Set lens to 80x80. */
	final static int		LENS80x80 = 5;
	
	/** Set lens to 90x90. */
	final static int		LENS90x90 = 6;
	
	/** Set lens to 100x100. */
	final static int		LENS100x100 = 7;
	
	/** Set lens to 120x120. */
	final static int		LENS120x120 = 8;
	
	/** Set lens to 150x150. */
	final static int		LENS150x150 = 9;
	
	/** the parent component of the magnifying lens. */
	private LensComponent	lens;
	
	/** The index which refers to the change in the lens size.*/
	private int				index;
	
	/** Names for each action associated with the change in lens size. */
	private static String[]     names;
	   
	static {
	        names = new String[MAX+1];
	        names[LENSDEFAULTSIZE] = "Set Lens to Default size";
	        names[LENS40x40] = "Set lens to 40x40";
	        names[LENS50x50] = "Set lens to 50x50";
	        names[LENS60x60] = "Set lens to 60x60";
	        names[LENS70x70] = "Set lens to 70x70";
	        names[LENS80x80] = "Set lens to 80x80";
	        names[LENS90x90] = "Set lens to 90x90";
	        names[LENS100x100] = "Set lens to 100x100";
	        names[LENS120x120] = "Set lens to 120x120";
	        names[LENS150x150] = "Set lens to 150x150";
	}

    /** 
     * Controls if the specified index is valid.
     * 
     * @param i The index to check.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case LENSDEFAULTSIZE:
            case LENS40x40:
            case LENS50x50:
            case LENS60x60:
            case LENS70x70:
            case LENS80x80:
            case LENS90x90:
            case LENS100x100:
            case LENS120x120:
            case LENS150x150:
                return;
            default:
                throw new IllegalArgumentException("Index not supported.");
        }
    }
    
	/**
	 * Lens action changes the size of the lens based on the parameter 
	 * lensIndex. 
	 * 
	 * @param lens      The parent component. Mustn't be <code>null</code>.
	 * @param lensIndex The action index. One of the constants
	 * 					defined by this class.
	 */
	LensAction(LensComponent lens, int lensIndex)
	{
		if (lens == null)
			throw new IllegalArgumentException("No parent.");
		this.lens = lens;
		checkIndex(lensIndex);
		index = lensIndex;
        putValue(Action.NAME, names[index]);
	}
	
	/** 
     * Sets the size of the lens.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) { lens.setLensSize(index); }
	    
}



