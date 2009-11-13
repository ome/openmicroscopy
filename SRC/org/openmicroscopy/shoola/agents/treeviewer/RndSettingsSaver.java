/*
 * org.openmicroscopy.shoola.agents.treeviewer.RndSettingsSaver 
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
package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

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
	extends DataTreeViewerLoader
{
	
	/** Indicates to paste the rendering settings. */
	public static final int PASTE = 0;
	
	/** Indicates to reset the rendering settings. */
	public static final int RESET = 1;
	
	/** Indicates to set the rendering settings. */
	public static final int SET = 2;
	
	/** The id of the pixels set of reference. */
	private long 			pixelsID;

	/** 
	 * One of the following supported types:
	 * <code>ImageData</code>, <code>DatasetData</code> or
	 * <code>CategoryData</code>.
	 */
	private Class			rootType;

	/** Collection of data objects id. */
	private List<Long>		ids;

	/** Time reference object. */
	private TimeRefObject 	ref;

	/** Handle to the asynchronous call so that we can cancel it. */
	private CallHandle  	handle;

	/** One of the constants defined by this class. */
    private int				index;
   
    /**
     * Controls if the passed index is supported.
     * 
     * @param index The value to handle.
     */
    private void checkIndex(int index)
    {
    	switch (index) {
			case PASTE:
			case RESET:
			case SET:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
	/** 
	 * Controls if the passed type is supported.
	 * 
	 * @param type The type to check;
	 */
	private void checkRootType(Class type)
	{
		if (ImageData.class.equals(type) || DatasetData.class.equals(type) ||
			PlateData.class.equals(type) || ProjectData.class.equals(type) ||
			ScreenData.class.equals(type))
			return;
		throw new IllegalArgumentException("Type not supported.");
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The TreeViewer this data loader is for.
	 *               	Mustn't be <code>null</code>.
	 * @param rootType	The type of nodes. Supported type 
	 * 					<code>ImageData</code>, <code>DatasetData</code> or
	 * 					<code>CategoryData</code>.
	 * @param ids		Collection of nodes ids. If the rootType equals 
	 * 					<code>DatasetData</code> or
	 * 					<code>CategoryData</code>, the settings will be applied
	 * 					to the images contained in the specified containers.
	 * @param index 	One of the constants defined by this class.
	 */
	public RndSettingsSaver(TreeViewer viewer, Class rootType, List<Long> ids,
							int index)
	{
		super(viewer);
		checkRootType(rootType);
		checkIndex(index);
		this.index = index;
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		this.rootType = rootType;
		this.ids = ids;
		ref = null;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The TreeViewer this data loader is for.
	 *               	Mustn't be <code>null</code>.
	 * @param ref		The time reference object.
	 * @param index 	One of the constants defined by this class.
	 */
	public RndSettingsSaver(TreeViewer viewer, TimeRefObject ref, int index)
	{
		super(viewer);
		checkIndex(index);
		this.index = index;
		if (ref == null)
			throw new IllegalArgumentException("Period not valid.");
		this.ref = ref;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The TreeViewer this data loader is for.
	 *               	Mustn't be <code>null</code>.
	 * @param rootType	The type of nodes. Supported type 
	 * 					<code>ImageData</code>, <code>DatasetData</code>, 
	 * 					<code>ProjectData</code>, <code>ScreenData</code>,
	 * 					<code>PlateData</code>.
	 * @param ids		Collection of nodes ids. If the rootType equals 
	 * 					<code>DatasetData</code>, the settings will be applied
	 * 					to the images contained in the specified containers.
	 * @param pixelsID	The id of the pixels of reference.
	 */
	public RndSettingsSaver(TreeViewer viewer, Class rootType, List<Long> ids, 
							long pixelsID)
	{
		super(viewer);
		checkRootType(rootType);
		this.index = PASTE;
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		this.rootType = rootType;
		this.pixelsID = pixelsID;
		this.ids = ids;
		ref = null;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The TreeViewer this data loader is for.
	 *               	Mustn't be <code>null</code>.
	 * @param ref		The time reference object.
	 * @param pixelsID	The id of the pixels of reference.
	 */
	public RndSettingsSaver(TreeViewer viewer, TimeRefObject ref, long pixelsID)
	{
		super(viewer);
		this.index = PASTE;
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		if (ref == null)
			throw new IllegalArgumentException("Period not valid.");
		this.pixelsID = pixelsID;
		this.ref = ref;
	}
	
	/** 
	 *  Cancels the data loading. 
	 * @see DataTreeViewerLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Pastes the rendering settings.
	 * @see DataTreeViewerLoader#load()
	 */
	public void load()
	{
		switch (index) {
			case PASTE:
				if (ref == null)
					handle = dhView.pasteRndSettings(pixelsID, rootType, ids, 
													this);
				else 
					handle = dhView.pasteRndSettings(pixelsID, ref, this);
				break;
			case RESET:
				if (ref == null)
					handle = dhView.resetRndSettings(rootType, ids, this);
				else 
					handle = dhView.resetRndSettings(ref, this);
				break;
			case SET:
				if (ref == null)
					handle = dhView.setRndSettings(rootType, ids, this);
				else 
					handle = dhView.setRndSettings(ref, this);
		}
	}

	/** 
	 * Feeds the result back to the viewer. 
	 * @see DataTreeViewerLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
		if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
		viewer.rndSettingsPasted((Map) result);
	}

}
