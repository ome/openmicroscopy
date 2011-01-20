/*
 * org.openmicroscopy.shoola.agents.measurement.util.AnnotationField 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;

/** 
 * Helper class used to store various information about the annotation key.
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
public class AnnotationField
{
	
	/** The key hosted by this class. */
	private AnnotationKey 	key;
	
	/** The name of the field. */
	private String 			name;
	
	/** Flag indicating if the field is editable or not. */
	private boolean 		editable;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param key	The key hosted by this class.
	 * @param name	The name of the field.
	 * @param editable	Pass <code>true</code> to edit the field, 
	 * 					<code>false</code> otherwise.
	 */
	public AnnotationField(AnnotationKey key, String name, boolean editable)
	{
		this.key = key;
		this.name = name;
		this.editable = editable;
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
	public AnnotationKey getKey() { return key; }
	
}
