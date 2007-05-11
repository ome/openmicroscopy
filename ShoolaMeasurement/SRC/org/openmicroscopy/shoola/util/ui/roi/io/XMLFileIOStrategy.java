/*
 * org.openmicroscopy.shoola.util.ui.roi.io.XMLFileIOStrategy 
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

//Third-party libraries
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

//Application-internal dependencies
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;

import static org.jhotdraw.draw.AttributeKeys.*;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;

import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.EllipseAnnotationFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.LineConnectionAnnotationFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.RectAnnotationFigure;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROICollection;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKey;
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
public 	class XMLFileIOStrategy
		implements XMLIOStrategy
{
	public final static String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
	public final static String ROI_NAMESPACE = "http://www.openmicroscopy.org.uk/roi";
	
	private final static HashMap<Integer, String> strokeLinejoinMap;
    static {
        strokeLinejoinMap = new HashMap<Integer, String>();
        strokeLinejoinMap.put(BasicStroke.JOIN_MITER, "miter");
        strokeLinejoinMap.put(BasicStroke.JOIN_ROUND, "round");
        strokeLinejoinMap.put(BasicStroke.JOIN_BEVEL, "bevel");
    }
    private final static HashMap<Integer, String> strokeLinecapMap;
    static {
        strokeLinecapMap = new HashMap<Integer, String>();
        strokeLinecapMap.put(BasicStroke.CAP_BUTT, "butt");
        strokeLinecapMap.put(BasicStroke.CAP_ROUND, "round");
        strokeLinecapMap.put(BasicStroke.CAP_SQUARE, "square");
    }
    

	
	IXMLElement document;
	IXMLElement defs; 
	
	 /**
     * This is a counter used to create the next unique identification.
     */
    private int nextId;
    
    /**
     * In this hash map we store all elements to which we have assigned
     * an id.
     */
    private HashMap<IXMLElement,String> identifiedElements;
    

	 /**
     * Gets a unique ID for the specified element.
     */
    public String getId(IXMLElement element) {
        if (identifiedElements.containsKey(element)) {
            return identifiedElements.get(element);
        } else {
            String id = Integer.toString(nextId++, Character.MAX_RADIX);
            identifiedElements.put(element, id);
            return id;
        }
    }

	public XMLFileIOStrategy()
	{
		nextId = 0;
	}
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.roi.io.XMLIOStrategy#closeConnection()
	 */
	public void closeConnection() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.roi.io.XMLIOStrategy#openConnection()
	 */
	public void openConnection() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.roi.io.XMLIOStrategy#readXML()
	 */
	public void readXML() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.roi.io.XMLIOStrategy#writeXML()
	 */
	public void writeXML() {
		// TODO Auto-generated method stub
		
	}

	public void write(String filename, ROICollection collection)
	{
		File file = new File(filename);
		try {
			write(file, collection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(File file, ROICollection collection) throws IOException 
	{
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	    try 
	    {
	    	write(out, collection);
	    } 
	    finally 
	    {
	    	if (out != null) 
	    	{
	    		out.close();
	        }
	    }
	}

	
	public void write(OutputStream out, ROICollection roiCollection) throws IOException 
	{
		document = new XMLElement("roi", ROI_NAMESPACE);
		defs = new XMLElement("defs");
		document.addChild(defs);
		ROICollection collection = roiCollection;
	        
	    TreeMap<Long, ROI> roiMap = collection.getROIMap();
	    Iterator iterator = roiMap.values().iterator();
	        
	    while(iterator.hasNext())
	    {
	    	write(document, (ROI)iterator.next());
	    }
	        
	    new XMLWriter(out).write(document);
	}

	public void write(IXMLElement document, ROI roi) throws IOException
	{
		XMLElement roiElement = new XMLElement("ROI");
		document.addChild(roiElement);

		writeROIAnnotations(roiElement, roi);
		
		TreeMap<Coord3D, ROIShape> roiShapes = roi.getShapes();
	    Iterator iterator = roiShapes.values().iterator();
	    
	    while(iterator.hasNext())
	    	writeROIShape(roiElement, (ROIShape)iterator.next());
	}

	public void writeROIAnnotations(IXMLElement roiElement, ROI roi)
	{
		roiElement.setAttribute("id", roi.getID()+"");
		Map<AnnotationKey, Object> annotationMap = roi.getAnnotation();
		Iterator iterator = annotationMap.keySet().iterator();
		while(iterator.hasNext())
		{
			AnnotationKey key = (AnnotationKey)iterator.next();
			XMLElement annotation = new XMLElement(key.getKey());
			addAttributes(annotation, annotationMap.get(key));
		}
	}

	private void writeROIShapeAnnotations(IXMLElement shapeElement, ROIShape shape)
	{
		Map<AnnotationKey, Object> annotationMap = shape.getAnnotation();
		Iterator iterator = annotationMap.keySet().iterator();
		while(iterator.hasNext())
		{
			AnnotationKey key = (AnnotationKey)iterator.next();
			XMLElement annotation = new XMLElement(key.getKey());
			addAttributes(annotation, annotationMap.get(key));
		}
	}
	
	private void writeROIShape(XMLElement roiElement, ROIShape shape) throws IOException
	{
		XMLElement shapeElement = new XMLElement("ROIShape");
		roiElement.addChild(shapeElement);
		shapeElement.setAttribute("t",shape.getCoord3D().getTimePoint()+"");
		shapeElement.setAttribute("z",shape.getCoord3D().getZSection()+"");
		writeROIShapeAnnotations(shapeElement, shape);
		ROIFigure figure = shape.getFigure();
		figure.calculateMeasurements();
		writeFigure(shapeElement, figure);
	}

	public void writeFigure(XMLElement shapeElement, ROIFigure figure) throws IOException
	{
		if(figure instanceof RectAnnotationFigure)
		{
			writeRectAnnotationFigure(shapeElement, (RectAnnotationFigure)figure);
		}
		else if (figure instanceof EllipseAnnotationFigure)
		{
			writeEllipseAnnotationFigure(shapeElement, (EllipseAnnotationFigure)figure);		
		}
		else if (figure instanceof BezierAnnotationFigure)
		{
			writeBezierAnnotationFigure(shapeElement, (BezierAnnotationFigure)figure);		
		}
		else if (figure instanceof LineAnnotationFigure)
		{
			writeLineAnnotationFigure(shapeElement, (LineAnnotationFigure)figure);
		}
		else if(figure instanceof LineConnectionAnnotationFigure)
		{
			
		}
	}

	public void writeBezierAnnotationFigure(XMLElement shapeElement, BezierAnnotationFigure fig) throws IOException
	{
		XMLElement svgElement = new XMLElement("svg", SVG_NAMESPACE);
		svgElement.setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
		svgElement.setAttribute("version","1.2");
		if(fig.isClosed())
			writePolygonFigure(svgElement, fig);
		else
			writePolylineFigure(svgElement, fig);
	}

	public void writePolygonFigure(XMLElement svgElement, BezierAnnotationFigure fig) throws IOException
	{
		XMLElement bezierElement = new XMLElement("polygon");
		svgElement.addChild(bezierElement);
		LinkedList<Point2D.Double> points = new LinkedList<Point2D.Double>();
	    BezierPath[] beziers = new BezierPath[fig.getChildCount()];
	    for (int i=0, n = fig.getChildCount(); i < n; i++) 
	    {
	    	BezierPath bezier = ((BezierAnnotationFigure) fig.getChild(i)).getBezierPath();
	      	for (BezierPath.Node node: bezier) 
	       	{
	       		points.add(new Point2D.Double(node.x[0], node.y[0]));
	        }
	    }
	    writeAttribute(bezierElement, "points", toPoints(points.toArray(new Point2D.Double[points.size()])), null);
	    writeShapeAttributes(bezierElement, fig.getAttributes());
	    writeTransformAttribute(bezierElement, fig.getAttributes());
	}
	
	public void writePolylineFigure(XMLElement svgElement, BezierAnnotationFigure fig) throws IOException
	{
		XMLElement bezierElement = new XMLElement("polyline");
		svgElement.addChild(bezierElement);
		LinkedList<Point2D.Double> points = new LinkedList<Point2D.Double>();
        BezierPath[] beziers = new BezierPath[fig.getChildCount()];
        for (int i=0, n = fig.getChildCount(); i < n; i++) 
        {
            BezierPath bezier = ((BezierAnnotationFigure) fig.getChild(i)).getBezierPath();
            for (BezierPath.Node node: bezier) 
            {
                points.add(new Point2D.Double(node.x[0], node.y[0]));
            }
        }
	    writeAttribute(bezierElement, "points", toPoints(points.toArray(new Point2D.Double[points.size()])), null);
	    writeShapeAttributes(bezierElement, fig.getAttributes());
	    writeTransformAttribute(bezierElement, fig.getAttributes());
	}
	    
	public void writeLineAnnotationFigure(XMLElement shapeElement, LineAnnotationFigure fig) throws IOException
	{
		XMLElement svgElement = new XMLElement("svg", SVG_NAMESPACE);
		svgElement.setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
		svgElement.setAttribute("version","1.2");
		
	  	XMLElement lineElement = new XMLElement("line");
	  	shapeElement.addChild(svgElement);
		svgElement.addChild(lineElement);
      
	  	lineElement.setAttribute("x1", fig.getNode(0).x[0]+"");
	  	lineElement.setAttribute("y1", fig.getNode(0).y[0]+"");
	  	lineElement.setAttribute("x2", fig.getNode(1).x[0]+"");
	  	lineElement.setAttribute("y2", fig.getNode(1).y[0]+"");
      	writeShapeAttributes(lineElement, fig.getAttributes());
      	writeTransformAttribute(lineElement, fig.getAttributes());
  	}
    
   
	public void writeEllipseAnnotationFigure(XMLElement shapeElement, EllipseAnnotationFigure fig) throws IOException
	{
		XMLElement svgElement = new XMLElement("svg", SVG_NAMESPACE);
		svgElement.setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
		svgElement.setAttribute("version","1.2");

		XMLElement ellipseElement = new XMLElement("ellipse");
		shapeElement.addChild(svgElement);
		svgElement.addChild(ellipseElement);
		double cx = fig.getX() + fig.getWidth() / 2d;		
		double cy = fig.getY() + fig.getHeight() / 2d;
		double rx = fig.getWidth() / 2d;
		double ry = fig.getHeight() / 2d;
		ellipseElement.setAttribute("cx", cx+"");
		ellipseElement.setAttribute("cy", cy+"");
		ellipseElement.setAttribute("rx", rx+"");
		ellipseElement.setAttribute("ry", ry+"");
        writeShapeAttributes(ellipseElement, fig.getAttributes());
        writeTransformAttribute(ellipseElement, fig.getAttributes());
	}

	
	public void writeRectAnnotationFigure(XMLElement shapeElement, RectAnnotationFigure fig) throws IOException
	{
		XMLElement svgElement = new XMLElement("svg", SVG_NAMESPACE);
		svgElement.setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
		svgElement.setAttribute("version","1.2");

		XMLElement rectElement = new XMLElement("rect");
		shapeElement.addChild(svgElement);
		svgElement.addChild(rectElement);
		rectElement.setAttribute("x", fig.getX()+"");
		rectElement.setAttribute("y", fig.getY()+"");
		rectElement.setAttribute("width", fig.getWidth()+"");
		rectElement.setAttribute("height", fig.getHeight()+"");
        writeShapeAttributes(rectElement, fig.getAttributes());
        writeTransformAttribute(rectElement, fig.getAttributes());
	}

	protected void writeShapeAttributes(IXMLElement elem, Map<AttributeKey,Object> f)
    throws IOException {
        Color color;
        String value;
        int intValue;
        
        //'color'
        // Value:  	<color> | inherit
        // Initial:  	 depends on user agent
        // Applies to:  	None. Indirectly affects other properties via currentColor
        // Inherited:  	 yes
        // Percentages:  	 N/A
        // Media:  	 visual
        // Animatable:  	 yes
        // Computed value:  	 Specified <color> value, except inherit
        //
        // Nothing to do: Attribute 'color' is not needed.
        
        //'color-rendering'
        // Value:  	 auto | optimizeSpeed | optimizeQuality | inherit
        // Initial:  	 auto
        // Applies to:  	 container elements , graphics elements and 'animateColor'
        // Inherited:  	 yes
        // Percentages:  	 N/A
        // Media:  	 visual
        // Animatable:  	 yes
        // Computed value:  	 Specified value, except inherit
        //
        // Nothing to do: Attribute 'color-rendering' is not needed.
        
        // 'fill'
        // Value:  	<paint> | inherit (See Specifying paint)
        // Initial:  	 black
        // Applies to:  	 shapes and text content elements
        // Inherited:  	 yes
        // Percentages:  	 N/A
        // Media:  	 visual
        // Animatable:  	 yes
        // Computed value:  	 "none", system paint, specified <color> value or absolute IRI
        Object gradient = FILL_GRADIENT.get(f);
        if (gradient != null) {
            IXMLElement gradientElem;
            if (gradient instanceof LinearGradient) {
                LinearGradient lg = (LinearGradient) gradient;
                gradientElem = createLinearGradient(document,
                        lg.getX1(), lg.getY1(),
                        lg.getX2(), lg.getY2(),
                        lg.getStopOffsets(),
                        lg.getStopColors(),
                        lg.isRelativeToFigureBounds()
                        );
            } else /*if (gradient instanceof RadialGradient)*/ {
                RadialGradient rg = (RadialGradient) gradient;
                gradientElem = createRadialGradient(document,
                        rg.getCX(), rg.getCY(),
                        rg.getR(),
                        rg.getStopOffsets(),
                        rg.getStopColors(),
                        rg.isRelativeToFigureBounds()
                        );
            }
            String id = getId(gradientElem);
            gradientElem.setAttribute("id","xml",id);
            defs.addChild(gradientElem);
            writeAttribute(elem, "fill", "url(#"+id+")", "#000");
        } else {
            writeAttribute(elem, "fill", toColor(FILL_COLOR.get(f)), "#000");
        }
        
        
        //'fill-opacity'
        //Value:  	 <opacity-value> | inherit
        //Initial:  	 1
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "fill-opacity", FILL_OPACITY.get(f), 1d);
        
        // 'fill-rule'
        // Value:	 nonzero | evenodd | inherit
        // Initial: 	 nonzero
        // Applies to:  	 shapes and text content elements
        // Inherited:  	 yes
        // Percentages:  	 N/A
        // Media:  	 visual
        // Animatable:  	 yes
        // Computed value:  	 Specified value, except inherit
        if (WINDING_RULE.get(f) != WindingRule.NON_ZERO) {
            writeAttribute(elem, "fill-rule", "evenodd", "nonzero");
        }
        
        //'stroke'
        //Value:  	<paint> | inherit (See Specifying paint)
        //Initial:  	 none
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 "none", system paint, specified <color> value
        // or absolute IRI
        gradient = STROKE_GRADIENT.get(f);
        if (gradient != null) {
            IXMLElement gradientElem;
            if (gradient instanceof LinearGradient) {
                LinearGradient lg = (LinearGradient) gradient;
                gradientElem = createLinearGradient(document,
                        lg.getX1(), lg.getY1(),
                        lg.getX2(), lg.getY2(),
                        lg.getStopOffsets(),
                        lg.getStopColors(),
                        lg.isRelativeToFigureBounds()
                        );
            } else /*if (gradient instanceof RadialGradient)*/ {
                RadialGradient rg = (RadialGradient) gradient;
                gradientElem = createRadialGradient(document,
                        rg.getCX(), rg.getCY(),
                        rg.getR(),
                        rg.getStopOffsets(),
                        rg.getStopColors(),
                        rg.isRelativeToFigureBounds()
                        );
            }
            String id = getId(gradientElem);
            gradientElem.setAttribute("id","xml",id);
            defs.addChild(gradientElem);
            writeAttribute(elem, "stroke", "url(#"+id+")", "none");
        } else {
            writeAttribute(elem, "stroke", toColor(STROKE_COLOR.get(f)), "none");
        }
        
        //'stroke-dasharray'
        //Value:  	 none | <dasharray> | inherit
        //Initial:  	 none
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes (non-additive)
        //Computed value:  	 Specified value, except inherit
        double[] dashes = STROKE_DASHES.get(f);
        if (dashes != null) {
            StringBuilder buf = new StringBuilder();
            for (int i=0; i < dashes.length; i++) {
                if (i != 0) {
                    buf.append(',');
                }
                buf.append(toNumber(dashes[i]));
            }
            writeAttribute(elem, "stroke-dasharray", buf.toString(), null);
        }
        
        //'stroke-dashoffset'
        //Value:  	<length> | inherit
        //Initial:  	 0
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "stroke-dashoffset", STROKE_DASH_PHASE.get(f), 0d);
        
        //'stroke-linecap'
        //Value:  	 butt | round | square | inherit
        //Initial:  	 butt
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "stroke-linecap", strokeLinecapMap.get(STROKE_CAP.get(f)), "butt");
        
        //'stroke-linejoin'
        //Value:  	 miter | round | bevel | inherit
        //Initial:  	 miter
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "stroke-linejoin", strokeLinejoinMap.get(STROKE_JOIN.get(f)), "miter");
        
        //'stroke-miterlimit'
        //Value:  	 <miterlimit> | inherit
        //Initial:  	 4
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "stroke-miterlimit", STROKE_MITER_LIMIT.get(f), 4d);
        
        //'stroke-opacity'
        //Value:  	 <opacity-value> | inherit
        //Initial:  	 1
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "stroke-opacity", STROKE_OPACITY.get(f), 1d);
        
        //'stroke-width'
        //Value:  	<length> | inherit
        //Initial:  	 1
        //Applies to:  	 shapes and text content elements
        //Inherited:  	 yes
        //Percentages:  	 N/A
        //Media:  	 visual
        //Animatable:  	 yes
        //Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "stroke-width", STROKE_WIDTH.get(f), 1d);
    }
    /* Writes the transform attribute as specified in
     * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
     *
     */
    protected void writeTransformAttribute(IXMLElement elem, Map<AttributeKey,Object> a)
    throws IOException {
        AffineTransform t = TRANSFORM.get(a);
        if (t != null) {
            writeAttribute(elem, "transform", toTransform(t), "none");
        }
    }
    /* Reads font attributes as listed in
     * http://www.w3.org/TR/SVGMobile12/feature.html#Font
     */
    private void writeFontAttributes(IXMLElement elem, Map<AttributeKey,Object> a)
    throws IOException {
        String value;
        double doubleValue;
        
        // 'font-family'
        // Value:  	[[ <family-name> |
        // <generic-family> ],]* [<family-name> |
        // <generic-family>] | inherit
        // Initial:  	depends on user agent
        // Applies to:  	text content elements
        // Inherited:  	yes
        // Percentages:  	N/A
        // Media:  	visual
        // Animatable:  	yes
        // Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "font-family", FONT_FACE.get(a).getFamily(), "Dialog");
        
        // 'font-size'
        // Value:  	<absolute-size> | <relative-size> |
        // <length> | inherit
        // Initial:  	medium
        // Applies to:  	text content elements
        // Inherited:  	yes, the computed value is inherited
        // Percentages:  	N/A
        // Media:  	visual
        // Animatable:  	yes
        // Computed value:  	 Absolute length
        writeAttribute(elem, "font-size", FONT_SIZE.get(a), 0d);
        
        // 'font-style'
        // Value:  	normal | italic | oblique | inherit
        // Initial:  	normal
        // Applies to:  	text content elements
        // Inherited:  	yes
        // Percentages:  	N/A
        // Media:  	visual
        // Animatable:  	yes
        // Computed value:  	 Specified value, except inherit
        writeAttribute(elem, "font-style", (FONT_ITALIC.get(a)) ? "italic" : "normal", "normal");
        
        
        //'font-variant'
        //Value:  	normal | small-caps | inherit
        //Initial:  	normal
        //Applies to:  	text content elements
        //Inherited:  	yes
        //Percentages:  	N/A
        //Media:  	visual
        //Animatable:  	no
        //Computed value:  	 Specified value, except inherit
        // XXX - Implement me
        writeAttribute(elem, "font-variant", "normal", "normal");
        
        // 'font-weight'
        // Value:  	normal | bold | bolder | lighter | 100 | 200 | 300
        // | 400 | 500 | 600 | 700 | 800 | 900 | inherit
        // Initial:  	normal
        // Applies to:  	text content elements
        // Inherited:  	yes
        // Percentages:  	N/A
        // Media:  	visual
        // Animatable:  	yes
        // Computed value:  	 one of the legal numeric values, non-numeric
        // values shall be converted to numeric values according to the rules
        // defined below.
        writeAttribute(elem, "font-weight", (FONT_BOLD.get(a)) ? "bold" : "normal", "normal");
    }
    public static String toPath(BezierPath[] paths) {
        StringBuilder buf = new StringBuilder();
        
        for (int j=0; j < paths.length; j++) {
            BezierPath path = paths[j];
            
            if (path.size() == 0) {
                // nothing to do
            } else if (path.size() == 1) {
                BezierPath.Node current = path.get(0);
                buf.append("M ");
                buf.append(current.x[0]);
                buf.append(' ');
                buf.append(current.y[0]);
                buf.append(" L ");
                buf.append(current.x[0]);
                buf.append(' ');
                buf.append(current.y[0] + 1);
            } else {
                BezierPath.Node previous;
                BezierPath.Node current;
                
                previous = current = path.get(0);
                buf.append("M ");
                buf.append(current.x[0]);
                buf.append(' ');
                buf.append(current.y[0]);
                for (int i=1, n = path.size(); i < n; i++) {
                    previous = current;
                    current = path.get(i);
                    
                    if ((previous.mask & BezierPath.C2_MASK) == 0) {
                        if ((current.mask & BezierPath.C1_MASK) == 0) {
                            buf.append(" L ");
                            buf.append(current.x[0]);
                            buf.append(' ');
                            buf.append(current.y[0]);
                        } else {
                            buf.append(" Q ");
                            buf.append(current.x[1]);
                            buf.append(' ');
                            buf.append(current.y[1]);
                            buf.append(' ');
                            buf.append(current.x[0]);
                            buf.append(' ');
                            buf.append(current.y[0]);
                        }
                    } else {
                        if ((current.mask & BezierPath.C1_MASK) == 0) {
                            buf.append(" Q ");
                            buf.append(current.x[2]);
                            buf.append(' ');
                            buf.append(current.y[2]);
                            buf.append(' ');
                            buf.append(current.x[0]);
                            buf.append(' ');
                            buf.append(current.y[0]);
                        } else {
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
                if (path.isClosed()) {
                    if (path.size() > 1) {
                        previous = path.get(path.size() - 1);
                        current = path.get(0);
                        
                        if ((previous.mask & BezierPath.C2_MASK) == 0) {
                            if ((current.mask & BezierPath.C1_MASK) == 0) {
                                buf.append(" L ");
                                buf.append(current.x[0]);
                                buf.append(' ');
                                buf.append(current.y[0]);
                            } else {
                                buf.append(" Q ");
                                buf.append(current.x[1]);
                                buf.append(' ');
                                buf.append(current.y[1]);
                                buf.append(' ');
                                buf.append(current.x[0]);
                                buf.append(' ');
                                buf.append(current.y[0]);
                            }
                        } else {
                            if ((current.mask & BezierPath.C1_MASK) == 0) {
                                buf.append(" Q ");
                                buf.append(previous.x[2]);
                                buf.append(' ');
                                buf.append(previous.y[2]);
                                buf.append(' ');
                                buf.append(current.x[0]);
                                buf.append(' ');
                                buf.append(current.y[0]);
                            } else {
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
    
    protected void writeAttribute(IXMLElement elem, String name, String value, String defaultValue) {
        writeAttribute(elem, name, "", value, defaultValue);
    }
    protected void writeAttribute(IXMLElement elem, String name, String namespace, String value, String defaultValue) {
        if (! value.equals(defaultValue)) {
            elem.setAttribute(name, value);
        }
    }
    protected void writeAttribute(IXMLElement elem, String name, double value, double defaultValue) {
        writeAttribute(elem, name, SVG_NAMESPACE, value, defaultValue);
    }
    protected void writeAttribute(IXMLElement elem, String name, String namespace, double value, double defaultValue) {
        if (value != defaultValue) {
            elem.setAttribute(name, toNumber(value));
        }
    }

    
    /**
     * Returns a double array as a number attribute value.
     */
    public static String toNumber(double number) {
        String str = Double.toString(number);
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() -  2);
        }
        return str;
    }
    
    /**
     * Returns a Point2D.Double array as a Points attribute value.
     * as specified in http://www.w3.org/TR/SVGMobile12/shapes.html#PointsBNF
     */
    public static String toPoints(Point2D.Double[] points) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i < points.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(toNumber(points[i].x));
            buf.append(',');
            buf.append(toNumber(points[i].y));
        }
        return buf.toString();
    }
    /* Converts an AffineTransform into an SVG transform attribute value as specified in
     * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
     */
    public static String toTransform(AffineTransform t) throws IOException {
        StringBuilder buf = new StringBuilder();
        switch (t.getType()) {
            case AffineTransform.TYPE_IDENTITY :
                buf.append("none");
                break;
            case AffineTransform.TYPE_TRANSLATION :
                // translate(<tx> [<ty>]), specifies a translation by tx and ty.
                // If <ty> is not provided, it is assumed to be zero.
                buf.append("translate(");
                buf.append(toNumber(t.getTranslateX()));
                if (t.getTranslateY() != 0d) {
                    buf.append(' ');
                    buf.append(toNumber(t.getTranslateY()));
                }
                buf.append(')');
                break;
                /*
            case AffineTransform.TYPE_GENERAL_ROTATION :
            case AffineTransform.TYPE_QUADRANT_ROTATION :
            case AffineTransform.TYPE_MASK_ROTATION :
                // rotate(<rotate-angle> [<cx> <cy>]), specifies a rotation by
                // <rotate-angle> degrees about a given point.
                // If optional parameters <cx> and <cy> are not supplied, the
                // rotate is about the origin of the current user coordinate
                // system. The operation corresponds to the matrix
                // [cos(a) sin(a) -sin(a) cos(a) 0 0].
                // If optional parameters <cx> and <cy> are supplied, the rotate
                // is about the point (<cx>, <cy>). The operation represents the
                // equivalent of the following specification:
                // translate(<cx>, <cy>) rotate(<rotate-angle>)
                // translate(-<cx>, -<cy>).
                buf.append("rotate(");
                buf.append(toNumber(t.getScaleX()));
                buf.append(')');
                break;*/
            case AffineTransform.TYPE_UNIFORM_SCALE :
                // scale(<sx> [<sy>]), specifies a scale operation by sx
                // and sy. If <sy> is not provided, it is assumed to be equal
                // to <sx>.
                buf.append("scale(");
                buf.append(toNumber(t.getScaleX()));
                buf.append(')');
                break;
            case AffineTransform.TYPE_GENERAL_SCALE :
            case AffineTransform.TYPE_MASK_SCALE :
                // scale(<sx> [<sy>]), specifies a scale operation by sx
                // and sy. If <sy> is not provided, it is assumed to be equal
                // to <sx>.
                buf.append("scale(");
                buf.append(toNumber(t.getScaleX()));
                buf.append(' ');
                buf.append(toNumber(t.getScaleY()));
                buf.append(')');
                break;
            default :
                // matrix(<a> <b> <c> <d> <e> <f>), specifies a transformation
                // in the form of a transformation matrix of six values.
                // matrix(a,b,c,d,e,f) is equivalent to applying the
                // transformation matrix [a b c d e f].
                buf.append("matrix(");
                double[] matrix = new double[6];
                t.getMatrix(matrix);
                for (int i=0; i < matrix.length; i++) {
                    if (i != 0) {
                        buf.append(' ');
                    }
                    buf.append(toNumber(matrix[i]));
                }
                buf.append(')');
                break;
        }
        
        return buf.toString();
    }
    
    public static String toColor(Color color) {
        if (color == null) {
            return "none";
        }
        
        
        String value;
        value = "000000"+Integer.toHexString(color.getRGB());
        value = "#"+value.substring(value.length() - 6);
        if (value.charAt(1) == value.charAt(2) &&
                value.charAt(3) == value.charAt(4) &&
                value.charAt(5) == value.charAt(6)) {
            value = "#"+value.charAt(1)+value.charAt(3)+value.charAt(5);
        }
        return value;
    }
    

    protected IXMLElement createLinearGradient(IXMLElement doc,
            double x1, double y1, double x2, double y2,
            double[] stopOffsets, Color[] stopColors,
            boolean isRelativeToFigureBounds) throws IOException {
        IXMLElement elem = doc.createElement("linearGradient");
        
        writeAttribute(elem, "x1", toNumber(x1), "0");
        writeAttribute(elem, "y1", toNumber(y1), "0");
        writeAttribute(elem, "x2", toNumber(x2), "1");
        writeAttribute(elem, "y2", toNumber(y2), "0");
        writeAttribute(elem, "gradientUnits", 
                (isRelativeToFigureBounds) ? "objectBoundingBox" : "useSpaceOnUse",
                "objectBoundingBox"
                );
        
        for (int i=0; i < stopOffsets.length; i++) {
            IXMLElement stop = new XMLElement("stop");
            writeAttribute(stop, "offset", toNumber(stopOffsets[i]), null);
            writeAttribute(stop, "stop-color", toColor(stopColors[i]), null);
            writeAttribute(stop, "stop-opacity", toNumber(stopColors[i].getAlpha() / 255d), "1");
            elem.addChild(stop);
        }
        
        return elem;
    }
    
    protected IXMLElement createRadialGradient(IXMLElement doc,
            double cx, double cy, double r,
            double[] stopOffsets, Color[] stopColors,
            boolean isRelativeToFigureBounds) throws IOException {
        IXMLElement elem = doc.createElement("radialGradient");

        writeAttribute(elem, "cx", toNumber(cx), "0.5");
        writeAttribute(elem, "cy", toNumber(cy), "0.5");
        writeAttribute(elem, "r", toNumber(r), "0.5");
        writeAttribute(elem, "gradientUnits", 
                (isRelativeToFigureBounds) ? "objectBoundingBox" : "useSpaceOnUse",
                "objectBoundingBox"
                );
        
        for (int i=0; i < stopOffsets.length; i++) {
            IXMLElement stop = new XMLElement("stop");
            writeAttribute(stop, "offset", toNumber(stopOffsets[i]), null);
            writeAttribute(stop, "stop-color", toColor(stopColors[i]), null);
            writeAttribute(stop, "stop-opacity", toNumber(stopColors[i].getAlpha() / 255d), "1");
            elem.addChild(stop);
        }
        
        return elem;
    }

    
	public void addAttributes(XMLElement annotation, Object value)
	{
		String str;
		if( value instanceof Double ||
			value instanceof Float ||
			value instanceof Integer ||
			value instanceof Long ||
			value instanceof Float)
		{
			annotation.setAttribute("value", value+"");
		}
		else if(value instanceof String)
		{
			annotation.setAttribute("value", (String)value);
		}
		else if(value instanceof Point2D)
		{
			Point2D point = (Point2D)value;
			annotation.setAttribute("x", point.getX()+"");
			annotation.setAttribute("y", point.getY()+"");
		}
		else if(value instanceof Coord3D)
		{
			Coord3D coord = (Coord3D)value;
			annotation.setAttribute("t", coord.getTimePoint()+"");
			annotation.setAttribute("z", coord.getZSection()+"");
		}
		else if(value instanceof ArrayList)
		{
			String buildString = new String();
			ArrayList list = (ArrayList)value;
			for( int i = 0 ; i < list.size(); i++)
			{
				buildString = buildString + toString(list.get(i));
				if(i<list.size()-1)
					buildString = buildString+newLine();
			}
			annotation.setContent(buildString);
		}
	}

	private String toString(Object value)
	{
		String str = "";
		if( value instanceof Double ||
			value instanceof Float ||
			value instanceof Integer ||
			value instanceof Long ||
			value instanceof Float)
		{
			str = value+"";
		}
		else if(value instanceof String)
		{
			str = (String)value;
		}
		else if(value instanceof Point2D)
		{
			Point2D point = (Point2D)value;
			str = "x = " + point.getX() + " y = " + point.getY();
		}
		else if(value instanceof Coord3D)
		{
			Coord3D coord = (Coord3D)value;
			str = "t = " + coord.getTimePoint() + " z = " + coord.getZSection();
		}
		
		return str;
	}
	
	private String newLine()
	{
		return System.getProperty("line.separator");
	}
	
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.ui.roi.io.XMLIOStrategy#read(java.io.File, org.openmicroscopy.shoola.util.ui.roi.model.ROICollection)
	 */
	public void read(File file, ROICollection collection) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
}


