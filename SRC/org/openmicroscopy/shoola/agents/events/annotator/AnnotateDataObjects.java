/*
 * org.openmicroscopy.shoola.agents.events.annotator.AnnotateDataObjects 
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
package org.openmicroscopy.shoola.agents.events.annotator;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Annotates the specified <code>DataObject</code>s.
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
public class AnnotateDataObjects
	extends RequestEvent
{

	/** Collection of <code>DataObject</code>s to annotate. */
	private Set toAnnotate;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param toAnnotate Collection of <code>DataObject</code>s to annotate.
	 * 					 Mustn't be <code>null</code>.
	 */
	public AnnotateDataObjects(Set toAnnotate)
	{
		if (toAnnotate == null || toAnnotate.size() == 0)
			throw new IllegalArgumentException("No data object to annotate.");
		this.toAnnotate = toAnnotate;
	}
	
	/**
	 * Returns the collection of <code>DataObject</code>s to annotate.
	 * 
	 * @return See above.
	 */
	public Set getObjectsToAnnotate() { return toAnnotate; }
	
}
