/*
 * ome.formats.model.ChannelProcessor
 *
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

package ome.formats.model;

import static omero.rtypes.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

import loci.formats.IFormatReader;

import ome.formats.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Image;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the pixels sets of an IObjectContainerStore and ensures
 * that LogicalChannel containers are present in the container cache, adding
 * them if they are missing.
 *   
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class PixelsProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(PixelsProcessor.class);

    /** Container store we're currently working with. */
    private IObjectContainerStore store;

    /** First file importer **/   
    private Timestamp earliestMTime;
    
    private IFormatReader reader;


    /**
     * Processes the OMERO client side metadata store.
     * @param store OMERO metadata store to process.
     * @throws ModelException If there is an error during processing.
     */
    public void process(IObjectContainerStore store)
    throws ModelException
    {
        this.store = store;
        reader = store.getReader();

        List<IObjectContainer> containers = 
            store.getIObjectContainers(Pixels.class);
        for (IObjectContainer container : containers)
        {
            Integer imageIndex = container.indexes.get("imageIndex");
            Image image = (Image) store.getSourceObject(new LSID(Image.class, imageIndex));

            // If image is missing
            if (image == null)
            {
                LinkedHashMap<String, Integer> indexes = 
                    new LinkedHashMap<String, Integer>();
                indexes.put("imageIndex", imageIndex);
                container = store.getIObjectContainer(Image.class, indexes);
                image = (Image) container.sourceObject;
            }
            
            // If acquistionData is missing
            if (image.getAcquisitionDate() == null)
            {
                if (earliestMTime == null)
                {
                    String[] fileNameList = store.getReader().getUsedFiles();

                    long mtime = Long.MAX_VALUE;

                    for (int j = 0; j < fileNameList.length; j++) 
                    {
                        File f = new File(fileNameList[j]);

                        if (f.lastModified() < mtime)
                            mtime = f.lastModified(); 
                    }
                    earliestMTime = new Timestamp(mtime); 
                }
                image.setAcquisitionDate(rtime(earliestMTime));  
            }
            
            // Ensure that the Image name is set.
            String userSpecifiedName = store.getUserSpecifiedImageName();
            String saveName = "";

            if (image.getName() == null ||
                    image.getName().getValue().trim().length() == 0)
            {
                saveName = userSpecifiedName;
                
                if (reader.getSeriesCount() > 1)
                {
                    saveName += " [" + imageIndex + "]";
                }
            } 
            else
            {
                saveName = image.getName().getValue();
            } 

            image.setName(rstring(saveName));
        }
    }
}
