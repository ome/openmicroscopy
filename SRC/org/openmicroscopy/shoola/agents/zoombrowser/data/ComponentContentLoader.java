/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.ComponentContentLoader
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.zoombrowser.data;

//Java imports

import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ContentComponent;

/** 
 * A swing worker class that can be used to load different types of content and
 * associate that content with a component.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class ComponentContentLoader extends ContentLoader
{
	/** Component */
	protected ContentComponent component;
	
	
	public ComponentContentLoader(final DataManager dataManager,
			final ContentComponent component,
			final ContentGroup group) {
		super(dataManager,group);
		this.component = component;
		start();
	}	
	
	/**
	 * Do the work
	 */
	public Object construct() {
		List items = getContents();
		if (component != null) {
			component.setContents(items);
			component.layoutContents();
		}
		return items;
	}
	
	public abstract List getContents();
	
	/**
	 * handle finishing up 
	 */
	public void finished() {
		group.finishLoader(this);
	}
	
	/**
	 * retrieve the component 
	 */
	public ContentComponent getComponent() {
		return component;
	}
	
	public void completeInitialization() {
		if (component != null)
			component.completeInitialization();
	}
}