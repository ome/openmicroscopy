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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
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
 * @since 3.0-Beta4
 */
public class ImagesImporter
    extends BatchCallTree
{

    private static boolean isOffline(Registry context) {
        Boolean offline = (Boolean)
                context.lookup(LookupNames.OFFLINE_IMPORT_ENABLED);
        return offline != null && offline;
    }

    /**
     * Factory method to build an import task depending on weather this client
     * has been configure to offload imports to the OMERO Import Proxy.
     *
     * @param context the configuration store.
     * @param target the data to import.
     * @return depending on configuration, either an online importer that calls
     * OMERO directly to import each image given in the target {@link
     * ImportableObject} or one that offloads the imports to a proxy that will
     * carry them out in a separate background process.
     */
    public static BatchCallTree newImporter(Registry context,
                                            ImportableObject target) {
        if (context == null)
            throw new NullPointerException("No registry.");
        if (target == null || CollectionUtils.isEmpty(target.getFiles()))
            throw new IllegalArgumentException("No files to import.");

        return isOffline(context) ? new OfflineImagesImporter(target) :
                                    new ImagesImporter(target);
    }

    /** 
     * Map of result, key is the file to import, value is an object or a
     * string.
     */
    private Map<ImportableFile, Object> partialResult;

    /** The object hosting the information for the import. */
    private ImportableObject object;

    /**
     * Imports the file.
     *
     * @param importable The file to import.
     * @param close <code>true</code> to close the import,
     *        <code>false</code> otherwise.
     */
    private void importFile(ImportableFile importable, boolean close)
    {
        partialResult = new HashMap<>();
        OmeroImageService os = context.getImageService();
        try {
            partialResult.put(importable,
                    os.importFile(object, importable, close));
        } catch (Exception e) {
            partialResult.put(importable, e);
        }
    }

    /**
     * Adds the {@link #importFile} to the computation tree.
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
            io = i.next();
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
     * @return A Map whose key is the file to import and the value the
     *         imported object.
     */
    protected Object getPartialResult() { return partialResult; }

    /**
     * Returns the root node of the requested tree.
     *
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }

    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
     * exception so to fail early and in the call.
     *
     * @param object The object hosting all import information.
     */
    private ImagesImporter(ImportableObject object)
    {
        this.object = object;
    }

}
