/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImagesImporter 
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
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;

/** 
 * Command to import images in a container if specified.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImagesImporter 
	extends BatchCallTree
{

    /** The results of the call. */
    private Object 		results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /** Collection of objects to import. */
    private List<Object> images;
    
    /** The id of the user currently logged in. */
    private long 		 userID;
    
    /** The id of the group is logged as. */
    private long 		 groupID;
    
    /** The container the images have to be downloaded into. */
    private DataObject	container;
    
    /** 
     * Map of result, key is the file to import, value is an object or a
     * string. 
     */
    private Map<File, Object> partialResult;
    
    /** 
     * Imports the file.
     * 
     * @param f The file to import.
     */
    private void importFile(File f)
    {
    	partialResult = new HashMap<File, Object>();
    	OmeroImageService os = context.getImageService();
    	try {
    		Object ho = os.importImage(container, f, userID, groupID);
    		partialResult.put(f, ho);
		} catch (Exception e) {
			partialResult.put(f, e);
		}
    }
    
    /**
     * Creates a a {@link BatchCall} to import the images.
     * 
     * @param container The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param directory	The directory to monitor. Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final DataObject container, 
    						final File directory, final long userID, 
    						final long groupID)
    {
        return new BatchCall("Importing images: ") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
				results = os.monitor(directory.getAbsolutePath(), container, 
						userID, groupID);
            }
        };
    }
    
	 /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    { 
    	if (loadCall != null) {
    		add(loadCall); 
    	} else {
    		Iterator i = images.iterator();
    		Object ho;
    		while (i.hasNext()) {
				ho = i.next();
				if (ho instanceof File) {
					final File f = (File) ho;
					add(new BatchCall("Importing file") {
	            		public void doCall() { importFile(f); }
	            	}); 
				}
			}
    	}
    }

    /**
     * Returns the lastly retrieved thumbnail.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return  A Map whose key is the file to import and the value the 
     * 			imported object.
     */
    protected Object getPartialResult() { return partialResult; }
    
    /**
     * Returns the root node of the requested tree.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult()
    { 
    	
    	if (loadCall != null) return results; 
    	return null;
    }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
	 * 
     * @param container The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param images	The images to import. Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     */
    public ImagesImporter(DataObject container, List<Object> images, 
    					long userID, long groupID)
    {
    	if (images == null || images.size() == 0)
    		throw new IllegalArgumentException("No images to import.");
    	this.userID = userID;
    	this.groupID = groupID;
    	this.images = images;
    	this.container = container;
    	//loadCall = makeBatchCall(container, images, userID, groupID);
    }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
	 * 
     * @param container The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param directory	The directory to monitor. Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     */
    public ImagesImporter(DataObject container, File directory, 
    					long userID, long groupID)
    {
    	if (directory == null)
    		throw new IllegalArgumentException("No directory to monitor.");
    	loadCall = makeBatchCall(container, directory, userID, groupID);
    }
    
}
