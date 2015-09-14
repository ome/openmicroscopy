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

package ome.formats.model;

import static omero.rtypes.rstring;

import static ome.formats.model.UnitsFactory.makeLength;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger log = LoggerFactory.getLogger(PixelsProcessor.class);

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
			pixels.setPhysicalSizeX(makeLength(physicalPixelSizes[0],
			        UnitsFactory.Pixels_PhysicalSizeX));
		}
		if (physicalPixelSizes[1] != null
			&& pixels.getPhysicalSizeY() == null)
		{
			pixels.setPhysicalSizeY(makeLength(physicalPixelSizes[1],
			        UnitsFactory.Pixels_PhysicalSizeY));
		}
		if (physicalPixelSizes[2] != null
			&& pixels.getPhysicalSizeZ() == null)
		{
			pixels.setPhysicalSizeZ(makeLength(physicalPixelSizes[2],
			        UnitsFactory.Pixels_PhysicalSizeZ));
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

            /*
             * Acquisition dates returned to being nullable, but this code may have value following
             * TODO deeper review of why it was added in the first place.

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
            */

            // Ensure that the Image name is set
            String userSpecifiedName = store.getUserSpecifiedName();
            if (userSpecifiedName != null) {
                userSpecifiedName = userSpecifiedName.trim();
                if (userSpecifiedName.isEmpty()) {
                    userSpecifiedName = null;
                }
            }
            String saveName = "";
            String imageName;
            if (image.getName() != null && image.getName().getValue() != null) {
                imageName = image.getName().getValue().trim();
                if (imageName.isEmpty()) {
                    imageName = null;
                }
            } else {
                imageName = null;
            }
            if (userSpecifiedName != null) {
                saveName = userSpecifiedName;

                if (reader.getSeriesCount() > 1) {
                    if (imageName == null) {
                        imageName = Integer.toString(imageIndex);
                    }
                    saveName += " [" + imageName + "]";
                }
            } else {
                saveName = imageName;
            }
            if (saveName != null && saveName.length() > 255) {
                saveName = '…' + saveName.substring(saveName.length() - 254);
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
