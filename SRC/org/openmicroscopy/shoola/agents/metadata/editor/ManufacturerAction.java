/*
 * org.openmicroscopy.shoola.agents.metadata.editor.ManufacturerAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;



//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Action to display the details of a manufacturer.
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
class ManufacturerAction 
	extends MouseAdapter
{

	/** The component to display. */
	private JComponent comp;
	
	/** Reference to the model. */
	private Editor viewer;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer 	Reference to the mdoel. Mustn't be <code>null</code>.
	 * @param comp		The component to lay out. Mustn't be <code>null</code>.
	 */
	ManufacturerAction(Editor viewer,  JComponent comp)
	{
		if (viewer == null)
			throw new IllegalArgumentException("No viewer.");
		if (comp == null)
			throw new IllegalArgumentException("No component to display.");
		this.viewer = viewer;
		this.comp = comp;
	}
	
	/**
	 * Shows a dialog with the component.
	 * @see MouseAdapter#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent evt)
	{
		Point p = evt.getPoint();
		SwingUtilities.convertPointToScreen(p, (Component) evt.getSource());
		viewer.showManufacturer(comp, p);
	}
	
}
