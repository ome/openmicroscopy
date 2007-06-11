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
import java.awt.Color;
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
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.exception.ROIShapeCreationException;

import org.openmicroscopy.shoola.util.roi.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes;
import org.openmicroscopy.shoola.util.roi.figures.EllipseAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineConnectionAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.PointAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.figures.RectAnnotationFigure;

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
	FigureFactory figureFactory;

	
	public final static String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
	public final static String ROI_NAMESPACE = "http://www.openmicroscopy.org.uk";
	public final static String VERSION_TAG = "version";
	public final static String SVG_VERSION = "1.2";
	public final static String SVG_XLINK_VALUE = "http://www.w3.org/1999/xlink";
	public final static String XLINK_ATTRIBUTE = "xmlns:xlink";
	
	public final static String ROI_VERSION = "1.0";
	public final static String ROISET_TAG = "roiset";
	public final static String ROI_TAG = "roi";
	public final static String ROI_ID_ATTRIBUTE = "id";
	public final static String ROISHAPE_TAG = "roishape";
	public final static String ANNOTATION_TAG = "annotation";
	public final static String DEFS_TAG = "defs";
	public final static String SVG_TAG = "svg";
	public final static String VALUE_TAG = "value";
	public final static String RECT_TAG = "rect";
	public final static String ELLIPSE_TAG = "ellipse";
	public final static String LINE_TAG = "line";
	public final static String TEXT_TAG = "text";
	public final static String POLYLINE_TAG = "polyline";
	public final static String POLYGON_TAG = "polygon";
	public final static String POINT_TAG = "point";

	public final static String DATATYPE_ATTRIBUTE = "type";
	public final static String SIZE_ATTRIBUTE = "size"; 
	public final static String VALUE_ATTRIBUTE = "value";
	public final static String POINTS_ATTRIBUTE = "points";
	public final static String VALUE_NULL = "";
	
	public final static String CONNECTION_TO_ATTRIBUTE = "to";
	public final static String CONNECTION_FROM_ATTRIBUTE = "from";
	public final static String X_ATTRIBUTE = "x";
	public final static String X1_ATTRIBUTE = "x1";
	public final static String X2_ATTRIBUTE = "x2";
	public final static String Y_ATTRIBUTE = "y";
	public final static String Y1_ATTRIBUTE = "y1";
	public final static String Y2_ATTRIBUTE = "y2";
	public final static String CX_ATTRIBUTE = "cx";
	public final static String CY_ATTRIBUTE = "cy";
	public final static String RX_ATTRIBUTE = "rx";
	public final static String RY_ATTRIBUTE = "ry";
	public final static String Z_ATTRIBUTE = "z";
	public final static String C_ATTRIBUTE = "c";
	public final static String T_ATTRIBUTE = "t";
	public final static String WIDTH_ATTRIBUTE = "width";
	public final static String HEIGHT_ATTRIBUTE = "height";
	public final static String RED_ATTRIBUTE = "r";
	public final static String BLUE_ATTRIBUTE = "b";
	public final static String GREEN_ATTRIBUTE = "g";
	public final static String ALPHA_ATTRIBUTE = "a";
		
	public final static String ATTRIBUTE_DATATYPE_STRING = "String";
	public final static String ATTRIBUTE_DATATYPE_DOUBLE = "Double";
	public final static String ATTRIBUTE_DATATYPE_LONG = "Long";
	public final static String ATTRIBUTE_DATATYPE_INTEGER = "Integer";
	public final static String ATTRIBUTE_DATATYPE_BOOLEAN = "Boolean";
	public final static String ATTRIBUTE_DATATYPE_FLOAT = "Float";
	public final static String ATTRIBUTE_DATATYPE_POINT2D = "Point2D";
	public final static String ATTRIBUTE_DATATYPE_ELLIPSE2D = "Ellipse2D";
	public final static String ATTRIBUTE_DATATYPE_RECTANGLE2D = "Rectangle2D";
	public final static String ATTRIBUTE_DATATYPE_COLOUR = "Color";
	public final static String ATTRIBUTE_DATATYPE_COORD3D = "Coord3D";
	public final static String ATTRIBUTE_DATATYPE_ARRAYLIST = "ArrayList";
	
	public final static String SVG_FILL_ATTRIBUTE = "fill";
	public final static String SVG_FILL_OPACITY_ATTRIBUTE = "fill-opacity";
	public final static String SVG_FILL_RULE_ATTRIBUTE = "fill-rule";
	public final static String SVG_STROKE_ATTRIBUTE = "stroke";
	public final static String SVG_STROKE_OPACITY_ATTRIBUTE = "stroke-opacity";
	public final static String SVG_STROKE_WIDTH_ATTRIBUTE = "stroke-width";
	public final static String SVG_STROKE_DASHOFFSET_ATTRIBUTE = "stroke-dashoffset";
	public final static String SVG_STROKE_DASHARRAY_ATTRIBUTE = "stroke-dasharray";
	public final static String SVG_STROKE_LINECAP_ATTRIBUTE = "stroke-linecap";
	public final static String SVG_STROKE_LINEJOIN_ATTRIBUTE = "stroke-linejoin";
	public final static String SVG_STROKE_MITERLIMIT_ATTRIBUTE = "stroke-miterlimit";
	public final static String SVG_COLOR_INTERPOLATION_ATTRIBUTE = "color-interpolation";
	public final static String SVG_COLOR_RENDERING_ATTRIBUTE = "color-rendering";
	public final static String SVG_OPACITY_ATTRIBUTE = "opacity";
	public final static String SVG_MARKER_END_ATTRIBUTE = "marker-end";
	public final static String SVG_MARKER_MID_ATTRIBUTE = "color-rendering";
	public final static String SVG_MARKER_START_ATTRIBUTE = "color-rendering";
	public final static String SVG_FONT_FAMILY_ATTRIBUTE = "font-family";
	public final static String SVG_FONT_SIZE_ATTRIBUTE = "font-size";
	public final static String SVG_FONT_SIZE_ADJUST_ATTRIBUTE = "font-adjust";
	public final static String SVG_FONT_STRETCH_ATTRIBUTE = "font-strech";
	public final static String SVG_FONT_STYLE_ATTRIBUTE = "font-style";
	public final static String SVG_FONT_VARIANT_ATTRIBUTE = "font-variant";
	public final static String SVG_FONT_WEIGHT_ATTRIBUTE = "font-weight";
	public final static String SVG_ALIGNMENT_BASELINE_ATTRIBUTE = "alignment-baseline";
	public final static String SVG_BASELINE_SHIFT_ATTRIBUTE = "baseline-shift";
	public final static String SVG_DIRECTION_ATTRIBUTE = "direction";
	public final static String SVG_DOMINANT_BASELINE_ATTRIBUTE = "dominant-baseline";
	public final static String SVG_GLYPH_ORIENTATION_HORIZONTAL_ATTRIBUTE = "glyph-orientation-horizontal";
	public final static String SVG_GLYPH_ORIENTATION_VERTICAL_ATTRIBUTE = "glyph-orientation-vertical";
	public final static String SVG_KERNING_ATTRIBUTE = "kerning";
	public final static String SVG_LETTER_SPACING_ATTRIBUTE = "letter-spacing";
	public final static String SVG_TEXT_ANCHOR_ATTRIBUTE = "text-anchor";
	public final static String SVG_TEXT_DECORATION_ATTRIBUTE = "text-decoration";
	public final static String SVG_UNICODE_BIDI_ATTRIBUTE = "unicode-bidi";
	public final static String SVG_WORD_SPACING_ATTRIBUTE = "word-spacing";
	public final static String SVG_ROTATE_ATTRIBUTE = "rotate";
	public final static String SVG_TRANSFORM_ATTRIBUTE = "transform";

	private final static Color DEFAULT_TEXT_COLOUR = Color.ORANGE;
	private final static Color DEFAULT_FILL_COLOUR = new Color(255, 255, 255, 0);
	private final static Color DEFAULT_STROKE_COLOUR = new Color(255, 255, 255, 255);
	private final static Color DEFAULT_MEASUREMENT_TEXT_COLOUR = Color.YELLOW;
	
	private final static HashMap<AttributeKey, Object> defaultAttributes;
	static {
		defaultAttributes = new HashMap<AttributeKey, Object>();
		defaultAttributes.put(AttributeKeys.FILL_COLOR, DEFAULT_FILL_COLOUR);
		defaultAttributes.put(AttributeKeys.STROKE_COLOR, DEFAULT_STROKE_COLOUR);
		defaultAttributes.put(AttributeKeys.TEXT_COLOR, DEFAULT_TEXT_COLOUR);
		defaultAttributes.put(AttributeKeys.FONT_SIZE, new Double(10));
		defaultAttributes.put(AttributeKeys.FONT_BOLD, false);
		defaultAttributes.put(AttributeKeys.STROKE_WIDTH, new Double(1.0));
		defaultAttributes.put(AttributeKeys.TEXT, "Text");
		defaultAttributes.put(DrawingAttributes.MEASUREMENTTEXT_COLOUR, DEFAULT_MEASUREMENT_TEXT_COLOUR);
		defaultAttributes.put(DrawingAttributes.SHOWMEASUREMENT, new Boolean(false));
		defaultAttributes.put(DrawingAttributes.SHOWTEXT, new Boolean(false));
	}
	
	private final static HashMap<String, SVGAttributeParser> attributeParserMap;
	static {
		attributeParserMap = new HashMap<String, SVGAttributeParser>();
		attributeParserMap.put(SVG_FILL_ATTRIBUTE, new SVGFillParser());
		attributeParserMap.put(SVG_FILL_OPACITY_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FILL_RULE_ATTRIBUTE, new SVGFillRuleParser());
		attributeParserMap.put(SVG_STROKE_ATTRIBUTE, new SVGStrokeParser());
		attributeParserMap.put(SVG_STROKE_OPACITY_ATTRIBUTE, new SVGStrokeOpacityParser());
		attributeParserMap.put(SVG_STROKE_WIDTH_ATTRIBUTE, new SVGStrokeWidthParser());
		attributeParserMap.put(SVG_STROKE_DASHOFFSET_ATTRIBUTE, new SVGStrokeDashOffsetParser());
		attributeParserMap.put(SVG_STROKE_DASHARRAY_ATTRIBUTE, new SVGStrokeDashArrayParser());
		attributeParserMap.put(SVG_STROKE_LINECAP_ATTRIBUTE, new SVGStrokeLineCapParser());
		attributeParserMap.put(SVG_STROKE_LINEJOIN_ATTRIBUTE, new SVGStrokeLineJoinParser());
		attributeParserMap.put(SVG_STROKE_MITERLIMIT_ATTRIBUTE, new SVGMiterLimitParser());
		attributeParserMap.put(SVG_COLOR_INTERPOLATION_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_COLOR_RENDERING_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_OPACITY_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_END_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_MID_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_START_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_FAMILY_ATTRIBUTE, new SVGFontFamilyParser());
		attributeParserMap.put(SVG_MARKER_START_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_SIZE_ATTRIBUTE, new SVGFontSizeParser());
		attributeParserMap.put(SVG_MARKER_START_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_SIZE_ADJUST_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_STRETCH_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_STYLE_ATTRIBUTE, new SVGFontStyleAttribute());
		attributeParserMap.put(SVG_FONT_VARIANT_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_WEIGHT_ATTRIBUTE, new SVGFontWeightParser());
		attributeParserMap.put(SVG_ALIGNMENT_BASELINE_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_BASELINE_SHIFT_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_DIRECTION_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_DOMINANT_BASELINE_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_GLYPH_ORIENTATION_HORIZONTAL_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_GLYPH_ORIENTATION_VERTICAL_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_KERNING_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_LETTER_SPACING_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_TEXT_ANCHOR_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_TEXT_DECORATION_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_UNICODE_BIDI_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_WORD_SPACING_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_ROTATE_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_TRANSFORM_ATTRIBUTE, new SVGTransformParser());
	}
	
	private final static HashMap<String, Boolean> basicSVGAttribute;
	static {
		basicSVGAttribute = new HashMap<String, Boolean>();
		basicSVGAttribute.put(DATATYPE_ATTRIBUTE, true);
		basicSVGAttribute.put(SIZE_ATTRIBUTE, true);
		basicSVGAttribute.put(VALUE_ATTRIBUTE, true);
		basicSVGAttribute.put(POINTS_ATTRIBUTE, true);
		basicSVGAttribute.put(CONNECTION_TO_ATTRIBUTE, true);
		basicSVGAttribute.put(CONNECTION_FROM_ATTRIBUTE, true);
		basicSVGAttribute.put(X_ATTRIBUTE, true);
		basicSVGAttribute.put(X1_ATTRIBUTE, true);
		basicSVGAttribute.put(X2_ATTRIBUTE, true);
		basicSVGAttribute.put(Y1_ATTRIBUTE, true);
		basicSVGAttribute.put(Y2_ATTRIBUTE, true);
		basicSVGAttribute.put(CX_ATTRIBUTE, true);
		basicSVGAttribute.put(CY_ATTRIBUTE, true);
		basicSVGAttribute.put(RX_ATTRIBUTE, true);
		basicSVGAttribute.put(RY_ATTRIBUTE, true);
		basicSVGAttribute.put(Z_ATTRIBUTE, true);
		basicSVGAttribute.put(C_ATTRIBUTE, true);
		basicSVGAttribute.put(T_ATTRIBUTE, true);
		basicSVGAttribute.put(WIDTH_ATTRIBUTE, true);
		basicSVGAttribute.put(HEIGHT_ATTRIBUTE, true);
		basicSVGAttribute.put(RED_ATTRIBUTE, true);
		basicSVGAttribute.put(BLUE_ATTRIBUTE, true);
		basicSVGAttribute.put(GREEN_ATTRIBUTE, true);
		basicSVGAttribute.put(ALPHA_ATTRIBUTE, true);
	}
	
    /**
     * Maps to all XML elements that are identified by an xml:id.
     */
    private HashMap<String,IXMLElement>	identifiedElements;
    /**
     * Maps to all drawing objects from the XML elements they were created from.
     */
    private HashMap<IXMLElement,Object> elementObjects;
    
    /**
     * Holds the document that is currently being read.
     */
    private IXMLElement 				document;
    
	/**
	 * Holds the ROIs which have been created.
	 */
    private ArrayList<ROI> 				roiList;
	
    /**
     * The current coord of the shape being created.
     */
    private Coord3D 					currentCoord;
    
    /**
     * The current roi of the shape being created.
     */
    private long 						currentROI;
    
    /**
     * The ROIComponent 
     */
    private ROIComponent 				component;
    
    private void setCurrentCoord(Coord3D coord)
	{
		currentCoord = coord;
	}
	
	private Coord3D getCurrentCoord()
	{
		return currentCoord;
	}

	private void setCurrentROI(long ROIid)
	{
		currentROI = ROIid;
	}
	
	private long getCurrentROI()
	{
		return currentROI;
	}
	

	private ROI createROI(IXMLElement roiElement, ROIComponent component) 
		throws NoSuchROIException, ParsingException, ROICreationException, 
			ROIShapeCreationException
	{
		if (!roiElement.hasAttribute(ROI_ID_ATTRIBUTE)) return null;
		long id = new Long(roiElement.getAttribute(ROI_ID_ATTRIBUTE,"-1"));
		setCurrentROI(id);
		ROI newROI = null;
		newROI = component.createROI(id);
		ArrayList<IXMLElement> roiShapeList = 
				roiElement.getChildrenNamed(ROISHAPE_TAG);
		int cnt = 0;
		ArrayList<IXMLElement> annotationElementList = 
			roiElement.getChildrenNamed(ANNOTATION_TAG);
		ArrayList<IXMLElement> annotationList;
		for (IXMLElement annotationTagElement : annotationElementList)
		{
			annotationList = annotationTagElement.getChildren();
			for (IXMLElement annotation : annotationList)
				addAnnotation(annotation, newROI);
		}
		cnt = 0;
		ROIShape shape, returnedShape;
		for (IXMLElement roiShape : roiShapeList)
		{
			shape = createROIShape(roiShape, newROI);
			component.addShape(newROI.getID(), shape.getCoord3D(), shape);
			try {
				returnedShape = component.getShape(newROI.getID(), 
												shape.getCoord3D());
			} catch (NoSuchShapeException e) {
				throw new NoSuchROIException("No shape: ", e);
			}
		}
		return newROI;
	}
		
	private ROIShape createROIShape(IXMLElement shapeElement, ROI newROI) 
		throws ParsingException
	{
		int t = new Integer(shapeElement.getAttribute(T_ATTRIBUTE,"0"));
		int z = new Integer(shapeElement.getAttribute(Z_ATTRIBUTE,"0"));
		Coord3D coord = new Coord3D(t,z);
		setCurrentCoord(coord);
		
		IXMLElement figureElement = shapeElement.getFirstChildNamed(SVG_TAG);
		ROIFigure fig = createFigure(figureElement);
		ROIShape shape = new ROIShape(newROI, coord, fig, fig.getBounds());
		ArrayList<IXMLElement> annotationElementList = 
			shapeElement.getChildrenNamed(ANNOTATION_TAG);
		ArrayList<IXMLElement> annotationList;
		for (IXMLElement annotationTagElement : annotationElementList)
		{
			annotationList = annotationTagElement.getChildren();
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
		String key = annotationElement.getName();
		AnnotationKey v = new AnnotationKey(key);
		shape.setAnnotation(v, createAnnotationData(annotationElement));
	}
	
	public Object createAnnotationData(IXMLElement annotationElement)
	{
		String dataType = annotationElement.getAttribute(DATATYPE_ATTRIBUTE, 
					VALUE_NULL);
		if (dataType.equals(ATTRIBUTE_DATATYPE_STRING)) {			
			return annotationElement.getAttribute(VALUE_ATTRIBUTE, VALUE_NULL);
		} else if (dataType.equals(ATTRIBUTE_DATATYPE_INTEGER)) {
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE, 
										VALUE_NULL);
			if (value.equals(VALUE_NULL)) return 0;
			return new Integer(value);
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_BOOLEAN))
		{
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE, 
												VALUE_NULL);
			if (value.equals(VALUE_NULL)) return 0;
			return new Boolean(value);
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_LONG)) {
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE, 
							VALUE_NULL);
			if (value.equals(VALUE_NULL)) return 0;
			return new Long(value);
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_FLOAT)) {
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE, 
														VALUE_NULL);
			if (value.equals(VALUE_NULL)) return 0;
			return new Float(value);
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_DOUBLE)) {
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE,
															VALUE_NULL);
			if (value.equals(VALUE_NULL)) return 0;
			return new Double(value);
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_POINT2D)) {
			String xValue = annotationElement.getAttribute(X_ATTRIBUTE, 
																VALUE_NULL);
			String yValue = annotationElement.getAttribute(Y_ATTRIBUTE, 
															VALUE_NULL);
			if (xValue.equals(VALUE_NULL) || yValue.equals(VALUE_NULL))
				return new Point2D.Double(0,0);
			return new Point2D.Double(new Double(xValue), new Double(yValue));
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_RECTANGLE2D) ||
				dataType.equals(ATTRIBUTE_DATATYPE_ELLIPSE2D)) {
			String xValue = annotationElement.getAttribute(X_ATTRIBUTE, 
														VALUE_NULL);
			String yValue = annotationElement.getAttribute(Y_ATTRIBUTE, 
														VALUE_NULL);
			String widthValue = annotationElement.getAttribute(WIDTH_ATTRIBUTE, 
															VALUE_NULL);
			String heightValue = annotationElement.getAttribute(
									HEIGHT_ATTRIBUTE, VALUE_NULL);
			if (xValue.equals(VALUE_NULL) || yValue.equals(VALUE_NULL) ||
				widthValue.equals(VALUE_NULL) || heightValue.equals(VALUE_NULL))
			{
				if (dataType.equals(ATTRIBUTE_DATATYPE_RECTANGLE2D))
					return new Rectangle2D.Double();
				else
					return new Ellipse2D.Double();
			}
			if (dataType.equals(ATTRIBUTE_DATATYPE_RECTANGLE2D))
				new Rectangle2D.Double(new Double(xValue), new Double(yValue), 
							new Double(widthValue), new Double(heightValue));
			if (dataType.equals(ATTRIBUTE_DATATYPE_ELLIPSE2D))
				return new Ellipse2D.Double(new Double(xValue), 
						new Double(yValue), new Double(widthValue), 
						new Double(heightValue));
		} else if(dataType.equals(ATTRIBUTE_DATATYPE_COORD3D))
		{
			String zValue = annotationElement.getAttribute(Z_ATTRIBUTE, 
													VALUE_NULL);
			String tValue = annotationElement.getAttribute(T_ATTRIBUTE, 
													VALUE_NULL);
			if (zValue.equals(VALUE_NULL) || tValue.equals(VALUE_NULL))
				return new Coord3D(0,0);
			return new Coord3D(new Integer(tValue), new Integer(zValue));
		}
		else if (dataType.equals(ATTRIBUTE_DATATYPE_ARRAYLIST))
		{
			ArrayList list = new ArrayList();
			ArrayList<IXMLElement> arrayListElement = annotationElement.getChildren();
			for(IXMLElement dataElement : arrayListElement)
				list.add(createAnnotationData(dataElement));
			return list;
		}
		return null;
	}
	
	private void addAnnotation(IXMLElement annotationElement, ROI roi)
	{
		String key = annotationElement.getName();
		AnnotationKey annotation = new AnnotationKey(key);
		roi.setAnnotation(annotation, createAnnotationData(annotationElement));
	}
		
	private ROIFigure createFigure(IXMLElement svgElement) 
		throws ParsingException
	{
		IXMLElement parentElement = svgElement.getChildAtIndex(0);
		IXMLElement textElement;
		ROIFigure figure = createParentFigure(parentElement);
		// Check that the parent element is not a text element, as they have not
		// got any other text associated with them.
		if(!parentElement.getName().equals(TEXT_TAG))
		{
			textElement = svgElement.getChildAtIndex(1);
			addTextElementToFigure(figure, textElement);
		}
		addMissingAttributes(figure);
		return figure;
	}
	
	private ROIFigure createParentFigure(IXMLElement figureElement) 
		throws ParsingException
	{
		ROIFigure figure = null;
		
		if(figureElement.getName().equals(RECT_TAG))
			figure = createRectangleFigure(figureElement);
		if(figureElement.getName().equals(LINE_TAG))
			figure = createLineFigure(figureElement);
		if(figureElement.getName().equals(ELLIPSE_TAG))
			figure = createEllipseFigure(figureElement);
		if(figureElement.getName().equals(POINT_TAG))
			figure = createPointFigure(figureElement);
		if(figureElement.getName().equals(TEXT_TAG))
			figure = createTextFigure(figureElement);
		if(figureElement.getName().equals(POLYLINE_TAG))
			figure = createBezierFigure(figureElement, false);
		if(figureElement.getName().equals(POLYGON_TAG))
			figure = createBezierFigure(figureElement, true);
		return figure;
	}
	
	public BezierAnnotationFigure createBezierFigure(IXMLElement bezierElement, 
												boolean closed)
	{
		BezierAnnotationFigure fig = new BezierAnnotationFigure(closed);
		if (bezierElement.hasAttribute(POINTS_ATTRIBUTE))
		{
			String pointsValues = bezierElement.getAttribute(POINTS_ATTRIBUTE, 
												VALUE_NULL);
			Point2D.Double[] points = toPoints(pointsValues);
			for (int i = 0 ; i < points.length ; i++)
				fig.addNode(new Node(points[i].x, points[i].y));
		}
		addAttributes(fig, bezierElement);
		return fig;
	}
	
	private MeasureTextFigure createTextFigure(IXMLElement textElement)
	{
		String xValue = textElement.getAttribute(X_ATTRIBUTE, VALUE_NULL);
		String yValue = textElement.getAttribute(Y_ATTRIBUTE, VALUE_NULL);
		
		MeasureTextFigure textFigure = new MeasureTextFigure(new Double(xValue), 
				new Double(yValue));
		addAttributes(textFigure, textElement);
		return textFigure;
	}
	
	private EllipseAnnotationFigure createEllipseFigure(IXMLElement ellipseElement)
	{
		String cxValue = ellipseElement.getAttribute(CX_ATTRIBUTE, VALUE_NULL);
		String cyValue = ellipseElement.getAttribute(CY_ATTRIBUTE, VALUE_NULL);
		String rxValue = ellipseElement.getAttribute(RX_ATTRIBUTE, VALUE_NULL);
		String ryValue = ellipseElement.getAttribute(RY_ATTRIBUTE, VALUE_NULL);
		double cx = new Double(cxValue);
		double cy = new Double(cyValue);
		double rx = new Double(rxValue);
		double ry = new Double(ryValue);
		
		double x = cx-rx;
		double y = cy-ry;
		double width = rx*2;
		double height = ry*2;
		
		EllipseAnnotationFigure ellipseFigure = new EllipseAnnotationFigure(x, y, width, height);
		addAttributes(ellipseFigure, ellipseElement);
		return ellipseFigure;
	}
	
	private PointAnnotationFigure createPointFigure(IXMLElement pointElement)
	{
		String cxValue = pointElement.getAttribute(CX_ATTRIBUTE, VALUE_NULL);
		String cyValue = pointElement.getAttribute(CY_ATTRIBUTE, VALUE_NULL);
		String rxValue = pointElement.getAttribute(RX_ATTRIBUTE, VALUE_NULL);
		String ryValue = pointElement.getAttribute(RY_ATTRIBUTE, VALUE_NULL);
		double cx = new Double(cxValue);
		double cy = new Double(cyValue);
		double rx = new Double(rxValue);
		double ry = new Double(ryValue);
		
		double x = cx-rx;
		double y = cy-ry;
		double width = rx*2;
		double height = ry*2;
		
		PointAnnotationFigure pointFigure = new PointAnnotationFigure(x, y, width, height);
		addAttributes(pointFigure, pointElement);
		return pointFigure;
	}
	
	private RectAnnotationFigure createRectangleFigure(IXMLElement rectElement)
	{
		String xValue = rectElement.getAttribute(X_ATTRIBUTE, VALUE_NULL);
		String yValue = rectElement.getAttribute(Y_ATTRIBUTE, VALUE_NULL);
		String widthValue = rectElement.getAttribute(WIDTH_ATTRIBUTE, VALUE_NULL);
		String heightValue = rectElement.getAttribute(HEIGHT_ATTRIBUTE, VALUE_NULL);
		
		RectAnnotationFigure rectFigure = new RectAnnotationFigure(new Double(xValue), 
				new Double(yValue), new Double(widthValue), new Double(heightValue));
		addAttributes(rectFigure, rectElement);
		return rectFigure;
	}

	private ROIFigure createLineFigure(IXMLElement lineElement) 
		throws ParsingException
	{
		if(lineElement.hasAttribute(CONNECTION_TO_ATTRIBUTE))
			return createLineConnectionFigure(lineElement);
		else
			return createBasicLineFigure(lineElement);
	}
		
	private LineConnectionAnnotationFigure 
		createLineConnectionFigure(IXMLElement lineElement) 
		throws ParsingException
	{
		LineConnectionAnnotationFigure 
		lineFigure = new LineConnectionAnnotationFigure();
		long toROIid = new Long(lineElement.getAttribute(
								CONNECTION_TO_ATTRIBUTE, VALUE_NULL));
		long fromROIid = new Long(lineElement.getAttribute(
								CONNECTION_FROM_ATTRIBUTE, VALUE_NULL));
		ROI toROI, fromROI;
		try
		{
			toROI = component.getROI(toROIid);
			fromROI = component.getROI(fromROIid);
			ROIFigure toFigure = toROI.getFigure(getCurrentCoord());
			ROIFigure fromFigure = fromROI.getFigure(getCurrentCoord());
			if (lineElement.hasAttribute(POINTS_ATTRIBUTE))
			{
				lineFigure.removeAllNodes();
				String pointsValues = lineElement.getAttribute(POINTS_ATTRIBUTE, 
																	VALUE_NULL);
				Point2D.Double[] points = toPoints(pointsValues);
				for(int i = 0 ; i < points.length ; i++)
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
		catch(Exception e)
		{
			throw new ParsingException("In Line connection figure, " +
					"with ROI :" + getCurrentROI() + 
					" on Coord :" + getCurrentCoord() + 
					" Connection <to>/<from> tag invalid.");
		}
		
		addAttributes(lineFigure, lineElement);
		return lineFigure;
	}
	
	private LineAnnotationFigure createBasicLineFigure(IXMLElement lineElement)
	{
		LineAnnotationFigure lineFigure = new LineAnnotationFigure();
		if(lineElement.hasAttribute(POINTS_ATTRIBUTE))
		{
			lineFigure.removeAllNodes();
			String pointsValues = lineElement.getAttribute(POINTS_ATTRIBUTE, 
														VALUE_NULL);
			Point2D.Double[] points = toPoints(pointsValues);
			for(int i = 0 ; i < points.length ; i++)
				lineFigure.addNode(new Node(points[i].x, points[i].y));
		}
		else
		{
			String x1Value = lineElement.getAttribute(X1_ATTRIBUTE, VALUE_NULL);
			String y1Value = lineElement.getAttribute(Y1_ATTRIBUTE, VALUE_NULL);
			String x2Value = lineElement.getAttribute(X2_ATTRIBUTE, VALUE_NULL);
			String y2Value = lineElement.getAttribute(Y2_ATTRIBUTE, VALUE_NULL);
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
        StringTokenizer tt = new StringTokenizer(str," ,");
        Point2D.Double[] points =new Point2D.Double[tt.countTokens() / 2];
        for (int i=0; i < points.length; i++)  
            points[i] = new Point2D.Double(
                    new Double(tt.nextToken()),
                    new Double(tt.nextToken())
                    );
        return points;
    }
	
	private void addAttributes(ROIFigure figure, IXMLElement figureElement)
	{
		Properties attributes = figureElement.getAttributes();
		Iterator attributeKeys = attributes.keySet().iterator();
		String attribute;
		String value;
		while(attributeKeys.hasNext())
		{
			attribute = (String) attributeKeys.next();
			value = figureElement.getAttribute(attribute, VALUE_NULL);
			addAttribute(figure, figureElement, attribute, value);
		}
	}
	
	private boolean isBasicSVGAttribute(String attribute)
	{
		return basicSVGAttribute.containsKey(attribute);
	}
	
	private boolean isSVGAttribute(String attribute)
	{
		return attributeParserMap.containsKey(attribute);
	}
	
	private void parseAttribute(ROIFigure figure, IXMLElement figureElement, 
							String attribute, String value)
	{
		SVGAttributeParser parser = attributeParserMap.get(attribute);
		parser.parse(figure, figureElement, value);
	}
	
	private void addAttribute(ROIFigure figure, IXMLElement figureElement, 
							String attribute, String value)
	{
		if (isBasicSVGAttribute(attribute)) return;
		if (isSVGAttribute(attribute))
			parseAttribute(figure, figureElement, attribute, value);
	}
	
	private void addTextElementToFigure(ROIFigure figure, 
										IXMLElement textElement)
	{
		String text = textElement.getContent();
		setText(figure, text);
		addAttributes(figure, textElement);
	}
		
	private void setText(ROIFigure fig, String text)
	{
		AttributeKeys.TEXT.set(fig, text);
	}
	
	private void addMissingAttributes(ROIFigure figure)
	{
		Map<AttributeKey, Object> attributes = figure.getAttributes();
		Iterator<AttributeKey> iterator = defaultAttributes.keySet().iterator();
		AttributeKey key;
		while (iterator.hasNext())
		{
			key = iterator.next();
			if (!attributes.containsKey(key))
				key.set(figure, defaultAttributes.get(key));
		}
		
	}
	
	InputStrategy()
	{
		figureFactory = new ROIFigureFactory();
	}
	
	ArrayList<ROI> readROI(InputStream in, ROIComponent component) 
		throws ParsingException, ROIShapeCreationException, NoSuchROIException, 
				ROICreationException 
	{
		roiList = new ArrayList<ROI>();
		this.component = component;
		IXMLParser parser;
	    try 
	    {
	    	parser = XMLParserFactory.createDefaultXMLParser();
	    } 
	    catch (Exception ex) 
	    {
	    	InternalError e = new InternalError("Unable to instantiate NanoXML Parser");
	        e.initCause(ex);
	        throw e;
	    }
	    
	    try 
	    {
	    	IXMLReader reader = new StdXMLReader(in);
		    parser.setReader(reader);
		    document = (IXMLElement) parser.parse();
	    } catch (Exception ex) 
	    {
	    	ParsingException e = new ParsingException(ex.getMessage());
	        e.initCause(ex);
	        throw e;
	    }
	    
	    ArrayList<IXMLElement> roiElements = document.getChildrenNamed(ROI_TAG);
	    
	    int cnt = 0;
	    for(IXMLElement roi : roiElements)
	    	roiList.add(createROI(roi, component));
	    
		return roiList;
	}
	
}


