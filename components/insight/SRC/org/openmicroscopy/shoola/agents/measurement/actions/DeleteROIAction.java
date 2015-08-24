/*
 * org.openmicroscopy.shoola.agents.measurement.actions.DeleteROIAction 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to delete all the ROIs owned by the user currently logged in.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DeleteROIAction
	extends MeasurementViewerAction
{

	/** The name of the action. */
	private static final String NAME = "Delete ROI";

	/** The description of the action. */
	private static final String DESCRIPTION = "Delete all your ROIs on " +
			"this image.";
	

	/**
	 * Sets the enabled flag
	 * @see MeasurementViewerAction#onStateChange()
	 */
	protected void onStateChange()
	{
		if (model.getState() == MeasurementViewer.READY)
			setEnabled(model.canDelete() && model.hasROIToDelete());
		else setEnabled(false);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The model. Mustn't be <code>null</code>.
	 */
	public DeleteROIAction(MeasurementViewer model)
	{
		super(model);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.DELETE));
	}
	
	/** 
     * Deletes all ROI owned by the user currently logged in.
     */
    public void actionPerformed(ActionEvent e)
    { 
    	if (model.isMember()) {
    		String message = 
    			"Do you want to delete all your ROIs on this image.";
    		MessageBox msg = new MessageBox(model.getUI(), "Delete ROI",
    				message);
        	if (msg.centerMsgBox() == MessageBox.YES_OPTION)
        		model.deleteAllROIs(MeasurementViewer.ME); 
    	}
    }

}
