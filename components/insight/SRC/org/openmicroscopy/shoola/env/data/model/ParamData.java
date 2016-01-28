/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

import omero.gateway.util.PojoMapper;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RMap;
import omero.RObject;
import omero.RString;
import omero.RType;
import omero.grid.Param;
import omero.model.IObject;

/**
 * Wraps up a parameter object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ParamData
{

    /** The parameter to handle. */
    private Param param;

    /** The list of possible values. */
    private List<Object> values;

    /** The default value. */
    private Object defaultValue;

    /** The default value. */
    private Object minValue;

    /** The default value. */
    private Object maxValue;

    /** The type of object to handle. */
    private Class<?> type;

    /** The value to pass in order to run the script. */
    private Object valueToPass;

    /** Initializes the value. */
    private void initialize()
    {
        type = null;
        minValue = null;
        maxValue = null;
        RType t = param.prototype;
        Object o = convertRType(t);
        defaultValue = o;
        if (o instanceof Long) {
            type = Long.class;
        } else if (o instanceof Integer) {
            type = Integer.class;
        } else if (o instanceof String) {
            type = String.class;
        } else if (o instanceof Boolean) {
            type = Boolean.class;
        } else if (o instanceof Float) {
            type = Float.class;
        } else if (o instanceof Double) {
            type = Double.class;
        } else if (o instanceof List) {
            type = List.class;
        } else if (o instanceof Map) {
            type = Map.class;
        } else if (o instanceof omero.gateway.model.DataObject)
            type = omero.gateway.model.DataObject.class;
        Number n;
        boolean set = false;
        Object value = convertRType(param.min);
        if (value instanceof Number) {
            minValue = value;
            set = true;
            if (defaultValue == null) {
                defaultValue = minValue;
            } else {
                n = (Number) defaultValue;
                if (n.doubleValue() < ((Number) minValue).doubleValue())
                    defaultValue = minValue;
            }
        }
        value = convertRType(param.max);
        if (value instanceof Number) {
            maxValue = value;
            if (!set) {
                if (defaultValue != null) {
                    n = (Number) defaultValue;
                    if (n.doubleValue() > ((Number) maxValue).doubleValue())
                        defaultValue = maxValue;
                } else {
                    defaultValue = maxValue;
                }
            }
        }
        if (defaultValue instanceof List) {
            List l = (List) defaultValue;
            if (l.size() > 0) defaultValue = l.get(0);
        }
    }

    /**
     * Converts the passed value into the corresponding RType.
     * 
     * @param value The value to convert.
     * @return See above.
     */
    private static RType convertBasicValue(Object value)
    {
        if (value == null) return null;
        if (value instanceof Boolean)
            return omero.rtypes.rbool((Boolean) value);
        if (value instanceof String) {
            String v = (String) value;
            if (v.length() == 0) return null;
            return omero.rtypes.rstring(v);
        }
        if (value instanceof Long)
            return omero.rtypes.rlong((Long) value);
        if (value instanceof Integer)
            return omero.rtypes.rint((Integer) value);
        if (value instanceof Float)
            return omero.rtypes.rfloat((Float) value);
        if (value instanceof Double)
            return omero.rtypes.rdouble((Double) value);
        if (value instanceof omero.gateway.model.DataObject) {
            IObject o = ((omero.gateway.model.DataObject) value).asIObject();
            return omero.rtypes.robject(o);
        }
        return null;
    }

    /**
     * Converts the basic RType.
     * 
     * @param value The value to convert.
     * @return See above.
     */
    private static Object convertBasicRType(RType value)
    {
        if (value instanceof RBool) return ((RBool) value).getValue();
        if (value instanceof RString) return ((RString) value).getValue();
        if (value instanceof RLong) return ((RLong) value).getValue();
        if (value instanceof RInt) return ((RInt) value).getValue();
        if (value instanceof RFloat) return ((RFloat) value).getValue();
        if (value instanceof RDouble) return ((RDouble) value).getValue();
        if (value instanceof RObject) {
            IObject o = ((RObject) value).getValue();
            Object r = PojoMapper.asDataObject(o);
            if (r != null) return r;
            return o;
        }
        return null;
    }

    /**
     * Creates a new instance.
     * 
     * @param param The object to handle.
     */
    public ParamData(Param param)
    {
        this.param = param;
        initialize();
    }

    /**
     * Returns the grouping value if set.
     * 
     * @return See above.
     */
    public String getGrouping()
    {
        String grouping = "";
        if (param.grouping != null) grouping = param.grouping.trim();
        return grouping;
    }

    /**
     * Parses the grouping value to find if the parameter is a sub-parameter
     * e.g. grouping = 5.1, the returned value is 5.
     * 
     * @return See above.
     */
    public String getParent()
    {
        String v = getGrouping();
        if (v.length() == 0) return "";
        int index = v.lastIndexOf(".");
        if (index >= 0) return v.substring(0, index);
        return "";
    }

    /**
     * Returns <code>true</code> if the parameter is optional,
     * <code>false</code> otherwise.
     *  
     * @return See above.
     */
    public boolean isOptional() { return param.optional; }

    /**
     * Returns the description of the parameter.
     * 
     * @return See above.
     */
    public String getDescription() { return param.description; }

    /**
     * Returns the type of value expected.
     * 
     * @return See above.
     */
    public Class<?> getPrototype() { return type; }

    /** 
     * Returns the type of a key element if the prototype is a List or a Map.
     * 
     * @return See above.
     */
    public Class<?> getKeyType()
    {
        Object o;
        if (List.class.equals(type)) {
            List<RType> l = ((RList) param.prototype).getValue();
            if (l.size() > 0) {
                o = convertBasicRType(l.get(0));
                if (o instanceof Long || o instanceof Integer ||
                        o instanceof Double || o instanceof Float)
                    return o.getClass();
            }
            return String.class;
        } else if (Map.class.equals(type)) return String.class;
        return null;
    }

    /** 
     * Returns the type of a value element if the prototype is a List or a Map.
     * 
     * @return See above.
     */
    public Class<?> getValueType()
    {
        if (List.class.equals(type)) return getKeyType();
        else if (Map.class.equals(type)) {
            Map<String, RType> l = ((RMap) param.prototype).getValue();
            if (l.size() > 0) {
                Object o;
                Entry<String, RType> entry;
                Iterator<Entry<String, RType>> i = l.entrySet().iterator();
                o = null;
                while (i.hasNext()) {
                    entry = i.next();
                    o = convertBasicRType(entry.getValue());
                    if (o != null) {
                        break;
                    }
                }
                if (o instanceof Long || o instanceof Integer ||
                        o instanceof Double || o instanceof Float)
                    return o.getClass();
            }

            return String.class; //TODO
        }
        return null;
    }

    /**
     * Returns the list of possible values or <code>null</code> if none set.
     * 
     * @return See above.
     */
    public List<Object> getValues()
    {
        if (values != null) return values;
        RList list = param.values;
        if (list == null) return null;
        List<RType> l = list.getValue();
        if (CollectionUtils.isEmpty(l)) return null;
        values = new ArrayList<Object>();
        Iterator<RType> i = l.iterator();
        Object value;
        while (i.hasNext()) {
            value = convertRType(i.next());
            if (value != null) values.add(value);
        }
        return values;
    }

    /**
     * Returns the maximum value or <code>null</code> if none set.
     * 
     * @return See above.
     */
    public Number getMaxValue()
    {  
        if (maxValue == null) return null;
        return (Number) maxValue;
    }

    /**
     * Returns the minimum value or <code>null</code> if none set.
     * 
     * @return See above.
     */
    public Number getMinValue()
    {  
        if (minValue == null) return null;
        return (Number) minValue;
    }

    /**
     * Returns the default value or <code>null</code> if none set.
     * 
     * @return See above.
     */
    public Object getDefaultValue()
    {
        if (param.useDefault) return defaultValue;
        return null;
    }

    /**
     * Sets the value to pass while running the script.
     * 
     * @param valueToPass The value to pass.
     */
    public void setValueToPass(Object valueToPass)
    { 
        if (valueToPass == null) {
            valueToPass = getDefaultValue();
        }
        this.valueToPass = valueToPass;
    }
    /**
     * Returns the value to pass while running the script.
     * 
     * @return See above.
     */
    public RType getValueToPassAsRType()
    { 
        if (valueToPass instanceof Boolean || valueToPass instanceof String ||
            valueToPass instanceof Long || valueToPass instanceof Integer ||
            valueToPass instanceof Float || valueToPass instanceof Double)
            return convertBasicValue(valueToPass);
        if (valueToPass instanceof List) {
            List<RType> l = new ArrayList<RType>();
            List<?> list = (List<?>) valueToPass;
            Iterator<?> i = list.iterator();
            RType key;
            while (i.hasNext()) {
                key = convertBasicValue(i.next());
                if (key != null)
                    l.add(key);
            }
            if (l.size() == 0) return null;
            return omero.rtypes.rlist(l);
        }
        if (valueToPass instanceof Map) {
            Map<String, RType> m = new HashMap<String, RType>();
            Map<String, RType> map = (Map<String, RType>) valueToPass;
            Entry<String, RType> entry;
            RType type;
            Iterator<Entry<String, RType>> i = map.entrySet().iterator();
            while (i.hasNext()) {
                entry = i.next();
                type = convertBasicValue(entry.getValue());
                if (type != null) 
                    m.put(entry.getKey(), type);
            }
            if (m.size() == 0) return null;
            return omero.rtypes.rmap(m);
        }
        return null; 
    }

    /**
     * Converts the passed value.
     * 
     * @param value The value to handle.
     * @return The converted value.
     */
    public static Object convertRType(RType value)
    {
        if (value instanceof RBool || value instanceof RString ||
                value instanceof RLong || value instanceof RInt ||
                value instanceof RFloat || value instanceof RObject ||
                value instanceof RDouble)
            return convertBasicRType(value);
        if (value instanceof RList) {
            List<RType> list = ((RList) value).getValue();
            List<Object> l = new ArrayList<Object>();
            Iterator<RType> i = list.iterator();
            Object o;
            while (i.hasNext()) {
                o = convertRType(i.next());
                if (o != null) l.add(o);
            }
            return l;
        }
        if (value instanceof RMap) {
            Map<String, RType> map = ((RMap) value).getValue();
            Map<String, Object> r = new HashMap<String, Object>();
            Entry<String, RType> entry;
            Object v;
            Iterator<Entry<String, RType>> i = map.entrySet().iterator();

            while (i.hasNext()) {
                entry = i.next();
                v = convertRType(entry.getValue());
                if (v != null) {
                    r.put(entry.getKey(), v);
                }
            }
            return r;
        }
        return null;
    }

}
