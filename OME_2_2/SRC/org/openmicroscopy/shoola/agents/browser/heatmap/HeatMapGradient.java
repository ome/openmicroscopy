/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGradient
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
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.awt.Color;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version
 * @since
 */
public interface HeatMapGradient
{
    /**
     * Get the min value of the gradient.
     * @return
     */
    public double getMin();
    
    /**
     * Sets the min value of the gradient to the specified value.
     * @param d
     */
    public void setMin(double d);
    
    /**
     * Gets the max value of the gradient.
     * @return
     */
    public double getMax();
    
    /**
     * Sets the max value of the gradient to the specified value.
     * @param d
     */
    public void setMax(double d);
    
    /**
     * Gets the min color.
     * @return
     */
    public Color getMinColor();
    
    /**
     * Sets the min color to the specified color.
     * @param c
     */
    public void setMinColor(Color c);
    
    /**
     * Gets the max color.
     * @return
     */
    public Color getMaxColor();
    
    /**
     * Sets the max color to the specified color.
     * @param c
     */
    public void setMaxColor(Color c);
    
    public boolean isDiscrete();
    
    public void setDiscrete(boolean discrete);
}
