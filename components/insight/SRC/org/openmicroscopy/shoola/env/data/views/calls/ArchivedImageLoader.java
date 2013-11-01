/*
 * org.openmicroscopy.shoola.env.data.views.calls.ArchivedImageLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.util.file.IOUtil;

import com.google.common.io.Files;

/** 
 * Command to load the archived image.
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
public class ArchivedImageLoader 
	extends BatchCallTree
{

	/** The result of the query. */
    private Object      result;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;

    /**
     * Copies the specified file to the folder.
     * 
     * @param f The file to copy.
     * @param folder The destination folder.
     * @return The destination file.
     * @throws Exception Thrown if an error occurred during the copy.
     */
    private File copyFile(File f, File folder)
            throws Exception
    {
        FileUtils.copyFileToDirectory(f, folder, true);
        String name = f.getName();
        f.delete();
        return new File(folder, name);
    }

    /**
     * Creates a {@link BatchCall} to load the image.
     * 
     * @param ctx The security context.
     * @param imageID The ID of the image.
     * @param name The name of the image.
     * @param folder The path to the folder where to save the image.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    		final long imageID, final String name, final File folder)
    {
        return new BatchCall("Download the files. ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                //Create a tmp folder.
                File tmpFolder = Files.createTempDir();
                Map<Boolean, Object> r =
                        os.getArchivedImage(ctx, tmpFolder, imageID);
                List<File> files = (List<File>) r.get(Boolean.TRUE);
                //format the result
                if (!CollectionUtils.isEmpty(files)) {
                    File f;
                    //Copy the file to the destination folder.
                    if (files.size() == 1) {
                        f = files.get(0);
                        //copy from tmp to destination folder.
                        r.put(Boolean.TRUE, Arrays.asList(copyFile(f, folder)));
                    } else {
                        //zip the directory
                        f = IOUtil.zipDirectory(tmpFolder, false);
                        //move the zip
                        f = copyFile(f, folder);
                        File to = new File(f.getParentFile(),
                                name+"."+FilenameUtils.getExtension(
                                        f.getName()));
                        Files.move(f, to);
                        r.put(Boolean.TRUE, Arrays.asList(to));
                    }
                }
                result = r;
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Loads the archived images.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
	 * @param ctx The security context.
     * @param imageID The Id of the image.
     * @param name The name of the image.
     * @param folderPath The location where to download the archived image.
     */
    public ArchivedImageLoader(SecurityContext ctx, long imageID, String name,
    		File folderPath)
    {
    	if (imageID < 0)
    		 throw new IllegalArgumentException("Image's ID not valid.");
        loadCall = makeBatchCall(ctx, imageID, name, folderPath);
    }
}
