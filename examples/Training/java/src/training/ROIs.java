/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package training;



import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ome.formats.model.UnitsFactory;
import omero.api.RawPixelsStorePrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.ROIResult;
import omero.log.SimpleLogger;
import omero.model.AffineTransform;
import omero.model.AffineTransformI;
import omero.model.LengthI;
import omero.model.Roi;

import omero.model.Shape;
import omero.model.enums.UnitsLength;
import omero.gateway.model.EllipseData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LineData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TextData;

/** 
 * Sample code showing how interact with Region of interests.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ROIs 
{

    //The value used if the configuration file is not used. To edit*/
    /** The server address.*/
    private static String hostName = "serverName";

    /** The username.*/
    private static String userName = "userName";

    /** The password.*/
    private static String password = "password";

    /** Information to edit.*/
    private static long imageId = 1;
    //end edit

    private ImageData image;

    private Gateway gateway;

    private SecurityContext ctx;

    /**
     * start-code
     */

    /**
     * Loads the image.
     * @param imageID The id of the image to load.
     * @return See above.
     */
    private ImageData loadImage(long imageID)
            throws Exception
    {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        return browse.getImage(ctx, imageID);
    }

// Create ROIs
// ===========

    /** Creates roi and retrieve it. */
    private void createROIs()
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        ROIFacility roifac = gateway.getFacility(ROIFacility.class);

        ROIData data = new ROIData();
        data.setImage(image.asImage());
        
        //Create Rectangle ShapeData
        RectangleData rectangleData = new RectangleData(10, 10, 10, 10);
        rectangleData.setZ(0);
        rectangleData.setT(0);
        data.addShapeData(rectangleData);
        
        rectangleData = new RectangleData(10, 10, 10, 10);
        rectangleData.setZ(0);
        rectangleData.setT(1);
        data.addShapeData(rectangleData);


        //Create an ellipse.
        EllipseData ellipseData = new EllipseData(10, 10, 10, 10);
        ellipseData.setZ(1);
        ellipseData.setT(0);
        ellipseData.setText("ellipse text");
        data.addShapeData(ellipseData);

        //Create a line
        LineData lineData = new LineData(100, 200, 300, 400);
        lineData.setZ(1);
        lineData.setT(0);
        data.addShapeData(lineData);

        //Create a point
        PointData pointData = new PointData(75.0, 75.0);
        pointData.setZ(0);
        pointData.setT(0);
        data.addShapeData(pointData);

        //Polygon
        List<Point2D.Double> points = new ArrayList();
        points.add(new Point2D.Double(100.0,200));
        points.add(new Point2D.Double(553.9,593.5));
        points.add(new Point2D.Double(92.3,59.9));
        PolygonData polygonData = new PolygonData(points);
        polygonData.setZ(0);
        polygonData.setT(0);
        data.addShapeData(polygonData);

        //Polyline
        PolylineData polylineData = new PolylineData(points);
        polylineData.setZ(0);
        polylineData.setT(0);
        data.addShapeData(polylineData);

        // Display fields which could quickly
        // be parsed from known formats
        TextData textData = new TextData("This is a polyline", 10, 10);

        Color fillcolor = new Color(128, 128, 128);
        Color strokecolor = new Color(255, 255, 255);
        textData.getShapeSettings().setFill(fillcolor);;
        textData.getShapeSettings().setFontSize(new LengthI(40, UnitsLength.POINT));
        textData.getShapeSettings().setFontFamily("sans-serif");
        textData.getShapeSettings().setStroke(strokecolor);
        textData.getShapeSettings().setStrokeWidth(new LengthI(25, UnitsFactory.Shape_StrokeWidth));
        
        // Other options which may come with time
        textData.getShapeSettings().setFontStyle("italic");
        textData.getShapeSettings().setFillRule("even-odd");
        double[] doublearray = {(double) 10, (double) 20, (double) 30, (double) 10};
        textData.getShapeSettings().setStrokeDashArray(doublearray);
        textData.setZ(0);
        textData.setT(0);
        data.addShapeData(textData);

        //Add a mask
        PixelsData pixels = image.getDefaultPixels();
        long pixelsId = pixels.getId();
        RawPixelsStorePrx store = gateway.getPixelsStore(ctx);
        try {
            store.setPixelsId(pixelsId, false);
            byte[] mask = store.getStack(0, 0);
            MaskData maskData = new MaskData(10, 10, 100.0, 100.0, mask);
            maskData.setZ(0);
            maskData.setT(0);
            data.addShapeData(maskData);
        } finally {
            store.close();
        }

        //Create and Apply transform to an ellipse 
        //Don't set Z and T for this shape: this is also allowed in the model
        EllipseData ellipse = new EllipseData(10,10,10,10);
        ellipse.setText("ellipse text");
        //set angle of rotation
        int theta = 10;
        //create transform object
        AffineTransformI newTform = new AffineTransformI();
        newTform.setA00(omero.rtypes.rdouble(java.lang.Math.cos(theta)));
        newTform.setA10(omero.rtypes.rdouble(-java.lang.Math.sin(theta)));
        newTform.setA01(omero.rtypes.rdouble(java.lang.Math.sin(theta)));
        newTform.setA11(omero.rtypes.rdouble(java.lang.Math.cos(theta)));
        newTform.setA02(omero.rtypes.rdouble(0));
        newTform.setA12(omero.rtypes.rdouble(0));
        //add transform
        ellipse.setTransform(newTform);
        data.addShapeData(ellipse);

        ROIData roiData = roifac.saveROIs(ctx, image.getId(),
        Arrays.asList(data)).iterator().next();

        //Retrieve the shape on plane (0, 0)
        List<ShapeData> shapes = roiData.getShapes(0, 0);
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            ShapeData shape = i.next();
            //plane info
            int z = shape.getZ();
            int t = shape.getT();
            long id = shape.getId();
            if (shape instanceof RectangleData) {
                RectangleData rectangleData1 = (RectangleData) shape;
                //Insert code to handle rectangle
            } else if (shape instanceof EllipseData) {
                EllipseData ellipseData1 = (EllipseData) shape;
                //Insert code to handle ellipse
            } else if (shape instanceof LineData) {
                LineData lineData1 = (LineData) shape;
                //Insert code to handle line
            } else if (shape instanceof PointData) {
                PointData pointData1 = (PointData) shape;
                //Insert code to handle point
            } else if (shape instanceof MaskData) {
                MaskData maskData1 = (MaskData) shape;
                //Insert code to handle mask
            }
            //Check if the shape has transform
            //http://blog.openmicroscopy.org/data-model/future-plans/2016/06/20/shape-transforms/
            AffineTransform transform = shape.getTransform();
            if (transform != null) {

                double xScaling = transform.getA00().getValue();
                double xShearing = transform.getA01().getValue();
                double xTranslation = transform.getA02().getValue();
            
                double yScaling = transform.getA11().getValue();
                double yShearing = transform.getA10().getValue();
                double yTranslation = transform.getA12().getValue();
                // Insert code to handle transforms
            }
        }

        // Retrieve the roi linked to an image
        List<ROIResult> roiresults = roifac.loadROIs(ctx, image.getId());
        // Retrieve the roi linked to an image
        ROIResult r = roiresults.iterator().next();
        if (r == null)
            throw new Exception("No rois linked to Image:"+image.getId());
        List<Shape> list;
        Collection<ROIData> rois = r.getROIs();
        Iterator<ROIData> j = rois.iterator();
        Roi roi = null;
        while (j.hasNext()) { 
            roiData = j.next();
            roi = (Roi) roiData.asIObject();
            list = roi.copyShapes();
            //size = 2
            //remove first shape
            roi.removeShape(list.get(0));
            //update the roi
            dm.saveAndReturnObject(ctx, roi);
        }

        roiresults = roifac.loadROIs(ctx, image.getId());
        r = roiresults.iterator().next();
        if (r == null)
            throw new Exception("No rois linked to Image:"+image.getId());
        rois = r.getROIs();
        if (rois == null)
            throw new Exception("No rois linked to Image:"+image.getId());
        j = rois.iterator();
        while (j.hasNext()) {
            roiData = j.next();
            roi = (Roi) roiData.asIObject();
            list = roi.copyShapes();
            System.err.println(list.size());
        }  
        //Load rois on a plane z=1, t=0
        r = roifac.loadROIsByPlane(ctx, image.getId(), 1, 0).iterator().next();
        if (r == null)
            throw new Exception("No rois linked to image:"+image.getId());
        j = rois.iterator();
        while (j.hasNext()) {
            roi = (Roi) j.next().asIObject();
            list = roi.copyShapes();
            System.err.println(list.size());
        }
        //load a given roi
        r = roifac.loadROI(ctx, roi.getId().getValue());
        System.out.println(r.getROIs().size());
    }

    /**
     * end-code
     */
    /**
     * Connects and invokes the various methods.
     * @param args The login credentials.
     * @param imageId The image id
     */
    ROIs(String[] args, long imageId)
    {   
        LoginCredentials cred = new LoginCredentials(args);

        gateway = new Gateway(new SimpleLogger());

        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());

            image = loadImage(imageId);
            createROIs();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                gateway.disconnect(); // Be sure to disconnect
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs the script without configuration options.
     *
     * @param args The login credentials.
     */
    public static void main(String[] args)
    {
        if (args == null || args.length == 0)
            args = new String[] { "--omero.host=" + hostName,
                "--omero.user=" + userName, "--omero.pass=" + password };

        new ROIs(args, imageId);
        System.exit(0);
    }

}
