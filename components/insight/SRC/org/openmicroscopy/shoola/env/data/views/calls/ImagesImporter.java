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
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

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
     
    /** The id of the user currently logged in. */
    private long 		 			userID;
    
    /** The id of the group is logged as. */
    private long 		 			groupID;
    
    /** 
     * Map of result, key is the file to import, value is an object or a
     * string. 
     */
    private Map<File, Object> 		partialResult;
    
    /** The object hosting the information for the import. */
    private ImportableObject object;
    
    /** 
     * Imports the file.
     * 
     * @param ImportableFile The file to import.
     * @param Pass <code>true</code> to close the import,
     * 		<code>false</code> otherwise.
     */
    private void importFile(ImportableFile importable, boolean close)
    {
    	partialResult = new HashMap<File, Object>();
    	OmeroImageService os = context.getImageService();
    	try {
    		partialResult.put(importable.getFile(), 
    				os.importFile(object, importable, userID, groupID, close));
		} catch (Exception e) {
			partialResult.put(importable.getFile(), e);
		}
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    { 
    	ImportableFile io;
    	List<ImportableFile> files = object.getFiles();
		Iterator<ImportableFile> i = files.iterator();
		int index = 0;
		int n = files.size()-1;
		while (i.hasNext()) {
			io = (ImportableFile) i.next();
			final ImportableFile f = io;
			final boolean b = index == n;
			index++;
			add(new BatchCall("Importing file") {
        		public void doCall() { importFile(f, b); }
        	}); 
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
    	return null;
    }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
	 * 
     * @param context  The object hosting the information for the import. 
     * 					Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     */
    public ImagesImporter(ImportableObject object, long userID, long groupID)
    {
    	if (object == null || object.getFiles() == null ||
    			object.getFiles().size() == 0)
    		throw new IllegalArgumentException("No Files to import.");
    	this.userID = userID;
    	this.groupID = groupID;
    	this.object = object;
    }
    
}
