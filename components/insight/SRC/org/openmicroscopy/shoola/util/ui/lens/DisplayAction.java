/*
 * org.openmicroscopy.shoola.util.ui.lens.DisplayAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
 * Change the units displayed in the {@link StatusPanel} between micron and 
 * pixels. 
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
class DisplayAction 		
	extends AbstractAction
{
	
	/** Number of options in action. */
	final static int 		MAX = 2;

	/** Display units in microns. */
	final static int		MICRON_OPTION = 0;
	
	/** Display units in pixels. */
	final static int		PIXEL_OPTION = 1;
	
	/** the parent component of the magnifying lens. */
	private LensComponent	lens;
	
	/** The index which refers to the change in the lens size.*/
	private int				index;
	
	/** Names for each action associated with the change in lens size. */
	private static String[]     names;
	   
	static {
	        names = new String[MAX];
	        names[MICRON_OPTION] = "Microns";
	        names[PIXEL_OPTION] = "Pixels";
	}

    /** 
     * Controls if the specified index is valid.
     * 
     * @param i The index to check.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case MICRON_OPTION:
            case PIXEL_OPTION:
                return;
            default:
                throw new IllegalArgumentException("Index not supported.");
        }
    }
    
	/**
	 * Lens action changes the size of the lens based on the parameter 
	 * lensIndex. 
	 * 
	 * @param lens          The parent component. Mustn't be <code>null</code>.
	 * @param displayIndex  The index of the action. One of the constants
	 * 						defined by this class.
	 */
	DisplayAction(LensComponent lens, int displayIndex)
	{
		if (lens == null)
			throw new IllegalArgumentException("No parent.");
		this.lens = lens;
		checkIndex(displayIndex);
		index = displayIndex;
        putValue(Action.NAME, names[index]);
	}
	
	/** 
     * Displays the unit.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
    {
		lens.setDisplayInPixels(index == PIXEL_OPTION);
    }
    
	/**
	 * Get the type of this action
	 * @return See above
	 */
	int getIndex() {
	    return index;
	}
}


