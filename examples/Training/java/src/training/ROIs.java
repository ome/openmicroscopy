/*
 * training.ROIs 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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



import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ome.formats.model.UnitsFactory;
import omero.RInt;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.ROIResult;
import omero.log.SimpleLogger;
import omero.model.Ellipse;
import omero.model.EllipseI;
import omero.model.Label;
import omero.model.LabelI;
import omero.model.LengthI;
import omero.model.Line;
import omero.model.LineI;
import omero.model.Mask;
import omero.model.MaskI;
import omero.model.Path;
import omero.model.PathI;
import omero.model.PixelsI;
import omero.model.Point;
import omero.model.PointI;
import omero.model.Polygon;
import omero.model.PolygonI;
import omero.model.Polyline;
import omero.model.PolylineI;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import omero.model.enums.UnitsLength;
import pojos.EllipseData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.LineData;
import pojos.PointData;
import pojos.ROIData;
import pojos.RectangleData;
import pojos.ShapeData;

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
    private String hostName = "serverName";

    /** The username.*/
    private String userName = "userName";

    /** The password.*/
    private String password = "password";

    /** Information to edit.*/
    private long imageId = 1;
    //end edit

    private ImageData image;
    
    private Gateway gateway;
    
    private SecurityContext ctx;

    /**
     * Loads the image.
     * 
     * @param imageID The id of the image to load.
     * @return See above.
     */
    private ImageData loadImage(long imageID)
            throws Exception
    {
        BrowseFacility browse = gateway.getFacility(BrowseFacility.class);
        return browse.getImage(ctx, imageID);
    }

    /** 
     * Creates roi and retrieve it.
     */
    private void createROIs()
            throws Exception
    {
        DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
        ROIFacility roifac = gateway.getFacility(ROIFacility.class);
        
        Roi roi = new RoiI();
        roi.setImage(image.asImage());
        Rect rect = new RectI();
        rect.setX(omero.rtypes.rdouble(10));
        rect.setY(omero.rtypes.rdouble(10));
        rect.setWidth(omero.rtypes.rdouble(10));
        rect.setHeight(omero.rtypes.rdouble(10));
        rect.setTheZ(omero.rtypes.rint(0));
        rect.setTheT(omero.rtypes.rint(0));
        roi.addShape(rect);

        //Create a rectangular shape
        rect = new RectI();
        rect.setX(omero.rtypes.rdouble(10));
        rect.setY(omero.rtypes.rdouble(10));
        rect.setWidth(omero.rtypes.rdouble(10));
        rect.setHeight(omero.rtypes.rdouble(10));
        rect.setTheZ(omero.rtypes.rint(1));
        rect.setTheT(omero.rtypes.rint(0));
        roi.addShape(rect); //Add the shape

        //Create an ellipse.
        Ellipse ellipse = new EllipseI();
        ellipse.setCx(omero.rtypes.rdouble(10));
        ellipse.setCy(omero.rtypes.rdouble(10));
        ellipse.setRx(omero.rtypes.rdouble(10));
        ellipse.setRy(omero.rtypes.rdouble(10));
        ellipse.setTheZ(omero.rtypes.rint(1));
        ellipse.setTheT(omero.rtypes.rint(0));
        ellipse.setTextValue(omero.rtypes.rstring("ellipse text"));
        roi.addShape(ellipse);

        //Create a line
        Line line = new LineI();
        line.setX1(omero.rtypes.rdouble(100));
        line.setX2(omero.rtypes.rdouble(200));
        line.setY1(omero.rtypes.rdouble(300));
        line.setY2(omero.rtypes.rdouble(400));
        line.setTheZ(omero.rtypes.rint(1));
        line.setTheT(omero.rtypes.rint(0));
        line.setTransform(omero.rtypes.rstring("100 0 0 200 0 0"));
        roi.addShape(line);

        //Create a point
        Point point = new PointI();
        point.setCx(omero.rtypes.rdouble(75.0));
        point.setCy(omero.rtypes.rdouble(75.0));
        point.setTheZ(omero.rtypes.rint(0));
        point.setTheT(omero.rtypes.rint(0));
        point.setTransform(null);
        roi.addShape(point);

        //Polygon
        Polygon polygon = new PolygonI();
        polygon.setPoints(omero.rtypes.rstring(
                "100.0,200.0 553.9,593.5 92.3,59.9"));
        polygon.setTransform(null);
        polygon.setTheZ(omero.rtypes.rint(0));
        polygon.setTheT(omero.rtypes.rint(0));
        roi.addShape(polygon);

        //Polyline
        Polyline polyline = new PolylineI();
        polyline.setPoints(omero.rtypes.rstring(
                "100.0,200.0 553.9,593.5 92.3,59.9"));
        polyline.setTransform(null);
        roi.addShape(polyline);

        // Display fields which could quickly
        // be parsed from known formats
        RInt GREY = omero.rtypes.rint(11184810);
        Label text = new LabelI();
        text.setTextValue(omero.rtypes.rstring("This is a polyline"));
        text.setFontFamily(omero.rtypes.rstring("Verdana"));
        text.setFontSize(new LengthI(40, UnitsLength.POINT));
        text.setFontWeight(omero.rtypes.rstring("bold"));
        text.setFillColor(GREY);
        text.setStrokeColor(GREY);
        text.setStrokeWidth(new LengthI(25, UnitsFactory.Shape_StrokeWidth));
        text.setVisibility(omero.rtypes.rbool(true));
        text.setLocked(omero.rtypes.rbool(true));


        // Other options which may come with time
        text.setVectorEffect(omero.rtypes.rstring("non-scaling-stroke"));
        text.setFontStretch(omero.rtypes.rstring("wider"));
        text.setFontStyle(omero.rtypes.rstring("italic"));
        text.setFontVariant(omero.rtypes.rstring("small-caps"));
        text.setFillColor(GREY);
        text.setFillRule(omero.rtypes.rstring("even-odd"));
        text.setStrokeColor(GREY);
        text.setStrokeDashArray(omero.rtypes.rstring("10 20 30 10"));
        text.setStrokeDashOffset(omero.rtypes.rint(1));
        text.setStrokeLineCap(omero.rtypes.rstring("butt"));
        text.setStrokeLineJoin(omero.rtypes.rstring("bevel"));
        text.setStrokeMiterLimit(omero.rtypes.rint(1));
        text.setStrokeWidth(new LengthI(10, UnitsFactory.Shape_StrokeWidth));
        text.setAnchor(omero.rtypes.rstring("middle"));
        text.setDecoration(omero.rtypes.rstring("underline"));
        text.setBaselineShift(omero.rtypes.rstring("70%"));
        text.setGlyphOrientationVertical(omero.rtypes.rint(90));
        text.setDirection(omero.rtypes.rstring("rtl"));
        text.setWritingMode(omero.rtypes.rstring("tb-rl"));
        text.setTheZ(omero.rtypes.rint(0));
        text.setTheT(omero.rtypes.rint(0));
        roi.addShape(text);

        //Add a path shape
        Path path = new PathI();
        path.setD(omero.rtypes.rstring("M 100 100 L 300 100 L 200 300 z"));
        path.setTransform(null);
        roi.addShape(path);

        //Add a mask
        Mask mask = new MaskI();
        mask.setX(omero.rtypes.rdouble(10));
        mask.setY(omero.rtypes.rdouble(10));
        mask.setWidth(omero.rtypes.rdouble(100.0));
        mask.setHeight(omero.rtypes.rdouble(100.0));
        mask.setPixels(new PixelsI(image.getDefaultPixels().getId(), false));
        
        ROIData roiData = roifac.saveROIs(ctx, image.getId(), Arrays.asList(new ROIData(roi))).iterator().next();

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
                RectangleData rectData = (RectangleData) shape;
                //Handle rectangle
            } else if (shape instanceof EllipseData) {
                EllipseData ellipseData = (EllipseData) shape;
                //Handle ellipse
            } else if (shape instanceof LineData) {
                LineData lineData = (LineData) shape;
                //Handle line
            } else if (shape instanceof PointData) {
                PointData pointData = (PointData) shape;
                //Handle line
            }
        }


        List<ROIResult> roiresults = roifac.loadROIs(ctx, image.getId());
        
        // Retrieve the roi linked to an image
        ROIResult r = roiresults.iterator().next();
        Collection<ROIData> rois = r.getROIs();
        if (rois == null)
            throw new Exception("No rois linked to Image:"+image.getId());
        List<Shape> list;
        Iterator<ROIData> j = rois.iterator();
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

        //Check that the shape does not have shape.
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
     * Connects and invokes the various methods.
     * 
     * @param info The configuration information.
     */
    ROIs(ConfigurationInfo info)
    {
        if (info == null) {
            info = new ConfigurationInfo();
            info.setHostName(hostName);
            info.setPassword(password);
            info.setUserName(userName);
            info.setImageId(imageId);
        }
        
        LoginCredentials cred = new LoginCredentials();
        cred.getServer().setHostname(info.getHostName());
        cred.getServer().setPort(info.getPort());
        cred.getUser().setUsername(info.getUserName());
        cred.getUser().setPassword(info.getPassword());

        gateway = new Gateway(new SimpleLogger());
        
        try {
            ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            
            image = loadImage(info.getImageId());
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
     * @param args
     */
    public static void main(String[] args)
    {
        new ROIs(null);
        System.exit(0);
    }

}
