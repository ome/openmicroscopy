/*
 * org.openmicroscopy.shoola.agents.events.iviewer.ViewerCreated 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.iviewer;


//Java imports
import javax.swing.JComponent;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when the viewer is embedded in the another component 
 * and not displayed as a separate window.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ViewerCreated 
	extends RequestEvent
{

	/** Reference to the viewer. */
	private JComponent viewer;
	
	/** Reference to the components displaying metadata etc. */
	private JComponent controls;

	/** Flag indicating to add or to remove the viewer. */
	private boolean toAdd;
	
	/** Flag indicating to detach the window. */
	private boolean detach;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer Reference to the viewer.
	 * @param controls Reference to the controls.
	 * @param toAdd  Pass <code>true</code> to add the viewer,
	 * 				 <code>false</code> otherwise.
	 */
	public ViewerCreated(JComponent viewer, JComponent controls, boolean toAdd)
	{
		this.viewer = viewer;
		this.toAdd = toAdd;
		this.controls = controls;
	}
	
	/**
	 * Returns the component representing the viewer.
	 * 
	 * @return See above.
	 */
	public JComponent getViewer() { return viewer; }
	
	/**
	 * Returns the component representing the viewer.
	 * 
	 * @return See above.
	 */
	public JComponent getControls() { return controls; }
	
	
	/**
	 * Returns <code>true</code> to add the viewer,
	 * <code>false</code> to remove it.
	 * 
	 * @return See above.
	 */
	public boolean isToAdd() { return toAdd; }
	
	/**
	 * Returns <code>true</code> to detach the viewer,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isToDetach() { return detach; }
	
	/**
	 * Detaches the viewer.
	 * 
	 * @param detach Pass <code>true</code> to detach the viewer. 
	 * 				 <code>false</code> otherwise.
	 */
	public void setDetach(boolean detach) { this.detach = detach; }
	
}
