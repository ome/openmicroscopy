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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.math.geom2D.EllipseArea;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.math.geom2D.RectangleArea;
import org.openmicroscopy.shoola.util.ui.ColoredLabel;

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
    
    public static final int     SQUARE = 2;
    
    public static final int     CIRCLE = 3;
    
    
    /** Return a {@link PlaneArea} according to the shapeType. */
    public static PlaneArea makeShape(Point anchor, Point p, int shapeType)
    {
        if (anchor == null || p == null) return null;
        int topLeftX = Math.min(anchor.x, p.x), 
            topLeftY = Math.min(anchor.y, p.y),
            width = Math.abs(anchor.x-p.x),
            height = Math.abs(anchor.y-p.y);
        return makeShape(shapeType, topLeftX, topLeftY, width, height);
    }
    
    /** Return a {@link PlaneArea} according to the shapeType. */
    public static PlaneArea makeShape(int shapeType, int x, int y, int width, 
                                    int height)
    {
        PlaneArea area = null;
        switch (shapeType) {
            case RECTANGLE:
                area = new RectangleArea(x, y, width, height);  
                break;
            case ELLIPSE:
                area = new EllipseArea(x, y, width, height);  
                break;
            case SQUARE:
                int a = Math.min(width, height);
                area = new RectangleArea(x, y, a, a);
                break;
            case CIRCLE:
                int b = Math.min(width, height);
                area = new EllipseArea(x, y, b, b);
                break;
        }
        return area; 
    }
    
    public static int getLabelShapeType(PlaneArea pa)
    {
        int shapeType = ColoredLabel.NO_SHAPE;
        if (pa instanceof RectangleArea) 
            shapeType = ColoredLabel.SQUARE;
        else if (pa instanceof EllipseArea) 
            shapeType = ColoredLabel.CIRCLE;
        return shapeType;
    }
    
}
