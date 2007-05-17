/*
 * org.openmicroscopy.shoola.util.roi.io.ROIFigureFactory 
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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.EllipseAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineConnectionAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.figures.RectAnnotationFigure;


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
public class ROIFigureFactory implements FigureFactory
{
	/** Creates a new instance. */
    public ROIFigureFactory() 
    {
    
    }
    
    public ROIFigure createRect(double x, double y, double w, double h, Map<AttributeKey, Object> a) {
        RectAnnotationFigure figure = new RectAnnotationFigure();
        figure.basicSetBounds(new Point2D.Double(x,y),new Point2D.Double(x+w,y+h));
        figure.basicSetAttributes(a);
        return figure;
    }
      
    public ROIFigure createEllipse(double cx, double cy, double rx, double ry, Map<AttributeKey, Object> a) {
        EllipseAnnotationFigure figure = new EllipseAnnotationFigure();
        figure.basicSetBounds(new Point2D.Double(cx-rx,cy-ry),new Point2D.Double(cx+rx,cy+ry));
        figure.basicSetAttributes(a);
        return figure;
    }
    
    public ROIFigure createLine(
            double x1, double y1, double x2, double y2,
            Map<AttributeKey,Object> a) {
        LineAnnotationFigure figure = new LineAnnotationFigure();
        figure.removeAllChildren();
        BezierFigure bf = new BezierFigure();
        bf.addNode(new BezierPath.Node(x1, y1));
        bf.addNode(new BezierPath.Node(x2, y2));
        figure.add(bf);
        figure.basicSetAttributes(a);
        return figure;
    }
    
    public ROIFigure createPolyline(Point2D.Double[] points, Map<AttributeKey, Object> a) {
        BezierAnnotationFigure figure = new BezierAnnotationFigure();
        figure.removeAllChildren();
        BezierFigure bf = new BezierFigure();
        for (int i=0; i < points.length; i++) {
            bf.addNode(new BezierPath.Node(points[i].x, points[i].y));
        }
        figure.add(bf);
        figure.basicSetAttributes(a);
        return figure;
    }
    
    public ROIFigure createPolygon(Point2D.Double[] points, Map<AttributeKey, Object> a) {
    	BezierAnnotationFigure figure = new BezierAnnotationFigure();
        figure.removeAllChildren();
        BezierFigure bf = new BezierFigure();
        for (int i=0; i < points.length; i++) {
            bf.addNode(new BezierPath.Node(points[i].x, points[i].y));
        }
        bf.setClosed(true);
        figure.add(bf);
        figure.basicSetAttributes(a);
        return figure;
    }
    
  /*  public ROIFigure createPath(BezierPath[] beziers, Map<AttributeKey, Object> a) 
    {
        SVGPathFigure figure = new SVGPathFigure();
        figure.removeAllChildren();
        for (int i=0; i < beziers.length; i++) {
            BezierFigure bf = new BezierFigure();
            bf.basicSetBezierPath(beziers[i]);
            figure.add(bf);
        }
        figure.basicSetAttributes(a);
        return figure;
    }*/
            
   /* public ROIFigure createText(Point2D.Double[] coordinates, double[] rotates, StyledDocument text, Map<AttributeKey, Object> a) 
    {
        MeasureTextFigure figure = new MeasureTextFigure();
        figure.basicSetCoordinates(coordinates);
        figure.basicSetRotates(rotates);
        try {
            figure.basicSetText(text.getText(0, text.getLength()));
        } catch (BadLocationException e) {
            InternalError ex = new InternalError(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        figure.basicSetAttributes(a);
        return figure;
    } */
    
    public Gradient createRadialGradient(
            double cx, double cy, double r,
            double[] stopOffsets, Color[] stopColors,
            boolean isRelativeToFigureBounds) {
        RadialGradient g = new RadialGradient();
        g.setGradientCircle(cx, cy, r);
        g.setStops(stopOffsets, stopColors);
        g.setRelativeToFigureBounds(isRelativeToFigureBounds);
        return g;
    }
    
    public Gradient createLinearGradient(
            double x1, double y1, double x2, double y2,
            double[] stopOffsets, Color[] stopColors,
            boolean isRelativeToFigureBounds) {
        LinearGradient g = new LinearGradient();
        g.setGradientVector(x1, y1, x2, y2);
        g.setStops(stopOffsets, stopColors);
        g.setRelativeToFigureBounds(isRelativeToFigureBounds);
        return g;
    }

}


