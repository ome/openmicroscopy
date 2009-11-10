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
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import omero.rtypes;
import omero.model.Image;

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
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

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
 *
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
public class OutputServerStrategy 
{
	
	/** The ROIComponent to serialise. */
	private ROIComponent component;
	
	/** The list of ROI to be supplied to the server. */
	private List<ROIData>  ROIList;
	
	/**
	 * Instantiate the class.
	 */
	OutputServerStrategy()
	{
		
	}
	
	
	/**
	 * Write the ROI from the ROI component to the server. 
	 * @param component See above.
	 * @param image The image the ROI is on.
	 * @throws Exception 
	 */
	public List<ROIData> writeROI(ROIComponent component, ImageData image) throws Exception
	{
		this.component = component;
		this.ROIList = new ArrayList<ROIData>();
		parseROI(image);
		return ROIList;
	}
	
	/**
	 * Parse the ROI in the ROIComponent to create the appropriate ROIDAta 
	 * object to supply to the server.
	 * @param image The image the ROI is on.
	 * @throws Exception 
	 */
	private void parseROI(ImageData image) throws Exception
	{
		TreeMap<Long, ROI> map = component.getROIMap();
		Iterator<ROI> roiIterator = map.values().iterator();
		while(roiIterator.hasNext())
		{
			ROI roi = roiIterator.next();
			ROIData serverROI = createServerROI(roi, image);
			ROIList.add(serverROI);
		}
	}
	
	/**
	 * Creates an ROIData object from an ROI. 
	 * @param roi See above.
	 * @param image The image the ROI is on.
	 * @return See above.
	 * @throws Exception 
	 */
	private ROIData createServerROI(ROI roi, ImageData image) throws Exception
	{
		
		ROIData roiData = new ROIData();
		roiData.setClientSide(roi.isClientSide());
		if(!roi.isClientSide())
			roiData.setId(roi.getID());
		roiData.setImage(image.asImage());
		TreeMap<Coord3D, ROIShape> shapes =  roi.getShapes();
		Iterator<ROIShape> shapeIterator = shapes.values().iterator();
		while(shapeIterator.hasNext())
		{
			ROIShape roiShape = shapeIterator.next();
			ShapeData shape = createShapeData(roiShape);
			addShapeAttributes(roiShape.getFigure(), shape);
			roiData.addShapeData(shape);
		}
		return roiData;
	}
	
	/**
	 * Create the shapedata object for the ROIShape figure object.
	 * @param clientShape See above.
	 * @return See above.
	 * @throws Exception
	 */
	private ShapeData createShapeData(ROIShape clientShape) throws Exception
	{
		ROIFigure fig = clientShape.getFigure();
		if(fig instanceof MeasureBezierFigure)
			return createBezierFigure(clientShape);
		else if(fig instanceof MeasureEllipseFigure)
			return createEllipseFigure(clientShape);
		else if(fig instanceof MeasureLineFigure)
			return createLineFigure(clientShape);
		else if(fig instanceof MeasureMaskFigure)
			return createMaskFigure(clientShape);
		else if(fig instanceof MeasurePointFigure)
			return createPointFigure(clientShape);
		else if(fig instanceof MeasureRectangleFigure)
			return createRectangleFigure(clientShape);
		else if(fig instanceof MeasureTextFigure)
			return createTextFigure(clientShape);
		else
			throw new Exception("ROIShape not supported : " + 
									clientShape.getClass().toString());
	}
	
	/**
	 * Create a bezier figure server side object from a MeasureBezierFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private ShapeData createBezierFigure(ROIShape shape) throws ParsingException
	{
		MeasureBezierFigure fig = (MeasureBezierFigure)shape.getFigure();
		if(fig.isClosed())
			return createPolygonFigure(shape);
		else
			return createPolylineFigure(shape);
	}
	
	/**
	 * Create a ellipse figure server side object from a MeasureEllipseFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private EllipseData createEllipseFigure(ROIShape shape) throws ParsingException
	{
		MeasureEllipseFigure fig = (MeasureEllipseFigure)shape.getFigure();
		double rx=fig.getEllipse().getWidth()/2d;
		double ry=fig.getEllipse().getHeight()/2d;
		double cx=fig.getEllipse().getCenterX();
		double cy=fig.getEllipse().getCenterY();
		
		EllipseData ellipse = new EllipseData(cx, cy, rx, ry); 
		if(!fig.isClientObject())
			ellipse.setId(shape.getROIShapeID());
		ellipse.setDirty(fig.isDirty());
		ellipse.setT(shape.getT());
		ellipse.setZ(shape.getZ());
		ellipse.setText(fig.getText());
		AffineTransform t=TRANSFORM.get(fig);
		if(t!=null)
			ellipse.setTransform(toTransform(t));
		return ellipse;
	}
	
	
	/**
	 * Create a mask figure server side object from a MeasureMaskFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 */
	private MaskData createMaskFigure(ROIShape shape)
	{
		return null;
	}
	
	/**
	 * Create a bezier figure server side object from a MeasurePointFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private PointData createPointFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasurePointFigure fig = (MeasurePointFigure)shape.getFigure();
		double cx=fig.getCentre().getX();
		double cy=fig.getCentre().getY();
		
		PointData point = new PointData(cx, cy); 
		point.setDirty(fig.isDirty());
		point.setT(shape.getT());
		point.setZ(shape.getZ());
		point.setText(fig.getText());
		AffineTransform t=TRANSFORM.get(fig);
		if(t!=null)
			point.setTransform(toTransform(t));
		if(!fig.isClientObject())
			point.setId(shape.getROIShapeID());
		return point;
	}
	
	/**
	 * Create a text figure server side object from a MeasureTextFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private TextData createTextFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasurePointFigure fig = (MeasurePointFigure)shape.getFigure();
		double x=fig.getX();
		double y=fig.getY();
		
		TextData text = new TextData(fig.getText(),x, y); 
		text.setDirty(fig.isDirty());
		text.setT(shape.getT());
		text.setZ(shape.getZ());
		AffineTransform t=TRANSFORM.get(fig);
		if(t!=null)
			text.setTransform(toTransform(t));
		if(!fig.isClientObject())
			text.setId(shape.getROIShapeID());
		return text;
	}
	
	/**
	 * Create a rectangle figure server side object from a MeasureRectangleFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private RectangleData createRectangleFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureRectangleFigure fig = (MeasureRectangleFigure)shape.getFigure();
		double x = fig.getX();
		double y = fig.getY();
		double width = fig.getWidth();
		double height = fig.getHeight();
		
		RectangleData rectangle = new RectangleData(x, y, width, height); 
		rectangle.setDirty(fig.isDirty());
		rectangle.setT(shape.getT());
		rectangle.setZ(shape.getZ());
		rectangle.setText(fig.getText());
		
		AffineTransform t=TRANSFORM.get(fig);
		if(t!=null)
			rectangle.setTransform(toTransform(t));
		if(!fig.isClientObject())
			rectangle.setId(shape.getROIShapeID());
		return rectangle;
	}
	
	/**
	 * Create a polygon figure server side object from a MeasureBezierFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private PolygonData createPolygonFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureBezierFigure fig = (MeasureBezierFigure)shape.getFigure();
		AffineTransform t=TRANSFORM.get(fig);
		List<Point2D.Double> points=new LinkedList<Point2D.Double>();
		List<Point2D.Double> points1=new LinkedList<Point2D.Double>();
		List<Point2D.Double> points2=new LinkedList<Point2D.Double>();
		List<Integer> maskList=new LinkedList<Integer>();
		
		BezierPath bezier=fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		PolygonData poly = new PolygonData();
		poly.setT(shape.getT());
		poly.setZ(shape.getZ());
		poly.setPoints(points, points1, points2, maskList);
		if(t!=null)
			poly.setTransform(toTransform(t));
		poly.setText(fig.getText());
		if(!fig.isClientObject())
			poly.setId(shape.getROIShapeID());
		return poly;	
	}
	
	/**
	 * Create a line figure server side object from a MeasureLineFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private PolylineData createLineFigure(ROIShape shape) 
				throws ParsingException
	{
		MeasureLineFigure fig = (MeasureLineFigure)shape.getFigure();
		AffineTransform t=TRANSFORM.get(fig);
		List<Point2D.Double> points=new LinkedList<Point2D.Double>();
		List<Point2D.Double> points1=new LinkedList<Point2D.Double>();
		List<Point2D.Double> points2=new LinkedList<Point2D.Double>();
		List<Integer> maskList=new LinkedList<Integer>();
		
		BezierPath bezier=fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		PolylineData line = new PolylineData();
		line.setT(shape.getT());
		line.setZ(shape.getZ());
		line.setPoints(points, points1, points2, maskList);
		if(t!=null)
			line.setTransform(toTransform(t));
		line.setText(fig.getText());
		if(!fig.isClientObject())
			line.setId(shape.getROIShapeID());
		return line;
	}
	
	/**
	 * Create a polyline figure server side object from a MeasureBezierFigure 
	 * client side object.
	 * @param shape See above.
	 * @return See above.
	 * @throws ParsingException 
	 */
	private PolylineData createPolylineFigure(ROIShape shape) 
		throws ParsingException
	{
		MeasureBezierFigure fig = (MeasureBezierFigure)shape.getFigure();
		AffineTransform t=TRANSFORM.get(fig);
		List<Point2D.Double> points=new LinkedList<Point2D.Double>();
		List<Point2D.Double> points1=new LinkedList<Point2D.Double>();
		List<Point2D.Double> points2=new LinkedList<Point2D.Double>();
		List<Integer> maskList=new LinkedList<Integer>();
		
		BezierPath bezier=fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		PolylineData poly = new PolylineData();
		poly.setT(shape.getT());
		poly.setZ(shape.getZ());
		poly.setPoints(points, points1, points2, maskList);
		if(t!=null)
			poly.setTransform(toTransform(t));
		poly.setText(fig.getText());
		poly.setText(fig.getText());
		if(!fig.isClientObject())
			poly.setId(shape.getROIShapeID());
		return poly;	
	}
	
	/**
	 * Add the ShapeSettings attributes to the shape.
	 * @param fig The figure in the measurement tool.
	 * @param shape The shape to add setting to.
	 */
	private void addShapeAttributes(ROIFigure fig, ShapeData shape)
	{
		ShapeSettingsData settings = shape.getShapeSettings();
		if(MeasurementAttributes.FILL_COLOR.get(fig)!=null)
			settings.setFillColor(MeasurementAttributes.FILL_COLOR.get(fig));
		if(MeasurementAttributes.STROKE_COLOR.get(fig)!=null)
			settings.setStrokeColor(MeasurementAttributes.STROKE_COLOR.get(fig));
		if(MeasurementAttributes.STROKE_WIDTH.get(fig)!=null)
			settings.setStrokeWidth(MeasurementAttributes.STROKE_WIDTH.get(fig));
		if(MeasurementAttributes.FONT_FACE.get(fig)!=null)
			settings.setFontFamily(MeasurementAttributes.FONT_FACE.get(fig).getName());
		else
			settings.setFontFamily(ShapeSettingsData.DEFAULT_FONT_FAMILY);
		if(MeasurementAttributes.FONT_SIZE.get(fig)!=null)
			settings.setFontSize(MeasurementAttributes.FONT_SIZE.get(fig).intValue());
		else
			settings.setFontSize(ShapeSettingsData.DEFAULT_FONT_SIZE);
		if(MeasurementAttributes.FONT_BOLD.get(fig)!=null)
			settings.setFontWeight(ShapeSettingsData.FONT_BOLD);
		else
			settings.setFontWeight(ShapeSettingsData.DEFAULT_FONT_WEIGHT);
		if(MeasurementAttributes.FONT_ITALIC.get(fig)!=null)
			settings.setFontStyle(ShapeSettingsData.FONT_ITALIC);
		else
			settings.setFontStyle(ShapeSettingsData.DEFAULT_FONT_STYLE);
	}
	
	/*
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
				double[] matrix=new double[6];
				t.getMatrix(matrix);
				for (int i=0; i<matrix.length; i++)
				{
					if (i!=0)
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
	 * @param number the number to convert.
	 * @return See above.
	 */
	private static String toNumber(double number)
	{
		String str=Double.toString(number);
		if (str.endsWith(".0"))
		{
			str=str.substring(0, str.length()-2);
		}
		return str;
	}
	
}

