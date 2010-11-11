/*
 * org.openmicroscopy.shoola.agents.measurement.util.AttributeField 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.model;


//Java imports
import java.util.List;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;

//Application-internal dependencies

/** 
 * Helper class used to store name, editable flag and AttributeKey.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AttributeField
{

	/** The key hosted by this class. */
	private  AttributeKey 	key;
	
	/** The name of the field. */
	private String 			name;
	
	/** Flag indicating if the field is editable or not. */
	private boolean 		editable;
	
	/** Value range of objects */
	private List		valueRange;
	
	/** The type of value stored in the value range. */
	private ValueType		valueType;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param key	The key hosted by this class.
	 * @param name	The name of the field.
	 * @param editable	Pass <code>true</code> to edit the field, 
	 * 					<code>false</code> otherwise.
	 * @param range The range of values this attribute can take.
	 * @param type 		 the Type of value available to object, enum, or range
	 */
	public AttributeField(AttributeKey key, String name, boolean editable, 
			List range, ValueType type)
	{
		this.key = key;
		this.name = name;
		this.editable = editable;
		valueRange = range;
		valueType = type;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param key	The key hosted by this class.
	 * @param name	The name of the field.
	 * @param editable	Pass <code>true</code> to edit the field, 
	 * 					<code>false</code> otherwise.
	 * @param type The type of the value available.
	 */
	public AttributeField(AttributeKey key, String name, boolean editable, 
			ValueType type)
	{
		this(key, name, editable, null, type);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param key	The key hosted by this class.
	 * @param name	The name of the field.
	 * @param editable	Pass <code>true</code> to edit the field, 
	 * 					<code>false</code> otherwise.
	 */
	public AttributeField(AttributeKey key, String name, boolean editable)
	{
		this(key, name, editable, null,  ValueType.DEFAULT);
	}

	/**
	 * Indicates if the field can be edited or not.
	 * 
	 * @param editable Pass <code>true</code> to edit the field, 
	 * 				   <code>false</code> otherwise.
	 */
	public void setEditable(boolean editable) { this.editable = editable; }
	
	/** 
	 * Gets the value range the object can take.
	 * 
	 * @return value range.
	 */
	public List getValueRange() { return valueRange; }
	
	/** 
	 * Gets the value type the object can take.
	 * 
	 * @return value type.
	 */
	public ValueType getValueType()
	{
		return valueType;
	}
	
	/**
	 * Returns the name of the field.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns <code>true</code> if the field is editable, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isEditable() { return editable; }
	
	/**
	 * Returns the <code>key</code> hosted by this class.
	 * 
	 * @return See above.
	 */
	public AttributeKey getKey() { return key; }
	
}
