/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserModel 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;



//Java imports
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.RateFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.TagsFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailsManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.Layout;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.util.FilterContext;

import pojos.DataObject;

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
abstract class DataBrowserModel
{

	/** Holds one of the state flags defined by {@link DataBrowser}. */
    private int					state;
    
    /** Maps an image id to the list of thumbnail providers for that image. */
    private ThumbnailsManager   thumbsManager;
    
    /** Used to sort the nodes by date or alphabetically. */
    private ViewerSorter        sorter;
    
    private DataBrowserLoader	loader;
    
    /** Reference to the component that embeds this model. */
    protected DataBrowser		component;
    
    /** Reference to the browser. */
    protected Browser			browser;
    
    /** Creates a new instance. */
    DataBrowserModel()
    {
    	sorter = new ViewerSorter();
    	state = DataBrowser.NEW; 
    }
    
    /** Lays out the browser. */
    void layoutBrowser()
    {
    	if (browser == null) return;
    	//Do initial layout and set the icons.
        Layout layout = LayoutFactory.getDefaultLayout(sorter);
        browser.setSelectedLayout(layout.getIndex());
        browser.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
    }
    
    /**
     * Returns the current state.
     * 
     * @return See above.
     */
    int getState() { return state; }
    
    /**
     * Loads the data.
     * 
     * @param refresh
     */
    void loadData(boolean refresh)
    {
    	state = DataBrowser.LOADING;
    	loader = createDataLoader(refresh);
    	loader.load();
    }
    
    /**
     * Returns the browser.
     * 
     * @return See above.
     */
    Browser getBrowser() { return browser; }
    
    /**
     * Called by the <code>DataBrowser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(DataBrowser component) { this.component = component; }

    /**
     * Sets the specified thumbnail for all image nodes in the display that
     * map to the same image hierarchy object.
     * When every image object has a thumbnail, this method sets the state
     * to {@link HiViewer#READY}.
     * 
     * @param imageID The id of the hierarchy object to which the thumbnail 
     *                belongs.
     * @param thumb The thumbnail pixels.
     */
    void setThumbnail(long imageID, BufferedImage thumb)
    {
        if (thumbsManager == null) 
            thumbsManager = new ThumbnailsManager(browser.getImageNodes());
        thumbsManager.setThumbnail(imageID, thumb);
        if (thumbsManager.isDone()) {
            state = DataBrowser.READY;
            thumbsManager = null;
           
        }
    }
    
    /** Discards any on-going data loading. */
	void discard()
	{
		
	}
	
	void cancelFiltering()
	{
		
	}
	
	void setState(int state)
	{
		this.state = state;
	}
	
	/**
	 * Filters the passed <code>DataObject</code>s by rate.
	 * 
	 * @param rate	The selected rate. One of the constants defined 
	 * 				by {@link RateFilter}.
	 * @param nodes	The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByRate(int rate, Set nodes)
	{
		state = DataBrowser.FILTERING;
		RateFilter loader = new RateFilter(component, rate, nodes);
		loader.load();
	}
	
	/**
	 * Filters the passed <code>DataObject</code>s by tags.
	 * 
	 * @param tags	The selected rates.
	 * @param nodes	The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByTags(List<String> tags, Set<DataObject> nodes)
	{
		state = DataBrowser.FILTERING;
		TagsFilter loader = new TagsFilter(component, tags, nodes);
		loader.load();
	}

	/**
	 * Filters the passed <code>DataObject</code>s by context.
	 * 
	 * @param context 	The filtering context.
	 * @param nodes		The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByContext(FilterContext context, Set<DataObject> nodes)
	{
		state = DataBrowser.FILTERING;
		DataFilter loader = new DataFilter(component, context, nodes);
		loader.load();
	}
	
    /**
     * Creates a data loader that can retrieve the hierarchy objects needed
     * by this model.
     * 
     * @param refresh	Pass <code>false</code> if we load data for the first 
     * 					time, <code>true</code> otherwise.
     * @return A suitable data loader.
     */
    protected abstract DataBrowserLoader createDataLoader(boolean refresh);

}
