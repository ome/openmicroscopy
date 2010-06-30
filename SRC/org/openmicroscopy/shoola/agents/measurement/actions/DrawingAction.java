/*
 * org.openmicroscopy.shoola.agents.measurement.actions.DrawingAction 
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



//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Removes the action listener but listens to state changes via 
 * MeasurementViewerAction.
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
public class DrawingAction
	extends MeasurementViewerAction
{
	
	/** 
	 * Sets the action enabled if the ROIs are server ROI. 
	 * @see MeasurementViewerAction#onStateChange()
	 */
	protected void onStateChange()
	{
		if (model.isHCSData()) setEnabled(false);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	The model. Mustn't be <code>null</code>.
	 * @param button	The button the action will be applied to.
	 */
	public DrawingAction(MeasurementViewer model, JToggleButton button)
	{
		super(model);
		if (button == null)
			throw new IllegalArgumentException("No button specified. ");
		putValue(Action.NAME, button.getName());
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(button.getToolTipText()));
		//resize icons
		Icon icon = button.getIcon();
		if (icon != null) putValue(Action.SMALL_ICON, Factory.scaleIcon(icon));
	}
	
	/** 
	 * Removes the action listener.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}
    
}



