/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.SemanticType;

/**
 * Abstracts a (local) mapping between SemanticType classes/names and
 * the respective values contained within. 
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class AttributeMap
{
    // TODO: figure out dirty bits (I know, it's caching, but the thumbnails
    // can't make a call to the DB *every* time they need to display info!)
    
    private Map attributeMap;
    
    /**
     * Constructs a new AttributeMap.
     */
    public AttributeMap()
    {
        attributeMap = new HashMap();
    }
    
    /**
     * Retrieves the first (or single) Attribute for the semantic type with
     * the specified name.
     * @param typeName The name of the semantic type to retrieve an attribute
     *                 instance of.
     * @return See above.
     */
    public Attribute getAttribute(String typeName)
    {
        if(!attributeMap.containsKey(typeName))
        {
            return null;
        }
        List attributeList = (List)attributeMap.get(typeName);
        return (Attribute)attributeList.get(0);
    }
    
    /**
     * Retrieves the first (or single) Attribute for the specified
     * semantic type.
     * @param type The semantic type of which to retrieve an attribute instance.
     * @return See above.
     */
    public Attribute getAttribute(SemanticType type)
    {
        if(type == null)
        {
            return null;
        }
        else return getAttribute(type.getName());
    }
    
    /**
     * Retrieves an Attribute for the semantic type with the
     * specified name, with the specified ID.
     * @param typeName The name of the semantic type to retrieve the
     *                 Attribute for.
     * @return See above.
     */
    public Attribute getAttribute(String typeName, int attributeID)
    {
        if(!attributeMap.containsKey(typeName))
        {
            return null;
        }
        List attributeList = (List)attributeMap.get(typeName);
        
        for(Iterator iter = attributeList.iterator(); iter.hasNext();)
        {
            Attribute attribute = (Attribute)iter.next();
            if(attribute.getID() == attributeID)
            {
                return attribute;
            }
        }
        return null;
    }
    
    /**
     * Retrieves an Attribute for the semantic type, with the specified ID.
     * @param typeName The name of the semantic type to retrieve.
     * @return See above.
     */
    public Attribute getAttribute(SemanticType type, int attributeID)
    {
       if(type == null)
       {
           return null;
       }
       else return getAttribute(type.getName(),attributeID);
    }
    
    /**
     * Retrieves all Attributes for the semantic type with the specified name.
     * @param typeName The name of the type.
     * @return See above.
     */
    public List getAttributes(String typeName)
    {
        if(!attributeMap.containsKey(typeName))
        {
            return null;
        }
        List attributeList = (List)attributeMap.get(typeName);
        return Collections.unmodifiableList(attributeList);
    }
    
    /**
     * Retrieves all Attributes for the given semantic type.
     * @param type The type.
     * @return See above.
     */
    public List getAttributes(SemanticType type)
    {
        if(type == null)
        {
            return null;
        }
        return getAttributes(type.getName());
    }
    
    /**
     * Explicitly sets the attribute to be the only attribute of the
     * specified semantic type (good for one-to-one relationships)
     * 
     * @param typeName The semantic type that this attribute will represent.
     * @param attribute The attribute to assign to this ST.
     */
    public void putAttributeExclusive(Attribute attribute)
    {
        if(attribute == null)
        {
            return;
        }
        String typeName = attribute.getSemanticType().getName();
        List attributeList = new ArrayList();
        attributeList.add(attribute);
        attributeMap.put(typeName,attributeList);
    }
    
    /**
     * Adds/modifies an attribute in the map.  If the ID of the attribute is
     * already in the map, the attribute will be updated in the map.  Otherwise,
     * it will be added to the map.  The type is inferred from the Attribute
     * itself.
     * 
     * @param typeName The name of the semantic type
     * @param attribute The attribute to add.
     */
    public void putAttribute(Attribute attribute)
    {
        if(attribute == null)
        {
            return;
        }
        
        String typeName = attribute.getSemanticType().getName();
        if(attributeMap.containsKey(typeName))
        {
            List attributeList = (List)attributeMap.get(typeName);
            for(int i=0;i<attributeList.size();i++)
            {
                Attribute att = (Attribute)attributeList.get(i);
                if(att.getID() == attribute.getID())
                {
                    System.err.println("replace attribute");
                    attributeList.set(i,attribute);
                    return;
                }
            }
            System.err.println("add attribute");
            attributeList.add(attribute);
        }
        else
        {
            List newList = new ArrayList();
            newList.add(attribute);
            attributeMap.put(typeName,newList);
        }
    }
    
    /**
     * Removes an attribute from the mapping.  If the attribute in question
     * is the lone instance of a semantic type, the type will be removed from
     * the valid type list.
     * @param attribute The attribute to remove.
     */
    public void removeAttribute(Attribute attribute)
    {
        if(attribute == null)
        {
            return;
        }
        
        String typeName = attribute.getSemanticType().getName();
        if(attributeMap.containsKey(typeName))
        {
            List attributeList = (List)attributeMap.get(typeName);
            for(int i=0;i<attributeList.size();i++)
            {
                Attribute att = (Attribute)attributeList.get(i);
                if(att.getID() == attribute.getID())
                {
                    attributeList.remove(att);
                    break;
                }
            }
            
            // clear if that's the lone attribute
            if(attributeList.size() == 0)
            {
                attributeMap.remove(typeName);
            }
        }
    }
    
    /**
     * Gets a set of valid type names (and by proxy, semantic types) stored
     * in this map.
     * @return
     */
    public Set getValidTypeNames()
    {
        return Collections.unmodifiableSet(attributeMap.keySet());
    }
}
