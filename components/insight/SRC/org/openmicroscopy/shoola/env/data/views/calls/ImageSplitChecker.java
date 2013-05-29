/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImageSplitChecker
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

import pojos.DataObject;

/**
 * Checks if the images in the specified containers are split between
 * or not all selected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ImageSplitChecker
	extends BatchCallTree
{
	/** Result of the call. */
	private Object result;

	/** Loads the specified tree. */
	private BatchCall loadCall;
	
	/**
	 * Creates a {@link BatchCall} to retrieve rendering control.
	 * 
	 * @param ctx The security context.
	 * @param rootType The top-most type which will be searched.
	 * @param rootIDs A set of the IDs of objects.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final
			Map<SecurityContext, List<DataObject>> objects)
	{
		return new BatchCall("Checking for split MIF ") {
			public void doCall() throws Exception
			{
				OmeroDataService svc = context.getDataService();
				Entry<SecurityContext, List<DataObject>> e;
				Iterator<Entry<SecurityContext, List<DataObject>>> i =
						objects.entrySet().iterator();
				Map<Long, Map<Boolean, List<Long>>> result =
						new HashMap<Long, Map<Boolean, List<Long>>>();
				Iterator<DataObject> j;
				List<Long> ids;
				DataObject uo;
				
				while (i.hasNext()) {
					e = i.next();
					j = e.getValue().iterator();
					ids = new ArrayList<Long>();
					Class<?> klass = null;
					while (j.hasNext()) {
						uo = j.next();
						klass = uo.getClass();
						ids.add(uo.getId());
					}
					result.putAll(svc.getImagesBySplitFilesets(e.getKey(),
							klass, ids));
				}
			}
		};
	} 
	
	/**
	 * Adds the {@link #loadCall} to the computation tree.
	 * 
	 * @see BatchCallTree#buildTree()
	 */
	protected void buildTree() { add(loadCall); }

	/**
	 * Returns the {@link RenderingControl}.
	 * 
	 * @see BatchCallTree#getResult()
	 */
	protected Object getResult() { return result; }

	/**
	 * Creates a new instance.
	 * If bad arguments are passed, we throw a runtime exception so to fail
	 * early and in the caller's thread.
	 * 
	 * @param objects The object to handle.
	 */
	public ImageSplitChecker(Map<SecurityContext, List<DataObject>> objects)
	{
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No object to check.");
		loadCall = makeBatchCall(objects);
	}
}
