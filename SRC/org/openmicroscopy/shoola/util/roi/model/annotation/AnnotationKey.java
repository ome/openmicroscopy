/*
 * org.openmicroscopy.shoola.util.roi.model.AnnotationKey 
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
package org.openmicroscopy.shoola.util.roi.model.annotation;

//Java imports
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.AttributeKey;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

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
 * @param <T> 
 * @since OME3.0
 */
public class AnnotationKey<T>
	extends AttributeKey<T>
{
	    /** Creates a new instance. 
	     * @param key the new key of the annotation. 
	     */
	    public AnnotationKey(String key) 
	    {
	        this(key, null, true);
	    }
	    
	    /** Creates a new instance. 
	     * @param key the new key of the annotation. 
	     * @param defaultValue the default value of the key.
	     */
	    public AnnotationKey(String key, T defaultValue) 
	    {
	        this(key, defaultValue, true);
	    }
	    
	    /** Creates a new instance. 
	     * @param key the new key of the annotation. 
		 * @param defaultValue the default value of the key.
		 * @param isNullValueAllowed is the annotation allowed to have null 
		 * 			values. 
	     */
	    public AnnotationKey(String key, T defaultValue, 
	    											boolean isNullValueAllowed) 
	    {
	    	super(key, defaultValue, isNullValueAllowed);
	    }

	    /**
	     * Get the value for the annotation on ROIShape shape.
	     * @param roiShape see above.
	     * @return see above.
	     */
	    public T get(ROIShape roiShape) 
	    {
	        T value = (T) roiShape.getAnnotation(this);
	        return (value == null && ! isNullValueAllowed()) ? 
	        		getDefaultValue() : value;
	    }
	    
	    /**
	     * Get the value for the annotation on ROI roi.
	     * @param roi see above.
	     * @return see above.
	     */
	    public T get(ROI roi) 
	    {
	        T value = (T) roi.getAnnotation(this);
	        return (value == null && ! isNullValueAllowed()) ? 
	        		getDefaultValue() : value;
	    }
	    
	    /**
	     * Get the list of attributes for object.
	     * @param a see above.
	     * @return see above.
	     */
	    public T get(Map<AttributeKey,Object> a) {
	        T value = (T) a.get(this);
	        return (value == null && ! isNullValueAllowed()) ? 
	        		getDefaultValue() : value;
	    }
	    
	    /**
	     * Set the roiShape to have annotation T.
	     * @param roiShape see above.
	     * @param value see above.
	     */
	    public void set(ROIShape roiShape, T value) 
	    {
	    	if (value == null && ! super.isNullValueAllowed()) 
	    	{
	    		throw new NullPointerException("Null value not allowed for " +
	    				"AttributeKey "+super.getKey());
	    	}
	    	roiShape.setAnnotation(this, value);
	    }
	    
	    /**
	     * Set the roi to have annotation T.
	     * @param roi see above.
	     * @param value see above.
	     */
	    public void set(ROI roi, T value) 
	    {
	    	if (value == null && ! isNullValueAllowed()) 
	    	{
	    		throw new NullPointerException("Null value not allowed for " +
	    				"AttributeKey "+super.getKey());
	    	}
	    	roi.setAnnotation(this, value);
	    }

		    
	    /**
	     * Set the roiShape to have annotation T.
	     * @param roiShape see above.
	     * @param value see above.
	     */
	    public void basicSet(ROIShape roiShape, T value) 
	    {
	        if (value == null && ! isNullValueAllowed()) 
	        {
	            throw new NullPointerException("Null value not allowed for " +
	            		"AttributeKey "+getKey());
	        }
	        roiShape.basicSetAnnotation(this, value);
	    }
	    
	    /**
	     * Set the roi to have annotation T.
	     * @param roi see above.
	     * @param value see above.
	     */
	    public void basicSet(ROI roi, T value) 
	    {
	        if (value == null && ! isNullValueAllowed()) 
	        {
	            throw new NullPointerException("Null value not allowed for " +
	            		"AttributeKey "+getKey());
	        }
	        roi.basicSetAnnotation(this, value);
	    }
	    
	    /**
	     * Overload the equals to test for annotation equality.
	     * @param o object to compare.
	     * @return true if o==this.
	     */
	    public boolean equals(Object o) 
	    {
	        if (o instanceof AnnotationKey) 
	        {
	        	AnnotationKey that = (AnnotationKey) o;
	            return that.getKey().equals(this.getKey());
	        }
	        return false;
	    }

}


