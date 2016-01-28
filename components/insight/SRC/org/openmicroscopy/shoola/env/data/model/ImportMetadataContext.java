/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.model;

import java.util.Collection;

import omero.gateway.model.TagAnnotationData;

/** 
 * The metadata to import alongside the object.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImportMetadataContext 
{

	/** The collection of tags. */
	private Collection<TagAnnotationData> tags;
	
	/** The array containing pixels size.*/
	private double[]	pixelsSize;
	
	/**
	 * Returns the collections of tags if any.
	 * 
	 * @return See above.
	 */
	public Collection<TagAnnotationData> getTags() { return tags; }
	
	/**
	 * Returns an array of dimension 3, with the value of the size
	 * along the X-axis, Y-axis, and Z-axis.
	 * 
	 * @return See above.
	 */
	public double[] getPixelsSize() { return pixelsSize; }
	
	/** 
	 * Sets the collection of tags.
	 * 
	 * @param tags The value to set.
	 */
	public void setTags(Collection<TagAnnotationData> tags)
	{ 
		this.tags = tags; 
	}
	
	/**
	 * Sets the pixels size.
	 * 
	 * @param pixelsSize The value to set.
	 */
	public void setPixelsSize(double[] pixelsSize)
	{ 
		this.pixelsSize = pixelsSize; 
	}
	
}
