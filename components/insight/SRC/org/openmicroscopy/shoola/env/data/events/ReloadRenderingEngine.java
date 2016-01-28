/*
 * org.openmicroscopy.shoola.env.data.events.ReloadRenderingEngine 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.data.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event posted when some rendering engines could not be reloaded when
 * reconnected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ReloadRenderingEngine 
	extends RequestEvent
{

	/** The pixels for which the rendering engine could not be reloaded.*/
	private Map<SecurityContext, List<Long>> pixels;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixels The pixels to handle.
	 */
	public ReloadRenderingEngine(Map<SecurityContext, List<Long>> pixels)
	{
		this.pixels = pixels;
	}
	
	/**
	 * Returns the collection of pixels.
	 * 
	 * @return See above.
	 */
	public Map<SecurityContext, List<Long>> getPixels() { return pixels; }
	
	/**
	 * Returns the re-activated images.
	 * 
	 * @return See above.
	 */
	public List<Long> getPixelsID()
	{
		if (pixels == null) return null;
		Iterator<List<Long>> i = pixels.values().iterator();
		List<Long> list = new ArrayList<Long>();
		while (i.hasNext()) {
			list.addAll(i.next());
		}
		return list;
	}

}