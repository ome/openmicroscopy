 /*
 * org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent 
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

import org.openmicroscopy.shoola.env.event.RequestEvent;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Posting this event simply requests that OMERO.editor agent becomes 
 * active. 
 * If any instances of it are already open, they will become activated,
 * otherwise, a blank editor window will open. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ShowEditorEvent
	extends RequestEvent
{
	
	/**
	 * Creates a new instance.
	 */
	public ShowEditorEvent()
	{}
	
} 

