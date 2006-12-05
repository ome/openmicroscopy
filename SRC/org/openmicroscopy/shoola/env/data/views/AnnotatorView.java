/*
 * org.openmicroscopy.shoola.env.data.views.AnnotatorView 
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
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.AnnotationData;

/** 
 * Provides methods to support annotation.
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
public interface AnnotatorView
	extends DataServicesView
{

    /**
     * Retrieves all the annotations made by the current user linked to the 
     * specified nodes.
     * 
     * @param nodeType  The type of the node. One out of the following types:
     *                  <code>DatasetData, ImageData</code>.      
     * @param nodeIDs    The id of the node.
     * @param observer  Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadAnnotations(Class nodeType, Set nodeIDs,
                                        AgentEventListener observer);
    
    /** 
     * Creates an annotation of the specified type for the specified node.
     * 
     * @param annotatedObjects  The <code>DataObject</code>s to annotate. 
     *                          Mustn't be <code>null</code>.
     * @param data              The annotation to create.
     * @param observer          Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle createAnnotation(Set annotatedObjects, 
    								AnnotationData data,  
                                        AgentEventListener observer);
    
    /**
     * Updates the specified annotation.
     * 
     * @param annotatedObjects  The annotated <code>DataObject</code>s. 
     *                          Mustn't be <code>null</code>.
     * @param observer          Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle updateAnnotation(Map annotatedObjects,
                                        AgentEventListener observer);
    
    /**
     * Updates and creates the specified annotation.
     * 
     * @param toUpdate  		The annotated <code>DataObject</code>s.
     *                          Mustn't be <code>null</code>.
	 * @param toCreate  		The annotated <code>DataObject</code>s.
     *                          Mustn't be <code>null</code>.                          
     * @param data              The Annotation object to update.
     *                          Mustn't be <code>null</code>.
     * @param observer          Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle updateAndCreateAnnotation(Map toUpdate, Set toCreate, 
    						AnnotationData data, AgentEventListener observer);
    
}
