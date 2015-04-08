/*
 * org.openmicroscopy.shoola.env.data.views.calls.TabularDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Creates a batch call to load the tabular data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TabularDataLoader 
	extends BatchCallTree
{

	/** The result of the call. */
    private Object result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;
    
    /**
     * Creates a {@link BatchCall} to load the tabular data.
     * 
     * @param parameters The parameters to handle.
     * @param userID The identifier of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCall(final SecurityContext ctx,
    		final TableParameters parameters, final long userID)
    {
    	return new BatchCall("Load tabular data") {
    		public void doCall() throws Exception
    		{
    			OmeroMetadataService os = context.getMetadataService();
    			result = os.loadTabularData(ctx, parameters, userID);
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
     * @param ctx The security context.
     * @param parameters The parameters to handle.
     * @param userID The identifier of the user.
     */
	public TabularDataLoader(SecurityContext ctx,
			TableParameters parameters, long userID)
	{
		if (parameters == null) 
    		throw new IllegalArgumentException("No data to load."); 
		loadCall = makeCall(ctx, parameters, userID);
	}
	
}
