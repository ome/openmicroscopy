/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.DataAttribute
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

import java.util.*;

/**
 * A flexible representation of a data type (to be used for generic semantic
 * types or types in general, independent of the remote bindings contained in
 * OME semantic types)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class DataAttribute
{
    protected Map elementMap;
    protected Map elementTypeMap;
    protected String typeName;

    /**
     * Creates a new DataAttribute with the given type name.
     * 
     * @param typeName The name of the attribute type.
     * @throws IllegalArgumentException if typeName is null.
     */
    public DataAttribute(String typeName) throws IllegalArgumentException
    {
        if (typeName == null)
        {
            throw new IllegalArgumentException("typeName cannot be null.");
        }
        elementMap = new HashMap();
        elementTypeMap = new HashMap();
        this.typeName = typeName;
    }

    /**
     * Returns a list of the element names in alphabetical order.  The list
     * cannot be modified.
     * 
     * @return The names of the elements in the Attribute.
     */
    public List getElementNames()
    {
        // return in order
        List list = new ArrayList(elementMap.keySet());
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the name of the attribute.
     * @return The name of the attribute.
     */
    public String getAttributeName()
    {
        return typeName;
    }

    /**
     * Returns the type of the element with the specified key.  Returns null
     * if the key is invalid.
     * 
     * @param key The name of the element to investigate.
     * @return The type of the investigated element.
     */
    public DataElementType getElementType(String key)
    {
        if (elementTypeMap.containsKey(key))
        {
            return (DataElementType) elementTypeMap.get(key);
        }
        else
            return null;
    }

    /**
     * The fallback get method-- will return the element as an uncast Object.
     * Will return null if the element name is invalid.  This is also the only
     * valid way to get elements of type OBJECT.
     * 
     * @param key The name of the element to retrieve.
     * @return An uncast object value, or null if the key is invalid.
     */
    public Object getElement(String key)
    {
        if (key == null)
        {
            return null;
        }
        return elementMap.get(key);
    }

    /** Returns the object element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to an object (non-primitive).
     * 
     * @param key The name of the element.
     * @return The Integer corresponding to this element, or null if the name
     *         is invalid or does not refer to an integer type.
     */
    public Object getObjectElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.OBJECT)
        {
            return null;
        }
        return elementMap.get(key);
    }

    /**
     * Sets the element to the specified object, and the type of the element
     * to type OBJECT if the element is not yet present.  getElement() or
     * getObjectElement() must be used in conjunction with elements set with
     * this method.  Will return false if the key is null, or if
     * you try to set an existing element of an incompatible type.
     * 
     * @param key The name of the element to add.
     * @param element The element itself.
     * @return Whether or not the element was successfully set.
     */
    public boolean setObjectElement(String key, Object element)
    {
        if (element == null)
        {
            return false;
        }
        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.OBJECT)
            {
                return false;
            }
            else
            {
                elementMap.put(key, element);
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.OBJECT);
            elementMap.put(key, element);
            return true;
        }
    }

    /**
     * Returns the integer element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to an integer.
     * 
     * @param key The name of the element.
     * @return The Integer corresponding to this element, or null if the name
     *         is invalid or does not refer to an integer type.
     */
    public Integer getIntElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.INT)
        {
            return null;
        }
        Integer value = (Integer) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified int, and the type of the element to
     * INT if the element is not already present. getIntElement() must be
     * used in conjunction with elements set with this method.  Will return
     * false if the named key already refers to an element of a different type,
     * or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The int value of the element.
     * @return If the element was successfully set.
     */
    public boolean setIntElement(String key, int element)
    {
        if (key == null)
        {
            return false;
        }
        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.INT)
            {
                return false;
            }
            else
            {
                elementMap.put(key, new Integer(element));
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.INT);
            elementMap.put(key, new Integer(element));
            return true;
        }
    }

    /**
     * Returns the float element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to a float.
     * 
     * @param key The name of the element.
     * @return The Float corresponding to this element, or null if the name
     *         is invalid or does not refer to a float type.
     */
    public Float getFloatElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.FLOAT)
        {
            return null;
        }
        Float value = (Float) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified float, and the type of the element to
     * FLOAT if the element is not already present. getFloatElement() must be
     * used in conjunction with elements set with this method.  Will return
     * false if the named key already refers to an element of a different type,
     * or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The float value of the element.
     * @return If the element was successfully set.
     */
    public boolean setFloatElement(String key, float element)
    {
        if (key == null)
        {
            return false;
        }

        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.FLOAT)
            {
                return false;
            }
            else
            {
                elementMap.put(key, new Float(element));
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.FLOAT);
            elementMap.put(key, new Float(element));
            return true;
        }
    }

    /**
     * Returns the long element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to a long.
     * 
     * @param key The name of the element.
     * @return The Long corresponding to this element, or null if the name
     *         is invalid or does not refer to a long type.
     */
    public Long getLongElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.LONG)
        {
            return null;
        }
        Long value = (Long) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified long, and the type of the element to
     * LONG if the element is not already present. getLongElement() must be
     * used in conjunction with elements set with this method.  Will return
     * false if the named key already refers to an element of a different type,
     * or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The long value of the element.
     * @return If the element was successfully set.
     */
    public boolean setLongElement(String key, long element)
    {
        if (key == null)
        {
            return false;
        }

        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.LONG)
            {
                return false;
            }
            else
            {
                elementMap.put(key, new Long(element));
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.LONG);
            elementMap.put(key, new Long(element));
            return true;
        }
    }

    /**
     * Returns the double element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to a double.
     * 
     * @param key The name of the element.
     * @return The Double corresponding to this element, or null if the name
     *         is invalid or does not refer to a double type.
     */
    public Double getDoubleElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.DOUBLE)
        {
            return null;
        }
        Double value = (Double) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified double, and the type of the element to
     * DOUBLE if the element is not already present. getDoubleElement() must be
     * used in conjunction with elements set with this method.  Will return
     * false if the named key already refers to an element of a different type,
     * or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The double value of the element.
     * @return If the element was successfully set.
     */
    public boolean setDoubleElement(String key, double element)
    {
        if (key == null)
        {
            return false;
        }

        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.DOUBLE)
            {
                return false;
            }
            else
            {
                elementMap.put(key, new Double(element));
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.DOUBLE);
            elementMap.put(key, new Double(element));
            return true;
        }
    }

    /**
     * Returns the boolean element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to a boolean.
     * 
     * @param key The name of the element.
     * @return The Boolean corresponding to this element, or null if the name
     *         is invalid or does not refer to a boolean type.
     */
    public Boolean getBooleanElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.BOOLEAN)
        {
            return null;
        }
        Boolean value = (Boolean) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified boolean, and the type of the element to
     * BOOLEAN if the element is not already present. getBooleanElement() must be
     * used in conjunction with elements set with this method.  Will return
     * false if the named key already refers to an element of a different type,
     * or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The boolean value of the element.
     * @return If the element was successfully set.
     */
    public boolean setBooleanElement(String key, boolean element)
    {
        if (key == null)
        {
            return false;
        }

        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.BOOLEAN)
            {
                return false;
            }
            else
            {
                elementMap.put(key, new Boolean(element));
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.BOOLEAN);
            elementMap.put(key, new Boolean(element));
            return true;
        }
    }

    /**
     * Returns the string element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to a string.
     * 
     * @param key The name of the element.
     * @return The String corresponding to this element, or null if the name
     *         is invalid or does not refer to a string type.
     */
    public String getStringElement(String key)
    {
        if (elementTypeMap.get(key) != DataElementType.STRING)
        {
            return null;
        }
        String value = (String) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified string, and the type of the element to
     * STRING if the element is not already present. getStringElement() must be
     * used in conjunction with elements set with this method.  Will return
     * false if the named key already refers to an element of a different type,
     * or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The string value of the element.
     * @return If the element was successfully set.
     */
    public boolean setStringElement(String key, String element)
    {
        if (key == null || element == null)
        {
            return false;
        }

        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.STRING)
            {
                return false;
            }
            else
            {
                elementMap.put(key, element);
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.STRING);
            elementMap.put(key, element);
            return true;
        }
    }

    /**
     * Returns the attribute element tied to the key value.  Will return null if
     * the key is either invalid or does not refer to an attribute.
     * 
     * @param key The name of the element.
     * @return The DataAttribute corresponding to this element, or null if the name
     *         is invalid or does not refer to an attribute type.
     */
    public DataAttribute getAttributeElement(String key)
    {
        if (key == null)
        {
            return null;
        }
        if (elementTypeMap.get(key) != DataElementType.ATTRIBUTE)
        {
            return null;
        }
        DataAttribute value = (DataAttribute) getElement(key);
        return value;
    }

    /**
     * Sets the element to the specified attribute, and the type of the element
     * to ATTRIBUTE if the element is not already present. getAttributeElement()
     * must be used in conjunction with elements set with this method.  Will
     * return false if the named key already refers to an element of a different
     * type, or if the key is null.
     * 
     * @param key The name of the element to set.
     * @param element The int value of the element.
     * @return If the element was successfully set.
     */
    public boolean setAttributeElement(String key, DataAttribute element)
    {
        if (key == null || element == null)
        {
            return false;
        }

        if (elementTypeMap.containsKey(key))
        {
            if (elementTypeMap.get(key) != DataElementType.ATTRIBUTE)
            {
                return false;
            }
            else
            {
                elementMap.put(key, element);
                return true;
            }
        }
        else
        {
            elementTypeMap.put(key, DataElementType.ATTRIBUTE);
            elementMap.put(key, element);
            return true;
        }
    }

    /**
     * Defines an element without initializing it.  A call to setXXX will have
     * the same effect when called for the first time, but will assign a value.
     * If this is called on an element that already exists, nothing will happen,
     * and the method will return false.
     * 
     * @param key The element to define.
     * @param type The type of the element to define.
     * @return true if the element was created, false otherwise (including null
     *         parameters)
     */
    public boolean defineElement(String key, DataElementType type)
    {
        if (key == null || type == null)
        {
            return false;
        }
        if (elementMap.containsKey(key))
        {
            return false;
        }
        else
        {
            elementMap.put(key, null);
            elementTypeMap.put(key, type);
            return true;
        }
    }

    /**
     * Set the current value of the specified element to null.  Returns true
     * if the key is a valid element name, false otherwise.
     * 
     * @param key The name of the element to nullify.
     * @return Whether or not the key was valid (and thus, the element was
     *         nullified)
     */
    public boolean setElementNull(String key)
    {
        if (elementMap.containsKey(key))
        {
            elementMap.put(key, null);
            return true;
        }
        else
            return false;
    }
}
