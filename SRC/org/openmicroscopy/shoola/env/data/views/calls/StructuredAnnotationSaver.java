/*
 * org.openmicroscopy.shoola.env.data.views.calls.StructuredAnnotationSaver 
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.AnnotationData;
import pojos.DataObject;

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
public class StructuredAnnotationSaver 
	extends BatchCallTree
{

    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the users who viewed 
     * the specified set of pixels and also retrieve the rating associated
     * to that set.
     * 
     * @param data		The data objects to handle.
     * @param toAdd		The annotations to add.
     * @param toRemove	The annotations to remove.
     * @param metadata	The acquisition metadata.
     * @param userID	The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadCall(final Collection<DataObject> data, final
    		List<AnnotationData> toAdd, final List<AnnotationData> toRemove,
    		final List<Object> metadata, final long userID)
    {
        return new BatchCall("Saving") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	if (metadata != null) {
            		Iterator<Object> i = metadata.iterator();
            		while (i.hasNext()) {
						os.saveAcquisitionData(i.next()) ;
					}
            	}
            	result = os.saveData(data, toAdd, toRemove, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the users who viewed 
     * the specified set of pixels and also retrieve the rating associated
     * to that set.
     * 
     * @param data		The data objects to handle.
     * @param toAdd		The annotations to add.
     * @param toRemove	The annotations to remove.
     * @param metadata	The acquisition metadata.
     * @param userID	The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadBatchCall(final Collection<DataObject> data, final
    		List<AnnotationData> toAdd, final List<AnnotationData> toRemove,
    		final List<Object> metadata, final long userID)
    {
        return new BatchCall("Saving") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	if (metadata != null) {
            		Iterator<Object> i = metadata.iterator();
            		while (i.hasNext()) {
						os.saveAcquisitionData(i.next()) ;
					}
            	}
            	result = os.saveBatchData(data, toAdd, toRemove, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the users who viewed 
     * the specified set of pixels and also retrieve the rating associated
     * to that set.
     * 
     * @param data		The data objects to handle.
     * @param toAdd		The annotations to add.
     * @param toRemove	The annotations to remove.
     * @param metadata	The acquisition metadata.
     * @param userID	The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadBatchCall(final TimeRefObject data, final
    		List<AnnotationData> toAdd, final List<AnnotationData> toRemove,
    		final List<Object> metadata, final long userID)
    {
        return new BatchCall("Saving") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	if (metadata != null) {
            		Iterator<Object> i = metadata.iterator();
            		while (i.hasNext()) {
						os.saveAcquisitionData(i.next()) ;
					}
            	}
            	result = os.saveBatchData(data, toAdd, toRemove, userID);
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
     * @param data		The data objects to handle.
     * @param toAdd		The annotations to add.
     * @param toRemove	The annotations to remove.
     * @param metadata	The acquisition metadata.
     * @param userID	The id of the user.
     * @param batch		Pass <code>true</code> to indicate that it is a batch
     * 					annotation, <code>false</code> otherwise.
     */
    public StructuredAnnotationSaver(Collection<DataObject> data, 
    		List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
    		List<Object> metadata, long userID, boolean batch)
    {
    	if (data == null)
    		throw new IllegalArgumentException("No object to save.");
    	if (batch)
    		loadCall = loadBatchCall(data, toAdd, toRemove, metadata, userID);
    	else 
    		loadCall = loadCall(data, toAdd, toRemove, metadata, userID);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param timeRefObject	The object hosting the time interval.
     * @param toAdd			The annotations to add.
     * @param toRemove		The annotations to remove.
     * @param userID		The id of the user.
     */
    public StructuredAnnotationSaver(TimeRefObject timeRefObject, 
    		List<AnnotationData> toAdd, List<AnnotationData> toRemove, long
    		userID)
    {
    	if (timeRefObject == null)
    		throw new IllegalArgumentException("No time period sepecified.");
    	loadCall = loadBatchCall(timeRefObject, toAdd, toRemove, null, userID);
    }
    
}
