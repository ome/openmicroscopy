/*
* org.openmicroscopy.shoola.env.data.views.calls.WorkflowHandler
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
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
import pojos.WorkflowData;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Loads the workflows.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class WorkflowHandler
	extends BatchCallTree
{
	
	/** The result of the call. */
    private Object				result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   		loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve or store the workflows..
     * 
     * @param userID The id of the user to retrieve workflows for.. 
     * @param index  One of the constants defined by the script.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCall(final long userID)
    {
    	return new BatchCall("Retrieve Workflows") {
    		public void doCall() throws Exception
    		{
    			OmeroImageService os = context.getImageService();
 	    		result = os.retrieveWorkflows(userID);
	    	}
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve or store the workflows..
     * 
     * @param userID The id of the user to retrieve workflows for.. 
     * @param index  One of the constants defined by the script.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCall(final List<WorkflowData> workflows, 
    		final long userID)
    {
    	return new BatchCall("Run the script") {
    		public void doCall() throws Exception
    		{
    			OmeroImageService os = context.getImageService();
 	    		result = os.storeWorkflows(workflows, userID);
	    	}
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Map</code>.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param userID The userID of the workflows to retrieve.
     */
    public WorkflowHandler(long userID)
    {
    	if (userID < 0) 
    		throw new IllegalArgumentException("invalid userID specified."); 
		loadCall = makeCall(userID);
    }

    /**
     * Creates a new instance.
     * 
     * @param workflows The workflows to store.
     * @param userID The userID to store under.
     */
    public WorkflowHandler(List<WorkflowData> workflows, long userID)
    {
      	if (userID < 0) 
    		throw new IllegalArgumentException("invalid userID specified."); 
      	if (workflows == null || workflows.size() == 0) 
    		throw new IllegalArgumentException("Invalid workflows specified."); 
		loadCall = makeCall(workflows, userID);
    }
    
}