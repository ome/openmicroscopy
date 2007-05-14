/*
 * org.openmicroscopy.shoola.util.ui.roi.io.InputStrategy 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.roi.io;


//Java imports
import java.awt.BasicStroke;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.jhotdraw.draw.AttributeKeys.WindingRule;
import org.jhotdraw.samples.svg.SVGAttributeKeys.TextAnchor;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.roi.ROIComponent;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

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
	public final static String POLYLINE_TAG = "polyline";
	public final static String POLYGON_TAG = "polygon";
	
	public final static String DATATYPE_ATTRIBUTE = "type";
	public final static String SIZE_ATTRIBUTE = "size"; 
	public final static String VALUE_ATTRIBUTE = "value";
	public final static String POINTS_ATTRIBUTE = "points";
	
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
	
	
	public final static String DATATYPE_ATTRIBUTE_VALUE_STRING = "String";
	public final static String DATATYPE_ATTRIBUTE_VALUE_DOUBLE = "Double";
	public final static String DATATYPE_ATTRIBUTE_VALUE_LONG = "Long";
	public final static String DATATYPE_ATTRIBUTE_VALUE_INTEGER = "Integer";
	public final static String DATATYPE_ATTRIBUTE_VALUE_FLOAT = "Float";
	public final static String DATATYPE_ATTRIBUTE_VALUE_POINT2D = "Point2D";
	public final static String DATATYPE_ATTRIBUTE_VALUE_ELLIPSE2D = "Ellipse2D";
	public final static String DATATYPE_ATTRIBUTE_VALUE_RECTANGLE2D = "Rectangle2D";
	public final static String DATATYPE_ATTRIBUTE_VALUE_COLOUR = "Color";
	public final static String DATATYPE_ATTRIBUTE_VALUE_COORD3D = "Coord3D";
	public final static String DATATYPE_ATTRIBUTE_VALUE_ARRAYLIST = "ArrayList";
	
	
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
     * The ROIComponent 
     */
    ROIComponent component;
    
	InputStrategy()
	{
		figureFactory = new ROIFigureFactory();
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
		ArrayList<IXMLElement> figureElement = shapeElement.getChildrenNamed(SVG_TAG);
		if(figureElement.size()!=0)
		{
			throw new IOException("No Figure Element in ROIShape");
		}
		int t = new Integer(shapeElement.getAttribute(T_ATTRIBUTE,"0"));
		int z = new Integer(shapeElement.getAttribute(Z_ATTRIBUTE,"0"));
		Coord3D coord = new Coord3D(t,z);
		
		ROIFigure figure = createFigure(figureElement.get(0));
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
		String dataType = annotationElement.getAttribute(DATATYPE_ATTRIBUTE);
		String value = annotationElement.getContent();
		
	}
	
	private void addAnnotation(IXMLElement annotationElement, ROI roi)
	{
		
	}
		
	private ROIFigure createFigure(IXMLElement figureElement)
	{
		ROIFigure figure = null;
		return figure;
	}
}


