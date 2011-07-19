/*
 * ome.ij.dm.browser.Browser 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm.browser;


//Java imports
import java.util.Collection;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;

/** 
 * Defines the interface provided by the browser component.
 * The browser provides a <code>JComponent</code> to host and display one
 * visualization tree. That is, one {@link TreeImageDisplay} top node.
 * Use the {@link BrowserFactory} to create an object implementing this 
 * interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface Browser
	extends ObservableComponent	
{

	/** Bound property name indicating a new node is selected. */
	public static final String  	SELECTED_TREE_NODE_DISPLAY_PROPERTY = 
		"selectedTreeNodeDisplay";

	/** 
	 * Bound property name indicating to view the selected node.
	 */
	public static final String  	VIEW_DISPLAY_PROPERTY = "viewDisplay";
	
	/** 
	 * Bound property indicating that an error occurred while loading the
	 * data and the plugin will exit.
	 */
	public static final String		ERROR_EXIT_PROPERTY = "errorExit";
	
	/** Flag to denote the <i>New</i> state. */
	public static final int     	NEW = 10;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int     	LOADING_DATA = 11;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int     	LOADING_LEAVES = 12;
    
    /** Flag to denote the <i>Counting items</i> state. */
    public static final int     	COUNTING_ITEMS = 13;
    
    /** Flag to denote the <i>Browsing</i> state. */
    public static final int     	BROWSING_DATA = 14;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     	READY = 15;

    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     	DISCARDED = 16;

    /** Indicates to sort the nodes by date. */
    public static final int         SORT_NODES_BY_DATE = 300;
    
    /** Indicates to sort the nodes by name. */
    public static final int         SORT_NODES_BY_NAME = 301;
    
    /**
     * Views the image.
     * 
     * @param node The node to view.
     */
	void viewImage(TreeImageDisplay node);
	
    /**
     * Sets the selected {@link TreeImageDisplay node}.
     * 
     * @param display           The selected node.
     */
    void setSelectedDisplay(TreeImageDisplay display);
    
	/**
	 * Counts the images imported by the current user during certain periods 
	 * of time.
	 * 
	 * @param expNode 	The node hosting the experimenter. 
	 * 					Mustn't be <code>null</code>.
	 */
	void countExperimenterImages(TreeImageDisplay expNode);
	
    /**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor The visitor.  Mustn't be <code>null</code>.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor);
    
    /**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor   The visitor.  Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected to visit the visualization trees.
     *                  One of the constants defined by
     *                  {@link TreeImageDisplayVisitor}.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor, int algoType);
    
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
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();

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
    
    /** Cancels any ongoing data loading. */
    public void cancel();

    /** 
     * Loads the experimenter data.
     * 
     * @param dataOwner
     * @param display
     */
	void loadExperimenterData(TreeImageDisplay dataOwner,
			TreeImageDisplay display);

	/** Displays the full or partial name of the images. */
	void displaysImagesName();

	/**
	 * Sorts the nodes according to the passed index.
	 * 
	 * @param index One of the constants defined by this class.
	 */
	void sortTreeNodes(int index);

	/** Refreshes the display. */
	void refresh();   
    
	/**
	 * Returns the <code>DataObject</code> currently selected or 
	 * <code>null</code>.
	 * 
	 * @return See above.
	 */
	public DataObject getSelectedObject();
	
	/**
	 * Adds the data to the passed experimenter node.
	 * 
	 * @param expNode	The experimenter node. Mustn't be <code>null</code>.
	 * @param nodes		The nodes to add.
	 */
	public void setExperimenterData(TreeImageDisplay expNode, Collection nodes);

    /**
     * Callback used by a data loader to set the leaves contained in the 
     * currently selected node.
     * 
     * @param leaves    The collection of leaves.
     * @param parent    The parent of the leaves.
     * @param expNode	The experimenter the data belonged to.
     * @throws IllegalStateException If the current state is not 
     *                               {@link #LOADING_LEAVES}.
     */
    public void setLeaves(Set leaves, TreeImageDisplay parent, 
    		TreeImageDisplay expNode);
    
    /** 
     * Collapses the specified node. 
     * 
     * @param node The node to collapse.
     */
    public void collapse(TreeImageDisplay node);
}
