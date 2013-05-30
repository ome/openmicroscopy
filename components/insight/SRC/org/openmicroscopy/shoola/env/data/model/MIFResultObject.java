/*
 * org.openmicroscopy.shoola.env.data.model.MIFResultObject
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.SecurityContext;

import pojos.ImageData;

/**
 * Hosts result of MIF delete/chgrp check.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class MIFResultObject
{

	/** The maximum number of thumbnails for each list.*/
	private static final int MAX = 4;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/** The result of the call.*/
	private Map<Long, Map<Boolean, List<ImageData>>> result;

	/** 
	 * The thumbnails corresponding to the images. The thumbnail will
	 * not be loaded for each image. Only the first {@link #MAX} for each list.
	 */
	private List<ThumbnailData> thumbnails;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param result The result of the call.
	 */
	public MIFResultObject(SecurityContext ctx,
			Map<Long, Map<Boolean, List<ImageData>>> result)
	{
		this.ctx = ctx;
		this.result = result;
	}
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getContext() { return ctx; }
	
	/**
	 * Returns the result of the call.
	 * 
	 * @return See above.
	 */
	public Map<Long, Map<Boolean, List<ImageData>>> getResult()
	{
		return result;
	}
	
	/** 
	 * Returns the images to retrieve thumbnails for.
	 *
	 * @return See above.
	 */
	public List<ImageData> getImages()
	{
		List<ImageData> ids = new ArrayList<ImageData>();
		Entry<Long, Map<Boolean, List<ImageData>>> e;
		Iterator<Entry<Long, Map<Boolean, List<ImageData>>>> i =
				result.entrySet().iterator();
		List<ImageData> l;
		Entry<Boolean, List<ImageData>> entry;
		Iterator<Entry<Boolean, List<ImageData>>> j;
		while (i.hasNext()) {
			e = i.next();
			j = e.getValue().entrySet().iterator();
			while (j.hasNext()) {
				entry = j.next();
				l = entry.getValue();
				if (l.size() > MAX)
					ids.addAll(l.subList(0, MAX-1));
				else ids.addAll(l);
			}
		}
		return ids;
	}

	/**
	 * Sets the thumbnails corresponding to the list returned by 
	 * {@link #get}
	 * @param thumbnails
	 */
	public void setThumbnails(List<ThumbnailData> thumbnails)
	{
		this.thumbnails = thumbnails;
	}
}
