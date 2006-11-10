/*
 * org.openmicroscopy.shoola.util.ui.lens.LensMouseListener
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;
//Third-party libraries

//Application-internal dependencies

/** 
 * MouseListener attached to the LensUI, it simply relays mouse events to the 
 * LensUI which will inturn post them to the lensContoller. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class LensMouseListener 
	implements MouseMotionListener, MouseInputListener, MouseListener, 
	MouseWheelListener
{
	
	/** 
	 * Reference to the colourpanel, used so the listener can call the mouse 
	 * activation methods of the colourpanel upon action.
	 */ 
	private LensUI	changeview;
	
	/**
	 * Constructor which creates a reference to the UI ipon which the mouse 
	 * events are to act.
	 * @param view current view to which the mouselistner will be attached.
	 */
	LensMouseListener(LensUI view)
	{
		changeview = view;
	}

	/** 
	 * Overridden calls the views {@link LensUI#mouseDown} event
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent incomingEvent) 
	{
		if( incomingEvent.getButton() == MouseEvent.BUTTON3 || 
				incomingEvent.isControlDown())
			changeview.showMenu(incomingEvent.getX(), incomingEvent.getY());
		else
			changeview.mouseDown(incomingEvent.getX(), incomingEvent.getY());
	}

	/** (non-Javadoc)
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent incomingEvent) 
	{
		changeview.mouseUp(incomingEvent.getX(), incomingEvent.getY());
	}

	/** (non-Javadoc)
	 * Overridden calls the views {@link LensUI#mouseDown} event.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent incomingEvent) {
			changeview.mouseDrag(incomingEvent.getX(), incomingEvent.getY(), 
					incomingEvent.isShiftDown());
	}

	/** (non-Javadoc)
	 * @see 
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent incomingEvent) {
		changeview.mouseMoved(incomingEvent.getX(), incomingEvent.getY());
	}

	/** (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved
	 * (java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		changeview.mouseWheelMoved(e.getWheelRotation());
		
	}
	
	/** (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent incomingEvent) {	}


	/** (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {	}

	/** (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent incomingEvent) {	}
	
}


