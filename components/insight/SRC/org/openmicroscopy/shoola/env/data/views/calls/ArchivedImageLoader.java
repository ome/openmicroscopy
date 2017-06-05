/*
 * org.openmicroscopy.shoola.env.data.views.calls.ArchivedImageLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;
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

    /** Flag for zipping the downloaded images */
    private boolean zip = true;
    
    /** Flag for preserving the original folder structure */
    private boolean keepOriginalPaths = false;
    
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
     * @param objects The objects to download the original image files for.
     * @param name The name of the image.
     * @param folder The path to the folder where to save the image.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    		final List<DataObject> objects, final File folder)
    {
        return new BatchCall("Download the files. ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                
                List<Long> imageIDs = new ArrayList<Long>();
                if (objects.get(0) instanceof ImageData) {
                    for (DataObject obj : objects)
                        imageIDs.add(obj.getId());
                }
                if (objects.get(0) instanceof PlateData) {
                    List<Long> filesetIds = new ArrayList<Long>();
                    for (DataObject obj : objects) {
                        Collection wells = os.loadPlateWells(ctx, obj.getId(),
                                -1, context.getGateway().getLoggedInUser()
                                        .getId());
                        for (Object well : wells) {
                            for (WellSampleData ws : ((WellData) well)
                                    .getWellSamples()) {
                                if (!filesetIds.contains(ws.getImage()
                                        .getFilesetId())) {
                                    filesetIds
                                            .add(ws.getImage().getFilesetId());
                                    imageIDs.add(ws.getImage().getId());
                                }
                            }
                        }
                    }
                }
                
                File tmpFolder = null;
                try {
                    if(zip)
                        tmpFolder = Files.createTempDir();
                    else
                        tmpFolder = folder;
                    
                    List<File> files = new ArrayList<File>();
                    
                    for (Long imageID : imageIDs) {
                        Map<Boolean, Object> r = os.getArchivedImage(ctx,
                                tmpFolder, imageID, keepOriginalPaths);
                        files.addAll((List<File>) r.get(Boolean.TRUE));
                    }
                    
                    result = new HashMap<Boolean, List<File>>();
                    
                    if(CollectionUtils.isEmpty(files))
                        return;
                    
                    if (zip) {
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
                    else {
                        ((Map<Boolean, List<File>>)result).put(Boolean.TRUE, files);
                    }
                    
                } catch (Exception e) {
                    throw new Exception(e);
                } finally {
                    if (zip && tmpFolder != null)
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
     * @param objects The objects for which to download the original image files.
     * @param folderPath The location where to download the archived image.
     * @param override Flag indicating to override the existing file if it
     *                 exists, <code>false</code> otherwise.
     */
    public ArchivedImageLoader(SecurityContext ctx, List<DataObject> objects,
    		File folderPath, boolean override)
    {
    	if (CollectionUtils.isEmpty(objects))
    		 throw new IllegalArgumentException("No objects provided.");
    	this.override = override;
        loadCall = makeBatchCall(ctx, objects, folderPath);
    }
    
    /**
     * Loads the archived images.
     * If bad arguments are passed, we throw a runtime
     * exception so to fail early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param objects The objects for which to download the original image files.
     * @param folderPath The location where to download the archived image.
     * @param override Flag indicating to override the existing file if it
     *                 exists, <code>false</code> otherwise.
     * @param zip Pass <code>true</code> to create a zip file
     * @param keepOriginalPaths Pass <code>true</code> to preserve the original folder structure
     */
    public ArchivedImageLoader(SecurityContext ctx, List<DataObject> objects,
            File folderPath, boolean override, boolean zip, boolean keepOriginalPaths)
    {
        if (CollectionUtils.isEmpty(objects))
             throw new IllegalArgumentException("No objects provided.");
        this.override = override;
        this.zip = zip;
        this.keepOriginalPaths = keepOriginalPaths;
        loadCall = makeBatchCall(ctx, objects, folderPath);
    }
}
