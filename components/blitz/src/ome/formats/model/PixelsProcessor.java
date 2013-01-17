/*
 * ome.formats.model.PixelsProcessor
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

import static omero.rtypes.rdouble;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import loci.formats.IFormatReader;
import ome.formats.Index;
import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Annotation;
import omero.model.Image;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes the pixels sets of an IObjectContainerStore and ensures
 * that the physical pixel dimensions are updated if they were specified by
 * the user. If Image containers are present, Image.acquisitionDate is filled
 * out and that the Image name and description match that which was specified
 * by the user if the if provided either.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class PixelsProcessor implements ModelProcessor
{
    /** Logger for this class */
    private Log log = LogFactory.getLog(PixelsProcessor.class);

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
        reader = store.getReader();

        List<IObjectContainer> containers =
            store.getIObjectContainers(Pixels.class);
        for (IObjectContainer container : containers)
        {
            Integer imageIndex = container.indexes.get(Index.IMAGE_INDEX.getValue());
            LSID imageLSID = new LSID(Image.class, imageIndex);
            Image image = (Image) store.getSourceObject(imageLSID);
            Pixels pixels = (Pixels) container.sourceObject;
            Double[] physicalPixelSizes =
		store.getUserSpecifiedPhysicalPixelSizes();
            List<Annotation> annotations = store.getUserSpecifiedAnnotations();
            if (annotations == null)
            {
                annotations = new ArrayList<Annotation>();
            }

            // If we have user specified annotations
            Map<LSID, IObjectContainer> containerCache =
		store.getContainerCache();
            for (int i = 0; i < annotations.size(); i++)
            {
		LSID annotationLSID = new LSID("UserSpecifiedAnnotation:" + i);
		IObjectContainer annotationContainer = new IObjectContainer();
		annotationContainer.LSID = annotationLSID.toString();
		annotationContainer.sourceObject = annotations.get(i);
		containerCache.put(annotationLSID, annotationContainer);
		store.addReference(imageLSID, annotationLSID);
            }

            // If we have user specified physical pixel sizes
            if (physicalPixelSizes != null)
            {
		if (physicalPixelSizes[0] != null
			&& pixels.getPhysicalSizeX() == null)
		{
			pixels.setPhysicalSizeX(rdouble(physicalPixelSizes[0]));
		}
		if (physicalPixelSizes[1] != null
			&& pixels.getPhysicalSizeY() == null)
		{
			pixels.setPhysicalSizeY(rdouble(physicalPixelSizes[1]));
		}
		if (physicalPixelSizes[2] != null
			&& pixels.getPhysicalSizeZ() == null)
		{
			pixels.setPhysicalSizeZ(rdouble(physicalPixelSizes[2]));
		}
            }

            // If image is missing
            if (image == null)
            {
                LinkedHashMap<Index, Integer> indexes =
                    new LinkedHashMap<Index, Integer>();
                indexes.put(Index.IMAGE_INDEX, imageIndex);
                container = store.getIObjectContainer(Image.class, indexes);
                image = (Image) container.sourceObject;
            }

            // If acquistionDate is missing
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

            // Ensure that the Image name is set
            String userSpecifiedName = store.getUserSpecifiedName();
            String saveName = "";
            if (image.getName() == null
                || image.getName().getValue().trim().length() == 0
                || userSpecifiedName != null)
            {
                saveName = userSpecifiedName;

                if (reader.getSeriesCount() > 1)
                {
                    if (image.getName() == null)
                    {
                        saveName += " [" + imageIndex + "]";
                    }
                    else if (image.getName().getValue().trim().length() != 0)
                    {
                        saveName += " [" + image.getName().getValue() + "]";
                    }
                    else
                    {
                        saveName += " [" + imageIndex + "]";
                    }
                }
            }
            else
            {
                saveName = image.getName().getValue();
            }
            image.setName(rstring(saveName));

            // Set the Image description if one was supplied by the user
            String userSpecifiedDescription = store.getUserSpecifiedDescription();
            if (userSpecifiedDescription != null)
            {
		image.setDescription(rstring(userSpecifiedDescription));
            }
        }
    }
}
