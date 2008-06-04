/*
 * pojos.BooleanAnnotationData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package pojos;


//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.annotations.BooleanAnnotation;

/** 
 * Boolean annotation.
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
public class BooleanAnnotationData 	
	extends AnnotationData
{
	
	/** Creates a new instance. */
	public BooleanAnnotationData()
	{
		super(BooleanAnnotation.class);
		setContent(null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param value The value to set.
	 */
	public BooleanAnnotationData(boolean value)
	{
		super(BooleanAnnotation.class);
		setContent(value);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param annotation 	The {@link BooleanAnnotation} object corresponding 
	 * 						to this <code>DataObject</code>. 
     *            			Mustn't be <code>null</code>.
	 */
	public BooleanAnnotationData(BooleanAnnotation annotation)
	{
		super(annotation);
	}
	
	/**
	 * Returns the text.
	 * 
	 * @param value The value to set.
	 */
	public void setValue(boolean value) { setContent(value); }
	
	/**
	 * Returns the text of this annotation.
	 * 
	 * @return See above.
	 */
	public boolean getValue() { return (Boolean) getContent(); }
	
	/**
	 * Returns the textual content of the annotation.
	 * @see AnnotationData#getContent()
	 */
	public Object getContent()
	{
		return ((BooleanAnnotation) asAnnotation()).getBoolValue();
	}

	/**
	 * Returns the textual content of the annotation.
	 * @see AnnotationData#getContentAsString()
	 */
	public String getContentAsString() { return (String) getContent(); }

	/**
	 * Sets the text annotation.
	 * @see AnnotationData#setContent(Object)
	 */
	public void setContent(Object content)
	{
		if (content == null)
			throw new IllegalArgumentException("Annotation value cannot " +
												"be null.");
		
		if (!(content instanceof Boolean))
			throw new IllegalArgumentException("Object must be of type " +
										"Boolean");
		((BooleanAnnotation) asAnnotation()).setBoolValue((Boolean) content);
	}
	
}
