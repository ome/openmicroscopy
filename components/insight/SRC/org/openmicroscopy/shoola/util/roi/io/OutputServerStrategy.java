/*
 * org.openmicroscopy.shoola.util.roi.io.OutputServerStrategy
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.io;

//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries
import omero.model.FontFamily;
import omero.model.FontStyle;
import omero.model.LineCap;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.geom.BezierPath;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.EnumerationObject;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureMaskFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.EllipseData;
import pojos.ImageData;
import pojos.LineData;
import pojos.MaskData;
import pojos.PointData;
import pojos.PolygonData;
import pojos.PolylineData;
import pojos.ROIData;
import pojos.ShapeData;
import pojos.ShapeSettingsData;
import pojos.TextData;
import pojos.RectangleData;

/**
 * Handles ROI from server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class OutputServerStrategy 
{
	
	/** The ROIComponent to serialize. */
	private ROIComponent component;
	
	/** The list of ROI to be supplied to the server. */
	private List<ROIData>  ROIList;
	
	/** The collection of enumerations used to save the shape.*/
	private Map<Integer, List<EnumerationObject>> enumerations;
	
	/**
	 * Parses the ROI in the ROIComponent to create the appropriate ROIData 
	 * object to supply to the server.
	 * 
	 * @param image The image the ROI is on.
	 * @param ownerID The identifier of the owner.
	 * @throws Exception 
	 */
	private void parseROI(ImageData image, long ownerID) 
		throws Exception
	{
		TreeMap<Long, ROI> map = component.getROIMap();
		Iterator<ROI> roiIterator = map.values().iterator();
		ROI roi;
		while (roiIterator.hasNext())
		{
			roi = roiIterator.next();
			if (roi.getOwnerID() == ownerID || roi.getOwnerID() == -1)
				ROIList.add(createServerROI(roi, image));
		}
	}

	/**
	 * Creates the Shape object for the ROIShape figure object.
	 * @param clientShape See above.
	 * @return See above.
	 * @throws Exception If an error occurred while creating the shape.
	 */
	private ShapeData createShapeData(ROIShape clientShape) 
		throws Exception
	{
		ROIFigure fig = clientShape.getFigure();
		ShapeData shape = null;
		if (fig instanceof MeasureBezierFigure)
			shape = createBezierFigure(clientShape);
		else if (fig instanceof MeasureEllipseFigure)
			shape = createEllipseFigure(clientShape);
		else if (fig instanceof MeasureLineFigure)
			shape = createLineFigure(clientShape);
		else if (fig instanceof MeasureMaskFigure)
			shape = createMaskFigure(clientShape);
		else if (fig instanceof MeasurePointFigure)
			shape = createPointFigure(clientShape);
		else if (fig instanceof MeasureRectangleFigure)
			shape = createRectangleFigure(clientShape);
		else if (fig instanceof MeasureTextFigure)
			shape = createTextFigure(clientShape);
		if (shape == null)
			throw new Exception("ROIShape not supported : " + 
									clientShape.getClass().toString());
		shape.setT(clientShape.getT());
		shape.setZ(clientShape.getZ());
		shape.setDirty(fig.isDirty());
		if (!fig.isClientObject())
			shape.setId(clientShape.getROIShapeID());
		return shape;
	}
	
	/**
	 * Creates an ROIData object from an ROI. 
	 * 
	 * @param roi The ROI to handle.
	 * @param image The image the ROI is on.
	 * @return See above.
	 * @throws Exception If an error occurred while parsing the ROI.
	 */
	private ROIData createServerROI(ROI roi, ImageData image) 
		throws Exception
	{
		ROIData roiData = new ROIData();
		Object v = roi.getAnnotation(AnnotationKeys.TEXT);
		if (v != null) roiData.setDescription((String) v);
		roiData.setClientSide(roi.isClientSide());
		if (!roi.isClientSide())
			roiData.setId(roi.getID());
		roiData.setImage(image.asImage());
		TreeMap<Coord3D, ROIShape> shapes =  roi.getShapes();
		Iterator<ROIShape> shapeIterator = shapes.values().iterator();
		ROIShape roiShape;
		ShapeData shape;
		while (shapeIterator.hasNext())
		{
			roiShape = shapeIterator.next();
			shape = createShapeData(roiShape);
			addShapeAttributes(roiShape.getFigure(), shape);
			roiData.addShapeData(shape);
		}
		return roiData;
	}
	
	/**
	 * Creates a Bezier figure server side object from a MeasureBezierFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private ShapeData createBezierFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureBezierFigure fig = (MeasureBezierFigure) shape.getFigure();
		if (fig.isClosed()) return createPolygonFigure(shape);
		return createPolylineFigure(shape);
	}
	
	/**
	 * Creates an ellipse figure server side object from a MeasureEllipseFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private EllipseData createEllipseFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureEllipseFigure fig = (MeasureEllipseFigure) shape.getFigure();
		double rx = fig.getEllipse().getWidth()/2d;
		double ry = fig.getEllipse().getHeight()/2d;
		double cx = fig.getEllipse().getCenterX();
		double cy = fig.getEllipse().getCenterY();
		
		EllipseData ellipse = new EllipseData(cx, cy, rx, ry); 
		ellipse.setVisible(fig.isVisible());
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			ellipse.setText(text);
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		if (t != null)
			ellipse.setTransform(toTransform(t));
		return ellipse;
	}

	/**
	 * Creates a mask figure server side object from a MeasureMaskFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 */
	private MaskData createMaskFigure(ROIShape shape)
	{
		return null;
	}
	
	/**
	 * Creates a Bezier figure server side object from a MeasurePointFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private PointData createPointFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasurePointFigure fig = (MeasurePointFigure)shape.getFigure();
		double cx = fig.getCentre().getX();
		double cy = fig.getCentre().getY();
		
		PointData point = new PointData(cx, cy); 
		point.setVisible(fig.isVisible());
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			point.setText(fig.getText());
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		if (t != null)
			point.setTransform(toTransform(t));
		return point;
	}
	
	/**
	 * Creates a text figure server side object from a MeasureTextFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private TextData createTextFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureTextFigure fig = (MeasureTextFigure)shape.getFigure();
		double x = fig.getBounds().getX();
		double y = fig.getBounds().getY();
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				text.equals(ROIFigure.DEFAULT_TEXT))
			text = "";
		TextData data = new TextData(text, x, y); 
		data.setDirty(fig.isDirty());
		data.setT(shape.getT());
		data.setZ(shape.getZ());
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		if (t != null)
			data.setTransform(toTransform(t));
		if (!fig.isClientObject())
			data.setId(shape.getROIShapeID());
		return data;
	}
	
	/**
	 * Creates a rectangle figure server side object from a 
	 * MeasureRectangleFigure client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private RectangleData createRectangleFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureRectangleFigure fig = (MeasureRectangleFigure) shape.getFigure();
		double x = fig.getX();
		double y = fig.getY();
		double width = fig.getWidth();
		double height = fig.getHeight();
		
		RectangleData rectangle = new RectangleData(x, y, width, height); 
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			rectangle.setText(fig.getText());
		rectangle.setVisible(fig.isVisible());
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		if (t != null)
			rectangle.setTransform(toTransform(t));
		
		return rectangle;
	}
	
	/**
	 * Creates a polygon figure server side object from a MeasureBezierFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private PolygonData createPolygonFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureBezierFigure fig = (MeasureBezierFigure) shape.getFigure();
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		List<Point2D.Double> points = new LinkedList<Point2D.Double>();

		BezierPath bezier = fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
		}
		PolygonData poly = new PolygonData();
		poly.setVisible(fig.isVisible());
		poly.setPoints(points);
		if (t != null)
			poly.setTransform(toTransform(t));
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			poly.setText(fig.getText());
		return poly;	
	}
	
	/**
	 * Creates a line figure server side object from a MeasureLineFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private ShapeData createLineFigure(ROIShape shape) 
				throws ParsingException
	{
		MeasureLineFigure fig = (MeasureLineFigure) shape.getFigure();
		BezierPath bezier = fig.getBezierPath();
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		int n = bezier.size();
		if (n == 2) { //it is a line.
			BezierPath.Node start = bezier.get(0);
			BezierPath.Node end = bezier.get(1);
			LineData line = new LineData(start.x[0], start.y[0], 
					end.x[0], end.y[0]);
			line.setVisible(fig.isVisible());
			if (t != null) line.setTransform(toTransform(t));
			String text = fig.getText();
			if (text != null && text.trim().length() > 0 && 
					!text.equals(ROIFigure.DEFAULT_TEXT))
				line.setText(fig.getText());
			return line;
		}
		
		List<Point2D.Double> points = new LinkedList<Point2D.Double>();
		for (BezierPath.Node node : bezier)
			points.add(new Point2D.Double(node.x[0], node.y[0]));
		PolylineData line = new PolylineData();
		line.setVisible(fig.isVisible());
		line.setPoints(points);
		if (t != null)
			line.setTransform(toTransform(t));
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			line.setText(fig.getText());
		return line;
	}
	
	/**
	 * Creates a PolyLine figure server side object from a MeasureBezierFigure 
	 * client side object.
	 * 
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException If an error occurred while parsing.
	 */
	private PolylineData createPolylineFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureBezierFigure fig = (MeasureBezierFigure)shape.getFigure();
		AffineTransform t = AttributeKeys.TRANSFORM.get(fig);
		List<Point2D.Double> points = new LinkedList<Point2D.Double>();
		
		BezierPath bezier = fig.getBezierPath();
		for (BezierPath.Node node : bezier)
			points.add(new Point2D.Double(node.x[0], node.y[0]));
		PolylineData poly = new PolylineData();
		poly.setVisible(fig.isVisible());
		poly.setPoints(points);
		if (t != null)
			poly.setTransform(toTransform(t));
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			poly.setText(fig.getText());
		return poly;	
	}
	
	/**
	 * Returns the enumeration object corresponding to the passed family.
	 * 
	 * @param family The name of the family.
	 * @return See above.
	 */
	private FontFamily getFontFamily(String family)
	{
		List<EnumerationObject> l = enumerations.get(ROIComponent.FONT_FAMILY);
		Iterator<EnumerationObject> i = l.iterator();
		EnumerationObject o;
		while (i.hasNext()) {
			o = i.next();
			if (o.getValue().equals(family))
				return (FontFamily) o.getObject();
		}
		return null;
	}
	
	/**
	 * Returns the enumeration object corresponding to the passed style.
	 * 
	 * @param style The name of the style.
	 * @return See above.
	 */
	private FontStyle getFontStyle(String style)
	{
		List<EnumerationObject> l = enumerations.get(ROIComponent.FONT_STYLE);
		Iterator<EnumerationObject> i = l.iterator();
		EnumerationObject o;
		while (i.hasNext()) {
			o = i.next();
			if (o.getValue().equals(style))
				return (FontStyle) o.getObject();
		}
		return null;
	}
	
	/**
	 * Returns the enumeration object corresponding to the passed line cap.
	 * 
	 * @param lineCap The name of the line cap.
	 * @return See above.
	 */
	private LineCap getLineCap(String lineCap)
	{
		List<EnumerationObject> l = enumerations.get(ROIComponent.LINE_CAP);
		Iterator<EnumerationObject> i = l.iterator();
		EnumerationObject o;
		while (i.hasNext()) {
			o = i.next();
			if (o.getValue().equals(lineCap))
				return (LineCap) o.getObject();
		}
		return null;
	}
	
	/**
	 * Adds the ShapeSettings attributes to the shape.
	 * 
	 * @param fig The figure in the measurement tool.
	 * @param shape The shape to add setting to.
	 */
	private void addShapeAttributes(ROIFigure fig, ShapeData shape)
	{
		ShapeSettingsData settings = shape.getShapeSettings();
		Boolean bold;
		Boolean italic;
		String family = ShapeSettingsData.DEFAULT_FONT_FAMILY;
		String fontStyle = ShapeSettingsData.FONT_REGULAR;
		Coord3D coord = fig.getROIShape().getCoord3D();
		int channel = coord.getChannel();
		if (channel >= 0) shape.setC(channel);
		
		if (AttributeKeys.FILL_COLOR.get(fig) != null)
		{
			Color c = AttributeKeys.FILL_COLOR.get(fig);
			settings.setFill(c);
		}
		if (MeasurementAttributes.STROKE_COLOR.get(fig) != null)
			settings.setStroke(
					MeasurementAttributes.STROKE_COLOR.get(fig));
		if (MeasurementAttributes.STROKE_WIDTH.get(fig) != null)
			settings.setStrokeWidth(
					MeasurementAttributes.STROKE_WIDTH.get(fig));
		if (MeasurementAttributes.FONT_FACE.get(fig) != null) {
			family = UIUtilities.convertFont(
					MeasurementAttributes.FONT_FACE.get(fig).getName());
		}
		settings.setFontFamily(getFontFamily(family));
		if (MeasurementAttributes.FONT_SIZE.get(fig) != null)
			settings.setFontSize(
					MeasurementAttributes.FONT_SIZE.get(fig).intValue());
		else
			settings.setFontSize(ShapeSettingsData.DEFAULT_FONT_SIZE);
		bold = MeasurementAttributes.FONT_BOLD.get(fig);
		italic = MeasurementAttributes.FONT_ITALIC.get(fig);
		if (bold != null) {
			if (bold.booleanValue()) {
				if (italic != null && italic.booleanValue()) {
					fontStyle = ShapeSettingsData.FONT_BOLD_ITALIC;
				} else fontStyle = ShapeSettingsData.FONT_BOLD;
			} else {
				if (italic != null && italic.booleanValue()) {
					fontStyle = ShapeSettingsData.FONT_ITALIC;
				} else fontStyle = ShapeSettingsData.FONT_REGULAR;
			}
		} else if (italic != null) {
			if (italic.booleanValue()) {
				if (bold != null && bold.booleanValue()) {
					fontStyle = ShapeSettingsData.FONT_BOLD_ITALIC;
				} else fontStyle = ShapeSettingsData.FONT_ITALIC;
			} else {
				if (bold != null && bold.booleanValue()) {
					fontStyle = ShapeSettingsData.FONT_BOLD;
				} else fontStyle = ShapeSettingsData.FONT_REGULAR;
			}
		}
		
		settings.setFontStyle(getFontStyle(fontStyle));
		Integer value = MeasurementAttributes.STROKE_CAP.get(fig);
		if (value != null) {
			switch (value.intValue()) {
				case BasicStroke.CAP_BUTT:
				default:
					settings.setLineCap(
							getLineCap(ShapeSettingsData.LINE_CAP_BUTT));
					break;
				case BasicStroke.CAP_ROUND:
					settings.setLineCap(
							getLineCap(ShapeSettingsData.LINE_CAP_ROUND));
					break;
				case BasicStroke.CAP_SQUARE:
					settings.setLineCap(
							getLineCap(ShapeSettingsData.LINE_CAP_SQUARE));
			}
		}
	}
	
	/**
	 * Converts an AffineTransform into an SVG transform attribute value as
	 * specified in
	 * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
	 */
	private double[] toTransform(AffineTransform t)
			throws ParsingException
	{
		double[] matrix = new double[6];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = 0;
		}
		t.getMatrix(matrix);
		return matrix;
	}

	/** Creates a new instance. */
	OutputServerStrategy() {}

	/**
	 * Writes the ROI from the ROI component to the server. 
	 * 
	 * @param component See above.
	 * @param image The image the ROI is on.
	 * @param ownerID The identifier of the owner.
	 * @param enumerations The enumerations to use for shape settings.
	 * @throws Exception If an error occurred while parsing the ROI.
	 */
	List<ROIData> writeROI(ROIComponent component, ImageData image, 
			long ownerID, Map<Integer, List<EnumerationObject>> enumerations)
		throws Exception
	{
		this.component = component;
		this.enumerations = enumerations;
		ROIList = new ArrayList<ROIData>();
		parseROI(image, ownerID);
		return ROIList;
	}

}