/*
 * org.openmicroscopy.shoola.agents.browser.images.ZoomDependentPaintMethod
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.images;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Specifies a paint method that should only occur at certain levels of zoom.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ZoomDependentPaintMethod extends AbstractPaintMethod
{
    protected double minApplicableZoomLevel;
    protected double maxApplicableZoomLevel;
    protected PaintMethod method;
    
    /**
     * Constructs a paint method with the range over which it applies.
     * @param zoomMin The minimum applicable zoom level (1.0 = 100%), inclusive.
     * @param zoomMax The maximum applicable zoom level (1.0 = 100%), exclusive.
     * @param method The paint method to apply if the zoom level is applicable.
     */
    public ZoomDependentPaintMethod(double zoomMin, double zoomMax,
                                    PaintMethod method)
    {
        this.minApplicableZoomLevel = zoomMin;
        this.maxApplicableZoomLevel = zoomMax;
        if(method != null)
        {
            this.method = method;
        }
    }
    
    /**
     * Executes the paint method (dependent on the zoom level indicated in the
     * context)
     * @param t The thumbnail over which to paint.
     * @param context The context in which the thumbnail is being drawn.
     */
    public void paint(PPaintContext context, Thumbnail t)
    {
        if(t == null || context == null)
        {
            return;
        }
        double scale = context.getScale();
        if(scale >= minApplicableZoomLevel && scale < maxApplicableZoomLevel)
        {
            method.paint(context,t);
        }
    }
}
