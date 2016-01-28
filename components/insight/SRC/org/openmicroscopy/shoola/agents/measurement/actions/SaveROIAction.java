/*
 * org.openmicroscopy.shoola.agents.measurement.actions.SaveROIAction 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Saves the ROI for a given set of pixels.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class SaveROIAction
	extends MeasurementViewerAction
	implements PropertyChangeListener
{

	/** The name of the action. */
	private static final String NAME = "Save...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Save the ROI.";

	/**
	 * Sets the enabled flag
	 * @see MeasurementViewerAction#onStateChange()
	 */
	protected void onStateChange()
	{
		//Depends on the status of the group
		if (model.getState() == MeasurementViewer.READY)
			setEnabled(model.canAnnotate());
		else setEnabled(false);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The model. Mustn't be <code>null</code>.
	 */
	public SaveROIAction(MeasurementViewer model)
	{
		super(model);
		model.addPropertyChangeListener(
				MeasurementViewer.ROI_CHANGED_PROPERTY, this);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.SAVE_AS));
	}
	
	/** 
     * Saves the ROI.
     */
    public void actionPerformed(ActionEvent e) 
    { 
    	model.saveROIToServer(false); 
    }

    /**
     * Sets the enabled flag to <code>true</code> if ROI to save.
     * @param evt The event to handle.
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (MeasurementViewer.ROI_CHANGED_PROPERTY.equals(name))
			onStateChange();
	}

}
