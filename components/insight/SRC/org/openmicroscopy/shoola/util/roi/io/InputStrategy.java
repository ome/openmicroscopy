/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.geom.BezierPath.Node;

import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
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

import omero.gateway.model.ShapeSettingsData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class InputStrategy
{
	
	/**
	 * The hashmap of the default values of each object along with the keys 
	 * used.
	 */
	private final static Map<AttributeKey, Object>		defaultAttributes;
	static
	{
		defaultAttributes=new HashMap<AttributeKey, Object>();
		defaultAttributes.put(MeasurementAttributes.FILL_COLOR,
			ShapeSettingsData.DEFAULT_FILL_COLOUR);
		defaultAttributes.put(MeasurementAttributes.STROKE_COLOR,
				ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		defaultAttributes.put(MeasurementAttributes.TEXT_COLOR,
				ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		defaultAttributes.put(MeasurementAttributes.FONT_SIZE, 
				new Double(ShapeSettingsData.DEFAULT_FONT_SIZE));
		defaultAttributes.put(MeasurementAttributes.FONT_BOLD, 
				Boolean.valueOf(false));
		defaultAttributes.put(MeasurementAttributes.STROKE_WIDTH, 
				ShapeSettingsData.DEFAULT_STROKE_WIDTH);
		defaultAttributes.put(MeasurementAttributes.TEXT, 
				ROIFigure.DEFAULT_TEXT);
		defaultAttributes.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR,
				ShapeSettingsData.DEFAULT_STROKE_COLOUR);
		defaultAttributes.put(MeasurementAttributes.SHOWMEASUREMENT, 
				Boolean.valueOf(false));
		defaultAttributes.put(MeasurementAttributes.SHOWTEXT, 
				Boolean.valueOf(false));
		defaultAttributes.put(MeasurementAttributes.SCALE_PROPORTIONALLY,
				Boolean.valueOf(false));
	}
	
	/**
	 * Map used to map attribute with the object to parse that attribute.
	 */
	private final static HashMap<String, SVGAttributeParser> attributeParserMap;
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
	
	/**
	 * The map to determine if an attribute is an annotation or basic SVG 
	 * attribute.
	 */
	private final static HashMap<String, Boolean>		basicSVGAttribute;
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
	 * Holds the document that is currently being read.
	 */
	private IXMLElement										document;
		
	/**
	 * Holds the ROIs which have been created.
	 */
	private List<ROI>									roiList;
		
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
	
	/**
	 * Set the current coordinate being parsed to the coord.
	 * @param coord see above.
	 */
	private void setCurrentCoord(Coord3D coord)
	{
		currentCoord=coord;
	}
		
	/** 
	 * Get the current plane being parsed in the file. 
	 * @return see above.
	 */
	private Coord3D getCurrentCoord()
	{
		return currentCoord;
	}
		
	/**
	 * Set the current ROI id being worked on to id.
	 * @param ROIid see above.
	 */
	private void setCurrentROI(long ROIid)
	{
		currentROI = ROIid;
	}
		
	/**
	 * Get the current id of the ROI being worked on.
	 * @return see above.
	 */
	private long getCurrentROI()
	{
		return currentROI;
	}
		
	/**
	 * Create an ROI from the XML element add to component
	 * @param roiElement the XML element being parsed to create ROI.
	 * @param component the ROI component to add the created ROI to.
	 * @return the new ROI.
	 * @throws NoSuchROIException thrown if there is a bad ref to another ROI
	 * in the XML.
	 * @throws ParsingException thrown if there is badly formed xml
	 * @throws ROICreationException thrown if the ROI cannot be created.
	 */
	private ROI createROI(IXMLElement roiElement, ROIComponent component)
			throws NoSuchROIException, ParsingException, ROICreationException
	{
		if (!roiElement.hasAttribute(IOConstants.ROI_ID_ATTRIBUTE)) return null;
		long id = Long.valueOf(
				roiElement.getAttribute(IOConstants.ROI_ID_ATTRIBUTE, "-1"));
		if (currentROI == id) return null;
		setCurrentROI(id);
		ROI newROI = component.createROI(id);
		List<IXMLElement> roiShapeList =
				roiElement.getChildrenNamed(IOConstants.ROISHAPE_TAG);
		List<IXMLElement> annotationElementList =
				roiElement.getChildrenNamed(IOConstants.ANNOTATION_TAG);
		List<IXMLElement> annotationList;
		for (IXMLElement annotationTagElement : annotationElementList)
		{
			annotationList = annotationTagElement.getChildren();
			for (IXMLElement annotation : annotationList)
				addAnnotation(annotation, newROI);
		}
		ROIShape shape, returnedShape;
		for (IXMLElement roiShape : roiShapeList)
		{
			shape = createROIShape(roiShape, newROI);
			shape.getFigure().setMeasurementUnits(
					component.getMeasurementUnits());
			component.addShape(newROI.getID(), shape.getCoord3D(), shape);
			try
			{
				returnedShape =
						component.getShape(newROI.getID(), shape.getCoord3D());
			}
			catch (NoSuchROIException e)
			{
				throw new NoSuchROIException("No shape: ", e);
			}
		}
		return newROI;
	}
		
	/**
	 * Create an ROIShape from the XML element add to ROI
	 * @param shapeElement the XML element being parsed to create ROIShape.
	 * @param newROI the ROI to add the created ROIShape to.
	 * @return the new ROI.
	 * @throws ParsingException thrown if there is badly formed xml
	 */
	private ROIShape createROIShape(IXMLElement shapeElement, ROI newROI)
			throws ParsingException
	{
		int t = Integer.valueOf(
				shapeElement.getAttribute(IOConstants.T_ATTRIBUTE, "0"));
		int z = Integer.valueOf(
				shapeElement.getAttribute(IOConstants.Z_ATTRIBUTE, "0"));
		Coord3D coord = new Coord3D(z, t);
		setCurrentCoord(coord);
		
		IXMLElement figureElement =
				shapeElement.getFirstChildNamed(IOConstants.SVG_TAG);
		ROIFigure fig = createFigure(figureElement);
		ROIShape shape = new ROIShape(newROI, coord, fig, fig.getBounds());
		List<IXMLElement> annotationElementList =
				shapeElement.getChildrenNamed(IOConstants.ANNOTATION_TAG);
		List<IXMLElement> annotationList;
		for (IXMLElement annotationTagElement : annotationElementList)
		{
			annotationList=annotationTagElement.getChildren();
			for (IXMLElement annotation : annotationList)
				addAnnotation(annotation, shape);
		}
		return shape;
	}
		
	/**
	 * Add the annotation to the shape from the XML element.
	 * @param annotationElement the element.
	 * @param shape the ROI shape.
	 */
	private void addAnnotation(IXMLElement annotationElement, ROIShape shape)
	{
		if (annotationElement == null || shape == null) return;
		String key = annotationElement.getName();
		AnnotationKey v = new AnnotationKey(key);
		shape.setAnnotation(v, createAnnotationData(annotationElement));
	}
		
	/**
	 * Parse the dataType attribute in the annotationElement and create an
	 * object of that type, populate it with values from the data in the 
	 * annotation element.
	 * @param annotationElement see above.
	 * @return see above.
	 */
	Object createAnnotationData(IXMLElement annotationElement)
	{
		String dataType =
				annotationElement.getAttribute(IOConstants.DATATYPE_ATTRIBUTE,
					IOConstants.VALUE_NULL);
		if (IOConstants.ATTRIBUTE_DATATYPE_STRING.equals(dataType))
		{
			return annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
				IOConstants.VALUE_NULL);
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_INTEGER.equals(dataType))
		{
			String value =
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(value)) return 0;
			return Integer.valueOf(value);
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_BOOLEAN.equals(dataType))
		{
			String value =
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(value)) return 0;
			return Boolean.valueOf(value);
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_LONG.equals(dataType))
		{
			String value =
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(value)) return 0;
			return Long.valueOf(value);
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_FLOAT.equals(dataType))
		{
			String value =
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(value)) return 0;
			return Float.valueOf(value);
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_DOUBLE.equals(dataType))
		{
			String value =
					annotationElement.getAttribute(IOConstants.VALUE_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(value)) return 0;
			return Double.valueOf(value);
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_POINT2D.equals(dataType))
		{
			String xValue =
					annotationElement.getAttribute(IOConstants.X_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String yValue =
					annotationElement.getAttribute(IOConstants.Y_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(xValue)
					|| IOConstants.VALUE_NULL.equals(yValue)) 
				return new Point2D.Double(0, 0);
			return new Point2D.Double(Double.valueOf(xValue), 
					Double.valueOf(yValue));
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D.equals(dataType)
				|| IOConstants.ATTRIBUTE_DATATYPE_ELLIPSE2D.equals(dataType))
		{
			String xValue =
					annotationElement.getAttribute(IOConstants.X_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String yValue =
					annotationElement.getAttribute(IOConstants.Y_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String widthValue =
					annotationElement.getAttribute(IOConstants.WIDTH_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String heightValue =
					annotationElement.getAttribute(
						IOConstants.HEIGHT_ATTRIBUTE, IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(yValue)
					|| IOConstants.VALUE_NULL.equals(xValue)
					||IOConstants.VALUE_NULL.equals(widthValue)
					||IOConstants.VALUE_NULL.equals(heightValue))
			{
				if (IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D.equals(dataType))
					return new Rectangle2D.Double();
				return new Ellipse2D.Double();
			}
			if (IOConstants.ATTRIBUTE_DATATYPE_RECTANGLE2D.equals(dataType))
				new Rectangle2D.Double(
				new Double(xValue), new Double(yValue), new Double(widthValue),
				new Double(heightValue));
			if (IOConstants.ATTRIBUTE_DATATYPE_ELLIPSE2D.equals(dataType)) 
				return new Ellipse2D.Double(
				Double.valueOf(xValue), Double.valueOf(yValue),
				Double.valueOf(widthValue), Double.valueOf(heightValue));
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_COORD3D.equals(dataType))
		{
			String zValue =
					annotationElement.getAttribute(IOConstants.Z_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			String tValue =
					annotationElement.getAttribute(IOConstants.T_ATTRIBUTE,
						IOConstants.VALUE_NULL);
			if (IOConstants.VALUE_NULL.equals(zValue)
					||IOConstants.VALUE_NULL.equals(tValue))
				return new Coord3D(0, 0);
			return new Coord3D(Integer.valueOf(zValue), Integer.valueOf(tValue));
		}
		else if (IOConstants.ATTRIBUTE_DATATYPE_ARRAYLIST.equals(dataType))
		{
			List list = new ArrayList();
			List<IXMLElement> arrayListElement =
					annotationElement.getChildren();
			for (IXMLElement dataElement : arrayListElement)
				list.add(createAnnotationData(dataElement));
			return list;
		}
		return null;
	}
	
	/**
	 * Parse the annotationElement, create an attrbute key and add it to the roi.
	 * @param annotationElement 
	 * @param roi see above.
	 */	
	private void addAnnotation(IXMLElement annotationElement, ROI roi)
	{
		String key=annotationElement.getName();
		AnnotationKey annotation = new AnnotationKey(key);
		roi.setAnnotation(annotation, createAnnotationData(annotationElement));
	}
		
	/**
	 * Create an roiFigure of the correct type from the svgElement and 
	 * add any attributes as required.
	 * @param svgElement see above.
	 * @return the new ROIFigure.
	 * @throws ParsingException thrown if the svg is badly formed.
	 */
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
	
	/**
	 * Create the Parent figure, as the parser will allow multiple Figure 
	 * elements to be aggregated together. 
	 * @param figureElement see above.
	 * @return the ROIFogure.
	 * @throws ParsingException thrown if bad svg.
	 */
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
	
	/**
	 * Created from the create Parent figure method, this will create a 
	 * bezier figure.
	 * @param bezierElement the bezier element.
	 * @param closed should this be a piolygon.
	 * @return the figure.
	 * @throws ParsingException thrown if the svg is badly formed.
	 */
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
	
	
	/**
	 * Created from the create Parent figure method, this will create a 
	 * text figure.
	 * @param textElement the text element.
	 * @return the figure.
	 */
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
		
	/**
	 * Created from the create Parent figure method, this will create an 
	 * Ellipse figure.
	 * @param ellipseElement the text element.
	 * @return the figure.
	 */
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
		
		MeasureEllipseFigure ellipseFigure= new MeasureEllipseFigure();
		ellipseFigure.setEllipse(x, y, width, height);
		addAttributes(ellipseFigure, ellipseElement);
		
		return ellipseFigure;
	}
	
	
	/**
	 * Created from the create Parent figure method, this will create an 
	 * Point figure.
	 * @param pointElement the text element.
	 * @return the figure.
	 */
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
	
	
	/**
	 * Created from the create Parent figure method, this will create an 
	 * Rectangle figure.
	 * @param rectElement the text element.
	 * @return the figure.
	 */
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
	
	/**
	 * Created from the create Parent figure method, this will create an 
	 * Line figure either a line figure or conneciton figure depending on
	 * the attributes.
	 * @param lineElement the text element.
	 * @return the figure.
	 * @throws ParsingException thrown if the svg is badly formed.
	 */
	private ROIFigure createLineFigure(IXMLElement lineElement)
			throws ParsingException
	{
		if (lineElement.hasAttribute(IOConstants.CONNECTION_TO_ATTRIBUTE)) 
			return createLineConnectionFigure(lineElement);
		else return createBasicLineFigure(lineElement);
	}
	
	
	/**
	 * Created from the create Parent figure method, this will create  
	 * a line connection figure.
	 * @param lineElement the text element.
	 * @return the figure.
	 * @throws ParsingException thrown if the svg is badly formed.
	 */
	private MeasureLineConnectionFigure createLineConnectionFigure(
			IXMLElement lineElement) throws ParsingException
	{
		MeasureLineConnectionFigure lineFigure=
				new MeasureLineConnectionFigure();
		long toROIid =
				new Long(lineElement
					.getAttribute(IOConstants.CONNECTION_TO_ATTRIBUTE,
						IOConstants.VALUE_NULL));
		long fromROIid =
				new Long(lineElement.getAttribute(
					IOConstants.CONNECTION_FROM_ATTRIBUTE,
					IOConstants.VALUE_NULL));
		ROI toROI, fromROI;
		try
		{
			toROI = component.getROI(toROIid);
			fromROI = component.getROI(fromROIid);
			ROIFigure toFigure = toROI.getFigure(getCurrentCoord());
			ROIFigure fromFigure=fromROI.getFigure(getCurrentCoord());
			if (lineElement.hasAttribute(IOConstants.POINTS_ATTRIBUTE))
			{
				lineFigure.removeAllNodes();
				String pointsValues =
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
	
	
	/**
	 * Created from the create Parent figure method, this will create  
	 * a line figure.
	 * @param lineElement the text element.
	 * @return the figure.
	 */
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
	 * @param str see above.
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
	 * @param str see above.
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
	
	/**
	 * Add the attributes to the figure from the xmlElement.
	 * @param figure see above.
	 * @param figureElement see above.
	 */
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
		
	/**
	 * Parse the attribute in figureElement annd get the 
	 * @param figure see above.
	 * @param figureElement see above.
	 * @param attribute see above.
	 * @param value see above.
	 */
	private void parseAttribute(ROIFigure figure, IXMLElement figureElement,
			String attribute, String value)
	{
		SVGAttributeParser parser=attributeParserMap.get(attribute);
		parser.parse(figure, figureElement, value);
	}
	
	/**
	 * Add an attribute to the figure from the figureElement.
	 * @param figure see above.
	 * @param figureElement see above.
	 * @param attribute see above.
	 * @param value see above.
	 */
	private void addAttribute(ROIFigure figure, IXMLElement figureElement,
			String attribute, String value)
	{
		if (isBasicSVGAttribute(attribute)) return;
		if (isSVGAttribute(attribute)) parseAttribute(figure, figureElement,
			attribute, value);
	}
	
	/**
	 * Add a text figure to a composite figure. ** NOT USED JUST NOW. ** 
	 * @param figure
	 * @param textElement
	 */
	private void addTextElementToFigure(ROIFigure figure,
			IXMLElement textElement)
	{
		String text=textElement.getContent();
		setText(figure, text);
		addAttributes(figure, textElement);
	}
	
	/**
	 * Set the text of the figure from the text.
	 * @param fig see above.
	 * @param text see above.
	 */
	private void setText(ROIFigure fig, String text)
	{
		MeasurementAttributes.TEXT.set(fig, text);
	}
	
	/**
	 * Add any missing basic attributes from the default attributes map, 
	 * to the figure.
	 * @param figure see above.
	 */
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
	
	/** Create instance. */
	InputStrategy()
	{
		reset();
	}
		
	/**
	 * Read the input stream and creat ROI from it, add to ROIComponent.
	 * @param in input stream.
	 * @param component ROIComponent.
	 * @return see above.
	 * @throws ParsingException if any malformed xml encountered. 
	 * @throws ROICreationException if roi cannot be created.
	 * @throws NoSuchROIException if there is an error creating line connection 
	 * figure.
	 */
	List<ROI> readROI(InputStream in, ROIComponent component)
			throws ParsingException, ROICreationException,
			NoSuchROIException
	{
		roiList = new ArrayList<ROI>();
		this.component = component;
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
			IXMLReader reader = new StdXMLReader(in);
			parser.setReader(reader);
			document=(IXMLElement) parser.parse();
		}
		catch (Exception ex)
		{
			ParsingException e = new ParsingException(ex.getMessage());
			e.initCause(ex);
			throw e;
		}
		
		List<IXMLElement> roiElements =
				document.getChildrenNamed(IOConstants.ROI_TAG);
		
		for (IXMLElement roi : roiElements)
			roiList.add(createROI(roi, component));
		
		return roiList;
	}

	/** Indicates to reset the identifier when loading from local file.*/
	void reset()
	{
		currentROI = -1;
		currentCoord = null;
	}
	
}