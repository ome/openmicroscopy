/*
 * org.openmicroscopy.shoola.util.ui.lens.LensMouseListener
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
 * LensUI which will in turn post them to the lensContoller. 
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
	 * Calls the views {@link LensUI#mouseDown(int, int)} event
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent incomingEvent) 
	{
		if (incomingEvent.getButton() == MouseEvent.BUTTON3 || 
				incomingEvent.isControlDown())
			changeview.showMenu(incomingEvent.getX(), incomingEvent.getY());
		else
			changeview.mouseDown(incomingEvent.getX(), incomingEvent.getY());
	}

	/** 
     * Calls the views {@link LensUI#mouseUp(int, int)} event.
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent incomingEvent) 
	{
		changeview.mouseUp(incomingEvent.getX(), incomingEvent.getY());
	}

	/** 
	 * Calls the views {@link LensUI#mouseDrag(int, int, boolean)} 
     * event.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent incomingEvent)
    {
		changeview.mouseDrag(incomingEvent.getX(), incomingEvent.getY(), 
					incomingEvent.isShiftDown());
	}

	/** 
     * Calls the views {@link LensUI#mouseMoved(int, int)} event.
	 * @see MouseMotionListener#mouseMoved(MouseEvent)
	 */
	public void mouseMoved(MouseEvent incomingEvent)
	{
		changeview.mouseMoved(incomingEvent.getX(), incomingEvent.getY());
	}

	/** 
     * Calls the views {@link LensUI#mouseWheelMoved(int)} event.
	 * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
    {
		changeview.mouseWheelMoved(e.getWheelRotation());
	}
	
	/** 
     * Required by the {@link MouseListener} I/F but no-op implementation 
     * in our case.
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent incomingEvent) {}


	/** 
     * Required by the {@link MouseListener} I/F but no-op implementation 
     * in our case.
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent incomingEvent) {}

	/** 
     * Required by the {@link MouseListener} I/F but no-op implementation 
     * in our case.
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent incomingEvent) {}
	
}


