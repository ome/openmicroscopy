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
import omero.gateway.SecurityContext;
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

	/** Flag indicating to browse the object.*/
	private boolean		browse;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/** The plugin if any.*/
	private int plugin;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param object The object to view.
	 * @param source The component triggering the event.
	 */
	public ViewObjectEvent(SecurityContext ctx, Object object,
			JComponent source)
	{
		this.ctx = ctx;
		this.object = object;
		this.source = source;
		browse = false;
		plugin = -1;
	}
	
	/**
	 * Sets the plug-in.
	 * 
	 * @param plugin See above.
	 */
	public void setPlugin(int plugin) { this.plugin = plugin; }
	
	/**
	 * Returns the plug-in.
	 * 
	 * @return See above.
	 */
	public int getPlugin() { return plugin; }
	
	/**
	 * Sets to <code>true</code> to browse the node, <code>false</code>
	 * otherwise.
	 * 
	 * @param browse Pass <code>true</code> to browse, <code>false</code>
	 * 				 otherwise.
	 */
	public void setBrowseObject(boolean browse) { this.browse = browse; }
	
	/**
	 * Returns <code>true</code> to browse the node, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean browseObject() { return browse; }
	
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
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext() { return ctx; }
	
}
