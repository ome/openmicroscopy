/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.io;


//Java imports
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.frame.RoiManager;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.model.ImageI;

import org.apache.commons.collections.CollectionUtils;

import pojos.EllipseData;
import pojos.LineData;
import pojos.PointData;
import pojos.PolygonData;
import pojos.ROIData;
import pojos.RectangleData;
import pojos.ShapeData;
import pojos.ShapeSettingsData;

/**
 * Reads ROI from ImageJ.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ROIReader {

    /**
     * Converts the line.
     *
     * @param shape The line to convert.
     * @return See above.
     */
    private LineData convertLine(Line shape)
    {
        return null;
    }

    /**
     * Converts the ellipse.
     *
     * @param shape The ellipse to convert.
     * @return See above.
     */
    private EllipseData convertEllipse(OvalRoi shape)
    {
        return null;
    }

    /**
     * Converts the rectangle.
     *
     * @param shape The rectangle to convert.
     * @return See above.
     */
    private RectangleData convertRectangle(Roi shape)
    {
        Rectangle bounds = shape.getBounds();
        RectangleData r = new RectangleData(
                bounds.getX(), bounds.getY(), bounds.getWidth(),
                bounds.getHeight());
        r.setText(shape.getName());
        return r;
    }

    /**
     * Converts the point.
     *
     * @param shape The point to convert.
     * @return See above.
     */
    private PointData convertPoint(PointRoi shape)
    {
        return null;
    }

    /**
     * Converts the polygon.
     *
     * @param shape The point to convert.
     * @return See above.
     */
    private PolygonData convertPolygon(PointRoi shape)
    {
        return null;
    }

    /**
     * Formats the shape.
     *
     * @param roi The ImageJ roi.
     * @param shape The internal roi.
     */
    private void formatShape(Roi roi, ShapeData shape)
    {
        ShapeSettingsData settings = shape.getShapeSettings();
        if (roi.getInstanceColor() != null) {
            settings.setStroke(roi.getInstanceColor());
        }
    }

    /**
     * Reads the roi linked to the imageJ object.
     *
     * @param ids The identifiers of the images to the ROI to.
     * @return See above.
     */
    public List<ROIData> readImageJROI(List<Long> ids)
    {
        RoiManager manager = RoiManager.getInstance();
        if (manager == null) return null;
        Roi[] rois = manager.getRoisAsArray();
        if (rois == null || rois.length == 0) return null;
        Roi r;
        List<ROIData> pojos = new ArrayList<ROIData>();
        //check ij version
        String type;
        ROIData roiData;
        for (int i = 0; i < rois.length; i++) {
            r = rois[i];
            roiData = new ROIData();
            //r.get
            type = r.getTypeAsString();
            pojos.add(roiData);
            IJ.log(type+" "+r);
            if (r instanceof OvalRoi) {
                roiData.addShapeData(convertEllipse((OvalRoi) r));
            } else if (r instanceof Line) {
                roiData.addShapeData(convertLine((Line) r));
            } else if (r instanceof PolygonRoi) { //EllipseRoi check version.
                
                if (type.matches("Polyline") || type.matches("Freeline") ||
                        type.matches("Angle")) {
                   
                } else if (type.matches("Point")) {
                } else if (type.matches("Polygon") || type.matches("Freehand")
                        || type.matches("Traced") || type.matches("Oval")) {
                    
                }
            } else if (r instanceof ShapeRoi) {
                Roi[] subRois = ((ShapeRoi) r).getRois();
                Roi shape;
                for (int j = 0; j < subRois.length; j++) {
                    shape = subRois[j];
                    if (shape instanceof Line) {
                        
                    } else if (shape instanceof OvalRoi) {
                        
                    } else if (shape instanceof PolygonRoi) {
                        type = shape.getTypeAsString();
                        if (type.matches("Polyline") ||
                           type.matches("Freeline") || type.matches("Angle")) {
                           
                        } else if (type.matches("Point")) {
                        } else if (type.matches("Polygon") ||
                                type.matches("Freehand") ||
                                type.matches("Traced") ||
                                type.matches("Oval")) {
                        }
                    }
                }
            } else if (type.matches("Rectangle")) {
                roiData.addShapeData(convertRectangle(r));
            }
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            Iterator<ROIData> k = pojos.iterator();
            Iterator<Long> i;
            while (k.hasNext()) {
                roiData = k.next();
                i = ids.iterator();
                while (i.hasNext()) {
                    roiData.setImage(new ImageI(i.next(), false));
                }
            }
        }
        return pojos;
    }

}
