/*
 * org.openmicroscopy.shoola.env.data.MappedDTO
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.shoola.env.data.dto;

import org.openmicroscopy.shoola.env.data.DataException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>Provides a base implementation of the remote framework DTO
 * classes, backed by a {@link Map}.  Helper methods are provided to
 * retrieve values of specific types from the map.  These methods
 * perform any necessary type-conversions automatically, if possible.
 * In almost all cases, the accessors of the DTO classes only need to
 * delegate to the appropriate helper accessor, called with the
 * appropriate key value.</p>
 *
 * <p>Helper methods are also provided to aid in the parsing of the
 * results of the XML-RPC calls which generate the DTO's.  In the
 * simplest cases, the result of the XML-RPC call is a
 * <code>struct</code> which is suitable to be used directly as the
 * map backing the DTO.  In the more complex cases, though, the helper
 * methods must be used, for instance, to translate the contents of a
 * sublist into the appropriate DTO's.</p>
 *
 * @author Douglas Creager (dcreager@alum.mit.edu)
 * @version 2.2
 * @since OME2.2
 */

public class MappedDTO
{
    /**
     * This is the {@link Map} used to back the DTO.  All of the data
     * fields of the DTO are stored in the map.  The helper accessor
     * and mutator methods access values in this map.
     */
    protected Map elements;

    /**
     * Creates a new <code>MappedDTO</code> without an initialized
     * backing map.  Obviously, the new instance won't be useful until
     * it is provided with a backing map via the {@link setMap}
     * method.
     *
     * @return a new <code>MappedDTO</code> instance
     */
    public MappedDTO()
    {
        super();
        this.elements = null;
    }

    /**
     * Creates a new <code>MappedDTO</code> with the specified backing
     * map.  This map is usually the result of an XML-RPC call.
     *
     * @return a new <code>MappedDTO</code> instance
     */
    public MappedDTO(Map elements)
    {
        super();
        setMap(elements);
    }

    /**
     * Returns the backing map for this instance.
     */
    protected Map getMap() { return elements; }

    /**
     * Establishes a new backing map for this instance.  The previous
     * backing map is discarded.  Subclasses should override this
     * method to post-process the backing map, if necessary.  (For
     * instance, if one of the elements in the map is a list of
     * children objects, this method should be overridden to call
     * {@link parseListElement} to parse that list into Java objects.)
     */
    protected void setMap(Map elements) { this.elements = elements; }

    /**
     * Helper method for parsing an element which is a child object.
     * This will turn the <code>struct</code> into an instance of the
     * specified DTO class.  (This class must be a subclass of {@link
     * MappedDTO}.)  If the element specified doesn't exist in this
     * DTO (because it wasn't filled in by the XML-RPC method which
     * created this DTO), then nothing happens.
     */
    protected void parseChildElement(String element, Class dtoClazz)
    {
        // It's an error if the specified class isn't a MappedDTO
        // subclass.
        if (!MappedDTO.class.isAssignableFrom(dtoClazz))
            throw new DataException("Specified class is not a MappedDTO subclass");

        // If the desired element doesn't exist, return silently.
        if (!elements.containsKey(element))
            return;

        try
        {
            Map m = (Map) elements.get(element);
            MappedDTO dto = (MappedDTO) dtoClazz.newInstance();
            dto.setMap(m);
            elements.put(element,dto);
        } catch (InstantiationException e) {
            throw new DataException("Cannot create instance of "+dtoClazz);
        } catch (IllegalAccessException e) {
            throw new DataException("Cannot create instance of "+dtoClazz);
        }
    }

    /**
     * Helper method for parsing an element which is a list.  This
     * will turn the list of <code>struct</code>s into a list of the
     * specified DTO class.  (This class must be a subclass of {@link
     * MappedDTO}.)  If the element specified doesn't exist in this
     * DTO (because it wasn't filled in by the XML-RPC method which
     * created this DTO), then nothing happens.
     */
    protected void parseListElement(String element, Class dtoClazz)
    {
        // It's an error if the specified class isn't a MappedDTO
        // subclass.
        if (!MappedDTO.class.isAssignableFrom(dtoClazz))
            throw new DataException("Specified class is not a MappedDTO subclass");

        // If the desired element doesn't exist, return silently.
        if (!elements.containsKey(element))
            return;

        try
        {
            List list = (List) elements.get(element);
            for (int i = 0; i < list.size(); i++)
            {
                Map m = (Map) list.get(i);
                MappedDTO dto = (MappedDTO) dtoClazz.newInstance();
                dto.setMap(m);
                list.set(i,dto);
            }
        } catch (InstantiationException e) {
            throw new DataException("Cannot create instance of "+dtoClazz);
        } catch (IllegalAccessException e) {
            throw new DataException("Cannot create instance of "+dtoClazz);
        }
    }

    /**
     * Returns an <code>int</code> value from the backing map.  It is
     * an error if the specified key does not exist (i.e., it was not
     * populated by the XML-RPC call which created this DTO object).
     * If the value in the backing map is an {@link Integer}, the
     * <code>int</code> is returned directly.  If the value is a
     * {@link Long}, the <code>long</code> value is cast into an
     * <code>int</code> and returned.  If the value is a {@link Float}
     * or {@link Double}, is it rounded via the {@link Math#round}
     * method, and cast into an <code>int</code>.  If the value is a
     * {@link String} which can be parsed into an <code>int</code>,
     * the parsed value is returned.  In all other cases, an error
     * occurs.
     *
     * @param key the element of the backing map to retrieve
     * @return an <code>int</code> value representing the specified
     * element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map or if the value cannot be turned into an
     * <code>int</code>
     */
    protected int getIntElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        Object o = elements.get(key);

        if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof Long)
            return (int) ((Long) o).longValue();
        else if (o instanceof Float)
            return Math.round(((Float) o).floatValue());
        else if (o instanceof Double)
            return (int) Math.round(((Double) o).doubleValue());
        else if (o instanceof String) {
            try
            {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                throw new DataException("Expected an int, got an ugly String");
            }
        } else
            throw new DataException("Expected an int, got a "+o.getClass());
    }

    /**
     * Returns a <code>long</code> value from the backing map.  It is
     * an error if the specified key does not exist (i.e., it was not
     * populated by the XML-RPC call which created this DTO object).
     * If the value in the backing map is an {@link Integer} or {@link
     * Long}, the value is returned directly.  If the value is a
     * {@link Float} or {@link Double}, is it rounded via the {@link
     * Math#round} method, and cast into a <code>long</code>.  If the
     * value is a {@link String} which can be parsed into a
     * <code>long</code>, the parsed value is returned.  In all other
     * cases, an error occurs.
     *
     * @param key the element of the backing map to retrieve
     * @return a <code>long</code> value representing the specified
     * element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map or if the value cannot be turned into a
     * <code>long</code>
     */
    protected long getLongElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        Object o = elements.get(key);

        if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof Long)
            return ((Long) o).longValue();
        else if (o instanceof Float)
            return Math.round(((Float) o).floatValue());
        else if (o instanceof Double)
            return Math.round(((Double) o).doubleValue());
        else if (o instanceof String) {
            try
            {
                return Long.parseLong((String) o);
            } catch (NumberFormatException e) {
                throw new DataException("Expected a long, got an ugly String");
            }
        } else
            throw new DataException("Expected a long, got a "+o.getClass());
    }

    /**
     * Returns a <code>float</code> value from the backing map.  It is
     * an error if the specified key does not exist (i.e., it was not
     * populated by the XML-RPC call which created this DTO object).
     * If the value in the backing map is an {@link Integer}, {@link
     * Long}, {@link Float}, or {@link Double} the value is returned
     * directly, with an appropriate case as necessary.  If the value
     * is a {@link String} which can be parsed into a
     * <code>float</code>, the parsed value is returned.  In all other
     * cases, an error occurs.
     *
     * @param key the element of the backing map to retrieve
     * @return a <code>float</code> value representing the specified
     * element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map or if the value cannot be turned into a
     * <code>float</code>
     */
    protected float getFloatElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        Object o = elements.get(key);

        if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof Long)
            return ((Long) o).longValue();
        else if (o instanceof Float)
            return ((Float) o).floatValue();
        else if (o instanceof Double)
            return (float) ((Double) o).doubleValue();
        else if (o instanceof String) {
            try
            {
                return Float.parseFloat((String) o);
            } catch (NumberFormatException e) {
                throw new DataException("Expected a float, got an ugly String");
            }
        } else
            throw new DataException("Expected a float, got a "+o.getClass());
    }

    /**
     * Returns a <code>double</code> value from the backing map.  It is
     * an error if the specified key does not exist (i.e., it was not
     * populated by the XML-RPC call which created this DTO object).
     * If the value in the backing map is an {@link Integer}, {@link
     * Long}, {@link Float}, or {@link Double} the value is returned
     * directly, with an appropriate case as necessary.  If the value
     * is a {@link String} which can be parsed into a
     * <code>double</code>, the parsed value is returned.  In all other
     * cases, an error occurs.
     *
     * @param key the element of the backing map to retrieve
     * @return a <code>double</code> value representing the specified
     * element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map or if the value cannot be turned into a
     * <code>double</code>
     */
    protected double getDoubleElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        Object o = elements.get(key);

        if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof Long)
            return ((Long) o).longValue();
        else if (o instanceof Float)
            return ((Float) o).floatValue();
        else if (o instanceof Double)
            return ((Double) o).doubleValue();
        else if (o instanceof String) {
            try
            {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException e) {
                throw new DataException("Expected a double, got an ugly String");
            }
        } else
            throw new DataException("Expected a double, got a "+o.getClass());
    }

    /**
     * Returns a <code>boolean</code> value from the backing map.  It
     * is an error if the specified key does not exist (i.e., it was
     * not populated by the XML-RPC call which created this DTO
     * object).  If the value in the backing map is a {@link Boolean},
     * the <code>boolean</code> value is returned directly.  If it is
     * an {@link Integer} or {@link Long}, then a <code>false</code>
     * value is returned if the element's value is 0; otherwise
     * <code>true</code> is returned.  If the value is a {@link
     * String}, a <code>true</code> element is returned if the element
     * contains any of the following values (case-insensitive):
     * <code>true</code>, <code>t</code>, <code>yes</code>,
     * <code>y</code>, <code>1</code>.  If it contains any other
     * value, <code>false</code> is returned.  In all other cases, an
     * error occurs.
     *
     * @param key the element of the backing map to retrieve
     * @return a <code>boolean</code> value representing the specified
     * element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map or if the value cannot be turned into a
     * <code>boolean</code>
     */
    protected boolean getBooleanElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        Object o = elements.get(key);

        if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        if (o instanceof Integer)
            return ((Integer) o).intValue() != 0;
        else if (o instanceof Long)
            return ((Long) o).longValue() != 0;
        else if (o instanceof String) {
            String s = (String) o;
            return
                s.equalsIgnoreCase("true") ||
                s.equalsIgnoreCase("t") ||
                s.equalsIgnoreCase("yes") ||
                s.equalsIgnoreCase("y") ||
                s.equalsIgnoreCase("1");
        } else
            throw new DataException("Expected a boolean, got a "+o.getClass());
    }

    /**
     * Returns a {@link String} value from the backing map.  It is an
     * error if the specified key does not exist (i.e., it was not
     * populated by the XML-RPC call which created this DTO object).
     * If the object in the backing map for this key is not a {@link
     * String}, it is transformed into one via the {@link
     * Object#toString} method.
     *
     * @param key the element of the backing map to retrieve
     * @return a {@link String} object representing the specified
     * element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map
     */
    protected String getStringElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        return elements.get(key).toString();
    }

    /**
     * Returns the value for the specified key.  No type-checking or
     * type-casting is performed; whatever was returned by the XML-RPC
     * layer is returned from this method.  The only error condition
     * is if the key does not exist in the backing map.
     *
     * @param key the element of the backing map to retrieve
     * @return the specified element in the backing map
     * @throws DataException if the specified key does not exist in
     * the backing map
     */
    protected Object getObjectElement(String key)
    {
        if (!elements.containsKey(key))
            throw new DataException("The "+key+" field was not loaded");

        return elements.get(key);
    }

    protected void setElement(String key, Object value)
    {
        elements.put(key,value);
    }

}