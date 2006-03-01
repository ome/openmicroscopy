/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.ContentGroup
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
import java.util.Vector;
import java.util.Iterator;


//Third-party libraries

//Application-internal dependencies

/** 
 * A group of all of the SwingWorkers that are loading OME data.
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
public class ContentGroup 
{
	
	/** keep track of threads that I have to finish up. */
	private Vector workers = new Vector();
	
	/** workers that have completed */
	private Vector completed = new Vector();
	
	/** a flag to indicate when all have been added */
	private boolean allAdded = false;
	
	/** object that gets notified when this is done */
	private ContentGroupSubscriber subscriber = null;
	
	
	private ContentGroup() {
		
	}
	
	public ContentGroup(ContentGroupSubscriber subscriber) {
		this.subscriber = subscriber;
	}
	
	
	public void addLoader(ContentLoader c) {
		workers.add(c);
	}
	
	public void finishLoader(ContentLoader c) {
		if (workers.contains(c)) {
			workers.remove(c);
			completed.add(c);
		}
		if (workers.size() == 0 && allAdded == true) 
			completeWorkers();
	}
	
	public void setAllLoadersAdded() {
		allAdded = true;
		if (workers.size() == 0)
			completeWorkers();
	}
	
	private void completeWorkers() {
		Iterator iter = completed.iterator();
		ContentLoader loader;
		
		while (iter.hasNext()) {
			loader = (ContentLoader) iter.next();
			loader.completeInitialization();	
		}
		if (subscriber != null) 
			subscriber.contentComplete();
	}
}