 /*
 * org.openmicroscopy.shoola.agents.events.editor.CopyEvent 
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
package org.openmicroscopy.shoola.agents.events.editor;

//Java imports

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event to copy parts of an Editor file/protocol. Eg A list of fields/steps
 * or a portion of a field (text + parameters) etc. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CopyEvent 
	extends RequestEvent
{

	/** This could be text with parameters, a list of fields etc. */
	private Object 		copiedData;
	
	/**
	 * Creates an instance, saving the passed object. 
	 * 
	 * @param copiedObject		The object that was copied. 
	 */
	public CopyEvent(Object copiedObject) 
	{
		copiedData = copiedObject;
	}
	
	/**
	 * Returns the object that was copied to this event. 
	 * 
	 * @return	see above
	 */
	public Object getCopiedData() { return copiedData; }
}
