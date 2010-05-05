/*
 * org.openmicroscopy.shoola.env.data.model.ParamData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Freeze.Map;

//Third-party libraries

//Application-internal dependencies
import omero.RBool;
import omero.RFloat;
import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.grid.Param;

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
	private Class type;
	
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
		} else if (o instanceof List) {
			type = List.class;
		} else if (o instanceof Map) {
			type = Map.class;
		}
		Number n;
		Object value = convertRType(param.min);
		if (value instanceof Long || value instanceof Integer) {
			minValue = value;
			n = (Number) defaultValue;
			if (n.doubleValue() < ((Number) minValue).doubleValue())
				defaultValue = minValue;
		}
		value = convertRType(param.max);
		if (value instanceof Long || value instanceof Integer) {
			maxValue = value;
			n = (Number) defaultValue;
			if (n.doubleValue() > ((Number) maxValue).doubleValue())
				defaultValue = maxValue;
		}
			
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
	public Class getPrototype() { return type; }
	
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
		if (l == null || l.size() == 0) return null;
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
	 * Returns <code>true</code> if the minimum or maximum value is
	 * set.
	 * 
	 * @return See above.
	 */
	public boolean hasRangeSpecified()
	{
		return (minValue != null || maxValue != null);
	}
	
	/**
	 * Returns the default value or <code>null</code> if none set.
	 * 
	 * @return See above.
	 */
	public Object getDefaultValue()
	{
		return defaultValue;
	}
	
	/**
	 * Sets the value to pass while running the script.
	 * 
	 * @param valueToPass The value to pass.
	 */
	public void setValueToPass(Object valueToPass)
	{ 
		this.valueToPass = valueToPass;
	}
	
	/**
	 * Returns the value to pass while running the script.
	 * 
	 * @return See above.
	 */
	public RType getValueToPassAsRType()
	{ 
		if (valueToPass instanceof Boolean)
			return omero.rtypes.rbool((Boolean) valueToPass);
		if (valueToPass instanceof String)
			return omero.rtypes.rstring((String) valueToPass);
		if (valueToPass instanceof Long)
			return omero.rtypes.rlong((Long) valueToPass);
		if (valueToPass instanceof Integer)
			return omero.rtypes.rint((Integer) valueToPass);
		if (valueToPass instanceof Float)
			return omero.rtypes.rfloat((Float) valueToPass);
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
		if (value instanceof RBool) return ((RBool) value).getValue();
		if (value instanceof RString) return ((RString) value).getValue();
		if (value instanceof RLong)  return ((RLong) value).getValue();
		if (value instanceof RInt)  return ((RInt) value).getValue();
		if (value instanceof RFloat)  return ((RFloat) value).getValue();
		if (value instanceof RList)  return ((RList) value).getValue();
		return null;
	}
	
}
