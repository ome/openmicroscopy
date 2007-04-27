/*
 * measurement.MeasurementComponent 
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

import javax.swing.JComponent;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.MeasurementModel;
import org.openmicroscopy.shoola.util.ui.measurement.MeasurementView;
import org.openmicroscopy.shoola.util.ui.measurement.model.ImageModel;
import org.openmicroscopy.shoola.util.ui.roi.ROIComponent;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

/** 
 *	This is the measurement component for the measurement tool. This is the 
 *  interface to the main tool. Through this the external applications can 
 *  access the measurement tools functionality. It is not used by the internal
 *  components of the measurement tool, they are aggregated inside this 
 *  interface but communicate internally via MeasurementModel. 
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
public class MeasurementComponent
	extends Component
{
	private MeasurementModel	model;
	private MeasurementView		view;
	private ROIComponent 		roiComponent;
	
	public MeasurementComponent(ImageModel imageModel)
	{
		roiComponent = new ROIComponent();
		model = new MeasurementModel(imageModel, roiComponent);
		view = new MeasurementView(model);
	}
		
	public void setCoord3D(Coord3D coord)
	{
		model.setCoord3D(coord);
		view.setCoord3D(coord);
	}
	
	public boolean isVisible()
	{
		return view.isVisible();
	}
	
	public void setVisible(boolean isVisible)
	{
		view.setVisible(isVisible);
	}
	
	public void setZoomFactor(double zoom)
	{
		view.setZoomFactor(zoom);
	}
	
	public JComponent getMeasurementUI()
	{
		return view.getUI();
	}
	
}


