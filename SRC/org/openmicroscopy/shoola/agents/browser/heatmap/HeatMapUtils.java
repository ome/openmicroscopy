/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapUtils
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

import org.openmicroscopy.ds.dto.Attribute;

/**
 * Utility classes for the heat map component.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapUtils
{
    /**
     * Gets the double from an element name, supporting nested attributes.
     * @param attribute The attribute to parse.
     * @param elementName The name of the element to extract.
     * @return The double value of the element.
     */
    public static double parseElement(Attribute attribute, String elementName)
        throws IllegalArgumentException
    {
        if(attribute == null || elementName == null)
        {
            throw new IllegalArgumentException("Null parameters at " +
                "AbstractHeatMapMode.parseElement(Attribute,String)");
        }
        
        int index = 0;
        while((index = elementName.indexOf(".")) != -1)
        {
            String child = elementName.substring(0,index);
            attribute = attribute.getAttributeElement(child);
            elementName = elementName.substring(index+1);
        }
        try
        {
            System.err.println(attribute);
            double val = attribute.getDoubleElement(elementName).doubleValue();
            return val;
        }
        catch(NullPointerException npe)
        {
            throw new IllegalArgumentException("Invalid element parsed.");
        }
        catch(ClassCastException cce)
        {
            throw new IllegalArgumentException("Non-numeric element parsed.");
        }
    }
    
    /**
     * Extracts the numeric values from the attributes, given the specified
     * element name.
     * 
     * @param attributes The attributes to analyze.
     * @param elementName The elementName to index by.
     * @return The numeric values of the elements.
     * @throws IllegalArgumentException If elementName is invalid, non-numeric
     *                                  (for any attribute in the list), or
     *                                  if any/all attributes or the specified
     *                                  element name is null.
     */
    public static double[] extractValues(Attribute[] attributes,
                                         String elementName)
        throws IllegalArgumentException
    {
        if(attributes == null || elementName == null)
        {
            throw new IllegalArgumentException("Null parameters at " +
                "AbstractHeatMapMode.extractValues(Attribute[],String)");
        }
        double[] values = new double[attributes.length];
        for(int i=0;i<attributes.length;i++)
        {
            values[i] = parseElement(attributes[i],elementName);
        }
        return values;
    }
}
