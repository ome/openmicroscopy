/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapModes
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
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.Arrays;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.agents.browser.heatmap.AbstractHeatMapMode;
import org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapUtils;

/**
 * Collection of HeatMapModes.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class DisplayValueModes
{
    /**
     * The extract-minimum mode.
     */
    public static final DisplayValueMode MINIMUM_MODE = new AbstractHeatMapMode()
    {
        public double computeValue(Attribute[] attributes, String elementName)
            throws IllegalArgumentException
        {
            double[] values = HeatMapUtils.extractValues(attributes,elementName);
            double currentVal = Double.POSITIVE_INFINITY;
            for(int i=0;i<values.length;i++)
            {
                if(values[i] == Double.NaN) continue;
                if(values[i] < currentVal)
                {
                    currentVal = values[i];
                }
            }
            if(currentVal == Double.POSITIVE_INFINITY) return Double.NaN;
            else return currentVal;
        }
        
        public String toString()
        {
            return "Minimum";
        }
    };
    
    /**
     * The extract-mean mode.
     */
    public static final DisplayValueMode MEAN_MODE = new AbstractHeatMapMode()
    {
        public double computeValue(Attribute[] attributes, String elementName)
            throws IllegalArgumentException
        {
            double[] values = HeatMapUtils.extractValues(attributes,elementName);
            double currentVal = 0;
            for(int i=0;i<values.length;i++)
            {
                if(values[i] == Double.NaN) continue;
                currentVal += values[i];
            }
            return currentVal/(double)values.length;
        }
        
        public String toString()
        {
            return "Mean";
        }
    };
    
    /**
     * The extract-median mode.
     */
    public static final DisplayValueMode MEDIAN_MODE = new AbstractHeatMapMode()
    {
        public double computeValue(Attribute[] attributes, String elementName)
            throws IllegalArgumentException
        {
            double[] values = HeatMapUtils.extractValues(attributes,elementName);
            Arrays.sort(values);
            
            if(values.length % 2 != 0)
            {
                return values[values.length/2];
            }
            else
            {
                double value1 = values[values.length/2-1];
                double value2 = values[values.length/2];
                if(value1 == Double.NaN || value2 == Double.NaN)
                {
                    return Double.NaN;
                }
                return (value1+value2)/2.0;
            }
        }
        
        public String toString()
        {
            return "Median";
        }
    };
    
    /**
     * The extract-maximum mode.
     */
    public static final DisplayValueMode MAXIMUM_MODE = new AbstractHeatMapMode()
    {
        public double computeValue(Attribute[] attributes, String elementName)
            throws IllegalArgumentException
        {
            double[] values = HeatMapUtils.extractValues(attributes,elementName);
            double currentVal = Double.NEGATIVE_INFINITY;
            for(int i=0;i<values.length;i++)
            {
                if(values[i] == Double.NaN) continue;
                if(values[i] > currentVal)
                {
                    currentVal = values[i];
                }
            }
            if(currentVal == Double.NEGATIVE_INFINITY)
                return Double.NaN;
            else return currentVal;
        }
        
        public String toString()
        {
            return "Maximum";
        }
    };
}
