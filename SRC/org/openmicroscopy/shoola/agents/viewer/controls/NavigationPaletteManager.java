/*
 * org.openmicroscopy.shoola.agents.viewer.controls.NavigationPaletteManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.viewer.controls;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NavigationPaletteManager
{
	private NavigationPalette view;
	private ViewerCtrl eventManager;
	
	NavigationPaletteManager(NavigationPalette view, ViewerCtrl eventManager)
	{
		this.view = view;
		this.eventManager = eventManager;
	}

	Registry getRegistry()
	{
		return eventManager.getRegistry();
	}
	
	void onTChange(int t)
	{
		XYZNavigator navigator = view.getXYZNavigator();
		int z = navigator.getZSlider().getValue();
		eventManager.onPlaneSelected(z, t);
	}
	
	void onZChange(int z) 
	{
		TNavigator navigator = view.getTNavigator();
		int t = navigator.getTSlider().getValue();
		eventManager.onPlaneSelected(z, t);
	}
	
}
