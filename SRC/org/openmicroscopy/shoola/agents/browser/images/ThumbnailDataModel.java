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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.agents.browser.datamodel.DataAttribute;
import org.openmicroscopy.shoola.agents.browser.datamodel.DataAttributeListener;

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
     * The ID of the model/thumbnail.
     */
    protected int ID;

    /**
     * The set of attributes in the data model.
     */
    protected Map attributeMap;

    /**
     * The set of event listeners listening for changes in the data model.
     */
    protected Set eventListeners;

    /**
     * Creates a data model with the pixel source and an empty set of
     * attributes.
     * 
     * @param source
     */
    public ThumbnailDataModel(int ID)
    {
        this.ID = ID;
        attributeMap = new HashMap();
        eventListeners = new HashSet();
    }

    /**
     * Creates a data model with the pixel source and a specified set of
     * attributes.
     * 
     * @param source
     * @param attributeMap
     */
    public ThumbnailDataModel(int ID, Map attributeMap)
    {
        this.ID = ID;
        this.attributeMap = new HashMap();
        eventListeners = new HashSet();

        // avoid NPE on null map.
        if (attributeMap == null)
        {
            return;
        }

        // deep copy
        for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext();)
        {
            Object key = iter.next();
            this.attributeMap.put(key, attributeMap.get(key));
        }
    }

    /**
     * Returns the ID of the image.
     * @return The image ID.
     */
    public int getID()
    {
        return ID;
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
    public DataAttribute getAttribute(String key)
    {
        if (key != null)
        {
            return (DataAttribute) attributeMap.get(key);
        }
        else
            return null;
    }

    /**
     * Binds the name to the specified DataAttribute in the data model.
     * Will have no effect if either parameter is null.
     * @param key The name of the attribute.
     * @param attribute The attribute to bind.
     */
    public void setAttribute(String key, DataAttribute attribute)
    {
        if (key == null || attribute == null)
        {
            return;
        }
        else
        {
            attributeMap.put(key, attribute);
            // TODO: add event listener change info
        }
    }

    /**
     * Unbinds (and returns) the DataAttribute previously bound to
     * the specified name.
     * @param key The attribute to remove.
     * @return The removed attribute, or null if none existed.
     */
    public DataAttribute removeAttribute(String key)
    {
        if (attributeMap.containsKey(key))
        {
            DataAttribute attr = (DataAttribute) attributeMap.get(key);
            attributeMap.remove(key);
            return attr;
        }
        else
            return null;
    }

    /**
     * Adds a listener to this model.
     */
    public void addListener(DataAttributeListener listener)
    {
        if (listener != null)
        {
            eventListeners.add(listener);
        }
    }

    /**
     * Removes a listener from this model.
     */
    public void removeListener(DataAttributeListener listener)
    {
        if (listener != null)
        {
            eventListeners.remove(listener);
        }
    }
}
