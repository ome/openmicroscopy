/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.util;

import java.util.List;

import omero.gateway.model.AnnotationData;

/** 
 * Holds the annotation to remove. 
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DataToSave
{
	
	/** The list of annotation to add.*/
	private List<AnnotationData> toAdd;

	/** The object to remove, it can be link or annotation.*/
	private List<Object> toRemove;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param toAdd The annotations to add.
	 * @param toRemove The annotations or links to remove.
	 */
	public DataToSave(List<AnnotationData> toAdd, List<Object> toRemove)
	{
		this.toAdd = toAdd;
		this.toRemove = toRemove;
	}
	
	/**
	 * Returns the collection of annotations/links to remove,
	 * or <code>null</code> if any.
	 * 
	 * @return See above.
	 */
	public List<Object> getToRemove() { return toRemove; }
	
	/**
	 * Returns the collection of annotations to save, or <code>null</code>
	 * if any.
	 * 
	 * @return See above.
	 */
	public List<AnnotationData> getToAdd() { return toAdd; }

}