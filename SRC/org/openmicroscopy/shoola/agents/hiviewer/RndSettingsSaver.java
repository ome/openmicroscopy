/*
 * org.openmicroscopy.shoola.agents.hiviewer.RndSettingsSaver 
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
package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.CategoryData;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Pastes the rendering settings associated to the passed set of pixels
 * across a collection of images.
 * This class calls the <code>pasteRndSettings</code> method in the
 * <code>DataManagerView</code>.
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
public class RndSettingsSaver
	extends DataLoader
{
	
	/** The id of the pixels set of reference. */
	private long 		pixelsID;
	
	/** 
	 * One of the following supported types:
	 * <code>ImageData</code>, <code>DatasetData</code> or
	 * <code>CategoryData</code>.
	 */
	private Class		rootType;
	
	/** Collection of data objects id. */
	private Set<Long>	ids;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
	/** 
	 * Controls if the passed type is supported.
	 * 
	 * @param type The type to check;
	 */
	private void checkRootType(Class type)
	{
		if (ImageData.class.equals(type) || DatasetData.class.equals(type) ||
				CategoryData.class.equals(type))
			return;
		throw new IllegalArgumentException("Type not supported.");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The HiViewer this data loader is for.
     *               	Mustn't be <code>null</code>.
	 * @param rootType	The type of nodes. Supported type 
	 * 					<code>ImageData</code>, <code>DatasetData</code> or
	 * 					<code>CategoryData</code>.
	 * @param ids		Collection of nodes ids. If the rootType equals 
	 * 					<code>DatasetData</code> or
	 * 					<code>CategoryData</code>, the settings will be applied
	 * 					to the images contained in the specified containers.
	 * @param pixelsID	The id of the pixels of reference.
	 */
	public RndSettingsSaver(HiViewer viewer, Class rootType, Set<Long> ids, 
							long pixelsID)
	{
		super(viewer);
		checkRootType(rootType);
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		this.rootType = rootType;
		this.pixelsID = pixelsID;
		this.ids = ids;
	}
	
	/** 
     * Cancels the data loading.  
     * @see DataLoader#cancel()
     */
	public void cancel() { handle.cancel(); }

	/** 
     * Pastes the rendering settings.
     * @see DataLoader#load()
     */
	public void load()
	{
		handle = dhView.pasteRndSettings(pixelsID, rootType, ids, this);
	}

	/** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        viewer.rndSettingsPasted((Map) result);
    }
    
}
