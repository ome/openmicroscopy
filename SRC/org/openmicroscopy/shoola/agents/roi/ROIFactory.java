/*
 * org.openmicroscopy.shoola.agents.roi.ROIFactory
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.roi;

//Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class to build shapes.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ROIFactory
{
    
    /** The ROI type supported. */
    public static final int     RECTANGLE = 0;
    
    public static final int     ELLIPSE = 1;
    
    /** Size of the rectangle to handle resizing event. */
    public static final int     SIZE = 2;
    
    /** Build a shape according to the specified type. */
    public static Shape makeShape(Point anchor, Point p, int shapeType)
    {
        if (anchor == null || p == null) return null;
        int topLeftX = Math.min(anchor.x, p.x), 
            topLeftY = Math.min(anchor.y, p.y),
            width = Math.abs(anchor.x-p.x),
            height = Math.abs(anchor.y-p.y);
        return makeShape(shapeType, topLeftX, topLeftY, width, height);
    }
    
    /** Build a shape according to the specified type. */
    public static Shape makeShape(int shapeType, int x, int y, int width, 
            int height)
    {
        Shape s = null;
        switch (shapeType) {
            case RECTANGLE:
                s = new Rectangle(x, y, width, height);
                break;
            case ELLIPSE:
                s = new Ellipse2D.Float(x, y, width, height);
        }
        return s; 
    }
    
    /** Reset the bounds of the specified shape. */
    public static void setShapeBounds(Shape s, int shapeType, int x, int y, 
                                        int width, int height) 
    {
        if (s != null) {
            switch (shapeType) {
                case RECTANGLE:
                    Rectangle r = (Rectangle) s;
                    r.setBounds(x, y, width, height);
                    s = r;
                    break;
                case ELLIPSE:
                    Ellipse2D.Float e = (Ellipse2D.Float) s;
                    e.setFrame(x, y, width, height);
                    s =  e;
            } 
        }   
    } 

    /** Set the location of the label. */
    public static Point setLabelLocation(Shape s, int shapeType, int l)
    {
        Point p = new Point(0, 0);
        if (s == null) return p;
        Rectangle r = s.getBounds();
        switch (shapeType) {
            case RECTANGLE:
                p.x = r.x-l/2;
                p.y = r.y-l/2;
                break;
            case ELLIPSE:
                p.x = r.x-3*l/2;
                p.y = r.y+r.height/2-l;
        } 
        return p;
    }
    
    /** Build a rectangle to resize the shape. */
    public static Shape getVerticalArea(int x, int y, int height)
    {
        Rectangle r = new Rectangle();
        r.setBounds(x-SIZE, y, 2*SIZE, y+height);
        return r;
    }
    
    public static Shape getHorizontalArea(int x, int y, int width)
    {
        Rectangle r = new Rectangle();
        r.setBounds(x, y-SIZE, x+width, 2*SIZE);
        return r;
    }
    
}
