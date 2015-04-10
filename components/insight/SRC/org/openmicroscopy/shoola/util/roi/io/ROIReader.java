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
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.frame.RoiManager;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ome.formats.model.UnitsFactory;
import omero.model.ImageI;
import omero.model.LengthI;
import pojos.EllipseData;
import pojos.LineData;
import pojos.PointData;
import pojos.PolygonData;
import pojos.PolylineData;
import pojos.ROIData;
import pojos.RectangleData;
import pojos.ShapeData;
import pojos.ShapeSettingsData;
import pojos.TextData;

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
        LineData r = new LineData(shape.x1d, shape.y1d, shape.x2d, shape.y2d);
        r.setText(shape.getName());
        formatShape(shape, r);
        return r;
    }

    /**
     * Converts the ellipse.
     *
     * @param shape The ellipse to convert.
     * @return See above.
     */
    private EllipseData convertEllipse(OvalRoi shape)
    {
        Rectangle bounds = shape.getBounds();
        double rx = bounds.getWidth();
        double ry = bounds.getHeight();
        EllipseData r = new EllipseData(bounds.getX()+rx/2, bounds.getY()+ry/2,
                rx/2, ry/2);
        r.setText(shape.getName());
        formatShape(shape, r);
        return r;
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
        formatShape(shape, r);
        return r;
    }

    /**
     * Converts the point.
     *
     * @param shape The point to convert.
     * @return See above.
     */
    private void convertPoint(PointRoi shape, ROIData roi)
    {
        int[] xc = shape.getPolygon().xpoints;
        int[] yc = shape.getPolygon().ypoints;
        PointData p;
        for (int i = 0; i < xc.length; i++) {
            p = new PointData((double) xc[i], (double) yc[i]);
            p.setText(shape.getName());
            formatShape(shape, p);
            roi.addShapeData(p);
        }
    }

    /**
     * Converts the polygon.
     *
     * @param shape The point to convert.
     * @return See above.
     */
    private ShapeData convertPolygon(PolygonRoi shape)
    {
        int[] xc = shape.getPolygon().xpoints;
        int[] yc = shape.getPolygon().ypoints;
        String type = shape.getTypeAsString();
        List<Point2D.Double> points = new LinkedList<Point2D.Double>();
        List<Integer> masks = new ArrayList<Integer>();
        for (int i = 0; i < xc.length; i++) {
            points.add(new Point2D.Double(xc[i], yc[i]));
        }
        ShapeData data;
        if (type.matches("Polyline") || type.matches("Freeline") ||
                type.matches("Angle")) {
            data = new PolylineData(points, points, points, masks);
            ((PolylineData) data).setText(shape.getName());
        } else if (type.matches("Polygon") || type.matches("Freehand") ||
                type.matches("Traced")){
            data = new PolygonData(points, points, points, masks);
            ((PolygonData) data).setText(shape.getName());
        } else {
            data = new PolygonData(points, points, points, masks);
            ((PolygonData) data).setText(shape.getName());
        }
        formatShape(shape, data);
        return data;
    }

    /**
     * Converts the text roi.
     *
     * @param shape The point to convert.
     * @return See above.
     */
    private TextData convertText(TextRoi shape)
    {
        Rectangle b = shape.getPolygon().getBounds();
        TextData data = new TextData(shape.getText(), b.getX(), b.getY());
        return data;
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
        if (roi.getStrokeWidth() > 0) {
            settings.setStrokeWidth(new LengthI((double) roi.getStrokeWidth(),
                    UnitsFactory.Shape_StrokeWidth));
        }
        if (roi.getStrokeColor() != null) {
            settings.setStroke(roi.getStrokeColor());
        }
        if (roi.getFillColor() != null) {
            settings.setFill(roi.getFillColor());
        }

        int c = roi.getCPosition();
        int z = roi.getZPosition();
        int t = roi.getTPosition();
        if(c!=0){
            shape.setC(c-1);
        }
        if(z!=0){
            shape.setZ(z-1);
        }
        if(t!=0){
            shape.setT(t-1);
        }

    }

    /**
     * Reads the roi linked to the imageJ object.
     *
     * @param imageID The identifier of the image to link the ROI to.
     * @param rois The rois to convert.
     * @return See above.
     */
    private List<ROIData> read(long imageID, Roi[] rois)
    {
        if (rois == null || rois.length == 0) return null;
        Roi r;
        List<ROIData> pojos = new ArrayList<ROIData>();
        //check ij version
        String type;
        ROIData roiData;
        for (int i = 0; i < rois.length; i++) {
            r = rois[i];
            roiData = new ROIData();
            type = r.getTypeAsString();
            if (imageID >=0) {
                roiData.setImage(new ImageI(imageID, false));
            }
            pojos.add(roiData);
            if (r.isDrawingTool()) {//Checks if the given roi is a Text box/Arrow/Rounded Rectangle
                if (type.matches("Text")){
                    roiData.addShapeData(convertText((TextRoi) r));
                } else if (type.matches("Rectangle")){
                    roiData.addShapeData(convertRectangle(r));
                }
            } else if (r instanceof OvalRoi) {
                roiData.addShapeData(convertEllipse((OvalRoi) r));
            } else if (r instanceof Line) {
                roiData.addShapeData(convertLine((Line) r));
            } else if (r instanceof PolygonRoi || r instanceof EllipseRoi) {
                if (type.matches("Point")) {
                    convertPoint((PointRoi) r, roiData);
                } else if (type.matches("Polyline") || type.matches("Freeline") ||
                        type.matches("Angle") || type.matches("Polygon") ||
                        type.matches("Freehand")
                        || type.matches("Traced") || type.matches("Oval")) {
                    roiData.addShapeData(convertPolygon((PolygonRoi) r));
                }
            } else if (r instanceof ShapeRoi) {
                Roi[] subRois = ((ShapeRoi) r).getRois();
                Roi shape;
                for (int j = 0; j < subRois.length; j++) {
                    shape = subRois[j];
                    if (shape instanceof Line) {
                        roiData.addShapeData(convertLine((Line) shape));
                    } else if (shape instanceof OvalRoi) {
                        roiData.addShapeData(convertEllipse((OvalRoi) shape));
                    } else if (shape instanceof PolygonRoi || r instanceof EllipseRoi) {
                        if (type.matches("Point")) {
                            convertPoint((PointRoi) shape, roiData);
                        } else if (type.matches("Polyline") ||
                                type.matches("Freeline") ||
                                type.matches("Angle") ||
                                type.matches("Polygon") ||
                                type.matches("Freehand")
                                || type.matches("Traced") ||
                                type.matches("Oval")) {
                            roiData.addShapeData(
                                    convertPolygon((PolygonRoi) shape));
                        }
                    }
                }
            } else if (type.matches("Rectangle")) {
                roiData.addShapeData(convertRectangle(r));
            }
        }
        return pojos;
    }
    /**
     * Reads the roi linked to the imageJ object.
     *
     * @param imageID The identifier of the image to link the ROI to.
     * @return See above.
     */
    public List<ROIData> readImageJROI(long imageID, ImagePlus image)
    {
        if (image == null) return null;
        Overlay overlay = image.getOverlay();
        if (overlay == null) return null;
        return read(imageID, overlay.toArray());
    }

    /**
     * Reads the roi linked to the imageJ object.
     *
     * @param imageID The identifier of the image to link the ROI to.
     * @return See above.
     */
    public List<ROIData> readImageJROI(long imageID)
    {
        RoiManager manager = RoiManager.getInstance();
        if (manager == null) return null;
        return read(imageID, manager.getRoisAsArray());
    }

    /**
     * Reads the rois from the ROI manager.
     *
     * @return See above.
     */
    public List<ROIData> readImageJROI()
    {
        RoiManager manager = RoiManager.getInstance();
        if (manager == null) return null;
        return read(-1, manager.getRoisAsArray());
    }

}
