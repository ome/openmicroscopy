/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

import java.io.File;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;

/** 
 * Command to saved a file back to the server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ArchivedFilesSaver 	
	extends BatchCallTree
{

	/** Loads the specified annotations. */
    private BatchCall   loadCall;
    
    /** The result of the call. */
    private Object		result;
    
    /**
     * Creates a {@link BatchCall} to retrieve the archived files.
     * 
     * @param ctx The security context.
     * @param fileAnnotation The annotation hosting the previous info.
     * @param file The file to save.
     * @param index Index used to determine the name space.
     *  @param linkTo The <code>DataObject</code> to link the annotation to.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    	final FileAnnotationData fileAnnotation, final File file,
    	final int index, final DataObject linkTo)
    {
        return new BatchCall("Loading annotation") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                result = os.archivedFile(ctx, fileAnnotation, file, index,
                		linkTo);
            }
        };
    }

    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the collection of archives files.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param fileAnnotation The annotation hosting the previous info.
     * @param file The file to save.
     * @param index Index used to determine the name space.
     * @param linkTo The <code>DataObject</code> to link the annotation to.
     */
    public ArchivedFilesSaver(SecurityContext ctx,
    	FileAnnotationData fileAnnotation, File file, int  index, 
    	DataObject linkTo)
    {
    	loadCall = makeBatchCall(ctx, fileAnnotation, file, index, linkTo);
    }

}
