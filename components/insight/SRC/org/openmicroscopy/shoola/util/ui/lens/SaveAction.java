/*
 * org.openmicroscopy.shoola.util.ui.lens.SaveAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Launches the Saving dialog to save the image as <code>JPEG</code>,
 * <code>PNG</code>, etc.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class SaveAction 
	extends AbstractAction
{

	/** The name of the action.*/
	static final String NAME = "Save As";
	
	/** The name of the action.*/
	static final String DESCRIPTION = "Save the image displayed in the " +
			"Zoom Window as JPEG, PNG, etc.";
	
	/** The parent component of the magnifying lens.*/
	private LensComponent	lens;
	
	/**
	 * Creates a new instance 
	 * 
	 * @param lens	The parent component. Mustn't be <code>null</code>.
	 */
	SaveAction(LensComponent lens)
	{
		if (lens == null)
			throw new IllegalArgumentException("No parent.");
		this.lens = lens;
        putValue(Action.NAME, NAME+"...");
        putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));

	}
	
	/** 
     * Launches the saving dialog.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) { lens.saveAs(); }
	
}
