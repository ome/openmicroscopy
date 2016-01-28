/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;

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
 * @since OME3.0
 */
public class RndSettingsSaver 
	extends DataTreeViewerLoader
{
	
	/** Indicates to paste the rendering settings. */
	public static final int PASTE = 0;
	
	/** Indicates to reset the rendering settings. */
	public static final int RESET = 1;
	
	/** Indicates to set the min/max for each channel. */
	public static final int SET_MIN_MAX = 2;
	
	/** Indicates to set the rendering settings used by the owner. */
	public static final int SET_OWNER = 3;

	/** 
	 * One of the following supported types:
	 * <code>ImageData</code>, <code>DatasetData</code>, 
	 * <code>ProjectData</code>, <code>PlateData</code> 
	 * or <code>ScreenData</code>.
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
    
   	/** 'Pending' rendering settings to paste */
   	 private RndProxyDef defToPaste;
    
        /**
         * Image to which the rendering settings belong, respectively to copy the
         * renderings settings from
         */
   	 private ImageData refImage;
    
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
			case SET_MIN_MAX:
			case SET_OWNER:
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
			ScreenData.class.equals(type) || 
			PlateAcquisitionData.class.equals(type))
			return;
		throw new IllegalArgumentException("Type not supported.");
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The TreeViewer this data loader is for.
	 *               	Mustn't be <code>null</code>.
	 * @param ctx The security context.
	 * @param rootType	The type of nodes. Supported type 
	 * 					<code>ImageData</code>, <code>DatasetData</code>, 
	 * 					<code>ProjectData</code>, <code>PlateData</code> 
	 * 					or <code>ScreenData</code>.
	 * @param ids		Collection of nodes identifiers. If the rootType equals 
	 * 					<code>DatasetData</code>, 
	 * 					<code>ProjectData</code>, <code>PlateData</code> 
	 * 					or <code>ScreenData</code>, the settings will be applied
	 * 					to the images contained in the specified containers.
	 * @param index 	One of the constants defined by this class.
	 */
	public RndSettingsSaver(TreeViewer viewer, SecurityContext ctx, 
			Class rootType, List<Long> ids, int index)
	{
		super(viewer, ctx);
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
	 * @param ctx The security context.
	 * @param ref		The time reference object.
	 * @param index 	One of the constants defined by this class.
	 */
	public RndSettingsSaver(TreeViewer viewer, SecurityContext ctx,
			TimeRefObject ref, int index)
	{
		super(viewer, ctx);
		checkIndex(index);
		this.index = index;
		if (ref == null)
			throw new IllegalArgumentException("Period not valid.");
		this.ref = ref;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The TreeViewer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param ctx The security context.
	 * @param rootType The type of nodes.
	 * @param ids The identifiers of the nodes.
     * @param defToPaste 'Pending' rendering settings to paste
     * @param refImage Image to which the rendering settings belong
	 */
	public RndSettingsSaver(TreeViewer viewer, 
                SecurityContext ctx, Class rootType, List<Long> ids,
                RndProxyDef defToPaste, ImageData refImage)
        {
                super(viewer, ctx);
                checkRootType(rootType);
                this.index = PASTE;
                if (ids == null || ids.size() == 0)
                        throw new IllegalArgumentException("No nodes specified.");
                if (refImage == null)
                        throw new IllegalArgumentException("No reference image provided.");
                this.rootType = rootType;
                this.ids = ids;
                this.defToPaste = defToPaste;
                this.refImage = refImage;
                ref = null;
        }

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The TreeViewer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param ctx The security context.
	 * @param ref The time reference object.
	 * @param refImage Image to which the rendering settings belong
	 */
	public RndSettingsSaver(TreeViewer viewer, SecurityContext ctx,
			TimeRefObject ref, ImageData refImage)
	{
		super(viewer, ctx);
		this.index = PASTE;
		if (refImage == null)
			throw new IllegalArgumentException("Pixels ID not valid.");
		if (ref == null)
			throw new IllegalArgumentException("Period not valid.");
		this.refImage = refImage;
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
				if (ref == null) {
				    if(defToPaste==null)
					handle = dhView.pasteRndSettings(ctx, refImage.getDefaultPixels().getId(), rootType,
							ids, this);
				    else
				        handle = dhView.pasteRndSettings(ctx, rootType,
                                                ids, defToPaste, refImage, this);
				}
				else 
					handle = dhView.pasteRndSettings(ctx, refImage.getDefaultPixels().getId(), ref, this);
				break;
			case RESET:
				if (ref == null)
					handle = dhView.resetRndSettings(ctx, rootType, ids, this);
				else 
					handle = dhView.resetRndSettings(ctx, ref, this);
				break;
			case SET_MIN_MAX:
				if (ref == null)
					handle = dhView.setMinMaxSettings(ctx, rootType, ids, this);
				else 
					handle = dhView.setMinMaxSettings(ctx, ref, this);
				break;
			case SET_OWNER:
				if (ref == null)
					handle = dhView.setOwnerRndSettings(ctx, rootType, ids,
							this);
				else 
					handle = dhView.setOwnerRndSettings(ctx, ref, this);
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
