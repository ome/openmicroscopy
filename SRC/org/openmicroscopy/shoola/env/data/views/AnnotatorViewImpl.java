/*
 * org.openmicroscopy.shoola.env.data.views.AnnotatorViewImpl 
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
package org.openmicroscopy.shoola.env.data.views;


//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AnnotationSaver;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;

/** 
 * Implementation of the {@link AnnotatorView} implementation.
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
public class AnnotatorViewImpl 
	implements AnnotatorView
{

    /**
     * Implemented as specified by the view interface.
     * @see AnnotatorView#createAnnotation(Set, AnnotationData, 
     * 										AgentEventListener)
     */
	public CallHandle createAnnotation(Set annotatedObjects, 
							AnnotationData d, AgentEventListener observer) 
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObjects, d);
		return cmd.exec(observer);
	}

    /**
     * Implemented as specified by the view interface.
     * @see AnnotatorView#loadAnnotations(Class, Set, AgentEventListener)
     */
	public CallHandle loadAnnotations(Class nodeType, Set nodeIDs, 
							AgentEventListener observer) 
	{
		BatchCallTree cmd = new AnnotationLoader(nodeType, nodeIDs, true);
        return cmd.exec(observer);
	}

    /**
     * Implemented as specified by the view interface.
     * @see AnnotatorView#updateAndCreateAnnotation(Map, Set, 
     * 									AnnotationData, AgentEventListener)
     */
	public CallHandle updateAndCreateAnnotation(Map toUpdate, Set toCreate, 
					AnnotationData data, AgentEventListener observer) 
	{
		BatchCallTree cmd = new AnnotationSaver(toUpdate, toCreate, data);
		return cmd.exec(observer);
	}

    /**
     * Implemented as specified by the view interface.
     * @see AnnotatorView#updateAnnotation(Map, AgentEventListener)
     */
	public CallHandle updateAnnotation(Map annotatedObjects, 
										AgentEventListener observer)
	{
		BatchCallTree cmd = new AnnotationSaver(annotatedObjects);
		return cmd.exec(observer);
	}

}
