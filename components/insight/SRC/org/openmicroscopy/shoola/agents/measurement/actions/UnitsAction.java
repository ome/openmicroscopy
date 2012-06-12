/*
 * org.openmicroscopy.shoola.agents.measurement.actions.UnitsAction 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.UnitsObject;

/** 
 * Sets the unit either in the reference units or in pixels.
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
public class UnitsAction
	extends MeasurementViewerAction
{

	/** Show the pixels in Pixels. */
	private static final String NAME_PIXELS = "in Pixels";
	
	/** Show the pixels in Microns. */
	private static final String NAME_MICRONS = "in Microns";
	
	/** The description of the action for microns. */
	private static final String DESCRIPTION_MICRONS = "Show the measurement " +
											"units ";

	/** The description of the action for pixels. */
	private static final String DESCRIPTION_PIXELS = "Show the measurement " +
												"units in Pixels.";

	/** Display measurement in microns. */
	private boolean				inMicrons; 
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model 	The model. Mustn't be <code>null</code>.
	 * @param inMicrons Passed <code>true</code> to set the unit in microns,
	 * 					<code>false</code> otherwise.
	 */
	public UnitsAction(MeasurementViewer model, boolean inMicrons)
	{
		super(model);
		this.inMicrons = inMicrons;
		if (inMicrons) {
			name = NAME_MICRONS;
			putValue(Action.NAME, NAME_MICRONS);
			putValue(Action.SHORT_DESCRIPTION, 
            UIUtilities.formatToolTipText(DESCRIPTION_MICRONS+NAME_MICRONS));
		} else {
			name = NAME_PIXELS;
			putValue(Action.NAME, NAME_PIXELS);
			putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION_PIXELS));
		}
	}
	
	/**
	 * Sets the reference units.
	 * 
	 * @param units The units of reference.
	 */
	public void setRefUnits(String units)
	{
		String value = NAME_MICRONS;
		if (UnitsObject.CENTIMETER.equals(units)) {
			value = "in Centimeters";
		} else if (UnitsObject.MILLIMETER.equals(units)) {
			value = "in Millimeters";
		} else if (UnitsObject.METER.equals(units)) {
			value = "in Meters";
		} else if (UnitsObject.NANOMETER.equals(units)) {
			value = "in Nanometers";
		} else if (UnitsObject.PICOMETER.equals(units)) {
			value = "in Picometers";
		} else if (UnitsObject.ANGSTROM.equals(units)) {
			value = "in Angstroms";
		}
		name = value;
		putValue(Action.NAME, value);
		putValue(Action.SHORT_DESCRIPTION, 
        UIUtilities.formatToolTipText(DESCRIPTION_MICRONS+value));
	}
	
	/** 
     * Sets the units.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    { 
    	model.showMeasurementsInMicrons(inMicrons); 
    }

}