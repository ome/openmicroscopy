/*
 * org.openmicroscopy.shoola.agents.measurement.actions.FigureCreationAction 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class FigureCreationAction
extends MeasurementViewerAction
{

	/** Show the pixels in Pixels. */
	private static final String NAME_CONTINUOUS = "Create lots of figures.";
	
	/** Show the pixels in Microns. */
	private static final String NAME_SINGLE = "Create a single figure.";
	
	/** The description of the action for microns. */
	private static final String DESCRIPTION_CONTINUOUS = "Create lots of figures.";

	/** The description of the action for pixels. */
	private static final String DESCRIPTION_SINGLE = "Create a single figure.";

	/** Create a single figure rather than a whole pile of figures. */
	private boolean				createSingle; 
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The model. Mustn't be <code>null</code>.
	 */
	public FigureCreationAction(MeasurementViewer model, boolean createSingle)
	{
		super(model);
		this.createSingle = createSingle;
		if(!createSingle)
		{
			name = NAME_CONTINUOUS;
			putValue(Action.NAME, NAME_CONTINUOUS);
			putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION_CONTINUOUS));
		}
		else
		{
			name = NAME_SINGLE;
			putValue(Action.NAME, NAME_SINGLE);
			putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION_SINGLE));
		}
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.STATUS_INFO));
	}
	
	/** 
     * Brings up the results wizard.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    { 
    	model.createSingleFigure(createSingle); 
    }
    
}
