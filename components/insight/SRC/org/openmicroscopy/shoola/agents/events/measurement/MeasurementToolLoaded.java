/*
 * org.openmicroscopy.shoola.agents.events.measurement.MeasurementToolLoaded 
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
package org.openmicroscopy.shoola.agents.events.measurement;


//Java imports
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.event.ResponseEvent;

/** 
 * Event posted to either add or remove the measurement to the viewer.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MeasurementToolLoaded 
	extends ResponseEvent
{

	/** Index indicating to add the component. */
	public static final int ADD = 0;
	
	/** Index indicating to remove the component. */
	public static final int REMOVE = 1;
	
	/** The component to add to the image display. */
	private JComponent	view;
	
	/** One of the constants defined by this class. */
	private int index;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/**
	 * Controls if the passed index is valid.
	 * 
	 * @param i The value to control.
	 */
	private void checkIndex(int i)
	{
		switch (i) {
			case ADD:
			case REMOVE:
				return;
	
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param act The original request.
	 * @param ctx The security context.
	 * @param view The component to add to the display.
	 * @param index One of the constants defined by this class.
	 */
	public MeasurementToolLoaded(RequestEvent act, SecurityContext ctx,
	JComponent view, int index) 
	{
		super(act);
		checkIndex(index);
		this.index = index;
		this.view = view;
		this.ctx = ctx;
	}

	/**
	 * Returns the component to add to the display.
	 * 
	 * @return See above.
	 */
	public JComponent getView() { return view; }
	
	/**
	 * Returns either {@link #ADD} or {@link #REMOVE}.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext() { return ctx; }

}
