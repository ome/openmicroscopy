/*
 * org.openmicroscopy.shoola.agents.metadata.viewedby.ViewedByComponent 
 *
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
package org.openmicroscopy.shoola.agents.metadata.viewedby;


//Java imports
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.metadata.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.metadata.util.ThumbnailView;



//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * 
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
public class ViewedByComponent 
	extends JPanel
	implements ThumbnailView
{
	
    private ImageData				image;
    
    private Map<Long, ViewedItem> 	items;
    
    private ThumbnailLoader			loader;
    
    /** 
     * Loads the thumbnail for the specified users.
     * 
     * @param userIDs The collection of user's id.
     */
    private void loadThumbnail(Set<Long> userIDs)
    {
    	if (loader != null) loader.cancel();
    	loader = new ThumbnailLoader(this, image, userIDs);
    	loader.load();
    }
    
    private void initialize(Map values)
	{
		Iterator i = values.keySet().iterator();
		ExperimenterData exp;
		ViewedItem item;
		items = new HashMap<Long, ViewedItem>();
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			item = new ViewedItem(exp, values.get(exp));
			items.put(exp.getId(), item);
			add(item);
		}
		loadThumbnail(items.keySet());
	}
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	
    }
    
	
	/**
	 * 
	 * @param values
	 * @param image
	 */
	public ViewedByComponent(Map values, ImageData image)
	{
		if (image == null)
			throw new IllegalArgumentException("No image.");
		this.image = image;
		initialize(values);
		buildGUI();
	}

	public void setThumbnail(BufferedImage thumb, long pixelsID, long userID)
	{
		//if (image.getDefaultPixels().getId() != pixelsID) return;
		if (thumb == null) return;
		ViewedItem item = items.get(userID);
		if (item != null)
			item.setThumbnail(thumb);
	}
	
}
