/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapStats
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

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;

/**
 * Gets the min and max of a scalar attribute element over a list of attributes.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class RangeChecker
{
    /**
     * Gets the min/max over an array of attributes.
     * @param attributes
     * @param elementName
     * @return
     * @throws IllegalArgumentException
     */
    public static double[] getMinMax(Attribute[] attributes,
                                     String elementName)
        throws IllegalArgumentException
    {
        double[] vals = HeatMapUtils.extractValues(attributes,elementName);
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        
        for(int i=0;i<vals.length;i++)
        {
            if(vals[i] == Double.NaN) continue;
            if(vals[i] < min)
            {
                min = vals[i];
            }
            if(vals[i] > max)
            {
                max = vals[i];
            }
        }
        return new double[] {min,max};
    }
    
    /**
     * Gets the min/max over an entire array of thumbnails.
     * @param thumbnails
     * @param attributeName
     * @param elementName
     * @return
     * @throws IllegalArgumentException
     */
    public static double[] getGlobalMinMax(Thumbnail[] thumbnails,
                                           String attributeName,
                                           String elementName)
        throws IllegalArgumentException
    {
        if(thumbnails == null || attributeName == null || elementName == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        else if(thumbnails.length == 0)
        {
            return new double[]{0,1};
        }
        List attributeList = new ArrayList();
        for(int i=0;i<thumbnails.length;i++)
        {
            if(thumbnails[i].isMultipleThumbnail())
            {
                ThumbnailDataModel[] tdms = thumbnails[i].getMultipleModels();
                for(int j=0;j<tdms.length;j++)
                {
                    ThumbnailDataModel tdm = tdms[j];
                    AttributeMap map = tdm.getAttributeMap();
                    List attributes = map.getAttributes(attributeName);
                    if(attributes != null)
                    {
                        attributeList.addAll(attributes);
                    }
                }
                
            }
            else
            {
                ThumbnailDataModel tdm = thumbnails[i].getModel();
                AttributeMap map = tdm.getAttributeMap();
                List attributes = map.getAttributes(attributeName);
                if(attributes != null)
                {
                    attributeList.addAll(attributes);
                }
            }
        }
        Attribute[] attrs = new Attribute[attributeList.size()];
        attributeList.toArray(attrs);
        return getMinMax(attrs,elementName);
    }
}
