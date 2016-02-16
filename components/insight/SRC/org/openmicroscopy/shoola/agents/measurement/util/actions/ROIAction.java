/*
 * org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIMenuAction 
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
package org.openmicroscopy.shoola.agents.measurement.util.actions;


import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController.CreationActionType;
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ROIAction
	extends AbstractAction
{
	
	/** action for this 'action' to perform. */
	protected CreationActionType 	action;
	
	/** The controller for the event. */
	protected ROIActionController 	controller;

	/**
	 * Sets the name of the action.
	 * 
	 * @param name The value to set.
	 */
	private void setName(String name)
	{
		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, name);
	}

	/**
	 * Sets the tool-tip of the action.
	 * 
	 * @param name The value to set.
	 */
	private void setToolTip(String name)
	{
		putValue(Action.LONG_DESCRIPTION, name);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller the reference to the action controller.
	 * @param action The type of action to create.
	 */
	public ROIAction(ROIActionController controller, CreationActionType action)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (action == null)
			throw new IllegalArgumentException("No Action type specified.");
		this.controller = controller;
		this.action = action;
		setEnabled(false);
		switch (action)
		{
			case DUPLICATE:
				this.
				setName("Duplicate");
				setToolTip("Duplicate the selected ROI, " +
						"ROIShapes, creating new ROI.");
				break;
			case DELETE:
				setName("Delete");
				setToolTip("Delete selected ROI and ROIShapes.");
				break;
			case MERGE:
				setName("Merge");
				setToolTip("Merge the selected ROIShapes into a new ROI.");
				break;
			case SPLIT:
				setName("Split");
				setToolTip("Split the selected ROIShapes from parent " +
						"ROI into a new ROI.");
				break;
			case PROPAGATE:
				setName("Propagate");
				setToolTip("Propagate the selected ROI through " +
						"Time and Z Sections.");
				break;
			case TAG:
                setName("Tag");
                setToolTip("Tag the selected shapes.");
                break;
			case ADD_TO_FOLDER:
                setName("Add to Folder");
                setToolTip("Adds the Rois to specific Folders.");
                break;
            case REMOVE_FROM_FOLDER:
                setName("Remove from Folder");
                setToolTip("Removes the Rois from specific Folders.");
                break;
            case CREATE_FOLDER:
                setName("Create Folder");
                setToolTip("Creates a new Folder.");
                break;
            case DELETE_FOLDER:
                setName("Delete Folders");
                setToolTip("Deletes the Folders.");
                break;
            case EDIT_FOLDER:
                setName("Edit Folder");
                setToolTip("Edit Folder.");
                break;
            case MOVE_FOLDER:
                setName("Move Folder");
                setToolTip("Move Folder to another Folder.");
		}
	}
	
	/**
	 * Returns the type of this action.
	 * 
	 * @return See above.
	 */
	public CreationActionType getCreationActionType()
	{
		return action;
	}
	
	/**
	 * Manipulates the ROI.
	 * @see AbstractAction#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		switch (action)
		{
			case DUPLICATE:
				controller.duplicateROI();
				break;
			case DELETE:
				controller.deleteROI();
				break;
			case MERGE:
				controller.mergeROI();
				break;
			case SPLIT:
				controller.splitROI();
				break;
			case PROPAGATE:
				controller.propagateROI();
				break;
			case TAG:
                controller.loadTags();
                break;
			case ADD_TO_FOLDER:
                controller.addToFolder();
                break;
            case REMOVE_FROM_FOLDER:
                controller.removeFromFolder();
                break;
            case CREATE_FOLDER:
                controller.createFolder();
                break;
            case DELETE_FOLDER:
                controller.deleteFolder();
                break;
            case EDIT_FOLDER:
                controller.editFolder();
                break;
            case MOVE_FOLDER:
                controller.moveFolder();
		}
	}
}
