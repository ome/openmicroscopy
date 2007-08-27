/*
 * org.openmicroscopy.shoola.util.roi.io.InputStrategy 
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

//Java imports
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.geom.BezierPath.Node;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;

import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGAttributeParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFillParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFillRuleParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontFamilyParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontSizeParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontStyleAttribute;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontWeightParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGMiterLimitParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGNullParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeDashArrayParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeDashOffsetParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeLineCapParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeLineJoinParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeOpacityParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeWidthParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGTransformParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.ShowMeasurementParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.ShowTextParser;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;

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
 * @since OME3.0
 */
public class InputStrategy
{
	
	private final static HashMap<AttributeKey, Object>			defaultAttributes;
	static
	{
		defaultAttributes=new HashMap<AttributeKey, Object>();
		defaultAttributes.put(AttributeKeys.FILL_COLOR,
			IOConstants.DEFAULT_FILL_COLOUR);
		defaultAttributes.put(AttributeKeys.STROKE_COLOR,
			IOConstants.DEFAULT_STROKE_COLOUR);
		defaultAttributes.put(AttributeKeys.TEXT_COLOR,
			IOConstants.DEFAULT_TEXT_COLOUR);
		defaultAttributes.put(AttributeKeys.FONT_SIZE, new Double(10));
		defaultAttributes.put(AttributeKeys.FONT_BOLD, false);
		defaultAttributes.put(AttributeKeys.STROKE_WIDTH, new Double(1.0));
		defaultAttributes.put(AttributeKeys.TEXT, "Text");
		defaultAttributes.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
			IOConstants.DEFAULT_MEASUREMENT_TEXT_COLOUR);
		defaultAttributes.put(MeasurementAttributes.SHOWMEASUREMENT, new Boolean(
			false));
		defaultAttributes.put(DrawingAttributes.SHOWTEXT, new Boolean(false));
	}
	
	
	private final static HashMap<String, SVGAttributeParser>	attributeParserMap;
	static
	{
		attributeParserMap=new HashMap<String, SVGAttributeParser>();
		attributeParserMap.put(IOConstants.ATTRIBUTE_SHOWTEXT,
			new ShowTextParser());
		attributeParserMap.put(IOConstants.ATTRIBUTE_SHOWMEASUREMENT,
			new ShowMeasurementParser());
		attributeParserMap.put(IOConstants.SVG_FILL_ATTRIBUTE,
			new SVGFillParser());
		attributeParserMap.put(IOConstants.SVG_FILL_OPACITY_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FILL_RULE_ATTRIBUTE,
			new SVGFillRuleParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_ATTRIBUTE,
			new SVGStrokeParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_OPACITY_ATTRIBUTE,
			new SVGStrokeOpacityParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
			new SVGStrokeWidthParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE,
			new SVGStrokeDashOffsetParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE,
			new SVGStrokeDashArrayParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_LINECAP_ATTRIBUTE,
			new SVGStrokeLineCapParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE,
			new SVGStrokeLineJoinParser());
		attributeParserMap.put(IOConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE,
			new SVGMiterLimitParser());
		attributeParserMap.put(IOConstants.SVG_COLOR_INTERPOLATION_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_COLOR_RENDERING_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_OPACITY_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_MARKER_END_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_MARKER_MID_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_MARKER_START_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FONT_FAMILY_ATTRIBUTE,
			new SVGFontFamilyParser());
		attributeParserMap.put(IOConstants.SVG_MARKER_START_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FONT_SIZE_ATTRIBUTE,
			new SVGFontSizeParser());
		attributeParserMap.put(IOConstants.SVG_MARKER_START_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FONT_SIZE_ADJUST_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FONT_STRETCH_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FONT_STYLE_ATTRIBUTE,
			new SVGFontStyleAttribute());
		attributeParserMap.put(IOConstants.SVG_FONT_VARIANT_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_FONT_WEIGHT_ATTRIBUTE,
			new SVGFontWeightParser());
		attributeParserMap.put(IOConstants.SVG_ALIGNMENT_BASELINE_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_BASELINE_SHIFT_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_DIRECTION_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_DOMINANT_BASELINE_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(
			IOConstants.SVG_GLYPH_ORIENTATION_HORIZONTAL_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(
			IOConstants.SVG_GLYPH_ORIENTATION_VERTICAL_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_KERNING_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_LETTER_SPACING_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_TEXT_ANCHOR_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_TEXT_DECORATION_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_UNICODE_BIDI_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_WORD_SPACING_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_ROTATE_ATTRIBUTE,
			new SVGNullParser());
		attributeParserMap.put(IOConstants.SVG_TRANSFORM_ATTRIBUTE,
			new SVGTransformParser());
	}
	
	
	private final static HashMap<String, Boolean>				basicSVGAttribute;
	static
	{
		basicSVGAttribute=new HashMap<String, Boolean>();
		basicSVGAttribute.put(IOConstants.DATATYPE_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.SIZE_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.VALUE_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.POINTS_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.CONNECTION_TO_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.CONNECTION_FROM_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.X_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.X1_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.X2_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.Y1_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.Y2_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.CX_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.CY_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.RX_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.RY_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.Z_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.C_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.T_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.WIDTH_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.HEIGHT_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.RED_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.BLUE_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.GREEN_ATTRIBUTE, true);
		basicSVGAttribute.put(IOConstants.ALPHA_ATTRIBUTE, true);
	}
		
	/**
	 * Maps to all XML elements that are identified by an xml:id.
	 */
	private HashMap<String, IXMLElement>					identifiedElements;
	
	/**
	 * Maps to all drawing objects from the XML elements they were created from.
	 */
	private HashMap<IXMLElement, Object>					elementObjects;
		
	/**
	 * Holds the document that is currently being read.
	 */
	private IXMLElement										document;
		
	/**
	 * Holds the ROIs which have been created.
	 */
	private ArrayList<ROI>									roiList;
		
	/**
	 * The current coord of the shape being created.
	 */
	private Coord3D											currentCoord;
		
	/**
	 * The current roi of the shape being created.
	 */
	private long											currentROI;
		
	/**
	 * The ROIComponent 
	 */
	private ROIComponent									component;
	
	private void setCurrentCoord(Coord3D coord)
	{
		currentCoord=coord;
	}
		
	private Coord3D getCurrentCoord()
	{
		return currentCoord;
	}
		
	private void setCurrentROI(long ROIid)
	{
		currentROI=ROIid;
	}
		
	private long getCurrentROI()
	{
		return currentROI;
	}
		
	private ROI createROI(IXMLElement roiElement, ROIComponent component)
			throws NoSuchROIException, ParsingException, ROICreationException
	{
		if (!roiElement.hasAttribute(IOConstants.ROI_ID_ATTRIBUTE)) return null;
		long id=
				new Long(roiElement.getAttribute(IOConstants.ROI_ID_ATTRIBUTE,
					"-1"));
		setCurrentROI(id);
		ROI newROI=null;
		newROI=component.createROI(id);
		ArrayList<IXMLElement> roiShapeList=
				roiElement.getChildrenNamed(IOConstants.ROISHAPE_TAG);
		int cnt=0;
		ArrayList<IXMLElement> annotationElementList=
				roiElement.getChildrenNamed(IOConstants.ANNOTATION_TAG);
		ArrayList<IXMLElement> annotationList;
		for (IXMLElement annotationTagElement : annotationElementList)
		{
			annotationList=annotationTagElement.getChildren();
			for (IXMLElement annotation : annotationList)
				addAnnotation(annotation, newROI);
		}
		cnt=0;
		ROIShape shape, returnedShape;
		for (IXMLElement roiShape : roiShapeList)
		{
			shape=createROIShape(roiShape, newROI);
			shape.getFigure().setMeasurementUnits(component.getMeasurementUnits());
			component.addShape(newROI.getID(), shape.getCoord3D(), shape);
			try
			{
				returnedShape=
						component.getShape(newROI.getID(), shape.getCoord3D());
			}
			catch (NoSuchROIException e)
			{
				throw new NoSuchROIException("No shape: ", e);
			}
		}
		return newROI;
	}
		
	private ROIShape createROIShape(IXMLElement shapeElement, ROI newROI)
			throws ParsingException
	{
		int t=
				new Integer(shapeElement.getAttribute(IOConstants.T_ATTRIBUTE,
					"0"));
		int z=
				new Integer(shapeElement.getAttribute(IOConstants.Z_ATTRIBUTE,
					"0"));
		Coord3D coord=new Coord3D(z, t);
		setCurrentCoord(coord);
		
		IXMLElement figureElement=
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		ROIFigure fig=createFigure(figureElement);
		ROIShape shape=new ROIShape(newROI, coord, fig, fig.getBounds());
		ArrayList<IXMLElement> annotationElementList=
				shapeElement.getChildrenNamed(IOConstants.ANNOTATION_TAG);
		ArrayList<IXMLElement> annotationList;
		for (IXMLElement annotationTagElement : annotationElementList)
		{
			annotationList=annotationTagElement.getChildren();
			for (IXMLElement annotation : annotationList)
				addAnnotation(annotation, shape);
		}
		return shape;
	}
		
	private boolean isAnnotation(String name)
	{
		return (AnnotationKeys.supportedAnnotations.contains(name));
	}
		
	private void addAnnotation(IXMLElement annotationElement, ROIShape shape)
	{
		String key=annotationElement.getName();
		AnnotationKey v=new AnnotationKey(key);
		shape.setAnnotation(v, createAnnotationData(annotationElement));
	}
		
	public Object createAnnotationData(IXMLElement annotationElement)
	{
		String dataType=
				annotationElement.getAttribute(IOConstants.DATATYPE_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_STRING))
		{
			return annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
				IOConstants.VALUE_NULL);
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_INTEGER))
		{
			String value=
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (value.equals(IOConstants.VALUE_NULL)) return 0;
			return new Integer(value);
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_BOOLEAN))
		{
			String value=
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (value.equals(IOConstants.VALUE_NULL)) return 0;
			return new Boolean(value);
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_LONG))
		{
			String value=
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (value.equals(IOConstants.VALUE_NULL)) return 0;
			return new Long(value);
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_FLOAT))
		{
			String value=
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (value.equals(IOConstants.VALUE_NULL)) return 0;
			return new Float(value);
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_DOUBLE))
		{
			String value=
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (value.equals(IOConstants.VALUE_NULL)) return 0;
			return new Double(value);
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_POINT2D))
		{
			String xValue=
					annotationElement.getAttribute(IOConstants.X_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String yValue=
					annotationElement.getAttribute(IOConstants.Y_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (xValue.equals(IOConstants.VALUE_NULL)
					||yValue.equals(IOConstants.VALUE_NULL)) return new Point2D.Double(
				0, 0);
			return new Point2D.Double(new Double(xValue), new Double(yValue));
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D)
				||dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_ELLIPSE2D))
		{
			String xValue=
					annotationElement.getAttribute(IOConstants.X_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String yValue=
					annotationElement.getAttribute(IOConstants.Y_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String widthValue=
					annotationElement.getAttribute(IOConstants.WIDTH_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String heightValue=
					annotationElement.getAttribute(
						IOConstants.HEIGHT_ATTRIBUTE, IOConstants.VALUE_NULL);
			if (xValue.equals(IOConstants.VALUE_NULL)
					||yValue.equals(IOConstants.VALUE_NULL)
					||widthValue.equals(IOConstants.VALUE_NULL)
					||heightValue.equals(IOConstants.VALUE_NULL))
			{
				if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D)) 
					return new Rectangle2D.Double();
				else return new Ellipse2D.Double();
			}
			if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D)) 
				new Rectangle2D.Double(
				new Double(xValue), new Double(yValue), new Double(widthValue),
				new Double(heightValue));
			if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_ELLIPSE2D)) 
				return new Ellipse2D.Double(
				new Double(xValue), new Double(yValue), new Double(widthValue),
				new Double(heightValue));
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_COORD3D))
		{
			String zValue=
					annotationElement.getAttribute(IOConstants.Z_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String tValue=
					annotationElement.getAttribute(IOConstants.T_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (zValue.equals(IOConstants.VALUE_NULL)
					||tValue.equals(IOConstants.VALUE_NULL)) return new Coord3D(
				0, 0);
			return new Coord3D(new Integer(zValue),new Integer(tValue));
		}
		else if (dataType.equals(IOConstants.ATTRIBUTE_DATATYPE_ARRAYLIST))
		{
			ArrayList list=new ArrayList();
			ArrayList<IXMLElement> arrayListElement=
					annotationElement.getChildren();
			for (IXMLElement dataElement : arrayListElement)
				list.add(createAnnotationData(dataElement));
			return list;
		}
		return null;
	}
		
	private void addAnnotation(IXMLElement annotationElement, ROI roi)
	{
		String key=annotationElement.getName();
		AnnotationKey annotation=new AnnotationKey(key);
		roi.setAnnotation(annotation, createAnnotationData(annotationElement));
	}
		
	private ROIFigure createFigure(IXMLElement svgElement)
			throws ParsingException
	{
		IXMLElement parentElement=svgElement.getChildAtIndex(0);
		IXMLElement textElement;
		ROIFigure figure=createParentFigure(parentElement);
		// Check that the parent element is not a text element, as they have not
		// got any other text associated with them.
		if (!parentElement.getName().equals(IOConstants.TEXT_TAG))
		{
			textElement=svgElement.getChildAtIndex(1);
			addTextElementToFigure(figure, textElement);
		}
		addMissingAttributes(figure);
		return figure;
	}
		
	private ROIFigure createParentFigure(IXMLElement figureElement)
			throws ParsingException
	{
		ROIFigure figure=null;
		
		if (figureElement.getName().equals(IOConstants.RECT_TAG)) figure=
				createRectangleFigure(figureElement);
		if (figureElement.getName().equals(IOConstants.LINE_TAG)) figure=
				createLineFigure(figureElement);
		if (figureElement.getName().equals(IOConstants.ELLIPSE_TAG)) figure=
				createEllipseFigure(figureElement);
		if (figureElement.getName().equals(IOConstants.POINT_TAG)) figure=
				createPointFigure(figureElement);
		if (figureElement.getName().equals(IOConstants.TEXT_TAG)) figure=
				createTextFigure(figureElement);
		if (figureElement.getName().equals(IOConstants.POLYLINE_TAG)) figure=
				createBezierFigure(figureElement, false);
		if (figureElement.getName().equals(IOConstants.POLYGON_TAG)) figure=
				createBezierFigure(figureElement, true);
		return figure;
	}	
	
	public MeasureBezierFigure createBezierFigure(IXMLElement bezierElement,
			boolean closed) throws ParsingException
	{
		MeasureBezierFigure fig=new MeasureBezierFigure(closed);
		Point2D.Double[] points=null;
		Point2D.Double[] points1=null;
		Point2D.Double[] points2=null;
		Integer []mask=null;
		if (bezierElement.hasAttribute(IOConstants.POINTS_ATTRIBUTE))
		{
			String pointsValues=
					bezierElement.getAttribute(IOConstants.POINTS_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			points=toPoints(pointsValues);
		}
		if (bezierElement.hasAttribute(IOConstants.POINTS_CONTROL1_ATTRIBUTE))
		{
			String pointsValues=
					bezierElement.getAttribute(IOConstants.POINTS_CONTROL1_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			points1=toPoints(pointsValues);
		}
		if (bezierElement.hasAttribute(IOConstants.POINTS_CONTROL2_ATTRIBUTE))
		{
			String pointsValues=
					bezierElement.getAttribute(IOConstants.POINTS_CONTROL2_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			points2=toPoints(pointsValues);
		}
		if (bezierElement.hasAttribute(IOConstants.POINTS_MASK_ATTRIBUTE))
		{
			String pointsValues=
					bezierElement.getAttribute(IOConstants.POINTS_MASK_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			mask=toIntArray(pointsValues);
		}
			
		if(points==null || points1==null || points2==null || mask == null
				|| (points.length!=points1.length) || (points.length!=points2.length)
				|| (points.length!=mask.length))
		{
			throw new ParsingException("Error parsing points attributes in ROI : " + 
					currentROI + " ROIShape Coord t : " + 
					currentCoord.getTimePoint() + " z : " +
					currentCoord.getZSection());
		}
		
		for (int i=0; i<points.length; i++)
		{
			Node newNode = new Node(mask[i], points[i], points1[i], points2[i]);
			fig.addNode(newNode);
		}
		
		addAttributes(fig, bezierElement);
		return fig;
	}
	
	
	private MeasureTextFigure createTextFigure(IXMLElement textElement)
	{
		String xValue=
				textElement.getAttribute(IOConstants.X_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String yValue=
				textElement.getAttribute(IOConstants.Y_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		
		MeasureTextFigure textFigure=
				new MeasureTextFigure(new Double(xValue), new Double(yValue));
		addAttributes(textFigure, textElement);
		return textFigure;
	}
		
	private MeasureEllipseFigure createEllipseFigure(
			IXMLElement ellipseElement)
	{
		String cxValue=
				ellipseElement.getAttribute(IOConstants.CX_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String cyValue=
				ellipseElement.getAttribute(IOConstants.CY_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String rxValue=
				ellipseElement.getAttribute(IOConstants.RX_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String ryValue=
				ellipseElement.getAttribute(IOConstants.RY_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		double cx=new Double(cxValue);
		double cy=new Double(cyValue);
		double rx=new Double(rxValue);
		double ry=new Double(ryValue);
		
		double x=cx-rx;
		double y=cy-ry;
		double width=rx*2d;
		double height=ry*2d;
		
		MeasureEllipseFigure ellipseFigure=
				new MeasureEllipseFigure(x, y, width, height);
		addAttributes(ellipseFigure, ellipseElement);
		return ellipseFigure;
	}
	
	
	private MeasurePointFigure createPointFigure(IXMLElement pointElement)
	{
		String cxValue=
				pointElement.getAttribute(IOConstants.CX_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String cyValue=
				pointElement.getAttribute(IOConstants.CY_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String rxValue=
				pointElement.getAttribute(IOConstants.RX_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String ryValue=
				pointElement.getAttribute(IOConstants.RY_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		double cx=new Double(cxValue);
		double cy=new Double(cyValue);
		double rx=new Double(rxValue);
		double ry=new Double(ryValue);
		
		double x=cx-rx;
		double y=cy-ry;
		double width=rx*2;
		double height=ry*2;
		
		MeasurePointFigure pointFigure=
				new MeasurePointFigure(x, y, width, height);
		addAttributes(pointFigure, pointElement);
		return pointFigure;
	}
	
	
	private MeasureRectangleFigure createRectangleFigure(IXMLElement rectElement)
	{
		String xValue=
				rectElement.getAttribute(IOConstants.X_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String yValue=
				rectElement.getAttribute(IOConstants.Y_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String widthValue=
				rectElement.getAttribute(IOConstants.WIDTH_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		String heightValue=
				rectElement.getAttribute(IOConstants.HEIGHT_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		
		MeasureRectangleFigure rectFigure=
				new MeasureRectangleFigure(new Double(xValue),
					new Double(yValue), new Double(widthValue), new Double(
						heightValue));
		addAttributes(rectFigure, rectElement);
		return rectFigure;
	}
	
	
	private ROIFigure createLineFigure(IXMLElement lineElement)
			throws ParsingException
	{
		if (lineElement.hasAttribute(IOConstants.CONNECTION_TO_ATTRIBUTE)) 
			return createLineConnectionFigure(lineElement);
		else return createBasicLineFigure(lineElement);
	}
	
	
	private MeasureLineConnectionFigure createLineConnectionFigure(
			IXMLElement lineElement) throws ParsingException
	{
		MeasureLineConnectionFigure lineFigure=
				new MeasureLineConnectionFigure();
		long toROIid=
				new Long(lineElement
					.getAttribute(IOConstants.CONNECTION_TO_ATTRIBUTE,
						IOConstants.VALUE_NULL));
		long fromROIid=
				new Long(lineElement.getAttribute(
					IOConstants.CONNECTION_FROM_ATTRIBUTE,
					IOConstants.VALUE_NULL));
		ROI toROI, fromROI;
		try
		{
			toROI=component.getROI(toROIid);
			fromROI=component.getROI(fromROIid);
			ROIFigure toFigure=toROI.getFigure(getCurrentCoord());
			ROIFigure fromFigure=fromROI.getFigure(getCurrentCoord());
			if (lineElement.hasAttribute(IOConstants.POINTS_ATTRIBUTE))
			{
				lineFigure.removeAllNodes();
				String pointsValues=
						lineElement.getAttribute(IOConstants.POINTS_ATTRIBUTE,
							IOConstants.VALUE_NULL);
				Point2D.Double[] points=toPoints(pointsValues);
				for (int i=0; i<points.length; i++)
					lineFigure.addNode(new Node(points[i].x, points[i].y));
				lineFigure.setStartConnector(toFigure.findCompatibleConnector(
					lineFigure.getStartConnector(), true));
				lineFigure.setEndConnector(fromFigure.findCompatibleConnector(
					lineFigure.getEndConnector(), false));
			}
			else
			{
				lineFigure.setStartConnector(toFigure.findCompatibleConnector(
					lineFigure.getStartConnector(), true));
				lineFigure.setEndConnector(fromFigure.findCompatibleConnector(
					lineFigure.getEndConnector(), false));
			}
		}
		catch (Exception e)
		{
			throw new ParsingException("In Line connection figure, "
					+"with ROI :"+getCurrentROI()+" on Coord :"
					+getCurrentCoord()+" Connection <to>/<from> tag invalid.");
		}
		
		addAttributes(lineFigure, lineElement);
		return lineFigure;
	}
	
	
	private MeasureLineFigure createBasicLineFigure(IXMLElement lineElement)
	{
		MeasureLineFigure lineFigure=new MeasureLineFigure();
		if (lineElement.hasAttribute(IOConstants.POINTS_ATTRIBUTE))
		{
			lineFigure.removeAllNodes();
			String pointsValues=
					lineElement.getAttribute(IOConstants.POINTS_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			Point2D.Double[] points=toPoints(pointsValues);
			for (int i=0; i<points.length; i++)
				lineFigure.addNode(new Node(points[i].x, points[i].y));
		}
		else
		{
			String x1Value=
					lineElement.getAttribute(IOConstants.X1_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String y1Value=
					lineElement.getAttribute(IOConstants.Y1_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String x2Value=
					lineElement.getAttribute(IOConstants.X2_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String y2Value=
					lineElement.getAttribute(IOConstants.Y2_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			lineFigure.removeAllNodes();
			lineFigure.addNode(new Node(new Double(x1Value),
				new Double(y1Value)));
			lineFigure.addNode(new Node(new Double(x2Value),
				new Double(y2Value)));
		}
		addAttributes(lineFigure, lineElement);
		return lineFigure;
	}
	
	
	
	/**
	 * Returns a value as a Point2D.Double array.
	 * as specified in http://www.w3.org/TR/SVGMobile12/shapes.html#PointsBNF
	 * 
	 * @return See above.
	 */
	private Point2D.Double[] toPoints(String str)
	{
		StringTokenizer tt=new StringTokenizer(str, " ,");
		Point2D.Double[] points=new Point2D.Double[tt.countTokens()/2];
		for (int i=0; i<points.length; i++)
			points[i]=
					new Point2D.Double(new Double(tt.nextToken()), new Double(
						tt.nextToken()));
		return points;
	}
	
	/**
	 * Returns a value as a Point2D.Double array.
	 * as specified in http://www.w3.org/TR/SVGMobile12/shapes.html#PointsBNF
	 * 
	 * @return See above.
	 */
	private Integer[] toIntArray(String str)
	{
		StringTokenizer tt=new StringTokenizer(str, " ,");
		Integer[] points=new Integer[tt.countTokens()];
		for (int i=0; i<points.length; i++)
			points[i]=
					new Integer(tt.nextToken());
		return points;
	}
	
	private void addAttributes(ROIFigure figure, IXMLElement figureElement)
	{
		Properties attributes=figureElement.getAttributes();
		Iterator attributeKeys=attributes.keySet().iterator();
		String attribute;
		String value;
		while (attributeKeys.hasNext())
		{
			attribute=(String) attributeKeys.next();
			value=figureElement.getAttribute(attribute, IOConstants.VALUE_NULL);
			addAttribute(figure, figureElement, attribute, value);
		}
	}
	
	/**
	 * Is the string passed a basic SVG Attribute, if so return true.
	 * 
	 * @param attribute String which may be an attribute.
	 * 
	 * @return See above. 
	 */
	private boolean isBasicSVGAttribute(String attribute)
	{
		return basicSVGAttribute.containsKey(attribute);
	}
	
	/**
	 * Is the string passed an SVG Attribute, if so return true.
	 * 
	 * @param attribute String which may be an attribute.
	 * 
	 * @return See above. 
	 */
	private boolean isSVGAttribute(String attribute)
	{
		return attributeParserMap.containsKey(attribute);
	}
		
	private void parseAttribute(ROIFigure figure, IXMLElement figureElement,
			String attribute, String value)
	{
		SVGAttributeParser parser=attributeParserMap.get(attribute);
		parser.parse(figure, figureElement, value);
	}
	
	
	private void addAttribute(ROIFigure figure, IXMLElement figureElement,
			String attribute, String value)
	{
		if (isBasicSVGAttribute(attribute)) return;
		if (isSVGAttribute(attribute)) parseAttribute(figure, figureElement,
			attribute, value);
	}
	
	
	private void addTextElementToFigure(ROIFigure figure,
			IXMLElement textElement)
	{
		String text=textElement.getContent();
		setText(figure, text);
		addAttributes(figure, textElement);
	}
	
	
	private void setText(ROIFigure fig, String text)
	{
		AttributeKeys.TEXT.set(fig, text);
	}
	
	
	private void addMissingAttributes(ROIFigure figure)
	{
		Map<AttributeKey, Object> attributes=figure.getAttributes();
		Iterator<AttributeKey> iterator=defaultAttributes.keySet().iterator();
		AttributeKey key;
		while (iterator.hasNext())
		{
			key=iterator.next();
			if (!attributes.containsKey(key)) key.set(figure, defaultAttributes
				.get(key));
		}
		
	}
		
	InputStrategy()
	{

	}
		
	ArrayList<ROI> readROI(InputStream in, ROIComponent component)
			throws ParsingException, ROICreationException,
			NoSuchROIException
	{
		roiList=new ArrayList<ROI>();
		this.component=component;
		IXMLParser parser;
		try
		{
			parser=XMLParserFactory.createDefaultXMLParser();
		}
		catch (Exception ex)
		{
			InternalError e=
					new InternalError("Unable to instantiate NanoXML Parser");
			e.initCause(ex);
			throw e;
		}
		
		try
		{
			IXMLReader reader=new StdXMLReader(in);
			parser.setReader(reader);
			document=(IXMLElement) parser.parse();
		}
		catch (Exception ex)
		{
			ParsingException e=new ParsingException(ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		
		ArrayList<IXMLElement> roiElements=
				document.getChildrenNamed(IOConstants.ROI_TAG);
		
		int cnt=0;
		for (IXMLElement roi : roiElements)
			roiList.add(createROI(roi, component));
		
		return roiList;
	}
	
}
