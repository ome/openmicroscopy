/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer;

import java.util.List;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import omero.gateway.model.ImageData;

/** 
 * Creates rendering settings for the passed projected image.
 * This class calls <code>createRndSetting</code> method in the
 * <code>ImViewerView</code>.
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
public class RenderingSettingsCreator 
	extends DataLoader
{
	
	/** The projected image. */
    private ImageData      	image;
    
    /** The rendering settings to copy i.e. setting of the original image. */
    private RndProxyDef		rndToCopy;
    
    /** Collection of channel's indexes. */
    private List<Integer> 	indexes;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for.Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param image The projected image.
     * @param rndToCopy The rendering settings of the original image.
     * @param indexes Collection of channel's indexes. 
     * 					Mustn't be <code>null</code>.
     */
    public RenderingSettingsCreator(ImViewer viewer, SecurityContext ctx,
    		ImageData image, RndProxyDef rndToCopy, List<Integer> indexes)
    {
        super(viewer, ctx);
        if (image == null)
        	throw new IllegalArgumentException("No image specified.");
        this.image = image;
        this.rndToCopy = rndToCopy;
        this.indexes = indexes;
    }
    
    /**
     * Creates rendering settings for the passed pixels set.
     * @see DataLoader#load()
     */
    public void load()
    {
       handle = ivView.createRndSetting(ctx, image.getDefaultPixels().getId(),
    		   rndToCopy, indexes, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
    	viewer.setProjectedRenderingSettings((Boolean) result, image);
    }
    
}
