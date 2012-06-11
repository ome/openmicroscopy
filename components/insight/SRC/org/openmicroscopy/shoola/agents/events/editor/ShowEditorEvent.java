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


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;

/** 
 * Requests that OMERO.editor agent becomes active. 
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
	
	/** Possible extension of the experiment. */
	public static final String EXPERIMENT_EXTENSION = "_exp";
	
	/** Indicates to open an experiment file. */
	public static final int EXPERIMENT = 0;
	
	/** Indicates to open a protocol file. */
	public static final int PROTOCOL = 1;
	
	/** The data object the new file has to be linked to. */
	private DataObject parent;
	
	/** The name of the file. */
	private String name;
	
	/** The type of editor file to create. */
	private int type;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 */
	public ShowEditorEvent(SecurityContext ctx)
	{
		type = PROTOCOL;
		parent = null;
		this.ctx = ctx;
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param ctx The security context.
	 * @param parent The data object the new experiment should be linked to. 
	 * @param name   The name to give to the experiment.
	 * @param type	 One of the constants defined by this class.
	 */
	public ShowEditorEvent(SecurityContext ctx, DataObject parent, String name,
			int type)
	{
		this.parent = parent;
		this.name = name;
		if (type == PROTOCOL || type == EXPERIMENT) this.type = type;
		else this.type = PROTOCOL;
		this.ctx = ctx;
	} 
	
	/**
	 * Returns the data object the new experiment should be linked to. 
	 * 
	 * @return See above.
	 */
	public DataObject getParent() { return parent; }
	
	/**
	 * Returns the name to give to the experiment.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the type.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }

	/** 
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext() { return ctx; }
} 

