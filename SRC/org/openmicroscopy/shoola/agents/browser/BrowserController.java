/*
 * org.openmicroscopy.shoola.agents.browser.BrowserController
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import java.awt.Image;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.env.data.model.ImageData;

/**
 * The controller of the browser component MVC architecture.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal Version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserController
{
    private BrowserModel browserModel;
    private BrowserTopModel overlayModel;
    private ThumbnailSourceModel sourceModel;

    private BrowserView browserView;

    /**
     * Constructs a browser controller with an empty browser model.
     *
     */
    public BrowserController()
    {
        browserModel = new BrowserModel();
        overlayModel = new BrowserTopModel();
    }
    
    /**
     * Constructs a browser controller with a specific browser backing model.
     * @param model The browser model to back the controller.
     */
    public BrowserController(BrowserModel model)
    {
        this.browserModel = model;
        overlayModel = new BrowserTopModel();
        
    }
    
    /**
     * Constructs a browser controller with a specific browser backing model
     * and target view.  Parameters must not be null.
     * 
     * @param the model in this MVC pattern (non-null).
     * @param the view in this MVC pattern (non-null).
     */
    public BrowserController(BrowserModel model, BrowserView view)
    {
    	this.browserModel = model;
    	overlayModel = new BrowserTopModel();
    	this.browserView = view;
    	model.addModelListener(view);
    }
    
    /**
     * Return the image data set that 
     * @return
     */
    public ThumbnailSourceModel getDataModel()
    {
    	return sourceModel;
    }
    
    /**
     * Sets the thumbnails that back this browser MVC to the specified
     * collection.  The browser model and view must be specified before
     * this method is called, as this method loads the information into
     * the model and reports loading information through the view.
     * 
     * @param model The image data set to use.
     */
    public void setDataModel(ThumbnailSourceModel model)
    {
    	this.sourceModel = model; // can be null.
    	browserModel.clearThumbnails();
    	
    	// for the rest, now it can't.
    	if(model != null)
    	{
    		List keyList = model.getImageKeys();
			browserView.processStarted(keyList.size());
			ThumbnailDataLoader dataLoader = ThumbnailDataLoader.getInstance();
			ThumbnailImageLoader imageLoader = ThumbnailImageLoader.getInstance();

			for(Iterator iter = keyList.iterator(); iter.hasNext();)
			{
				int ID = ((Integer)iter.next()).intValue();
				ImageData data = model.getImageData(ID);
				ThumbnailDataModel tdm = dataLoader.loadDataFrom(data);
            
				// TODO: fix composite settings
				Image image = imageLoader.getImage(data,null);
				Thumbnail t = new Thumbnail(image,tdm);
				browserModel.addThumbnail(t);
				browserView.processAdvanced("Loaded image "+String.valueOf(ID));
			}
			browserView.processFailed("I dunno... did you break it?");
    	}
    	
    }
    
    /**
     * Gets the current view selected by this controller.
     * @return The current view.
     */
    public BrowserView getView()
    {
        return browserView;
    }
    
    /**
     * Sets the current view selected by this controller.
     * @param view The view for the controller to present the model by.
     */
    public void setView(BrowserView view)
    {
    	if(browserView != null)
    	{
    		browserModel.removeModelListener(browserView);
    	}
        if(view != null)
        {
            browserView = view;
            browserModel.addModelListener(browserView);
        }
        view.repaint();
    }
    
}
