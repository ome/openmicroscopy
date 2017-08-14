/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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

import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import ome.formats.model.UnitsFactory;
import omero.model.ImageI;
import omero.model.LengthI;

import omero.gateway.model.EllipseData;
import omero.gateway.model.LineData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.ShapeSettingsData;
import omero.gateway.model.TextData;

/**
 * Reads ROI from ImageJ.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ROIReader {

    private static final String PRECISION = "precision";

    private static final String MEASUREMENTS = "measurements";

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
        if (formatShape(shape, r)) {
            return r;
        }
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
        Rectangle bounds = shape.getBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        EllipseData r = new EllipseData(
            bounds.getX()+width/2, bounds.getY()+height/2, width/2, height/2);
        r.setText(shape.getName());
        if (formatShape(shape, r)) {
            return r;
        }
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
        if (formatShape(shape, r)) {
            return r;
        }
        return null;
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
            if (formatShape(shape, p)) {
                roi.addShapeData(p);
            }
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
        for (int i = 0; i < xc.length; i++) {
            points.add(new Point2D.Double(xc[i], yc[i]));
        }
        ShapeData data;
        if (type.matches("Polyline") || type.matches("Freeline") ||
                type.matches("Angle")) {
            data = new PolylineData(points);
            ((PolylineData) data).setText(shape.getName());
        } else if (type.matches("Polygon") || type.matches("Freehand") ||
                type.matches("Traced")){
            data = new PolygonData(points);
            ((PolygonData) data).setText(shape.getName());
        } else {
            data = new PolygonData(points);
            ((PolygonData) data).setText(shape.getName());
        }
        if (formatShape(shape, data)){
            return data;
        }
        return null;
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
     * @return <code>true</code> if the shape is valid, <code>false</code>
     *         otherwise.
     */
    private boolean formatShape(Roi roi, ShapeData shape)
    {
        ShapeSettingsData settings = shape.getShapeSettings();
        if (roi.getStrokeWidth() > 0) {
            settings.setStrokeWidth(new LengthI((double) roi.getStrokeWidth(),
                    UnitsFactory.Shape_StrokeWidth));
        }
        Color color;
        if (roi.getStrokeColor() != null) {
            color = roi.getStrokeColor();
            settings.setStroke(new Color(color.getRed(), color.getGreen(),
                    color.getBlue(), color.getAlpha()));
        }
        if (roi.getFillColor() != null) {
            color = roi.getFillColor();
            settings.setFill(new Color(color.getRed(), color.getGreen(),
                    color.getBlue(), color.getAlpha()));
        }
        int pos = roi.getPosition();
        int c = roi.getCPosition();
        int z = roi.getZPosition();
        int t = roi.getTPosition();

        ImagePlus image = roi.getImage();
        int imageC = image.getNChannels();
        int imageT = image.getNFrames();
        int imageZ = image.getNSlices();
        if (imageC == 1 && imageZ == 1) {
            shape.setC(0);
            shape.setZ(0);
            z = 0;
            c = 0;
            t = pos;
        } else if (imageZ == 1 && imageT == 1) {
            c = pos;
            z = 0;
            t = 0;
            shape.setZ(0);
            shape.setT(0);
        } else if (imageC == 1 && imageT == 1) {
            z = pos;
            t = 0;
            c = 0;
            shape.setC(0);
            shape.setT(0);
        }
        if (c > imageC || z > imageZ || t > imageT) {
            return false;
        }
        if (c != 0) {
            shape.setC(c-1);
        }
        if (z != 0) {
            shape.setZ(z-1);
        }
        if (t != 0) {
            shape.setT(t-1);
        }
        return true;
    }

    /**
     * Links image and rois.
     *
     * @param rois The rois to handle.
     */
    private void setImage(Roi[] rois)
    {
        ImagePlus img = WindowManager.getCurrentImage();
        for (int i = 0; i < rois.length; i++) {
            int id = rois[i].getImageID();
            if (id <= 0) { //no image set.
                rois[i].setImage(img);
            } else {
                rois[i].setImage(WindowManager.getImage(id));
            }
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
        ShapeData shape;
        for (int i = 0; i < rois.length; i++) {
            r = rois[i];
            roiData = new ROIData();
            type = r.getTypeAsString();
            if (imageID >= 0) {
                roiData.setImage(new ImageI(imageID, false));
            }
            pojos.add(roiData);
            if (r.isDrawingTool()) {//Checks if the given roi is a Text box/Arrow/Rounded Rectangle
                if (type.matches("Text")){
                    roiData.addShapeData(convertText((TextRoi) r));
                } else if (type.matches("Rectangle")) {
                    shape = convertRectangle(r);
                    if (shape != null) {
                        roiData.addShapeData(shape);
                    }
                }
            } else if (r instanceof OvalRoi) {
                shape = convertEllipse((OvalRoi) r);
                if (shape != null) {
                    roiData.addShapeData(shape);
                }
            } else if (r instanceof Line) {
                shape = convertLine((Line) r);
                if (shape != null) {
                    roiData.addShapeData(shape);
                }
            } else if (r instanceof PolygonRoi || r instanceof EllipseRoi) {
                if (type.matches("Point")) {
                    convertPoint((PointRoi) r, roiData);
                } else if (type.matches("Polyline") || type.matches("Freeline") ||
                        type.matches("Angle") || type.matches("Polygon") ||
                        type.matches("Freehand")
                        || type.matches("Traced") || type.matches("Oval")) {
                    shape = convertPolygon((PolygonRoi) r);
                    if (shape != null) {
                        roiData.addShapeData(shape);
                    }
                }
            } else if (r instanceof ShapeRoi) {
                Roi[] subRois = ((ShapeRoi) r).getRois();
                Roi shapeij;
                for (int j = 0; j < subRois.length; j++) {
                    shapeij = subRois[j];

                    // Set ImagePlus reference in subROIs for the check in L216 to work
                    ImagePlus imp = r.getImage();
                    shapeij.setImage(imp);
                    // Transfer correct ROI positions (according to IJ) from superROI
                    int pos = r.getPosition();
                    int c = r.getCPosition();
                    int z = r.getZPosition();
                    int t = r.getTPosition();
                    if (imp.getNChannels() == 1 && imp.getNSlices() == 1) {
                        shapeij.setPosition(pos);
                    } else if (imp.getNChannels() == 1 &&
                            imp.getNFrames() == 1) {
                        shapeij.setPosition(pos);
                    } else if (imp.getNSlices() == 1 &&
                            imp.getNFrames() == 1) {
                        shapeij.setPosition(pos);
                    } else if (imp.isHyperStack()) {
                        shapeij.setPosition(c, z, t);
                    }
                    type = shapeij.getTypeAsString();
                    if (shapeij instanceof Line) {
                        shape = convertLine((Line) shapeij);
                        if (shape != null) {
                            roiData.addShapeData(shape);
                        }
                    } else if (shapeij instanceof OvalRoi) {
                        shape = convertEllipse((OvalRoi) shapeij);
                        if (shape != null) {
                            roiData.addShapeData(shape);
                        }
                    } else if (shapeij instanceof PolygonRoi || r instanceof EllipseRoi) {
                        if (type.matches("Point")) {
                            convertPoint((PointRoi) shapeij, roiData);
                        } else if (type.matches("Polyline") ||
                                type.matches("Freeline") ||
                                type.matches("Angle") ||
                                type.matches("Polygon") ||
                                type.matches("Freehand")
                                || type.matches("Traced") ||
                                type.matches("Oval")) {
                            shape = convertPolygon((PolygonRoi) shapeij);
                            if (shape != null) {
                                roiData.addShapeData(shape);
                            }
                        }
                    }
                }
            } else if (type.matches("Rectangle")) {
                shape = convertRectangle(r);
                if (shape != null) {
                    roiData.addShapeData(shape);
                }
            }
        }
        return pojos;
    }

    /**
     * Reads the roi linked to the imageJ object.
     *
     * @param imageID The identifier of the image to link the ROI to.
     * @param image The ImageJ object.
     * @return See above.
     */
    public List<ROIData> readImageJROI(long imageID, ImagePlus image)
    {
        if (image == null) return null;
        Overlay overlay = image.getOverlay();
        Roi[] rois;
        if (overlay != null) {
            rois = overlay.toArray();
            for (Roi roi : rois) {
                roi.setImage(image);
            }
            return read(imageID, rois);
        }
        RoiManager manager = RoiManager.getInstance();
        if (manager == null) return null;
        rois = manager.getRoisAsArray();
        for (Roi roi : rois) {
            roi.setImage(image);
        }
        return read(imageID, rois);
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
        Roi[] rois = manager.getRoisAsArray();
        setImage(rois);
        return read(imageID, rois);
    }

    /**
     * Reads the roi linked to the imageJ object.
     * First checks the overlays then the roi manager.
     *
     * @param imageID The identifier of the image to link the ROI to.
     * @param image The ImageJ object.
     * @return See above.
     */
    public List<ROIData> readImageJROIFromSources(long imageID, ImagePlus image)
    {
        if (image == null) return null;
        Overlay overlay = image.getOverlay();
        if (overlay != null) {
            Roi[] rois = overlay.toArray();
            for (int i = 0; i < rois.length; i++) {
                rois[i].setImage(image);
            }
            return read(imageID, rois);
        }
        return readImageJROI(imageID, image);
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
        Roi[] rois = manager.getRoisAsArray();
        setImage(rois);
        return read(-1, rois);
    }

    /**
     * Reads the results and save them to the specified file.
     *
     * @param f The file to save the results to.
     */
    public void readROIMeasurement(File f)
        throws IOException
    {
        if (f == null) return;
        readROIMeasurement(f.getAbsolutePath());
    }

    /**
     * Reads the results and save them to the specified file.
     *
     * @param f The file to save the results to.
     */
    public void readROIMeasurement(String f)
        throws IOException
    {
        if (CommonsLangUtils.isBlank(f)) return;
        int totint = Measurements.AREA+Measurements.AREA_FRACTION+
                Measurements.CENTER_OF_MASS+Measurements.CENTROID+
                Measurements.CIRCULARITY+Measurements.ELLIPSE+
                Measurements.FERET+Measurements.KURTOSIS+Measurements.LIMIT+
                Measurements.MAX_STANDARDS+Measurements.MEAN+
                Measurements.MEDIAN+Measurements.MIN_MAX+Measurements.MODE+
                Measurements.PERIMETER+Measurements.RECT+
                Measurements.SHAPE_DESCRIPTORS+Measurements.SKEWNESS+
                Measurements.SLICE+Measurements.STACK_POSITION+
                Measurements.STD_DEV;
        int precision = Prefs.getInt(PRECISION, 5);
        RoiManager.getInstance().runCommand("Measure");

        Analyzer.setMeasurement(Prefs.getInt(MEASUREMENTS,totint), true);
        Analyzer.setPrecision(precision);
        ResultsTable rt = Analyzer.getResultsTable();
        rt.updateResults();
        rt.show("Results");
        rt.saveAs(f);
    }

    /**
     * Reads the results and save them to the specified file.
     * Returns <code>false</code> if no results to read, <code>true</code>
     * otherwise.
     *
     * @param f The file to save the results to.
     * @return Returns <c
     */
    public boolean readResults(File f)
        throws IOException
    {
        if (f == null) return false;
        return readResults(f.getAbsolutePath());
    }

    /**
     * Reads the results and save them to the specified file.
     * Returns <code>false</code> if no results to read, <code>true</code>
     * otherwise.
     *
     * @param f The file to save the results to.
     */
    public boolean readResults(String f)
        throws IOException
    {
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null || rt.getCounter() == 0) return false;
        rt.updateResults();
        rt.saveAs(f);
        return true;
    }

}
