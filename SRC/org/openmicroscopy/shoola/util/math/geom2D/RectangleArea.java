/*
 * org.openmicroscopy.shoola.util.math.geom2D.RectangleArea
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

package org.openmicroscopy.shoola.util.math.geom2D;

//Java imports
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.mem.Handle;

/** 
 * Represents an rectangle in Euclidean space <b>R</b><sup>2</sup>.
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
public class RectangleArea
    extends Handle 
    implements PlaneArea
{

    /** 
     * Constructs a new {@link RectangleAreaAdapter} whose top-left corner 
     * is (0, 0) and whose width and height are both zero.
     */
    public RectangleArea()
    {
        super(new RectangleAreaAdapter());
    }
    
    /** 
     * Constructs a new {@link RectangleAreaAdapter} whose top-left corner 
     * is specified as (x, y) and whose width and height are specified by 
     * the arguments of the same name.
     */
    public RectangleArea(int x, int y, int width, int height)
    {
        super(new RectangleAreaAdapter(x, y, width, height));
    }
    
    /** Implemented as specified in the {@link PlaneArea} I/F. */
    public void setBounds(int x, int y, int width, int height)
    {
        breakSharing();
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        adapter.setBounds(x, y, width, height);
    }

    /** Implemented as specified in the {@link PlaneArea} I/F. */
    public void scale(double factor)
    {  
        breakSharing();
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        Rectangle r = adapter.getBounds();
        adapter.setBounds((int) (r.x*factor), (int) (r.y*factor), 
                  (int) (r.width*factor), (int) (r.height*factor)); 
    }
    
    /** Implemented as specified in the {@link PlaneArea} I/F. */
    public PlanePoint[] getPoints()
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();;
        return adapter.getPoints();
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public boolean contains(double x, double y)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.contains(x, y);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public boolean contains(double x, double y, double w, double h)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.contains(x, y, w, h);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public boolean intersects(double x, double y, double w, double h)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.intersects(x, y, w, h);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public Rectangle getBounds()
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.getBounds();
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public boolean contains(Point2D p)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.contains(p);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public Rectangle2D getBounds2D()
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.getBounds2D();
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public boolean contains(Rectangle2D r)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.contains(r);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public boolean intersects(Rectangle2D r)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.intersects(r);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public PathIterator getPathIterator(AffineTransform at)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.getPathIterator(at);
    }

    /** Required by the {@link java.awt.Shape Shape} I/F. */
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        RectangleAreaAdapter adapter = (RectangleAreaAdapter) getBody();
        return adapter.getPathIterator(at, flatness);
    }

}
