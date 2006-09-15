/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.MagnifierMouseListener
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
package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.event.MouseInputListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class HSVWheelListener 
	implements MouseMotionListener, MouseInputListener
{
	
	/** 
	 * Reference to the colourpanel, used so the listener can call the mouse 
	 * activation methods of the colourpanel upon action.
	 */ 
	private HSVWheel	changeview;
	
	/**
	 * Constructor which creates a reference to the UI ipon which the mouse 
	 * events are to act.
	 * @param view current view to which the mouselistner will be attached.
	 */
	HSVWheelListener(HSVWheel view)
	{
		changeview = view;
	}

	/** 
	 * Calls the views {@link HSVWheel#mouseDown} event
	 * @see MouseInputListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent evt) 
	{
		changeview.mouseDown(evt.getX(), evt.getY());
	}

	/** 
	 * Calls the views {@link HSVWheel#mouseDown} event.
	 * @see MouseMotionListener#mouseDragged(MouseEvent)
	 */
	public void mouseDragged(MouseEvent incomingEvent) {
		changeview.mouseDrag(incomingEvent.getX(), incomingEvent.getY());
	}

    /** 
     * Required by {@link MouseInputListener} I/F but not actually needed
     * in our case, no op implementation.
     * @see MouseInputListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent evt) {}
    
    /** 
     * Required by {@link MouseInputListener} I/F but not actually needed
     * in our case, no op implementation.
     * @see MouseInputListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent incomingEvent) {}


    /** 
     * Required by {@link MouseInputListener} I/F but not actually needed
     * in our case, no op implementation.
     * @see MouseInputListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent evt) {}

    /** 
     * Required by {@link MouseInputListener} I/F but not actually needed
     * in our case, no op implementation.
     * @see MouseInputListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent evt) {}

    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed
     * in our case, no op implementation.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */ 
	public void mouseMoved(MouseEvent evt) {}
	
}


