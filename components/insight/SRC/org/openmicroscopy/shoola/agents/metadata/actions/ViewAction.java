/*
 * org.openmicroscopy.shoola.agents.metadata.actions.ViewAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Opens the viewer.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ViewAction 
	extends RndAction
{

	/** The name of the action. */
	public static final String NAME = "View...";
	
	/** The description of the action. */
	public static final String DESCRIPTION = "Open the viewer.";
	
	/** Name of the action. */
	public static final String NAME_IJ = "View in ImageJ...";

    /** Description of the action. */
	public static final String DESCRIPTION_IJ = "View the selected image " +
    		"in ImageJ.";
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	public ViewAction(Renderer model)
	{
		super(model);
		setEnabled(true);
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.VIEWER));
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
	}
	
	/**
	 * Launches the viewer.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		model.viewImage();
	}
	
}
