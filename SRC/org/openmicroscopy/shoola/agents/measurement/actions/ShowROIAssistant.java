/*
 * org.openmicroscopy.shoola.agents.measurement.actions.ShowROIAssistant 
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
import java.util.Collection;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Brings up the ROI assistant.
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
public class ShowROIAssistant
	extends MeasurementViewerAction
{

	/** The name of the action. */
	private static final String NAME = "ROI Assistant...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Show the ROI Assistant.";

	/** 
	 * Sets the action enabled if the ROIs are server ROI. 
	 * @see MeasurementViewerAction#onStateChange()
	 */
	protected void onStateChange()
	{
		if (model.isHCSData()) setEnabled(false);
		else {
			if (model.getState() == MeasurementViewer.READY) {
				Collection values = model.getSelectedFigures();
				if (values != null) {
					setEnabled(values.size() == 1);
				}
			} else setEnabled(false);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The model. Mustn't be <code>null</code>.
	 */
	public ShowROIAssistant(MeasurementViewer model)
	{
		super(model);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.WIZARD));
	}
	
	/** 
     * Brings up the results wizard.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.showROIAssistant(); }
    
}
