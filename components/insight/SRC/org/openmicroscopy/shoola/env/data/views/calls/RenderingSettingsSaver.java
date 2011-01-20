/*
 * org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver 
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
* Command to paste the rendering settings.
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
public class RenderingSettingsSaver 
	extends BatchCallTree
{

	/** Indicates to paste the rendering settings. */
	public static final int PASTE = 0;
	
	/** Indicates to reset the rendering settings. */
	public static final int RESET = 1;
	
	/** Indicates to reset the rendering settings. */
	public static final int SET_ORIGINAL = 2;
	
	/** Indicates to create rendering settings. */
	public static final int CREATE = 3;
	
	/** Result of the call. */
	private Object    	result;

	/** Loads the specified tree. */
	private BatchCall	loadCall;

	/** 
	 * Controls if the passed type is supported.
	 * 
	 * @param type The type to check;
	 */
	private void checkRootType(Class type)
	{
		if (ImageData.class.equals(type) || DatasetData.class.equals(type) ||
				PlateData.class.equals(type) || ProjectData.class.equals(type)
				|| ScreenData.class.equals(type))
			return;
		throw new IllegalArgumentException("Type not supported.");
	}

	/**
	 * Creates a {@link BatchCall} to paste the rendering settings.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param rootType		The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>, 
	 * 						<code>ProjectData</code>, <code>ScreenData</code>,
	 * 						<code>PlateData</code>.
	 * @param ids			The id of the nodes to apply settings to. 
	 * @param index 		One of the constants defined by this class.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final long pixelsID, final Class rootType,
			final List<Long> ids, final int index)
	{
		return new BatchCall("Modify the rendering settings: ") {
			public void doCall() throws Exception
			{
				OmeroImageService rds = context.getImageService();
				switch (index) {
					case PASTE:
						result = rds.pasteRenderingSettings(pixelsID, rootType, 
															ids);
						break;
					case RESET:
						result = rds.resetRenderingSettings(rootType, ids);
						break;
					case SET_ORIGINAL:
						result = rds.setOriginalRenderingSettings(rootType, ids);
				}
				
			}
		};
	} 

	/**
	 * Creates a {@link BatchCall} to paste the rendering settings.
	 * 
	 * @param pixelsID	The id of the pixels set of reference.
	 * @param ref		The time reference object.
	 * @param index 	One of the constants defined by this class.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final long pixelsID,
			final TimeRefObject ref, final int index)
	{
		return new BatchCall("Modify the rendering settings: ") {
			public void doCall() throws Exception
			{
				long userID = ((ExperimenterData) context.lookup(
						LookupNames.CURRENT_USER_DETAILS)).getId();
				OmeroDataService os = context.getDataService();
				Collection l = os.getImagesPeriod(ref.getStartTime(), 
												ref.getEndTime(), userID, false); 
				if (l != null) {
					Iterator i = l.iterator();
					IObject element;
					List<Long> ids = new ArrayList<Long>(l.size());
					while (i.hasNext()) {
						element = (IObject) i.next();
						ids.add(element.getId());
					}
					OmeroImageService rds = context.getImageService();
					switch (index) {
						case PASTE:
							result = rds.pasteRenderingSettings(pixelsID, 
													ImageData.class, ids);
							break;
						case RESET:
							result = rds.resetRenderingSettings(ImageData.class, 
																ids);
							break;
						case SET_ORIGINAL:
							result = rds.setOriginalRenderingSettings(
												ImageData.class, ids);
					}
				}

			}
		};
	} 

	/**
	 *  Creates a {@link BatchCall} to create the rendering settings.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @param rndToCopy The rendering setting to copy.
	 * @param indexes	Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
     * @return The {@link BatchCall}.
	 */
	private BatchCall makeCreateBatchCall(final long pixelsID, 
										final RndProxyDef rndToCopy,
										final List<Integer> indexes)
	{
		return new BatchCall("Paste the rendering settings: ") {
			public void doCall() throws Exception
			{
				OmeroImageService os = context.getImageService();
				result = os.createRenderingSettings(pixelsID, rndToCopy, 
						indexes);

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
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>, 
	 * 						<code>ProjectData</code>, <code>ScreenData</code>,
	 * 						<code>PlateData</code>.
	 * @param ids			The nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 * @param index			One of the constants defined by this class.
	 */
	public RenderingSettingsSaver(Class rootNodeType, List<Long> ids, int index)
	{
		checkRootType(rootNodeType);
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		loadCall = makeBatchCall(-1, rootNodeType, ids, index);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID		The id of the pixels set of reference.
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>, 
	 * 						<code>ProjectData</code>, <code>ScreenData</code>,
	 * 						<code>PlateData</code>.
	 * @param ids			The nodes to apply settings to. 
	 * 						Mustn't be <code>null</code>.
	 */
	public RenderingSettingsSaver(long pixelsID, Class rootNodeType, 
			List<Long> ids)
	{
		checkRootType(rootNodeType);
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		loadCall = makeBatchCall(pixelsID, rootNodeType, ids, PASTE);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID	The id of the pixels set of reference.
	 * @param ref		The time reference object.
	 * @param index 	One of the constants defined by this class.
	 */
	public RenderingSettingsSaver(long pixelsID, TimeRefObject ref)
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		if (ref == null)
			throw new IllegalArgumentException("Period not valid.");
		loadCall = makeBatchCall(pixelsID, ref, PASTE);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ref		The time reference object.
	 * @param index 	One of the constants defined by this class.
	 */
	public RenderingSettingsSaver(TimeRefObject ref, int index)
	{
		if (ref == null)
			throw new IllegalArgumentException("Period not valid.");
		loadCall = makeBatchCall(-1, ref, index);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @param rndToCopy The rendering setting to copy.
	 * @param indexes	Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
	 */
	public RenderingSettingsSaver(long pixelsID, RndProxyDef rndToCopy,
								List<Integer> indexes)
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		loadCall = makeCreateBatchCall(pixelsID, rndToCopy, indexes);
	}
	
}
