/*
 * org.openmicroscopy.shoola.agents.imviewer.ImageLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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



//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import com.sun.opengl.util.texture.TextureData;

/** 
 * Renders the specified plane. This class calls the <code>render</code> in the
 * <code>ImViewerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImageLoader
    extends DataLoader
{

    /** The ID of the pixels set. */
    private long        pixelsID;
    
    /** The plane to render. */
    private PlaneDef    pd;
    
    /** Flag indicating if the image is large or not. */
    private boolean		largeImage;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance
     * 
     * @param viewer    The view this loader is for.
     *                  Mustn't be <code>null</code>.
     * @param pixelsID  The id of the pixels set.
     * @param pd        The plane to render. 
     * @param largeImae Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
     */
    public ImageLoader(ImViewer viewer, long pixelsID, PlaneDef pd, boolean
    		largeImage)
    {
        super(viewer);
        this.pixelsID = pixelsID;
        this.pd = pd;
        this.largeImage = largeImage;
    }

    /**
     * Renders a 2D-plane.
     * @see DataLoader#load()
     */
    public void load()
    {
    	boolean asTexture = ImViewerAgent.hasOpenGLSupport();
    	handle = ivView.render(pixelsID, pd, asTexture, largeImage, this);
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
    	if ((ImViewerAgent.hasOpenGLSupport())) {
    		viewer.setImageAsTexture((TextureData) result);
    	} else {
    		viewer.setImage((BufferedImage) result);
    	}
    }

}
