/*
 * org.openmicroscopy.shoola.env.ui.ViewObjectEvent 
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Views the passed object.
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
public class ViewObjectEvent 
	extends RequestEvent
{

	/** The object to view. */
	private Object object;
	
	/** The UI component used to trigger the event.*/
	private JComponent source;

	/**
	 * Creates a new instance.
	 * 
	 * @param object The object to view.
	 * @param source The component triggering the event.
	 */
	public ViewObjectEvent(Object object, JComponent source)
	{
		this.object = object;
		this.source = source;
	}
	
	/**
	 * Returns the object to view.
	 * 
	 * @return See above.
	 */
	public JComponent getSource() { return source; }
	
	/**
	 * Returns the object to view.
	 * 
	 * @return See above.
	 */
	public Object getObject() { return object; }
	
}
