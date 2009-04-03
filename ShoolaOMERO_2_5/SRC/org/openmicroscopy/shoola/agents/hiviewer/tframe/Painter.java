/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.Painter
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports
import java.awt.Graphics2D;
import java.awt.Rectangle;

//Third-party libraries

//Application-internal dependencies

/** 
 * Abstract parent of classes that paint on a given area.
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
abstract class Painter
{
    
    /**
     * Subclasses implement this method to paint something on the given
     * area.
     * The area is defined by the rectangle with origin in <code>(0, 0)</code>
     * and dimensions given by <code>width</code> and <code>height</code>.
     * The graphics context has already been clipped to the specified area,
     * so painting outside the bounds is not going to be a problem.  Moreover,
     * the graphics context is private to this method, so properties can be
     * set without side-effects.
     * 
     * @param g2D   The graphics context. Guaranteed not to be 
     *              <code>null</code>.
     * @param width The width of the area to paint.
     * @param height The height of the area to paint.
     */
    protected abstract void doPaint(Graphics2D g2D, int width, int height);
    
    /**
     * Paints on the given <code>area</code>.
     * What is actually painted depends on the specific sublcass implementation,
     * but this method makes sure only the specified <code>area</code> is 
     * affected by the painting operation.
     *  
     * @param g2D   The graphics context.  Mustn't be <code>null</code>.
     * @param area  The area to paint.  Mustn't be <code>null</code>.
     */
    void paint(Graphics2D g2D, Rectangle area)
    {
        if (g2D == null) throw new NullPointerException("No graphics context.");
        if (area == null) throw new NullPointerException("No area.");
        Graphics2D scratchGraphics = (Graphics2D) g2D.create(
                                                      area.x, area.y,
                                                      area.width, area.height);
        doPaint(scratchGraphics, area.width, area.height);
        scratchGraphics.dispose();
    }
    
}
