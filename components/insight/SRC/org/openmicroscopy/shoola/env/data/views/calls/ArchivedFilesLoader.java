/*
 * org.openmicroscopy.shoola.env.data.views.calls.ArchivedFilesLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

import java.util.Collection;
import java.util.Iterator;

import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;


/** 
 * Command to load the archived files for a given set of pixels.
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
public class ArchivedFilesLoader
	extends BatchCallTree
{
	
    /** The result of the call. */
    private Object result;

    /** The collection of files to download. */
    private Collection<Long> pixelsID;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Downloads the original file.
     * 
     * @param id The id of the pixel set.
     */
    private void downloadFile(long id) 
    {
    	try {
    		OmeroDataService os = context.getDataService();
    		result = os.getOriginalFiles(ctx, id);
        } catch (Exception e) {
        	context.getLogger().error(this, 
        			"Cannot retrieve download the file: "+e.getMessage());
        }
    }
    
    /**
     * Adds the call per pixels' id to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    { 
    	Iterator<Long> i = pixelsID.iterator();
    	String description; 
    	Long id;
    	description = "Downloading original files.";
		while (i.hasNext()) {
			id = i.next();
			final Long pix = id;
			add(new BatchCall(description) {
        		public void doCall() { downloadFile(pix); }
        	});  
		}
    }

    /**
     * Returns the lastly downloaded files.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return See above
     */
    protected Object getPartialResult() { return result; }
    
    /**
     * Returns <code>null</code> as there's no final result.
     * In fact, files are progressively delivered with 
     * feedback events. 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param pixelsID	The collection of the pixels set.
     */
    public ArchivedFilesLoader(SecurityContext ctx,
    		Collection<Long> pixelsID)
    {
    	this.pixelsID = pixelsID;
    	this.ctx = ctx;
    }
    
}
