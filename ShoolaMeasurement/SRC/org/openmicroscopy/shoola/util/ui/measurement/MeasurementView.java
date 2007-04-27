/*
 * measurement.MeasurementView 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.measurement;


//Java imports
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.MeasurementModel;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIViewComponent;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

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
public class MeasurementView 
	extends Component
	implements PropertyChangeListener
{
	private MeasurementModel 	model;
	private JPanel				parent;

	private UIViewComponent		uiViewComponent;
	
	public MeasurementView(MeasurementModel model)
	{
		this.model = model;
		uiViewComponent = new UIViewComponent(model);
	}

	
	public void setCoord3D(Coord3D coord)
	{
		uiViewComponent.setCoord3D(coord);
	}
	
	public void setVisible(boolean isVisible)
	{
		uiViewComponent.setVisible(isVisible);
	}
	
	public boolean isVisible()
	{
		return uiViewComponent.isVisible();
	}
	
	public JComponent getUI()
	{
		return uiViewComponent.getUI();
	}
	
	public void setZoomFactor(double zoom)
	{
		uiViewComponent.setZoomFactor(zoom);
	}
	
	/* (non-Javadoc)
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		
	}
	
	
}


