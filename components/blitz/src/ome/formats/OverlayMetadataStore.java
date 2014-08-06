/*
 * ome.formats.OverlayMetadataStore
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
 *
 */
package ome.formats;

import static omero.rtypes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.formats.meta.DummyMetadata;
import ome.util.LSID;
import ome.xml.model.primitives.Color;
import omero.ServerError;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Mask;
import omero.model.MaskI;
import omero.model.Pixels;
import omero.model.Roi;
import omero.model.RoiI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side metadata store implementation that only deals with overlays. At
 * the moment this is restricted to <b>mask</b> based ROI inserted in OMERO
 * tables.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class OverlayMetadataStore extends DummyMetadata
{
    /** Logger for this class. */
    private static Logger log = LoggerFactory.getLogger(OverlayMetadataStore.class);

    private List<Pixels> pixelsList;

    private Map<LSID, Roi> roiMap = new HashMap<LSID, Roi>();

    private Map<String, Roi> authoritativeRoiMap = new HashMap<String, Roi>();

    private IUpdatePrx updateService;

    /**
     * Initializes the metadata store implementation.
     * @param sf Client side service factory.
     * @param pixelsList List of pixels already saved in the database.
     * @param plateIds List of plate Ids already saved in the database. (This
     * should have <code>plateIds.size() == 1</code>).
     * @throws ServerError If there is an error retrieving the update service.
     */
    public void initialize(ServiceFactoryPrx sf, List<Pixels> pixelsList,
                           List<Long> plateIds) throws ServerError
    {
        this.pixelsList = pixelsList;
        updateService = sf.getUpdateService();
    }

    /**
     * Completes overlay population, flushing in memory ROI.
     * @throws ServerError Thrown if there was an error saving the ROI to the
     * OMERO server instance.
     */
    public void complete() throws ServerError
    {
        updateService.saveArray(new ArrayList<IObject>(roiMap.values()));
    }

    private Roi getRoi(int roiIndex)
    {
        LSID lsid = new LSID(Roi.class, roiIndex);
        Roi o = roiMap.get(lsid);
        if (o == null)
        {
            o = new RoiI();
            roiMap.put(lsid, o);
        }
        return o;
    }

    private Mask getMask(int roiIndex, int shapeIndex)
    {
        Roi roi = getRoi(roiIndex);
        Mask o;
        try
        {
            o = (Mask) roi.getShape(shapeIndex);
        }
        catch (IndexOutOfBoundsException e)
        {
            if (roi.sizeOfShapes() != shapeIndex)
            {
                log.error(String.format(
                        "Unable to retrieve a shape where index:%d > length:%d + 1",
                        shapeIndex, roi.sizeOfShapes()));
                return null;
            }
            o = new MaskI();
            o.setTheZ(rint(0));
            o.setTheT(rint(0));
            roi.addShape(o);
        }
        return o;
    }

    @Override
    public void setImageROIRef(String roi, int imageIndex, int ROIRefIndex)
    {
        Roi o = authoritativeRoiMap.get(roi);
        if (o == null)
        {
            log.error(String.format(
                    "Unable to retrieve ROI with authoritative LSID: %s", roi));
            return;
        }
        try
        {
            Image image = pixelsList.get(imageIndex).getImage();
            image = new ImageI(image.getId(), false);
            o.setImage(image);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            log.error(String.format(
                    "Unable to retrieve Image with index: %d", imageIndex));
            return;
        }
    }

    @Override
    public void setROIID(String id, int ROIIndex)
    {
        Roi o = getRoi(ROIIndex);
        authoritativeRoiMap.put(id, o);
    }

    @Override
    public void setMaskStrokeColor(Color stroke, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            java.awt.Color javaColor = new java.awt.Color(
                    stroke.getRed(), stroke.getGreen(), stroke.getBlue(),
                    stroke.getAlpha());
            o.setStrokeColor(rint(javaColor.getRGB()));
        }
    }

    @Override
    public void setMaskHeight(Double height, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            o.setHeight(rdouble(height));
        }
    }

    @Override
    public void setMaskWidth(Double width, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            o.setWidth(rdouble(width));
        }
    }

    @Override
    public void setMaskX(Double x, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            o.setX(rdouble(x));
        }
    }

    @Override
    public void setMaskY(Double y, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            o.setY(rdouble(y));
        }
    }

    @Override
    public void setMaskBinData(byte[] binData, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            o.setBytes(binData);
        }
    }
}
