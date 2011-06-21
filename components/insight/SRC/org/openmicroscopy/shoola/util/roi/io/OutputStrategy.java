/*
 * org.openmicroscopy.shoola.util.roi.io.OutputStrategy 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

// Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

// Third-party libraries
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_GRADIENT;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.STROKE_GRADIENT;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.TRANSFORM;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.TextFigure;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.AttributeKeys.WindingRule;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

// Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class OutputStrategy
{
	
	private final static HashMap<Integer, String>	strokeLinejoinMap;
	static
	{
		strokeLinejoinMap=new HashMap<Integer, String>();
		strokeLinejoinMap.put(BasicStroke.JOIN_MITER, "miter");
		strokeLinejoinMap.put(BasicStroke.JOIN_ROUND, "round");
		strokeLinejoinMap.put(BasicStroke.JOIN_BEVEL, "bevel");
	}
	
	
	private final static HashMap<Integer, String>	strokeLinecapMap;
	static
	{
		strokeLinecapMap=new HashMap<Integer, String>();
		strokeLinecapMap.put(BasicStroke.CAP_BUTT, "butt");
		strokeLinecapMap.put(BasicStroke.CAP_ROUND, "round");
		strokeLinecapMap.put(BasicStroke.CAP_SQUARE, "square");
	}
	
	
	private IXMLElement								document;
	
	
	private IXMLElement								defs;
	
	
	
	/**
	 * This is a counter used to create the next unique identification.
	 */
	private int										nextId;
	
	
	
	/**
	 * In this hash map we store all elements to which we have assigned an id.
	 */
	private Map<IXMLElement, String>			identifiedElements;
	
	/** Creates a new instance. */
	OutputStrategy()
	{
		nextId = 0;
		identifiedElements = new HashMap<IXMLElement, String>();
	}

	/**
	 * Gets a unique ID for the specified element.
	 */
	private String getId(IXMLElement element)
	{
		if (identifiedElements.containsKey(element))
		{
			return identifiedElements.get(element);
		}
		else
		{
			String id=Integer.toString(nextId++, Character.MAX_RADIX);
			identifiedElements.put(element, id);
			return id;
		}
	}
	
	
	public void write(OutputStream out, ROIComponent roiComponent)
			throws ParsingException
	{
		document=
				new XMLElement(IOConstants.ROISET_TAG,
					IOConstants.ROI_NAMESPACE);
		document.setAttribute(IOConstants.VERSION_TAG, IOConstants.ROI_VERSION);
		defs=new XMLElement(IOConstants.DEFS_TAG);
		document.addChild(defs);
		ROIComponent collection=roiComponent;
		
		TreeMap<Long, ROI> roiMap=collection.getROIMap();
		Iterator iterator=roiMap.values().iterator();
		try
		{
			while (iterator.hasNext())
			{
				write(document, (ROI) iterator.next());
			}
			new XMLWriter(out).write(document,true);
		}
		catch (Exception e)
		{
			throw new ParsingException("Cannot create XML output", e);
		}
	}
	
	
	private void write(IXMLElement document, ROI roi) throws ParsingException
	{
		XMLElement roiElement=new XMLElement(IOConstants.ROI_TAG);
		document.addChild(roiElement);
		
		writeROIAnnotations(roiElement, roi);
		
		TreeMap<Coord3D, ROIShape> roiShapes=roi.getShapes();
		Iterator iterator=roiShapes.values().iterator();
		
		while (iterator.hasNext())
			writeROIShape(roiElement, (ROIShape) iterator.next());
	}
	
	/**
	 * Writes the annotations.
	 * 
	 * @param roiElement The element to handle.
	 * @param roi The roi.
	 */
	private void writeROIAnnotations(IXMLElement roiElement, ROI roi)
	{
		roiElement.setAttribute(IOConstants.ROI_ID_ATTRIBUTE, roi.getID()+"");
		Map<AnnotationKey, Object> annotationMap=roi.getAnnotation();
		Iterator i = annotationMap.entrySet().iterator();
		Entry entry;
		AnnotationKey key;
		XMLElement annotation;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			key = (AnnotationKey) entry.getKey();
			annotation = new XMLElement(key.getKey());
			addAttributes(annotation, entry.getValue());
		}
	}
	
	/**
	 * Writes the shapes.
	 * 
	 * @param shapeElement The XML element to handle.
	 * @param shape The shape.
	 */
	private void writeROIShapeAnnotations(IXMLElement shapeElement,
			ROIShape shape)
	{
		Map<AnnotationKey, Object> annotationMap=shape.getAnnotation();
		IXMLElement annotationLeaf=new XMLElement(IOConstants.ANNOTATION_TAG);
		Iterator i = annotationMap.entrySet().iterator();
		Entry entry;
		AnnotationKey key;
		XMLElement annotation;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			key = (AnnotationKey) entry.getKey();
			annotation = new XMLElement(key.getKey());
			addAttributes(annotation, entry.getValue());
			annotationLeaf.addChild(annotation);
		}
		shapeElement.addChild(annotationLeaf);
	}
	
	/**
	 * Adds the attributes to the passed object.
	 * 
	 * @param annotation The annotation to handle.
	 * @param value The value.
	 */
	private void addAttributes(XMLElement annotation, Object value)
	{
		if (value instanceof Double||value instanceof Float
				||value instanceof Integer||value instanceof Long
				||value instanceof Boolean)
		{
			if (value instanceof Double) annotation.setAttribute(
				IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_DOUBLE);
			if (value instanceof Float) annotation.setAttribute(
				IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_FLOAT);
			if (value instanceof Integer) annotation.setAttribute(
				IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_INTEGER);
			if (value instanceof Long) annotation.setAttribute(
				IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_LONG);
			if (value instanceof Boolean) annotation.setAttribute(
				IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_BOOLEAN);
			annotation.setAttribute(IOConstants.VALUE_ATTRIBUTE, value+"");
		}
		else if (value instanceof Color)
		{
			Color colour=(Color) value;
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_COLOUR);
			annotation.setAttribute(IOConstants.RED_ATTRIBUTE, colour.getRed()
					+"");
			annotation.setAttribute(IOConstants.GREEN_ATTRIBUTE, colour
				.getGreen()
					+"");
			annotation.setAttribute(IOConstants.BLUE_ATTRIBUTE, colour
				.getBlue()
					+"");
			annotation.setAttribute(IOConstants.ALPHA_ATTRIBUTE, colour
				.getAlpha()
					+"");
		}
		else if (value instanceof Rectangle2D)
		{
			Rectangle2D object=(Rectangle2D) value;
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D);
			annotation.setAttribute(IOConstants.X_ATTRIBUTE, object.getX()+"");
			annotation.setAttribute(IOConstants.Y_ATTRIBUTE, object.getY()+"");
			annotation.setAttribute(IOConstants.WIDTH_ATTRIBUTE, object
				.getWidth()+"");
			annotation.setAttribute(IOConstants.HEIGHT_ATTRIBUTE, object
				.getHeight()+"");
		}
		else if (value instanceof Ellipse2D)
		{
			Ellipse2D object=(Ellipse2D) value;
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_ELLIPSE2D);
			annotation.setAttribute(IOConstants.X_ATTRIBUTE, object.getX()+"");
			annotation.setAttribute(IOConstants.Y_ATTRIBUTE, object.getY()+"");
			annotation.setAttribute(IOConstants.WIDTH_ATTRIBUTE, object
				.getWidth()+"");
			annotation.setAttribute(IOConstants.HEIGHT_ATTRIBUTE, object
				.getHeight()+"");
		}
		else if (value instanceof String)
		{
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_STRING);
			annotation
				.setAttribute(IOConstants.VALUE_ATTRIBUTE, (String) value);
		}
		else if (value instanceof Point2D)
		{
			Point2D point=(Point2D) value;
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_POINT2D);
			annotation.setAttribute(IOConstants.X_ATTRIBUTE, point.getX()+"");
			annotation.setAttribute(IOConstants.Y_ATTRIBUTE, point.getY()+"");
		}
		else if (value instanceof Coord3D)
		{
			Coord3D coord=(Coord3D) value;
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_COORD3D);
			annotation.setAttribute(IOConstants.T_ATTRIBUTE, coord
				.getTimePoint()	+"");
			annotation.setAttribute(IOConstants.Z_ATTRIBUTE, coord
				.getZSection()+"");
		}
		else if (value instanceof ArrayList)
		{
			ArrayList list=(ArrayList) value;
			annotation.setAttribute(IOConstants.DATATYPE_ATTRIBUTE,
				IOConstants.ATTRIBUTE_DATATYPE_ARRAYLIST);
			annotation.setAttribute(IOConstants.SIZE_ATTRIBUTE, list.size()+"");
			for (int i=0; i<list.size(); i++)
			{
				XMLElement valueElement=new XMLElement(IOConstants.VALUE_TAG);
				Object object=list.get(i);
				addAttributes(valueElement, object);
				annotation.addChild(valueElement);
			}
		}
	}
		
	private void writeROIShape(XMLElement roiElement, ROIShape shape)
			throws ParsingException
	{
		XMLElement shapeElement=new XMLElement(IOConstants.ROISHAPE_TAG);
		roiElement.addChild(shapeElement);
		shapeElement.setAttribute(IOConstants.T_ATTRIBUTE, shape.getCoord3D()
			.getTimePoint()
				+"");
		shapeElement.setAttribute(IOConstants.Z_ATTRIBUTE, shape.getCoord3D()
			.getZSection()
				+"");
		writeROIShapeAnnotations(shapeElement, shape);
		ROIFigure figure=shape.getFigure();
		figure.calculateMeasurements();
		writeFigure(shapeElement, figure);
	}
	
	
	private void writeFigure(XMLElement shapeElement, ROIFigure figure)
			throws ParsingException
	{
		
		if (figure instanceof MeasureRectangleFigure)
		{
			writeSVGHeader(shapeElement);
			writeMeasureRectangleFigure(shapeElement,
				(MeasureRectangleFigure) figure);
			writeTextFigure(shapeElement, (MeasureRectangleFigure) figure);
		}
		else if (figure instanceof MeasureEllipseFigure)
		{
			writeSVGHeader(shapeElement);
			writeMeasureEllipseFigure(shapeElement,
				(MeasureEllipseFigure) figure);
			writeTextFigure(shapeElement, (MeasureEllipseFigure) figure);
		}
		else if (figure instanceof MeasurePointFigure)
		{
			writeSVGHeader(shapeElement);
			writeMeasurePointFigure(shapeElement,
				(MeasurePointFigure) figure);
			writeTextFigure(shapeElement, (MeasurePointFigure) figure);
		}
		else if (figure instanceof MeasureLineConnectionFigure)
		{
			writeSVGHeader(shapeElement);
			writeLineConnectionFigure(shapeElement,
				(MeasureLineConnectionFigure) figure);
			writeTextFigure(shapeElement,
				(MeasureLineConnectionFigure) figure);
		}
		else if (figure instanceof MeasureBezierFigure)
		{
			writeSVGHeader(shapeElement);
			writeMeasureBezierFigure(shapeElement,
				(MeasureBezierFigure) figure);
			writeTextFigure(shapeElement, (MeasureBezierFigure) figure);
		}
		else if (figure instanceof MeasureLineFigure)
		{
			writeSVGHeader(shapeElement);
			writeMeasureLineFigure(shapeElement,
				(MeasureLineFigure) figure);
			writeTextFigure(shapeElement, (MeasureLineFigure) figure);
		}
		else if (figure instanceof MeasureTextFigure)
		{
			writeSVGHeader(shapeElement);
			writeTextFigure(shapeElement, (MeasureTextFigure) figure);
		}
	}
	
	
	private void writeSVGHeader(XMLElement shapeElement)
	{
		XMLElement svgElement=
				new XMLElement(IOConstants.SVG_TAG, IOConstants.SVG_NAMESPACE);
		svgElement.setAttribute(IOConstants.XLINK_ATTRIBUTE,
			IOConstants.SVG_XLINK_VALUE);
		svgElement.setAttribute(IOConstants.VERSION_TAG,
			IOConstants.SVG_VERSION);
		shapeElement.addChild(svgElement);
	}
	
	
	private void writeTextFigure(XMLElement shapeElement, MeasureTextFigure fig)
			throws ParsingException
	{
		writeTextFigure(shapeElement, (TextFigure) fig);
	}
	
	
	private void writeTextFigure(XMLElement shapeElement, TextHolderFigure fig)
			throws ParsingException
	{
		XMLElement textElement=new XMLElement(IOConstants.TEXT_TAG);
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		svgElement.addChild(textElement);
		
		textElement.setContent(fig.getText());
		textElement.setAttribute(IOConstants.X_ATTRIBUTE, fig.getStartPoint()
			.getX()
				+"");
		textElement.setAttribute(IOConstants.Y_ATTRIBUTE, fig.getStartPoint()
			.getY()
				+"");
		writeTransformAttribute(textElement, fig.getAttributes());
		writeFontAttributes(textElement, fig.getAttributes());
	}
	
	
	private void writeLineConnectionFigure(XMLElement shapeElement,
			MeasureLineConnectionFigure fig) throws ParsingException
	{
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		XMLElement lineConnectionElement=new XMLElement(IOConstants.LINE_TAG);
		svgElement.addChild(lineConnectionElement);
		
		ROIFigure startConnection=
				(ROIFigure) fig.getStartConnector().getOwner();
		ROIFigure endConnection=(ROIFigure) fig.getEndConnector().getOwner();
		lineConnectionElement.setAttribute(
			IOConstants.CONNECTION_FROM_ATTRIBUTE, startConnection.getROI()
				.getID()+"");
		lineConnectionElement.setAttribute(IOConstants.CONNECTION_TO_ATTRIBUTE,
			endConnection.getROI().getID()+"");
		if (fig.getNodeCount()==2)
		{
			lineConnectionElement.setAttribute(IOConstants.X1_ATTRIBUTE, fig
				.getNode(0).x[0]+"");
			lineConnectionElement.setAttribute(IOConstants.Y1_ATTRIBUTE, fig
				.getNode(0).y[0]+"");
			lineConnectionElement.setAttribute(IOConstants.X2_ATTRIBUTE, fig
				.getNode(1).x[0]+"");
			lineConnectionElement.setAttribute(IOConstants.Y2_ATTRIBUTE, fig
				.getNode(1).y[0]+"");
		}
		else
		{
			LinkedList<Point2D.Double> points=new LinkedList<Point2D.Double>();
			BezierPath bezier=fig.getBezierPath();
			for (BezierPath.Node node : bezier)
			{
				points.add(new Point2D.Double(node.x[0], node.y[0]));
			}
			String pointsValues=
					toPoints(points.toArray(new Point2D.Double[points.size()]));
			lineConnectionElement.setAttribute(IOConstants.POINTS_ATTRIBUTE,
				pointsValues);
		}
		writeShapeAttributes(lineConnectionElement, fig.getAttributes());
		writeTransformAttribute(lineConnectionElement, fig.getAttributes());
	}
	
	
	private void writeMeasureBezierFigure(XMLElement shapeElement,
			MeasureBezierFigure fig) throws ParsingException
	{
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		if (fig.isClosed()) writePolygonFigure(svgElement, fig);
		else writePolylineFigure(svgElement, fig);
	}
	
	
	private void writePolygonFigure(IXMLElement svgElement,
			MeasureBezierFigure fig) throws ParsingException
	{
		XMLElement bezierElement=new XMLElement(IOConstants.POLYGON_TAG);
		svgElement.addChild(bezierElement);
		
		LinkedList<Point2D.Double> points=new LinkedList<Point2D.Double>();
		LinkedList<Point2D.Double> points1=new LinkedList<Point2D.Double>();
		LinkedList<Point2D.Double> points2=new LinkedList<Point2D.Double>();
		LinkedList<Integer> maskList=new LinkedList<Integer>();
		
		BezierPath bezier=fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		
		String pointsValues =
			toPoints(points.toArray(new Point2D.Double[points.size()]));
		String points1Values =
			toPoints(points1.toArray(new Point2D.Double[points1.size()]));
		String points2Values =
			toPoints(points2.toArray(new Point2D.Double[points2.size()]));
		StringBuffer maskValues = new StringBuffer();
		for( int i = 0 ; i < maskList.size()-1; i++) {
			maskValues.append(maskList.get(i));
			maskValues.append(",");
		}
			
		maskValues.append(maskList.get(maskList.size()-1));

		bezierElement.setAttribute(IOConstants.POINTS_ATTRIBUTE, pointsValues);
		bezierElement.setAttribute(IOConstants.POINTS_CONTROL1_ATTRIBUTE, 
				points1Values);
		bezierElement.setAttribute(IOConstants.POINTS_CONTROL2_ATTRIBUTE, 
				points2Values);
		bezierElement.setAttribute(IOConstants.POINTS_MASK_ATTRIBUTE, 
				maskValues.toString());

		writeShapeAttributes(bezierElement, fig.getAttributes());
		writeTransformAttribute(bezierElement, fig.getAttributes());
	}
	
	
	private void writePolylineFigure(IXMLElement svgElement,
			MeasureBezierFigure fig) throws ParsingException
	{
		XMLElement bezierElement=new XMLElement(IOConstants.POLYLINE_TAG);
		svgElement.addChild(bezierElement);
		
		LinkedList<Point2D.Double> points=new LinkedList<Point2D.Double>();
		LinkedList<Point2D.Double> points1=new LinkedList<Point2D.Double>();
		LinkedList<Point2D.Double> points2=new LinkedList<Point2D.Double>();
		LinkedList<Integer> maskList=new LinkedList<Integer>();
		
		BezierPath bezier=fig.getBezierPath();
		for (BezierPath.Node node : bezier)
		{
			points.add(new Point2D.Double(node.x[0], node.y[0]));
			points1.add(new Point2D.Double(node.x[1], node.y[1]));
			points2.add(new Point2D.Double(node.x[2], node.y[2]));
			maskList.add(Integer.valueOf(node.getMask()));
		}
		
		String pointsValues =
			toPoints(points.toArray(new Point2D.Double[points.size()]));
		String points1Values =
			toPoints(points1.toArray(new Point2D.Double[points1.size()]));
		String points2Values =
			toPoints(points2.toArray(new Point2D.Double[points2.size()]));
		StringBuffer maskValues = new StringBuffer();
		for( int i = 0 ; i < maskList.size()-1; i++) {
			maskValues.append(maskList.get(i));
			maskValues.append(",");
		}
		maskValues.append(maskList.get(maskList.size()-1));

		bezierElement.setAttribute(IOConstants.POINTS_ATTRIBUTE, pointsValues);
		bezierElement.setAttribute(IOConstants.POINTS_CONTROL1_ATTRIBUTE, 
				points1Values);
		bezierElement.setAttribute(IOConstants.POINTS_CONTROL2_ATTRIBUTE, 
				points2Values);
		bezierElement.setAttribute(IOConstants.POINTS_MASK_ATTRIBUTE, 
				maskValues.toString());

	
		writeShapeAttributes(bezierElement, fig.getAttributes());
		writeTransformAttribute(bezierElement, fig.getAttributes());
	}
	
	
	private void writeMeasureLineFigure(XMLElement shapeElement,
			MeasureLineFigure fig) throws ParsingException
	{
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		XMLElement lineElement=new XMLElement(IOConstants.LINE_TAG);
		svgElement.addChild(lineElement);
		
		if (fig.getNodeCount()==2)
		{
			lineElement.setAttribute(IOConstants.X1_ATTRIBUTE,
				fig.getNode(0).x[0]+"");
			lineElement.setAttribute(IOConstants.Y1_ATTRIBUTE,
				fig.getNode(0).y[0]+"");
			lineElement.setAttribute(IOConstants.X2_ATTRIBUTE,
				fig.getNode(1).x[0]+"");
			lineElement.setAttribute(IOConstants.Y2_ATTRIBUTE,
				fig.getNode(1).y[0]+"");
		}
		else
		{
			LinkedList<Point2D.Double> points=new LinkedList<Point2D.Double>();
			BezierPath bezier=fig.getBezierPath();
			for (BezierPath.Node node : bezier)
			{
				points.add(new Point2D.Double(node.x[0], node.y[0]));
			}
			String pointsValues=
					toPoints(points.toArray(new Point2D.Double[points.size()]));
			lineElement
				.setAttribute(IOConstants.POINTS_ATTRIBUTE, pointsValues);
		}
		writeShapeAttributes(lineElement, fig.getAttributes());
		writeTransformAttribute(lineElement, fig.getAttributes());
	}	
	
	private void writeMeasureEllipseFigure(XMLElement shapeElement,
			MeasureEllipseFigure fig) throws ParsingException
	{
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		XMLElement ellipseElement=new XMLElement(IOConstants.ELLIPSE_TAG);
		svgElement.addChild(ellipseElement);
		
		
		
		double rx=fig.getEllipse().getWidth()/2d;
		double ry=fig.getEllipse().getHeight()/2d;
		double cx=fig.getEllipse().getCenterX();
		double cy=fig.getEllipse().getCenterY();
		ellipseElement.setAttribute(IOConstants.CX_ATTRIBUTE, cx+"");
		ellipseElement.setAttribute(IOConstants.CY_ATTRIBUTE, cy+"");
		ellipseElement.setAttribute(IOConstants.RX_ATTRIBUTE, rx+"");
		ellipseElement.setAttribute(IOConstants.RY_ATTRIBUTE, ry+"");
		writeShapeAttributes(ellipseElement, fig.getAttributes());
		writeTransformAttribute(ellipseElement, fig.getAttributes());
	}
		
	private void writeMeasurePointFigure(XMLElement shapeElement,
			MeasurePointFigure fig) throws ParsingException
	{
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		XMLElement ellipseElement=new XMLElement(IOConstants.POINT_TAG);
		svgElement.addChild(ellipseElement);
		
		double cx=fig.getCentre().getX();// fig.getX() + fig.getWidth() / 2d;
		double cy=fig.getCentre().getY();// fig.getY() + fig.getHeight() /
											// 2d;
		double rx=fig.getWidth()/2d;
		double ry=fig.getHeight()/2d;
		ellipseElement.setAttribute(IOConstants.CX_ATTRIBUTE, cx+"");
		ellipseElement.setAttribute(IOConstants.CY_ATTRIBUTE, cy+"");
		ellipseElement.setAttribute(IOConstants.RX_ATTRIBUTE, rx+"");
		ellipseElement.setAttribute(IOConstants.RY_ATTRIBUTE, ry+"");
		writeShapeAttributes(ellipseElement, fig.getAttributes());
		writeTransformAttribute(ellipseElement, fig.getAttributes());
	}
	
	
	private void writeMeasureRectangleFigure(XMLElement shapeElement,
			MeasureRectangleFigure fig) throws ParsingException
	{
		IXMLElement svgElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		XMLElement rectElement=new XMLElement(IOConstants.RECT_TAG);
		svgElement.addChild(rectElement);
		
		rectElement.setAttribute(IOConstants.X_ATTRIBUTE, fig.getX()+"");
		rectElement.setAttribute(IOConstants.Y_ATTRIBUTE, fig.getY()+"");
		rectElement
			.setAttribute(IOConstants.WIDTH_ATTRIBUTE, fig.getWidth()+"");
		rectElement.setAttribute(IOConstants.HEIGHT_ATTRIBUTE, fig.getHeight()
				+"");
		writeShapeAttributes(rectElement, fig.getAttributes());
		writeTransformAttribute(rectElement, fig.getAttributes());
	}
	
	
	protected void writeShapeAttributes(IXMLElement elem,
			Map<AttributeKey, Object> f) throws ParsingException
	{
		
		// 'color'
		// Value: <color> | inherit
		// Initial: depends on user agent
		// Applies to: None. Indirectly affects other properties via
		// currentColor
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified <color> value, except inherit
		//
		// Nothing to do: Attribute 'color' is not needed.
		
		// 'color-rendering'
		// Value: auto | optimizeSpeed | optimizeQuality | inherit
		// Initial: auto
		// Applies to: container elements , graphics elements and 'animateColor'
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		//
		// Nothing to do: Attribute 'color-rendering' is not needed.
		
		// 'fill'
		// Value: <paint> | inherit (See Specifying paint)
		// Initial: black
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: "none", system paint, specified <color> value or
		// absolute IRI
		Object gradient=FILL_GRADIENT.get(f);
		if (gradient!=null)
		{
			IXMLElement gradientElem;
			if (gradient instanceof LinearGradient)
			{
				LinearGradient lg=(LinearGradient) gradient;
				gradientElem=
						createLinearGradient(document, lg.getX1(), lg.getY1(),
							lg.getX2(), lg.getY2(), lg.getStopOffsets(), lg
								.getStopColors(), lg.isRelativeToFigureBounds());
			}
			else
			/* if (gradient instanceof RadialGradient) */{
				RadialGradient rg=(RadialGradient) gradient;
				gradientElem=
						createRadialGradient(document, rg.getCX(), rg.getCY(),
							rg.getR(), rg.getStopOffsets(), rg.getStopColors(),
							rg.isRelativeToFigureBounds());
			}
			String id=getId(gradientElem);
			gradientElem.setAttribute("id", "xml", id);
			defs.addChild(gradientElem);
			writeAttribute(elem, "fill", "url(#"+id+")", "#000");
		}
		else
		{
			writeAttribute(elem, "fill", toColor(MeasurementAttributes.FILL_COLOR.get(f)), "#000");
		}
		
		
		// 'fill-opacity'
		// Value: <opacity-value> | inherit
		// Initial: 1
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "fill-opacity",
			MeasurementAttributes.FILL_COLOR.get(f).getAlpha()/255.0, 1d);
		
		
		// 'fill-rule'
		// Value: nonzero | evenodd | inherit
		// Initial: nonzero
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		if (MeasurementAttributes.WINDING_RULE.get(f)!=WindingRule.NON_ZERO)
		{
			writeAttribute(elem, "fill-rule", "evenodd", "nonzero");
		}
		
		
		// 'stroke'
		// Value: <paint> | inherit (See Specifying paint)
		// Initial: none
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: "none", system paint, specified <color> value
		// or absolute IRI
		gradient=STROKE_GRADIENT.get(f);
		if (gradient!=null)
		{
			IXMLElement gradientElem;
			if (gradient instanceof LinearGradient)
			{
				LinearGradient lg=(LinearGradient) gradient;
				gradientElem=
						createLinearGradient(document, lg.getX1(), lg.getY1(),
							lg.getX2(), lg.getY2(), lg.getStopOffsets(), lg
								.getStopColors(), lg.isRelativeToFigureBounds());
			}
			else
			/* if (gradient instanceof RadialGradient) */{
				RadialGradient rg=(RadialGradient) gradient;
				gradientElem=
						createRadialGradient(document, rg.getCX(), rg.getCY(),
							rg.getR(), rg.getStopOffsets(), rg.getStopColors(),
							rg.isRelativeToFigureBounds());
			}
			String id=getId(gradientElem);
			gradientElem.setAttribute("id", "xml", id);
			defs.addChild(gradientElem);
			writeAttribute(elem, "stroke", "url(#"+id+")", "none");
		}
		else
		{
			writeAttribute(elem, "stroke", toColor(MeasurementAttributes.STROKE_COLOR.get(f)), "none");
		}
		
		
		// 'stroke-dasharray'
		// Value: none | <dasharray> | inherit
		// Initial: none
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes (non-additive)
		// Computed value: Specified value, except inherit
		double[] dashes=MeasurementAttributes.STROKE_DASHES.get(f);
		if (dashes!=null)
		{
			StringBuilder buf=new StringBuilder();
			for (int i=0; i<dashes.length; i++)
			{
				if (i!=0)
				{
					buf.append(',');
				}
				buf.append(toNumber(dashes[i]));
			}
			writeAttribute(elem, "stroke-dasharray", buf.toString(), null);
		}
		
		
		// 'stroke-dashoffset'
		// Value: <length> | inherit
		// Initial: 0
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "stroke-dashoffset", MeasurementAttributes.STROKE_DASH_PHASE.get(f), 0d);
		
		
		// 'stroke-linecap'
		// Value: butt | round | square | inherit
		// Initial: butt
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "stroke-linecap", strokeLinecapMap.get(MeasurementAttributes.STROKE_CAP
			.get(f)), "butt");
		
		
		// 'stroke-linejoin'
		// Value: miter | round | bevel | inherit
		// Initial: miter
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "stroke-linejoin", strokeLinejoinMap
			.get(MeasurementAttributes.STROKE_JOIN.get(f)), "miter");
		
		
		// 'stroke-miterlimit'
		// Value: <miterlimit> | inherit
		// Initial: 4
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "stroke-miterlimit", MeasurementAttributes.STROKE_MITER_LIMIT.get(f), 4d);
		
		
		// 'stroke-opacity'
		// Value: <opacity-value> | inherit
		// Initial: 1
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "stroke-opacity",
			MeasurementAttributes.STROKE_COLOR.get(f).getAlpha()/255.0, 1d);
		
		
		// 'stroke-width'
		// Value: <length> | inherit
		// Initial: 1
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "stroke-width", MeasurementAttributes.STROKE_WIDTH.get(f), 1d);
		
		writeAttribute(elem, MeasurementAttributes.SHOWMEASUREMENT.getKey(), 
			MeasurementAttributes.SHOWMEASUREMENT.get(f).toString(), "false");
		writeAttribute(elem, MeasurementAttributes.SHOWTEXT.getKey(), 
			MeasurementAttributes.SHOWTEXT.get(f).toString(), "false");
	}
	
	
	
	/*
	 * Writes the transform attribute as specified in
	 * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
	 * 
	 */
	protected void writeTransformAttribute(IXMLElement elem,
			Map<AttributeKey, Object> a) throws ParsingException
	{
		AffineTransform t=TRANSFORM.get(a);
		if (t!=null)
		{
			writeAttribute(elem, "transform", toTransform(t), "none");
		}
	}
	
	
	
	/*
	 * Writes font attributes as listed in
	 * http://www.w3.org/TR/SVGMobile12/feature.html#Font
	 */
	private void writeFontAttributes(IXMLElement elem,
			Map<AttributeKey, Object> a) throws ParsingException
	{
		Object gradient=FILL_GRADIENT.get(a);
		if (gradient!=null)
		{
			IXMLElement gradientElem;
			if (gradient instanceof LinearGradient)
			{
				LinearGradient lg=(LinearGradient) gradient;
				gradientElem=
						createLinearGradient(document, lg.getX1(), lg.getY1(),
							lg.getX2(), lg.getY2(), lg.getStopOffsets(), lg
								.getStopColors(), lg.isRelativeToFigureBounds());
			}
			else
			/* if (gradient instanceof RadialGradient) */{
				RadialGradient rg=(RadialGradient) gradient;
				gradientElem=
						createRadialGradient(document, rg.getCX(), rg.getCY(),
							rg.getR(), rg.getStopOffsets(), rg.getStopColors(),
							rg.isRelativeToFigureBounds());
			}
			String id=getId(gradientElem);
			gradientElem.setAttribute("id", "xml", id);
			defs.addChild(gradientElem);
			writeAttribute(elem, "fill", "url(#"+id+")", "#000");
		}
		else
		{
			writeAttribute(elem, "fill", toColor(MeasurementAttributes.TEXT_COLOR.get(a)), "#000");
		}
		
		
		// 'fill-opacity'
		// Value: <opacity-value> | inherit
		// Initial: 1
		// Applies to: shapes and text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "fill-opacity",
			MeasurementAttributes.TEXT_COLOR.get(a).getAlpha()/255.0, 1d);
		
		
		// 'font-family'
		// Value: [[ <family-name> |
		// <generic-family> ],]* [<family-name> |
		// <generic-family>] | inherit
		// Initial: depends on user agent
		// Applies to: text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "font-family", MeasurementAttributes.FONT_FACE.get(a).getFamily(),
			"Dialog");
		
		
		// 'font-size'
		// Value: <absolute-size> | <relative-size> |
		// <length> | inherit
		// Initial: medium
		// Applies to: text content elements
		// Inherited: yes, the computed value is inherited
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Absolute length
		writeAttribute(elem, "font-size", MeasurementAttributes.FONT_SIZE.get(a), 0d);
		
		
		// 'font-style'
		// Value: normal | italic | oblique | inherit
		// Initial: normal
		// Applies to: text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "font-style", (MeasurementAttributes.FONT_ITALIC.get(a)) ? "italic"
				: "normal", "normal");
		
		
		// 'font-variant'
		// Value: normal | small-caps | inherit
		// Initial: normal
		// Applies to: text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: no
		// Computed value: Specified value, except inherit
		writeAttribute(elem, "font-variant", "normal", "normal");
		
		
		// 'font-weight'
		// Value: normal | bold | bolder | lighter | 100 | 200 | 300
		// | 400 | 500 | 600 | 700 | 800 | 900 | inherit
		// Initial: normal
		// Applies to: text content elements
		// Inherited: yes
		// Percentages: N/A
		// Media: visual
		// Animatable: yes
		// Computed value: one of the legal numeric values, non-numeric
		// values shall be converted to numeric values according to the rules
		// defined below.
		writeAttribute(elem, "font-weight", (MeasurementAttributes.FONT_BOLD.get(a)) ? "bold"
				: "normal", "normal");
	}
	
	
	private static String toPath(BezierPath[] paths)
	{
		StringBuilder buf=new StringBuilder();
		
		for (int j=0; j<paths.length; j++)
		{
			BezierPath path=paths[j];
			
			if (path.size()==0)
			{
				// nothing to do
			}
			else if (path.size()==1)
			{
				BezierPath.Node current=path.get(0);
				buf.append("M ");
				buf.append(current.x[0]);
				buf.append(' ');
				buf.append(current.y[0]);
				buf.append(" L ");
				buf.append(current.x[0]);
				buf.append(' ');
				buf.append(current.y[0]+1);
			}
			else
			{
				BezierPath.Node previous;
				BezierPath.Node current;
				
				previous=current=path.get(0);
				buf.append("M ");
				buf.append(current.x[0]);
				buf.append(' ');
				buf.append(current.y[0]);
				for (int i=1, n=path.size(); i<n; i++)
				{
					previous=current;
					current=path.get(i);
					
					if ((previous.mask&BezierPath.C2_MASK)==0)
					{
						if ((current.mask&BezierPath.C1_MASK)==0)
						{
							buf.append(" L ");
							buf.append(current.x[0]);
							buf.append(' ');
							buf.append(current.y[0]);
						}
						else
						{
							buf.append(" Q ");
							buf.append(current.x[1]);
							buf.append(' ');
							buf.append(current.y[1]);
							buf.append(' ');
							buf.append(current.x[0]);
							buf.append(' ');
							buf.append(current.y[0]);
						}
					}
					else
					{
						if ((current.mask&BezierPath.C1_MASK)==0)
						{
							buf.append(" Q ");
							buf.append(current.x[2]);
							buf.append(' ');
							buf.append(current.y[2]);
							buf.append(' ');
							buf.append(current.x[0]);
							buf.append(' ');
							buf.append(current.y[0]);
						}
						else
						{
							buf.append(" C ");
							buf.append(previous.x[2]);
							buf.append(' ');
							buf.append(previous.y[2]);
							buf.append(' ');
							buf.append(current.x[1]);
							buf.append(' ');
							buf.append(current.y[1]);
							buf.append(' ');
							buf.append(current.x[0]);
							buf.append(' ');
							buf.append(current.y[0]);
						}
					}
				}
				if (path.isClosed())
				{
					if (path.size()>1)
					{
						previous=path.get(path.size()-1);
						current=path.get(0);
						
						if ((previous.mask&BezierPath.C2_MASK)==0)
						{
							if ((current.mask&BezierPath.C1_MASK)==0)
							{
								buf.append(" L ");
								buf.append(current.x[0]);
								buf.append(' ');
								buf.append(current.y[0]);
							}
							else
							{
								buf.append(" Q ");
								buf.append(current.x[1]);
								buf.append(' ');
								buf.append(current.y[1]);
								buf.append(' ');
								buf.append(current.x[0]);
								buf.append(' ');
								buf.append(current.y[0]);
							}
						}
						else
						{
							if ((current.mask&BezierPath.C1_MASK)==0)
							{
								buf.append(" Q ");
								buf.append(previous.x[2]);
								buf.append(' ');
								buf.append(previous.y[2]);
								buf.append(' ');
								buf.append(current.x[0]);
								buf.append(' ');
								buf.append(current.y[0]);
							}
							else
							{
								buf.append(" C ");
								buf.append(previous.x[2]);
								buf.append(' ');
								buf.append(previous.y[2]);
								buf.append(' ');
								buf.append(current.x[1]);
								buf.append(' ');
								buf.append(current.y[1]);
								buf.append(' ');
								buf.append(current.x[0]);
								buf.append(' ');
								buf.append(current.y[0]);
							}
						}
					}
					buf.append(" Z");
				}
			}
		}
		return buf.toString();
	}
	
	
	protected void writeAttribute(IXMLElement elem, String name, String value,
			String defaultValue)
	{
		writeAttribute(elem, name, "", value, defaultValue);
	}
	
	
	protected void writeAttribute(IXMLElement elem, String name,
			String namespace, String value, String defaultValue)
	{
		elem.setAttribute(name, value);
	}
	
	
	protected void writeAttribute(IXMLElement elem, String name, Color color,
			Color defaultColor)
	{
		writeAttribute(elem, name, IOConstants.SVG_NAMESPACE, toColor(color),
			toColor(defaultColor));
	}
	
	
	protected void writeAttribute(IXMLElement elem, String name, double value,
			double defaultValue)
	{
		writeAttribute(elem, name, IOConstants.SVG_NAMESPACE, value,
			defaultValue);
	}
	
	
	protected void writeAttribute(IXMLElement elem, String name,
			String namespace, double value, double defaultValue)
	{
		elem.setAttribute(name, toNumber(value));
		
	}
	
	
	
	/**
	 * Returns a double array as a number attribute value.
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
	
	
	
	/**
	 * Returns a Point2D.Double array as a Points attribute value. as specified
	 * in http://www.w3.org/TR/SVGMobile12/shapes.html#PointsBNF
	 */
	private static String toPoints(Point2D.Double[] points)
			throws ParsingException
	{
		StringBuilder buf=new StringBuilder();
		for (int i=0; i<points.length; i++)
		{
			if (i!=0)
			{
				buf.append(", ");
			}
			buf.append(toNumber(points[i].x));
			buf.append(',');
			buf.append(toNumber(points[i].y));
		}
		return buf.toString();
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
	
	
	private static String toColor(Color color)
	{
		if (color==null) { return "none"; }
		
		String value;
		value="000000"+Integer.toHexString(color.getRGB());
		value="#"+value.substring(value.length()-6);
		if (value.charAt(1)==value.charAt(2)&&value.charAt(3)==value.charAt(4)
				&&value.charAt(5)==value.charAt(6))
		{
			value="#"+value.charAt(1)+value.charAt(3)+value.charAt(5);
		}
		return value;
	}
	
	
	protected IXMLElement createLinearGradient(IXMLElement doc, double x1,
			double y1, double x2, double y2, double[] stopOffsets,
			Color[] stopColors, boolean isRelativeToFigureBounds)
			throws ParsingException
	{
		IXMLElement elem=doc.createElement("linearGradient");
		
		writeAttribute(elem, "x1", toNumber(x1), "0");
		writeAttribute(elem, "y1", toNumber(y1), "0");
		writeAttribute(elem, "x2", toNumber(x2), "1");
		writeAttribute(elem, "y2", toNumber(y2), "0");
		writeAttribute(elem, "gradientUnits",
			(isRelativeToFigureBounds) ? "objectBoundingBox" : "useSpaceOnUse",
			"objectBoundingBox");
		
		for (int i=0; i<stopOffsets.length; i++)
		{
			IXMLElement stop=new XMLElement("stop");
			writeAttribute(stop, "offset", toNumber(stopOffsets[i]), null);
			writeAttribute(stop, "stop-color", toColor(stopColors[i]), null);
			writeAttribute(stop, "stop-opacity", toNumber(stopColors[i]
				.getAlpha()/255d), "1");
			elem.addChild(stop);
		}
		
		return elem;
	}
	
	
	protected IXMLElement createRadialGradient(IXMLElement doc, double cx,
			double cy, double r, double[] stopOffsets, Color[] stopColors,
			boolean isRelativeToFigureBounds) throws ParsingException
	{
		IXMLElement elem=doc.createElement("radialGradient");
		
		writeAttribute(elem, "cx", toNumber(cx), "0.5");
		writeAttribute(elem, "cy", toNumber(cy), "0.5");
		writeAttribute(elem, "r", toNumber(r), "0.5");
		writeAttribute(elem, "gradientUnits",
			(isRelativeToFigureBounds) ? "objectBoundingBox" : "useSpaceOnUse",
			"objectBoundingBox");
		
		for (int i=0; i<stopOffsets.length; i++)
		{
			IXMLElement stop=new XMLElement("stop");
			writeAttribute(stop, "offset", toNumber(stopOffsets[i]), null);
			writeAttribute(stop, "stop-color", toColor(stopColors[i]), null);
			writeAttribute(stop, "stop-opacity", toNumber(stopColors[i]
				.getAlpha()/255d), "1");
			elem.addChild(stop);
		}
		
		return elem;
	}
	
}
