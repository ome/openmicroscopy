/*
 * org.openmicroscopy.shoola.util.roi.figures.DefaultAttributeSet 
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
package org.openmicroscopy.shoola.util.ui.drawingtools.attributes;


//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.io.IOConstants;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FigureProperties
	extends AttributeKeys
{	
	
	/** Initial Attribute set in the figure properties.*/
	public enum AttributeSet
	{
		EMPTY,
		DEFAULT 
	}
	
	
	/** Map containing all the default property values of the figures. */
	private  static Map<AttributeKey, Object> defaultProperties;
	static {
		defaultProperties = new HashMap<AttributeKey, Object>();
		defaultProperties.put(DrawingAttributes.FILL_COLOR, 
			IOConstants.DEFAULT_FILL_COLOUR); 
		defaultProperties.put(DrawingAttributes.STROKE_COLOR, 
			IOConstants.DEFAULT_STROKE_COLOUR);
		defaultProperties.put(DrawingAttributes.STROKE_WIDTH,
			IOConstants.DEFAULT_STROKE_WIDTH);
		defaultProperties.put(DrawingAttributes.TEXT, "Text");
		defaultProperties.put(DrawingAttributes.TEXT_COLOR,
			IOConstants.DEFAULT_TEXT_COLOUR);
		defaultProperties.put(DrawingAttributes.FONT_SIZE,
			IOConstants.DEFAULT_FONT_SIZE);
		defaultProperties.put(DrawingAttributes.SHOWTEXT,
			Boolean.valueOf(true));
		//defaultProperties.put(MeasurementAttributes.SHOWMEASUREMENT.getKey(),
		//								MeasurementAttributes.SHOWMEASUREMENT);
		//defaultProperties.put(MeasurementAttributes.MEASUREMENTTEXT_COLOUR.getKey(),
		//						MeasurementAttributes.MEASUREMENTTEXT_COLOUR);
        }

	
	/** 
	 * The map containing the values for the attributes in the propertySet 
	 * map.
	 */
	protected Map<AttributeKey, Object> propertyMap;   
	
	/**
	 * Construct the Figure properties object with the default values. The 
	 * initialSet parameter determines what, if any properties should be 
	 * put into the map by default. 
	 * @param initialSet see above.
	 */
	public FigureProperties(AttributeSet initialSet)
	{
		propertyMap = new HashMap<AttributeKey, Object>();
		switch(initialSet)
		{
			case EMPTY:
				break;
			case DEFAULT:
					propertyMap.putAll(defaultProperties);
		}
	}

	/**
	 * Construct the Figure properties object with the default values. The 
	 * initialSet parameter determines what, if any properties should be 
	 * put into the map by default. 
	 * @param initialSet see above.
	 */
	public FigureProperties(Map<AttributeKey, Object> initialSet)
	{
		propertyMap = new HashMap<AttributeKey, Object>();
		addAttribute(initialSet);
	}
	
	/**
	 * Set the attributes of the figure to those contained in the maps.
	 * @param fig see above.
	 */
	public void setAttributes(Figure fig)
	{
		Iterator<AttributeKey> attributeIterator = 
											propertyMap.keySet().iterator();
		while(attributeIterator.hasNext())
		{
			AttributeKey key = attributeIterator.next();
			key.set(fig, propertyMap.get(key));
		}
	}
	

	/**
	 * Adds an attribute and value to the property map.
	 * 
	 * @param attributeSet see above. the map.
	 */
	public void addAttribute(Map<AttributeKey, Object> attributeSet) 
	{
		if (attributeSet == null) return;
		Iterator i = attributeSet.entrySet().iterator();
		Entry entry;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			addAttribute((AttributeKey) entry.getKey(), entry.getValue());
		}
	}		
	
	/**
	 * Adds an attribute and value to the property map.
	 * 
	 * @param key see above.
	 * @param defaultValue see above.
	 */
	public void addAttribute(AttributeKey key, Object defaultValue)
	{
		if (key == null) return;
		propertyMap.put(key, defaultValue);
	}
	
	/**
	 * Sets the value for the property key to the value.
	 * 
	 * @param key see above.
	 * @param value see above.
	 */
	public void setPropertyValue(AttributeKey key, Object value)
	{
		propertyMap.put(key, value);
	}
	
	/**
	 * Get the value for the key.
	 * @param key see above.
	 * @return see above.
	 */
	public Object getPropertyValue(AttributeKey key)
	{
		if (key == null) return null;
		return propertyMap.get(key);
	}
	
	/**
	 * Does the key exist in the properties map.
	 * 
	 * @param key The key to handle.
	 * @return see above.
	 */
	public boolean hasProperty(AttributeKey key)
	{
		if (key == null) return false;
		return propertyMap.containsKey(key);
	}
	
	/**
	 * Remove the key from the properties map.
	 * @param key see above.
	 */
	public void removeAttributes(AttributeKey key)
	{
		if (hasProperty(key)) propertyMap.remove(key);
	}
	
	/**
	 * Get the values for the attributes in the map.
	 * @return see above.
	 */
	public Map<AttributeKey, Object> getProperties()
	{
		return propertyMap;
	}
	
}
