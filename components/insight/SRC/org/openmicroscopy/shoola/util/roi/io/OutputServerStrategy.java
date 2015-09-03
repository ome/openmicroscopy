/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.io;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.geom.BezierPath;

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
import omero.gateway.model.EllipseData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LineData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.ShapeSettingsData;
import omero.gateway.model.TextData;
import omero.gateway.model.RectangleData;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

/**
 * Handles ROI from server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class OutputServerStrategy 
{
	
	/** The ROIComponent to serialize. */
	private ROIComponent component;
	
	/** The list of ROI to be supplied to the server. */
	private List<ROIData>  ROIList;
	
	/**
	 * Parses the ROI in the ROIComponent to create the appropriate ROIData 
	 * object to supply to the server.
	 * 
	 * @param image The image the ROI is on.
	 * @param index One of the constants defined by {@link ROIComponent} class.
	 * @param userID The id of the user currently logged in.
	 * @throws Exception 
	 */
	private void parseROI(ImageData image, int index, long userID) 
		throws Exception
	{
		TreeMap<Long, ROI> map = component.getROIMap();
		Iterator<ROI> i = map.values().iterator();
		ROI roi;
		switch (index) {
			case ROIComponent.ANNOTATE:
				while (i.hasNext())
				{
					roi = i.next();
					if (roi.canAnnotate())
						ROIList.add(createServerROI(roi, image));
				}
				break;
			case ROIComponent.EDIT:
				while (i.hasNext())
				{
					roi = i.next();
					if (roi.canEdit())
						ROIList.add(createServerROI(roi, image));
				}
				break;
			case ROIComponent.DELETE:
				while (i.hasNext())
				{
					roi = i.next();
					if (roi.canDelete()) 
						ROIList.add(createServerROI(roi, image));
				}
				break;
			case ROIComponent.DELETE_MINE:
				while (i.hasNext())
				{
					roi = i.next();
					if (roi.canDelete()) {
						if (roi.getOwnerID() < 0 || roi.getOwnerID() == userID)
							ROIList.add(createServerROI(roi, image));
					}
				}
				break;
			case ROIComponent.DELETE_OTHERS:
				while (i.hasNext())
				{
					roi = i.next();
					if (roi.canDelete() && roi.getOwnerID() >= 0 &&
						roi.getOwnerID() != userID)
						ROIList.add(createServerROI(roi, image));
				}
				break;	
			case ROIComponent.ALL:
				while (i.hasNext())
					ROIList.add(createServerROI(i.next(), image));
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
		if (clientShape.getT() >= 0)
			shape.setT(clientShape.getT());
		if (clientShape.getZ() >= 0)
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
		String ns = (String) roi.getAnnotation(AnnotationKeys.NAMESPACE);
		List<String> list = UIUtilities.CSVToList(
				(String) roi.getAnnotation(AnnotationKeys.KEYWORDS));
		String[] kw = new String[list.size()];
		list.toArray(kw);
		roiData.setNamespaceKeywords(ns, kw);
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
		if (shape.getT() >=0) data.setT(shape.getT());
		if (shape.getZ() >=0) data.setZ(shape.getZ());
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
		List<Point2D.Double> points1 = new LinkedList<Point2D.Double>();
		List<Point2D.Double> points2 = new LinkedList<Point2D.Double>();
		List<Integer> maskList = new LinkedList<Integer>();
		
		BezierPath bezier = fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		PolygonData poly = new PolygonData();
		poly.setVisible(fig.isVisible());
		poly.setPoints(points, points1, points2, maskList);
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
		List<Point2D.Double> points1 = new LinkedList<Point2D.Double>();
		List<Point2D.Double> points2 = new LinkedList<Point2D.Double>();
		List<Integer> maskList =  new LinkedList<Integer>();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		PolylineData line = new PolylineData();
		line.setVisible(fig.isVisible());
		line.setPoints(points, points1, points2, maskList);
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
		List<Point2D.Double> points1 = new LinkedList<Point2D.Double>();
		List<Point2D.Double> points2 = new LinkedList<Point2D.Double>();
		List<Integer> maskList=new LinkedList<Integer>();
		
		BezierPath bezier = fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		PolylineData poly = new PolylineData();
		poly.setVisible(fig.isVisible());
		poly.setPoints(points, points1, points2, maskList);
		if (t != null)
			poly.setTransform(toTransform(t));
		String text = fig.getText();
		if (text != null && text.trim().length() > 0 && 
				!text.equals(ROIFigure.DEFAULT_TEXT))
			poly.setText(fig.getText());
		return poly;	
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
					new LengthI(MeasurementAttributes.STROKE_WIDTH.get(fig), UnitsLength.PIXEL));
		if (MeasurementAttributes.FONT_FACE.get(fig) != null) {
			settings.setFontFamily(UIUtilities.convertFont(
					MeasurementAttributes.FONT_FACE.get(fig).getName()));
		} else
			settings.setFontFamily(ShapeSettingsData.DEFAULT_FONT_FAMILY);
		if (MeasurementAttributes.FONT_SIZE.get(fig) != null)
			settings.setFontSize(
					new LengthI(MeasurementAttributes.FONT_SIZE.get(fig), UnitsLength.POINT));
		else
			settings.setFontSize(new LengthI(ShapeSettingsData.DEFAULT_FONT_SIZE, UnitsLength.POINT));
		bold = MeasurementAttributes.FONT_BOLD.get(fig);
		italic = MeasurementAttributes.FONT_ITALIC.get(fig);
		if (bold != null) {
			if (bold.booleanValue()) {
				if (italic != null && italic.booleanValue()) {
					settings.setFontStyle(ShapeSettingsData.FONT_BOLD_ITALIC);
				} else settings.setFontStyle(ShapeSettingsData.FONT_BOLD);
			} else {
				if (italic != null && italic.booleanValue()) {
					settings.setFontStyle(ShapeSettingsData.FONT_ITALIC);
				} else settings.setFontStyle(ShapeSettingsData.FONT_REGULAR);
			}
		} else if (italic != null) {
			if (italic.booleanValue()) {
				if (bold != null && bold.booleanValue()) {
					settings.setFontStyle(ShapeSettingsData.FONT_BOLD_ITALIC);
				} else settings.setFontStyle(ShapeSettingsData.FONT_ITALIC);
			} else {
				if (bold != null && bold.booleanValue()) {
					settings.setFontStyle(ShapeSettingsData.FONT_BOLD);
				} else settings.setFontStyle(ShapeSettingsData.FONT_REGULAR);
			}
		} else settings.setFontStyle(ShapeSettingsData.FONT_REGULAR);
	}
	
	/**
	 * Converts an AffineTransform into an SVG transform attribute value as
	 * specified in
	 * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
	 */
	private static String toTransform(AffineTransform t)
			throws ParsingException
	{
		StringBuilder buf=new StringBuilder();
		switch (t.getType())
		{
			case AffineTransform.TYPE_IDENTITY:
				buf.append("none");
				break;
			case AffineTransform.TYPE_TRANSLATION:
				// translate(<tx> [<ty>]), specifies a translation by tx and ty.
				// If <ty> is not provided, it is assumed to be zero.
				buf.append("translate(");
				buf.append(toNumber(t.getTranslateX()));
				if (t.getTranslateY()!=0d)
				{
					buf.append(' ');
					buf.append(toNumber(t.getTranslateY()));
				}
				buf.append(')');
				break;
			/*
			 * case AffineTransform.TYPE_GENERAL_ROTATION : case
			 * AffineTransform.TYPE_QUADRANT_ROTATION : case
			 * AffineTransform.TYPE_MASK_ROTATION : // rotate(<rotate-angle> [<cx>
			 * <cy>]), specifies a rotation by // <rotate-angle> degrees about a
			 * given point. // If optional parameters <cx> and <cy> are not
			 * supplied, the // rotate is about the origin of the current user
			 * coordinate // system. The operation corresponds to the matrix //
			 * [cos(a) sin(a) -sin(a) cos(a) 0 0]. // If optional parameters
			 * <cx> and <cy> are supplied, the rotate // is about the point (<cx>,
			 * <cy>). The operation represents the // equivalent of the
			 * following specification: // translate(<cx>, <cy>) rotate(<rotate-angle>) //
			 * translate(-<cx>, -<cy>). buf.append("rotate(");
			 * buf.append(toNumber(t.getScaleX())); buf.append(')'); break;
			 */
			case AffineTransform.TYPE_UNIFORM_SCALE:
				// scale(<sx> [<sy>]), specifies a scale operation by sx
				// and sy. If <sy> is not provided, it is assumed to be equal
				// to <sx>.
				buf.append("scale(");
				buf.append(toNumber(t.getScaleX()));
				buf.append(')');
				break;
			case AffineTransform.TYPE_GENERAL_SCALE:
			case AffineTransform.TYPE_MASK_SCALE:
				// scale(<sx> [<sy>]), specifies a scale operation by sx
				// and sy. If <sy> is not provided, it is assumed to be equal
				// to <sx>.
				buf.append("scale(");
				buf.append(toNumber(t.getScaleX()));
				buf.append(' ');
				buf.append(toNumber(t.getScaleY()));
				buf.append(')');
				break;
			default:
				// matrix(<a> <b> <c> <d> <e> <f>), specifies a transformation
				// in the form of a transformation matrix of six values.
				// matrix(a,b,c,d,e,f) is equivalent to applying the
				// transformation matrix [a b c d e f].
				buf.append("matrix(");
				double[] matrix = new double[6];
				t.getMatrix(matrix);
				for (int i = 0; i < matrix.length; i++)
				{
					if (i != 0)
					{
						buf.append(' ');
					}
					buf.append(toNumber(matrix[i]));
				}
				buf.append(')');
				break;
		}
		
		return buf.toString();
	}

	/**
	 * Returns a double array as a number attribute value.
	 * 
	 * @param number the number to convert.
	 * @return See above.
	 */
	private static String toNumber(double number)
	{
		String str = Double.toString(number);
		if (str.endsWith(".0"))
			str = str.substring(0, str.length()-2);
		return str;
	}
	
	/** Creates a new instance. */
	OutputServerStrategy() {}

	/**
	 * Writes the ROI from the ROI component to the server. 
	 * 
	 * @param component See above.
	 * @param image The image the ROI is on.
	 * @param index One of the constants defined by {@link ROIComponent} class.
	 * @param userID The id of the user currently logged in.
	 * @throws Exception If an error occurred while parsing the ROI.
	 */
	List<ROIData> writeROI(ROIComponent component, ImageData image, int index,
			long userID)
		throws Exception
	{
		this.component = component;
		ROIList = new ArrayList<ROIData>();
		parseROI(image, index, userID);
		return ROIList;
	}

}