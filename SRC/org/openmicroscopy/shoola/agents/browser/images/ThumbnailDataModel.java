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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.shoola.env.data.model.ImageData;

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
    protected ImageData imageData;

    /**
     * The set of attributes in the data model.
     */
    protected Map attributeMap;

    /**
     * Creates a data model with the backing ImageData information and an
     * empty set of attributes.
     * 
     * @param basicData The basic backing image data, including ID, name,
     *                  and owner.
     * @throws IllegalArgumentException if basicData is null.
     */
    public ThumbnailDataModel(ImageData basicData)
        throws IllegalArgumentException
    {
        if(basicData == null)
        {
            throw new IllegalArgumentException("Basic model cannot be null" +
                " in ThumbnailDataModel(ImageData)");
        }
        imageData = basicData;
        attributeMap = new HashMap();
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
    public ThumbnailDataModel(ImageData basicData, Map attributeMap)
    {
        if(basicData == null)
        {
            throw new IllegalArgumentException("Basic model cannot be null" +
                " in ThumbnailDataModel(ImageData,Map)");
        }
        imageData = basicData;
        
        if(attributeMap == null)
        {
            this.attributeMap = new HashMap();
        }
        else // deep copy
        {
            this.attributeMap = new HashMap();
            for(Iterator iter = attributeMap.keySet().iterator();
                iter.hasNext();)
            {
                Object key = iter.next();
                this.attributeMap.put(key,attributeMap.get(key));
            }
        }
    }

    /**
     * Returns the ID of the image.
     * @return The image ID.
     */
    public int getID()
    {
        return imageData.getID();
    }
    
    /**
     * Returns the name of the image.
     * @return The image name.
     */
    public String getName()
    {
        return imageData.getName();
    }
    
    /**
     * Returns the basic set of information about the image.
     * @return See above.
     */
    public ImageData getImageInformation()
    {
        return imageData;
    }

    /**
     * Returns the set of valid keys for attributes in this thumbnail.
     * 
     * @return See above.
     */
    public Set getKeyStrings()
    {
        return Collections.unmodifiableSet(attributeMap.keySet());
    }

    /**
     * Gets the attribute with the specified name.
     * 
     * @param key The name of the attribute.
     * @return The attribute with the specified name.
     */
    public Attribute getAttribute(String key)
    {
        if (key != null)
        {
            return (Attribute) attributeMap.get(key);
        }
        else
            return null;
    }

    /**
     * Binds the name to the specified Attribute in the data model.
     * Will have no effect if either parameter is null.
     * @param key The name of the attribute.
     * @param attribute The attribute to bind.
     */
    public void setAttribute(String key, Attribute attribute)
    {
        if (key == null || attribute == null)
        {
            return;
        }
        else
        {
            attributeMap.put(key, attribute);
        }
    }

    /**
     * Unbinds (and returns) the Attribute previously bound to
     * the specified name.
     * @param key The attribute to remove.
     * @return The removed attribute, or null if none existed.
     */
    public Attribute removeAttribute(String key)
    {
        if (attributeMap.containsKey(key))
        {
            Attribute attr = (Attribute) attributeMap.get(key);
            attributeMap.remove(key);
            return attr;
        }
        else
        {
            return null;
        }
    }
}
