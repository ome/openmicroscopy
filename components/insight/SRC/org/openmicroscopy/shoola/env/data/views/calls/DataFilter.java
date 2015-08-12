/*
 * org.openmicroscopy.shoola.env.data.views.calls.DataFilter 
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
package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Filters the nodes by annotation.
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
public class DataFilter 
	extends BatchCallTree
{

    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /** The security context.*/
	private SecurityContext ctx;
	
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param annotationType 	The type of the object.
     * @param nodeType			The type of object to filter.
     * @param ids				The collection of id of the object.
     * @param annotated				The collection of terms.
     * @param userID			The id of the user who tagged the object or 
     * 							<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall filterBy(final Class annotationType, final Class nodeType, 
    						final List<Long> ids, final boolean annotated,
    						final long userID)
    {
        return new BatchCall("Filtering annotated data") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.filterByAnnotated(ctx, nodeType, ids,
                	annotationType, annotated, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param annotationType 	The type of the object.
     * @param nodeType			The type of object to filter.
     * @param ids				The collection of id of the object.
     * @param terms				The collection of terms.
     * @param userID			The id of the user who tagged the object or 
     * 							<code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall filterBy(final Class annotationType, final Class nodeType,
    						final List<Long> ids, final List<String> terms, 
    						final long userID)
    {
        return new BatchCall("Filtering annotated data") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.filterByAnnotation(ctx, nodeType, ids,
                	annotationType, terms, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the ratings related to the object
     * identified by the class and the id.
     * 
     * @param nodeType	The type of objects to filter.	
     * @param ids		The collection of object ids.
     * @param filter	The filtering context.
     * @param userID	The id of the user or <code>-1</code> if the id 
     * 					is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall filterBy(final Class nodeType, final List<Long> ids, 
    						final FilterContext filter, final long userID)
    {
        return new BatchCall("Filtering annotated data") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
                result = os.filterByAnnotation(ctx, 
                		nodeType, ids, filter, userID);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx The security context.
     * @param annotationType The type of annotations to fetch.
     * @param nodeType The type of objects to filter.	
     * @param nodeIds The collection of object ids.
     * @param annotated Pass <code>true</code> to retrieve the 
	 *                  annotated nodes, <code>false</code> otherwise.
     * @param userID The id of the user or <code>-1</code> if the id 
     *               is not specified.
     */
    public DataFilter(SecurityContext ctx, Class annotationType,
    	Class nodeType, List<Long> nodeIds, boolean annotated, long userID)
    {
    	if (annotationType == null)
    		throw new IllegalArgumentException("Annotation type not valid.");
    	if (nodeType == null)
    		throw new IllegalArgumentException("Node type not valid.");
    	this.ctx = ctx;
    	loadCall = filterBy(annotationType, nodeType, nodeIds, annotated, 
    			          userID);
    }
    
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx The security context.
     * @param annotationType The type of annotations to fetch.
     * @param nodeType The type of objects to filter.	
     * @param nodeIds The collection of object ids.
     * @param terms The collection of terms.
     * @param userID The id of the user or <code>-1</code> if the id 
     * is not specified.
     */
    public DataFilter(SecurityContext ctx, Class annotationType, Class nodeType,
    	List<Long> nodeIds, List<String> terms, long userID)
    {
    	if (annotationType == null)
    		throw new IllegalArgumentException("Annotation type not valid.");
    	if (nodeType == null)
    		throw new IllegalArgumentException("Node type not valid.");
    	this.ctx = ctx;
    	loadCall = filterBy(annotationType, nodeType, nodeIds, terms, userID);
    }
    
    /**
     * Creates a new instance. Builds the call corresponding to the passed
     * index, throws an {@link IllegalArgumentException} if the index is not
     * supported.
     * 
     * @param ctx The security context.
     * @param nodeType	The type of objects to filter.	
     * @param nodeIds	The collection of object ids.
     * @param context	The filtering context.
     * @param userID	The id of the user or <code>-1</code> if the id 
     * 					is not specified.
     */
    public DataFilter(SecurityContext ctx, Class nodeType, List<Long> nodeIds,
    	FilterContext context, long userID)
    {
    	if (nodeType == null)
    		throw new IllegalArgumentException("Node type not valid.");
    	this.ctx = ctx;
    	loadCall = filterBy(nodeType, nodeIds, context, userID);
    }
    
}
