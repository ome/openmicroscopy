/*
 * org.openmicroscopy.shoola.util.ui.roi.model.AnnotationKey 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.roi.model;

//Java imports
import java.lang.reflect.TypeVariable;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

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
public class AnnotationKey<T>
{
		private String key;
	    private T defaultValue;
	    private boolean isNullValueAllowed;
	    
	    /** Creates a new instance. */
	    public AnnotationKey(String key) 
	    {
	        this(key, null, true);
	    }
	    
	    public AnnotationKey(String key, T defaultValue) 
	    {
	        this(key, defaultValue, true);
	    }
	    
	    public AnnotationKey(String key, T defaultValue, boolean isNullValueAllowed) 
	    {
	        this.key = key;
	        this.defaultValue = defaultValue;
	        this.isNullValueAllowed = isNullValueAllowed;
	    }
	    
	    public String getKey() 
	    {
	        return key;
	    }
	    
	    public T getDefaultValue() 
	    {
	        return defaultValue;
	    }
	    
	    public T get(ROIShape shape) 
	    {
	        T value = (T) shape.getAnnotation(this);
	        return (value == null && ! isNullValueAllowed) ? defaultValue : value;
	    }
	    
	    public T get(ROI roi) 
	    {
	        T value = (T) roi.getAnnotation(this);
	        return (value == null && ! isNullValueAllowed) ? defaultValue : value;
	    }
	    
	    public T get(Map<AnnotationKey,Object> a) 
	    {
	        T value = (T) a.get(this);
	        return (value == null && ! isNullValueAllowed) ? defaultValue : value;
	    }
	    	    
	    public void set(ROIShape shape, T value) 
	    {
	        if (value == null && ! isNullValueAllowed) 
	        {
	            throw new NullPointerException("Null value not allowed for AnnotationKey "+key);
	        }
	        shape.setAnnotation(this, value);
	    }
	    
	    public void set(ROI roi, T value) 
	    {
	        if (value == null && ! isNullValueAllowed) 
	        {
	            throw new NullPointerException("Null value not allowed for AnnotationKey "+key);
	        }
	        roi.setAnnotation(this, value);
	    }
	    
	    public void set(Map<AnnotationKey, Object> a, T value) 
	    {
	        if (value == null && ! isNullValueAllowed) 
	        {
	            throw new NullPointerException("Null value not allowed for AnnotationKey "+key);
	        }
	        a.put(this, value);
	    }
	    
	    public void basicSet(ROIShape shape, T value) 
	    {
	        if (value == null && ! isNullValueAllowed) 
	        {
	            throw new NullPointerException("Null value not allowed for AnnotationKey "+key);
	        }
	        shape.basicSetAnnotation(this, value);
	    }
	    
	    public void basicSet(ROI roi, T value) 
	    {
	        if (value == null && ! isNullValueAllowed) 
	        {
	            throw new NullPointerException("Null value not allowed for AnnotationKey "+key);
	        }
	        roi.basicSetAnnotation(this, value);
	    }

	    public boolean equals(Object o) 
	    {
	        if (o instanceof AnnotationKey) 
	        {
	        	AnnotationKey that = (AnnotationKey) o;
	            return that.key.equals(this.key);
	        }
	        return false;
	    }
	    
	    public int hashCode() 
	    {
	        return key.hashCode();
	    }
	    
	    public String toString() 
	    {
	        return key;
	    }
	    
	    public boolean isNullValueAllowed() 
	    {
	        return isNullValueAllowed;
	    }
	    
	    public static void main(String[] args) 
	    {
	        TypeVariable v = new AnnotationKey<Double>("hey").getClass().getTypeParameters()[0];
	    }
	    
	    public boolean isAssignable(Object value) 
	    {
	        if (value == null) 
	        {
	            return isNullValueAllowed();
	        }
	        
	        // XXX - This works, but maybe there is an easier way to do this?
	        try {
	            T a = (T) value;
	            return true;
	        }
	        catch (ClassCastException e) 
	        {
	            return false;
	        }
	    }
}


