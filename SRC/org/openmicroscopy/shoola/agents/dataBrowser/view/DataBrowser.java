/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser 
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
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.RateFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
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
public interface DataBrowser 
	extends ObservableComponent
{

	public static final int			ANNOTATE_SELECTION = 100;
	
	public static final int			ANNOTATE_IMAGES = 101;
	
	public static final int			ANNOTATE_CHILDREN = 102;
	
	/** Indicates to lay out the nodes as thumbnails. */
	public static final int			THUMBNAIL_VIEW = 0;
	
	/** Indicates to lay out the nodes as table rows. */
	public static final int			COLUMN_VIEW = 1;
	
	/** Indicates to lay out the nodes in a slide show. */
	public static final int			SLIDE_SHOW_VIEW = 2;
	
	/** Indicates to retrieve the node rated one or higher. */
	public static final int			RATE_ONE = RateFilter.RATE_ONE;
	
	/** Indicates to retrieve the node rated two or higher. */
	public static final int			RATE_TWO = RateFilter.RATE_TWO;
	
	/** Indicates to retrieve the node rated three or higher. */
	public static final int			RATE_THREE = RateFilter.RATE_THREE;
	
	/** Indicates to retrieve the node rated four or higher. */
	public static final int			RATE_FOUR = RateFilter.RATE_FOUR;
	
	/** Indicates to retrieve the node rated five. */
	public static final int			RATE_FIVE = RateFilter.RATE_FIVE;
	
	/** Indicates to retrieve the node rated two or higher. */
	public static final int			UNRATED = RateFilter.UNRATED;;
	
	/** 
	 * Bound property name indicating an {@link ImageDisplay} object has been
	 * selected in the visualization tree. 
	 */
	public static final String 		SELECTED_NODE_DISPLAY_PROPERTY = 
												"selectedNodeDisplay";
	
	/** Flag to denote the <i>New</i> state. */
    public static final int     	NEW = 1;

    /** Flag to denote the <i>Loading</i> state. */
    public static final int     	LOADING = 2;

    /** Flag to denote the <i>Ready</i> state. */
    public static final int     	READY = 3;

    /** Flag to denote the <i>Filtering</i> state. */
    public static final int     	FILTERING = 4;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     	DISCARDED = 5;
 
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Indicates what kind of hierarchy the viewer is displaying.
     * 
     * @return One of the hierarchy flags defined by this interface.
     */
    public int getHierarchyType();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Callback used by a data loader to set thumbnails as they are retrieved.
     * 
     * @param imageID The id of the image to which the thumbnail belongs.
     * @param thumb The thumbnail pixels.
     * @see org.openmicroscopy.shoola.agents.hiviewer.DataLoader
     */
    public void setThumbnail(long imageID, BufferedImage thumb);
    
    /**
     * Callback used by data loaders to provide the viewer with feedback about
     * the data retrieval.
     * 
     * @param description Textual description of the ongoing operation.
     * @param perc Percentage of the total work done.  If negative, it is
     *             interpreted as not available.
     * @see org.openmicroscopy.shoola.agents.hiviewer.DataLoader
     */
    public void setStatus(String description, int perc);
        
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** 
     * Returns the UI component. 
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JComponent getUI();
    
    /**
     * Sets the selected node.
     * 
     * @param node The node to set.
     */
	public void setSelectedDisplay(ImageDisplay node);

	/**
	 * Sets the collection of selected nodes.
	 * 
	 * @param nodes The selected nodes.
	 */
	public void setSelectedNodes(List<DataObject> nodes);
	
	/**
	 * Filters by tags.
	 * 
	 * @param tags The collection of selected tags.
	 */
	public void filterByTags(List<String> tags);
	
	/**
	 * Filters by comments.
	 * 
	 * @param comments The collection of comments to handle.
	 */
	public void filterByComments(List<String> comments);
	
	/**
	 * Filters the nodes by name and description.
	 * 
	 * @param terms The collection of terms to handle.
	 */
	public void filterByFullText(List<String> terms);
	
	/**
	 * Filters the nodes by rate.
	 * 
	 * @param rate 	The selected rate. One of the constants defined by this 
	 * 				class.
	 */
	public void filterByRate(int rate);
	
	/** Shows all the nodes. */
	public void showAll();
	
	/**
	 * Sets the collection of filtered nodes.
	 * 
	 * @param objects The nodes to filter.
	 */
	public void setFilteredNodes(List<DataObject> objects);

	public void filterByContext(FilterContext context);
	
	public void annotate(int index);

	public void loadExistingTags();

	public void setExistingTags(Collection collection);
	
}
