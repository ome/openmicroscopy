/*
 * org.openmicroscopy.shoola.env.data.views.calls.StructuredDataLoader 
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

//Third-party libraries

//Application-internal dependencies
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * 
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
public class StructuredDataLoader 
	extends BatchCallTree
{

	/** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the data related the image.
     * Data created by the specified user if the userID is not <code>-1</code>.
     * 
     * @param id		The id of the image.
     * @param userID	The id of the user or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadDataForImage(final long id, final long userID)
    {
        return new BatchCall("Loading Data for image ID: "+id) {
            public void doCall() throws Exception
            {
            	
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the data related the dataset.
     * Data created by the specified user if the userID is not <code>-1</code>.
     * 
     * @param id		The id of the dataset.
     * @param userID	The id of the user or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadDataForDataset(final long id, final long userID)
    {
        return new BatchCall("Loading Data for dataset ID: "+id) {
            public void doCall() throws Exception
            {
            	
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the data related the project.
     * Data created by the specified user if the userID is not <code>-1</code>.
     * 
     * @param id		The id of the project.
     * @param userID	The id of the user or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadDataForProject(final long id, final long userID)
    {
    	return new BatchCall("Loading Data for project ID: "+id) {
            public void doCall() throws Exception
            {
            	
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
     * Creates a new instance.
     * 
     * @param dataObject
     * @param userID
     */
    public StructuredDataLoader(Object dataObject, long userID)
    {
    	Class type = dataObject.getClass();
    	if (ImageData.class.equals(type))
    		loadCall = loadDataForImage(((ImageData) dataObject).getId(), 
    									userID);
    	else if (ProjectData.class.equals(type))
    		loadCall = loadDataForProject(((ProjectData) dataObject).getId(), 
											userID);
    	else if (DatasetData.class.equals(type))
    		loadCall = loadDataForDataset(((DatasetData) dataObject).getId(), 
					userID);
    }
    
}
