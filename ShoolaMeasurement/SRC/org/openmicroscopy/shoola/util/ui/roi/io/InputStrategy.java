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
import java.awt.BasicStroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.jhotdraw.draw.AttributeKeys.WindingRule;
import org.jhotdraw.geom.BezierPath.Node;
import org.jhotdraw.samples.svg.SVGAttributeKeys.TextAnchor;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.EllipseAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineConnectionAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.figures.RectAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGAttributeParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFillOpacityParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFillParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGMiterLimitParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGNullParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeOpacityParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeParser;
import org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeWidthParser;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

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
	public final static String ATTRIBUTE_DATATYPE_FLOAT = "Float";
	public final static String ATTRIBUTE_DATATYPE_POINT2D = "Point2D";
	public final static String ATTRIBUTE_DATATYPE_ELLIPSE2D = "Ellipse2D";
	public final static String ATTRIBUTE_DATATYPE_RECTANGLE2D = "Rectangle2D";
	public final static String ATTRIBUTE_DATATYPE_COLOUR = "Color";
	public final static String ATTRIBUTE_DATATYPE_COORD3D = "Coord3D";
	public final static String ATTRIBUTE_DATATYPE_ARRAYLIST = "ArrayList";
	
	private final static String SVG_FILL_ATTRIBUTE = "fill";
	private final static String SVG_FILL_OPACITY_ATTRIBUTE = "fill-opacity";
	private final static String SVG_FILL_RULE_ATTRIBUTE = "fill-rule";
	private final static String SVG_STROKE_ATTRIBUTE = "stroke";
	private final static String SVG_STROKE_OPACITY_ATTRIBUTE = "stroke-opacity";
	private final static String SVG_STROKE_WIDTH_ATTRIBUTE = "stroke-width";
	private final static String SVG_STROKE_DASHOFFSET_ATTRIBUTE = "stroke-dashoffset";
	private final static String SVG_STROKE_LINECAP_ATTRIBUTE = "stroke-linecap";
	private final static String SVG_STROKE_LINEJOIN_ATTRIBUTE = "stroke-linejoin";
	private final static String SVG_STROKE_MITERLIMIT_ATTRIBUTE = "stroke-miterlimit";
	private final static String SVG_COLOR_INTERPOLATION_ATTRIBUTE = "color-interpolation";
	private final static String SVG_COLOR_RENDERING_ATTRIBUTE = "color-rendering";
	private final static String SVG_OPACITY_ATTRIBUTE = "opacity";
	private final static String SVG_MARKER_END_ATTRIBUTE = "marker-end";
	private final static String SVG_MARKER_MID_ATTRIBUTE = "color-rendering";
	private final static String SVG_MARKER_START_ATTRIBUTE = "color-rendering";
	private final static String SVG_FONT_FAMILY_ATTRIBUTE = "font-family";
	private final static String SVG_FONT_SIZE_ATTRIBUTE = "font-size";
	private final static String SVG_FONT_SIZE_ADJUST_ATTRIBUTE = "font-adjust";
	private final static String SVG_FONT_STRETCH_ATTRIBUTE = "font-strech";
	private final static String SVG_FONT_STYLE_ATTRIBUTE = "font-style";
	private final static String SVG_FONT_VARIANT_ATTRIBUTE = "font-variant";
	private final static String SVG_FONT_WEIGHT_ATTRIBUTE = "font-weight";
	private final static String SVG_ALIGNMENT_BASELINE_ATTRIBUTE = "alignment-baseline";
	private final static String SVG_BASELINE_SHIFT_ATTRIBUTE = "baseline-shift";
	private final static String SVG_DIRECTION_ATTRIBUTE = "direction";
	private final static String SVG_DOMINANT_BASELINE_ATTRIBUTE = "dominant-baseline";
	private final static String SVG_GLYPH_ORIENTATION_HORIZONTAL_ATTRIBUTE = "glyph-orientation-horizontal";
	private final static String SVG_GLYPH_ORIENTATION_VERTICAL_ATTRIBUTE = "glyph-orientation-vertical";
	private final static String SVG_KERNING_ATTRIBUTE = "kerning";
	private final static String SVG_LETTER_SPACING_ATTRIBUTE = "letter-spacing";
	private final static String SVG_TEXT_ANCHOR_ATTRIBUTE = "text-anchor";
	private final static String SVG_TEXT_DECORATION_ATTRIBUTE = "text-decoration";
	private final static String SVG_UNICODE_BIDI_ATTRIBUTE = "unicode-bidi";
	private final static String SVG_WORD_SPACING_ATTRIBUTE = "word-spacing";
	private final static String SVG_ROTATE_ATTRIBUTE = "rotate";
	private final static String SVG_TRANSFORM_ATTRIBUTE = "transform";
		
	
	private final static HashMap<String, SVGAttributeParser> attributeParserMap;
	static {
		attributeParserMap = new HashMap<String, SVGAttributeParser>();
		attributeParserMap.put(SVG_FILL_ATTRIBUTE, new SVGFillParser());
		attributeParserMap.put(SVG_FILL_OPACITY_ATTRIBUTE, new SVGFillOpacityParser());
		attributeParserMap.put(SVG_FILL_RULE_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_STROKE_ATTRIBUTE, new SVGStrokeParser());
		attributeParserMap.put(SVG_STROKE_OPACITY_ATTRIBUTE, new SVGStrokeOpacityParser());
		attributeParserMap.put(SVG_STROKE_WIDTH_ATTRIBUTE, new SVGStrokeWidthParser());
		attributeParserMap.put(SVG_STROKE_DASHOFFSET_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_STROKE_LINECAP_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_STROKE_LINEJOIN_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_STROKE_MITERLIMIT_ATTRIBUTE, new SVGMiterLimitParser());
		attributeParserMap.put(SVG_COLOR_INTERPOLATION_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_COLOR_RENDERING_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_OPACITY_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_END_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_MID_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_START_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_FAMILY_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_START_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_SIZE_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_MARKER_START_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_SIZE_ADJUST_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_STRETCH_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_STYLE_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_VARIANT_ATTRIBUTE, new SVGNullParser());
		attributeParserMap.put(SVG_FONT_WEIGHT_ATTRIBUTE, new SVGNullParser());
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
		attributeParserMap.put(SVG_TRANSFORM_ATTRIBUTE, new SVGNullParser());
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

	private final static HashMap<String,WindingRule> fillRuleMap;
    static {
        fillRuleMap = new HashMap<String, WindingRule>();
        fillRuleMap.put("nonzero", WindingRule.NON_ZERO);
        fillRuleMap.put("evenodd", WindingRule.EVEN_ODD);
    }
    private final static HashMap<String,Integer> strokeLinecapMap;
    static {
        strokeLinecapMap = new HashMap<String, Integer>();
        strokeLinecapMap.put("butt", BasicStroke.CAP_BUTT);
        strokeLinecapMap.put("round", BasicStroke.CAP_ROUND);
        strokeLinecapMap.put("square", BasicStroke.CAP_SQUARE);
    }
    private final static HashMap<String,Integer> strokeLinejoinMap;
    static {
        strokeLinejoinMap = new HashMap<String, Integer>();
        strokeLinejoinMap.put("miter", BasicStroke.JOIN_MITER);
        strokeLinejoinMap.put("round", BasicStroke.JOIN_ROUND);
        strokeLinejoinMap.put("bevel", BasicStroke.JOIN_BEVEL);
    }
    private final static HashMap<String,Double> absoluteFontSizeMap;
    static {
        absoluteFontSizeMap = new HashMap<String,Double>();
        absoluteFontSizeMap.put("xx-small",6.944444);
        absoluteFontSizeMap.put("x-small",8.3333333);
        absoluteFontSizeMap.put("small", 10d);
        absoluteFontSizeMap.put("medium", 12d);
        absoluteFontSizeMap.put("large", 14.4);
        absoluteFontSizeMap.put("x-large", 17.28);
        absoluteFontSizeMap.put("xx-large",20.736);
    }
    private final static HashMap<String,Double> relativeFontSizeMap;
    static {
        relativeFontSizeMap = new HashMap<String,Double>();
        relativeFontSizeMap.put("larger", 1.2);
        relativeFontSizeMap.put("smaller",0.83333333);
    }
    private final static HashMap<String,TextAnchor> textAnchorMap;
    static {
        textAnchorMap = new HashMap<String, TextAnchor>();
        textAnchorMap.put("start", TextAnchor.START);
        textAnchorMap.put("middle", TextAnchor.MIDDLE);
        textAnchorMap.put("end", TextAnchor.END);
    }
    
    /**
     * Maps to all XML elements that are identified by an xml:id.
     */
    public HashMap<String,IXMLElement> identifiedElements;
    /**
     * Maps to all drawing objects from the XML elements they were created from.
     */
    public HashMap<IXMLElement,Object> elementObjects;
    
    /**
     * Holds the document that is currently being read.
     */
    private IXMLElement document;
    
	/**
	 * Holds the ROIs which have been created.
	 */
    private ArrayList<ROI> roiList;
	
    /**
     * The current coord of the shape being created.
     */
    private Coord3D currentCoord;
    
    /**
     * The current roi of the shape being created.
     */
    private long currentROI;
    
    /**
     * The ROIComponent 
     */
    ROIComponent component;
    
	InputStrategy()
	{
		figureFactory = new ROIFigureFactory();
	}
	
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
	
	ArrayList<ROI> readROI(InputStream in, ROIComponent component) throws IOException, ROIShapeCreationException 
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
	    IXMLReader reader = new StdXMLReader(in);
	    parser.setReader(reader);
	    try 
	    {
	    document = (IXMLElement) parser.parse();
	    } 
	    catch (XMLException ex) 
	    {
	    	IOException e = new IOException(ex.getMessage());
	        e.initCause(ex);
	        throw e;
	    }
	    
	    ArrayList<IXMLElement> roiElements = document.getChildrenNamed(ROI_TAG);
	    for(int i = 0 ; i < roiElements.size() ; i++)
	    {
	    	roiList.add(createROI(roiElements.get(i), component));
	    }
	    
		return roiList;
	}
	
	private ROI createROI(IXMLElement roiElement, ROIComponent component) throws ROIShapeCreationException, IOException
	{
		if(!roiElement.hasAttribute(ROI_ID_ATTRIBUTE))
			return null;
		long id = new Long(roiElement.getAttribute(ROI_ID_ATTRIBUTE,"-1"));
		setCurrentROI(id);
		ROI newROI = null;
		try 
		{
			newROI = component.createROI(id);
		} 
		catch (ROICreationException e) 
		{
			e.printStackTrace();
		}
		ArrayList<IXMLElement> roiChildren = roiElement.getChildren();
		for(int i = 0 ; i < roiChildren.size(); i++)
		{
			if(roiChildren.get(i).equals(ROISHAPE_TAG))
			{
				newROI.addShape(createROIShape(roiChildren.get(i), newROI));
			}
			else
			if(isAnnotation(roiChildren.get(i).getName()))
			{
				addAnnotation(roiChildren.get(i), newROI);
			}
		}
		return newROI;
	}
		
	private ROIShape createROIShape(IXMLElement shapeElement, ROI newROI) throws IOException
	{
		IXMLElement figureElement = shapeElement.getFirstChildNamed(SVG_TAG);
		int t = new Integer(shapeElement.getAttribute(T_ATTRIBUTE,"0"));
		int z = new Integer(shapeElement.getAttribute(Z_ATTRIBUTE,"0"));
		Coord3D coord = new Coord3D(t,z);
		setCurrentCoord(coord);
		ROIFigure figure = createFigure(figureElement);
		ROIShape shape = new ROIShape(newROI, coord, figure, figure.getBounds());
		ArrayList<IXMLElement> attributeList = shapeElement.getChildren();
		for(int i = 0 ; i < attributeList.size(); i++)
		{
			if(isAnnotation(attributeList.get(i).getName()))
			{
				addAnnotation(attributeList.get(i), shape);
			}
		}
				
		return shape;
	}

	private boolean isAnnotation(String name)
	{
		if(AnnotationKeys.supportedAnnotations.contains(name))
			return true;
		return false;
	}
	
	private void addAnnotation(IXMLElement annotationElement, ROIShape shape)
	{
		String key = annotationElement.getName();
		AnnotationKey annotation = new AnnotationKey(key);
		shape.setAnnotation(annotation, createAnnotationData(annotationElement));
	}
	
	public Object createAnnotationData(IXMLElement annotationElement)
	{
		String dataType = annotationElement.getAttribute(DATATYPE_ATTRIBUTE, VALUE_NULL);
		if(dataType.equals(ATTRIBUTE_DATATYPE_STRING))
		{			
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE, VALUE_NULL);
			return value;
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_INTEGER))
		{
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE,VALUE_NULL);
			if(value.equals(VALUE_NULL))
				return 0;
			return new Integer(value);
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_LONG))
		{
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE,VALUE_NULL);
			if(value.equals(VALUE_NULL))
				return 0;
			return new Long(value);
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_FLOAT))
		{
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE,VALUE_NULL);
			if(value.equals(VALUE_NULL))
				return 0;
			return new Float(value);
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_DOUBLE))
		{
			String value = annotationElement.getAttribute(VALUE_ATTRIBUTE,VALUE_NULL);
			if(value.equals(VALUE_NULL))
				return 0;
			return new Double(value);
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_POINT2D))
		{
			String xValue = annotationElement.getAttribute(X_ATTRIBUTE, VALUE_NULL);
			String yValue = annotationElement.getAttribute(Y_ATTRIBUTE, VALUE_NULL);
			if(xValue.equals(VALUE_NULL) || yValue.equals(VALUE_NULL))
				return new Point2D.Double(0,0);
			return new Point2D.Double(new Double(xValue), new Double(yValue));
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_RECTANGLE2D) ||
				dataType.equals(ATTRIBUTE_DATATYPE_ELLIPSE2D))
		{
			String xValue = annotationElement.getAttribute(X_ATTRIBUTE, VALUE_NULL);
			String yValue = annotationElement.getAttribute(Y_ATTRIBUTE, VALUE_NULL);
			String widthValue = annotationElement.getAttribute(WIDTH_ATTRIBUTE, VALUE_NULL);
			String heightValue = annotationElement.getAttribute(HEIGHT_ATTRIBUTE, VALUE_NULL);
			if(xValue.equals(VALUE_NULL) || yValue.equals(VALUE_NULL) ||
					widthValue.equals(VALUE_NULL) || heightValue.equals(VALUE_NULL))
			{
				if(dataType.equals(ATTRIBUTE_DATATYPE_RECTANGLE2D))
					return new Rectangle2D.Double();
				else
					return new Ellipse2D.Double();
			}
			if(dataType.equals(ATTRIBUTE_DATATYPE_RECTANGLE2D))
				new Rectangle2D.Double(new Double(xValue), new Double(yValue), new Double(widthValue), new Double(heightValue));
			if(dataType.equals(ATTRIBUTE_DATATYPE_ELLIPSE2D))
				return new Ellipse2D.Double(new Double(xValue), new Double(yValue), new Double(widthValue), new Double(heightValue));
		}
		else if(dataType.equals(ATTRIBUTE_DATATYPE_COORD3D))
		{
			String zValue = annotationElement.getAttribute(Z_ATTRIBUTE, VALUE_NULL);
			String tValue = annotationElement.getAttribute(T_ATTRIBUTE, VALUE_NULL);
			if(zValue.equals(VALUE_NULL) || tValue.equals(VALUE_NULL))
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
		
	private ROIFigure createFigure(IXMLElement svgElement) throws IOException
	{
		IXMLElement parentElement = svgElement.getChildAtIndex(0);
		IXMLElement textElement;
		ROIFigure figure = createParentFigure(parentElement);
		if(!parentElement.getName().equals(TEXT_TAG))
		{
			textElement = svgElement.getChildAtIndex(1);
			addTextElementToFigure(figure, textElement);
		}
		return figure;
	}
	
	private ROIFigure createParentFigure(IXMLElement figureElement) throws IOException
	{
		ROIFigure figure = null;
		
		if(figureElement.getName().equals(RECT_TAG))
			figure = createRectangleFigure(figureElement);
		if(figureElement.getName().equals(LINE_TAG))
			figure = createLineFigure(figureElement);
		if(figureElement.getName().equals(ELLIPSE_TAG))
			figure = createEllipseFigure(figureElement);
		if(figureElement.getName().equals(TEXT_TAG))
			figure = createTextFigure(figureElement);
		if(figureElement.getName().equals(POLYLINE_TAG))
			figure = createBezierFigure(figureElement, false);
		if(figureElement.getName().equals(POLYGON_TAG))
			figure = createBezierFigure(figureElement, true);
		return figure;
	}
	
	public BezierAnnotationFigure createBezierFigure(IXMLElement bezierElement, boolean closed)
	{
		BezierAnnotationFigure bezierFigure = new BezierAnnotationFigure(closed);
		if(bezierElement.hasAttribute(POINTS_ATTRIBUTE))
		{
			String pointsValues = bezierElement.getAttribute(POINTS_ATTRIBUTE, VALUE_NULL);
			Point2D.Double[] points = toPoints(pointsValues);
			for(int i = 0 ; i < points.length ; i++)
				bezierFigure.addNode(new Node(points[i].x, points[i].y));
		}
		addAttributes(bezierFigure, bezierElement);
		return bezierFigure;
	}
	
	private MeasureTextFigure createTextFigure(IXMLElement textElement)
	{
		String xValue = textElement.getAttribute(X_ATTRIBUTE, VALUE_NULL);
		String yValue = textElement.getAttribute(Y_ATTRIBUTE, VALUE_NULL);
		String widthValue = textElement.getAttribute(WIDTH_ATTRIBUTE, VALUE_NULL);
		String heightValue = textElement.getAttribute(HEIGHT_ATTRIBUTE, VALUE_NULL);
		
		MeasureTextFigure textFigure = new MeasureTextFigure(new Double(xValue), 
				new Double(yValue), new Double(widthValue), new Double(heightValue));
		addAttributes(textFigure, textElement);
		return textFigure;
	}
	
	private EllipseAnnotationFigure createEllipseFigure(IXMLElement ellipseElement)
	{
		String xValue = ellipseElement.getAttribute(X_ATTRIBUTE, VALUE_NULL);
		String yValue = ellipseElement.getAttribute(Y_ATTRIBUTE, VALUE_NULL);
		String widthValue = ellipseElement.getAttribute(WIDTH_ATTRIBUTE, VALUE_NULL);
		String heightValue = ellipseElement.getAttribute(HEIGHT_ATTRIBUTE, VALUE_NULL);
		
		EllipseAnnotationFigure ellipseFigure = new EllipseAnnotationFigure(new Double(xValue), 
				new Double(yValue), new Double(widthValue), new Double(heightValue));
		addAttributes(ellipseFigure, ellipseElement);
		return ellipseFigure;
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

	private ROIFigure createLineFigure(IXMLElement lineElement) throws IOException
	{
		if(lineElement.hasAttribute(CONNECTION_TO_ATTRIBUTE))
			return createLineConnectionFigure(lineElement);
		else
			return createBasicLineFigure(lineElement);
	}
		
	private LineConnectionAnnotationFigure createLineConnectionFigure(IXMLElement lineElement) throws IOException
	{
		LineConnectionAnnotationFigure lineFigure = new LineConnectionAnnotationFigure();
		long toROIid = new Long(lineElement.getAttribute(CONNECTION_TO_ATTRIBUTE, VALUE_NULL));
		long fromROIid = new Long(lineElement.getAttribute(CONNECTION_FROM_ATTRIBUTE, VALUE_NULL));
		ROI toROI, fromROI;
		try
		{
			toROI = component.getROI(toROIid);
			fromROI = component.getROI(fromROIid);
			ROIFigure toFigure = toROI.getFigure(getCurrentCoord());
			ROIFigure fromFigure = fromROI.getFigure(getCurrentCoord());
			lineFigure.setStartConnector(toFigure.findCompatibleConnector(lineFigure.getStartConnector(), true));
			lineFigure.setEndConnector(fromFigure.findCompatibleConnector(lineFigure.getEndConnector(), false));
		}
		catch(Exception e)
		{
			throw new IOException("In Line connection figure, with ROI :" + getCurrentROI() + " on Coord :" + getCurrentCoord() + " Connection <to>/<from> tag invalid.");
		}
		if(lineElement.hasAttribute(POINTS_ATTRIBUTE))
		{
			String pointsValues = lineElement.getAttribute(POINTS_ATTRIBUTE, VALUE_NULL);
			
		}
		addAttributes(lineFigure, lineElement);
		return lineFigure;
	}
	
	private LineAnnotationFigure createBasicLineFigure(IXMLElement lineElement)
	{
		LineAnnotationFigure lineFigure = new LineAnnotationFigure();
		if(lineElement.hasAttribute(POINTS_ATTRIBUTE))
		{
			String pointsValues = lineElement.getAttribute(POINTS_ATTRIBUTE, VALUE_NULL);
			Point2D.Double[] points = toPoints(pointsValues);
			for(int i = 1 ; i < points.length-1 ; i++)
				lineFigure.addNode(new Node(points[i].x, points[i].y));
		}
		else
		{
			String x1Value = lineElement.getAttribute(X1_ATTRIBUTE, VALUE_NULL);
			String y1Value = lineElement.getAttribute(Y1_ATTRIBUTE, VALUE_NULL);
			String x2Value = lineElement.getAttribute(X2_ATTRIBUTE, VALUE_NULL);
			String y2Value = lineElement.getAttribute(Y2_ATTRIBUTE, VALUE_NULL);
			lineFigure.addNode(new Node(new Double(x1Value), new Double(y1Value)));
			lineFigure.addNode(new Node(new Double(x2Value), new Double(y2Value)));
		}
		addAttributes(lineFigure, lineElement);
		
		
		return lineFigure;
	}	
	
	
	
	 /**
     * Returns a value as a Point2D.Double array.
     * as specified in http://www.w3.org/TR/SVGMobile12/shapes.html#PointsBNF
     */
    private Point2D.Double[] toPoints(String str) 
    {
        
        StringTokenizer tt = new StringTokenizer(str," ,");
        Point2D.Double[] points =new Point2D.Double[tt.countTokens() / 2];
        for (int i=0; i < points.length; i++) 
        {
            
            points[i] = new Point2D.Double(
                    new Double(tt.nextToken()),
                    new Double(tt.nextToken())
                    );
        }
        return points;
    }
	
	private void addAttributes(ROIFigure figure, IXMLElement figureElement)
	{
		Properties attributes = figureElement.getAttributes();
		Iterator attributeKeys = attributes.keySet().iterator();
		while(attributeKeys.hasNext())
		{
			String attribute = (String) attributeKeys.next();
			String value = figureElement.getAttribute(attribute, VALUE_NULL);
			addAttribute(figure, attribute, value);
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
	
	private void parseAttribute(ROIFigure figure, String attribute, String value)
	{
		SVGAttributeParser parser = attributeParserMap.get(attribute);
		parser.parse(figure, value);
	}
	
	private void addAttribute(ROIFigure figure, String attribute, String value)
	{
		if(isBasicSVGAttribute(attribute))
			return;
		if(isSVGAttribute(attribute))
			parseAttribute(figure, attribute, value);
	}
	
	private void addTextElementToFigure(ROIFigure figure, IXMLElement textElement)
	{
		
	}
}


