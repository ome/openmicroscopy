/*
 * org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/**
 * Model of thumbnail data.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailDataModel
{
    /**
     * The backing basic information of the model/thumbnail.
     */
    protected ImageSummary imageSummary;

    /**
     * The set of attributes in the data model.
     */
    protected AttributeMap attributeMap;
    
    /**
     * The set of non-Attribute metadata associated with an image.
     */
    protected Map metadataMap;

    /**
     * Creates a data model with the backing ImageData information and an
     * empty set of attributes.
     * 
     * @param basicData The basic backing image data, including ID, name,
     *                  and owner.
     * @throws IllegalArgumentException if basicData is null.
     */
    public ThumbnailDataModel(ImageSummary basicData)
        throws IllegalArgumentException
    {
        if(basicData == null)
        {
            throw new IllegalArgumentException("Basic model cannot be null" +
                " in ThumbnailDataModel(ImageData)");
        }
        imageSummary = basicData;
        attributeMap = new AttributeMap();
        metadataMap = new HashMap();
    }

    /**
     * Creates a data model with the pixel source and a specified set of
     * attributes.
     * 
     * @param basicData The basic backing image data, including ID, name
     *                  and owner.
     * @param attributeMap The specified set of String-to-Attribute mappings.
     * @throws IllegalArgumentException if basicData is null.
     */
    public ThumbnailDataModel(ImageSummary basicData, AttributeMap attributeMap)
    {
        if(basicData == null)
        {
            throw new IllegalArgumentException("Basic model cannot be null" +
                " in ThumbnailDataModel(ImageData,Map)");
        }
        imageSummary = basicData;
        metadataMap = new HashMap();
        
        if(attributeMap == null)
        {
            this.attributeMap = new AttributeMap();
        }
        else // deep copy
        {
            this.attributeMap = new AttributeMap();
            for(Iterator iter = attributeMap.getValidTypeNames().iterator();
                iter.hasNext();)
            {
                String key = (String)iter.next();
                List attributes = (List)attributeMap.getAttributes(key);
                for(Iterator iter2 = attributes.iterator(); iter2.hasNext();)
                {
                    Attribute attr = (Attribute)iter2.next();
                    this.attributeMap.putAttribute(attr);
                }
            }
        }
    }

    /**
     * Returns the ID of the image.
     * @return The image ID.
     */
    public int getID()
    {
        return imageSummary.getID();
    }
    
    /**
     * Returns the name of the image.
     * @return The image name.
     */
    public String getName()
    {
        return imageSummary.getName();
    }
    
    /**
     * Returns the basic set of information about the image.
     * @return See above.
     */
    public ImageSummary getImageInformation()
    {
        return imageSummary;
    }

    /**
     * Gets the backing attribute map for this image/thumbnail.
     * @return An attribute map for this data model.
     */
    public AttributeMap getAttributeMap()
    {
        return attributeMap;
    }

    /**
     * Sets the backing attribute map for this image/thumbnail to the
     * particular mapping.
     * 
     * @param attributeMap The map to use (can be null).
     */    
    public void setAttributeMap(AttributeMap attributeMap)
    {
        this.attributeMap = attributeMap;
    }
    
    /**
     * Gets a value stored with the specified key.
     * @param key The key of the value to retrieve.
     * @return The value bound to the specified key; null if none exists.
     */
    public Object getValue(String key)
    {
        return metadataMap.get(key);
    }
    
    /**
     * Puts a value stored with the specified key to the specified value.
     * @param key The key of a value to store.
     * @param value The value to store.
     */
    public void setValue(String key, Object value)
    {
        if(key != null)
        {
            metadataMap.put(key,value);
        }
    }
    
    /**
     * Returns a list of the valid metadata keys of the TDM.
     * @return See above.
     */
    public List getValidKeys()
    {
        List keyList = new ArrayList();
        for(Iterator iter = metadataMap.keySet().iterator(); iter.hasNext();)
        {
            keyList.add((String)iter.next());
        }
        Collections.sort(keyList);
        return keyList;
    }
}
