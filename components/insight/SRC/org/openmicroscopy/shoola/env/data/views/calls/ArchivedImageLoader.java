/*
 * org.openmicroscopy.shoola.env.data.views.calls.ArchivedImageLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries
import com.google.common.io.Files;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.util.file.IOUtil;


/** 
 * Command to load the archived image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ArchivedImageLoader 
	extends BatchCallTree
{

	/** The result of the query. */
    private Object result;
    
    /** Loads the specified tree. */
    private BatchCall loadCall;

    /**Flag indicating to override or not the existing file if it exists.*/
    private boolean override;

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
        //First check that the file exists
        File[] files = folder.listFiles();
        int count = 0;
        String fname = f.getName();
        String extension = FilenameUtils.getExtension(fname);
        String baseName = FilenameUtils.getBaseName(
                FilenameUtils.removeExtension(fname));
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String v = files[i].getName();
                String value = baseName+"_("+count+")."+extension;
                if (v.equals(fname) || v.equals(value)) {
                    count++;
                }
            }
        }
        if (count > 0) { //rename the file first.
            File to;
            if (override) {
                to = new File(folder, f.getName());
                to.delete();
            } else {
                to = new File(f.getParentFile(),
                        baseName+"_("+count+")."+extension);
                FileUtils.copyFile(f, to);
                f = to;
            }
        }
        FileUtils.moveFileToDirectory(f, folder, false);
        return new File(folder, f.getName());
    }

    /**
     * Creates a {@link BatchCall} to load the image.
     * 
     * @param ctx The security context.
     * @param imageIDs The IDs of the images.
     * @param name The name of the image.
     * @param folder The path to the folder where to save the image.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    		final List<Long> imageIDs, final File folder)
    {
        return new BatchCall("Download the files. ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                File tmpFolder = null;
                try {
                    tmpFolder = Files.createTempDir();
                    
                    List<File> files = new ArrayList<File>();
                    
                    for (Long imageID : imageIDs) {
                        Map<Boolean, Object> r = os.getArchivedImage(ctx,
                                tmpFolder, imageID);
                        files.addAll((List<File>) r.get(Boolean.TRUE));
                    }
                    
                    result = new HashMap<Boolean, List<File>>();
                    
                    if (!CollectionUtils.isEmpty(files)) {
                        File f = IOUtil.zipDirectory(tmpFolder, false);
                        // rename the zip
                        String baseName = FilenameUtils
                                .getBaseName(FilenameUtils
                                        .removeExtension(folder.getName()));
                        File to = new File(f.getParentFile(), baseName
                                + "."
                                + FilenameUtils.getExtension(f.getName()));
                        Files.move(f, to);
                        f = copyFile(to, folder.getParentFile());
                        ((Map<Boolean, List<File>>)result).put(Boolean.TRUE, Arrays.asList(f));
                    }
                    
                } catch (Exception e) {
                    throw new Exception(e);
                } finally {
                    if (tmpFolder != null)
                        FileUtils.deleteDirectory(tmpFolder);
                }
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
     * @param override Flag indicating to override the existing file if it
     *                 exists, <code>false</code> otherwise.
     */
    public ArchivedImageLoader(SecurityContext ctx, List<Long> imageIDs,
    		File folderPath, boolean override)
    {
    	if (CollectionUtils.isEmpty(imageIDs))
    		 throw new IllegalArgumentException("No image IDs provided.");
    	this.override = override;
        loadCall = makeBatchCall(ctx, imageIDs, folderPath);
    }
}
