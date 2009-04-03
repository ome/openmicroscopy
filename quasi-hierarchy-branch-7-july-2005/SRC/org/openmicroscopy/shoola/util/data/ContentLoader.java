/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.ContentLoader
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

package org.openmicroscopy.shoola.util.data;

//Java imports


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.SwingWorker;
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;


/** 
 * A swing worker class that can be used to load different types of content.
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
public abstract class ContentLoader extends SwingWorker
{
	
	/** Thread group */
	protected ContentGroup group;
	
	/** Data manager */
	protected DataManager dataManager;
	
	public ContentLoader(final DataManager dataManager,
			final ContentGroup group) {
		super();
		this.dataManager = dataManager;
		this.group = group;
		group.addLoader(this);
	}	
	
	/**
	 * Do the work
	 */
	public Object construct() {
		Object items = getContents();
		return items;
	}
	
	public abstract Object getContents();
	
	/**
	 * handle finishing up 
	 */
	public void finished() {
		group.finishLoader(this);
	}
	
	public void completeInitialization() {
		
	}
}