/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.LinearScale
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
import java.awt.geom.Point2D;

/**
 * Encapsulates a linear scale and figures ratios in between.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class LinearScale implements Scale
{
    protected double min;
    protected double max;
    
    /**
     * Creates a linear scale with the specified min and max.  If min > max,
     * an exception is thrown.
     * @param min The range minimum.
     * @param max The range maximum.
     */
    public LinearScale(double min, double max)
    {
        this.min = min;
        this.max = max;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.Scale#getMin()
     */
    public double getMin()
    {
        return min;
    }

    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.Scale#getMax()
     */
    public double getMax()
    {
        return max;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.Scale#getScalar(double, double, double)
     */
    public double getScalar(double value, double min, double max)
    {
        double scaleValue = (value-this.min)/(this.max-this.min);
        if(scaleValue < 0) return min;
        else if(scaleValue > 1) return max;
        else
        {
            return scaleValue*(max-min)+min;
        }
    }
    
    /**
     * Project a value onto a particular color.
     * @param value The value to check
     * @param minColor The color corresponding to min.
     * @param maxColor The color corresponding to max.
     * @return The color corresponding to val.
     */
    public Color getColor(double value, Color minColor, Color maxColor)
    {
        double scaleValue = (value-min)/(max-min);
        int minRed = minColor.getRed();
        int minGreen = minColor.getGreen();
        int minBlue = minColor.getBlue();
        int maxRed = maxColor.getRed();
        int maxGreen = maxColor.getGreen();
        int maxBlue = maxColor.getBlue();
        
        if(scaleValue < 0) return minColor;
        else if(scaleValue > 1) return maxColor;
        else
        {
            int scaleRed = (int)Math.round(scaleValue*(maxRed-minRed)+minRed);
            int scaleGreen = (int)Math.round(scaleValue*(maxGreen-minGreen)+minGreen);
            int scaleBlue = (int)Math.round(scaleValue*(maxBlue-minBlue)+minBlue);
            return new Color(scaleRed,scaleGreen,scaleBlue);
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.Scale#getPoint(double, java.awt.geom.Point2D, java.awt.geom.Point2D,)
     */
    public Point2D getPoint(double value, Point2D minPoint, Point2D maxPoint)
    {
        double scaleValue = (value-min)/(max-min);
        if(scaleValue < 0) return minPoint;
        else if(scaleValue > 1) return maxPoint;
        else
        {
            double minX = minPoint.getX();
            double minY = minPoint.getY();
            double maxX = maxPoint.getX();
            double maxY = maxPoint.getY();
            
            return new Point2D.Double(scaleValue*(maxX-minX)+minX,
                                      scaleValue*(maxY-minY)+minY);
        }
    }
}
