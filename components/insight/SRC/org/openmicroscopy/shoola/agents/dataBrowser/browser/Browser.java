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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.dataBrowser.layout.Layout;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import omero.gateway.model.DataObject;

/** 
 * Defines the interface provided by the browser component.
 * The browser provides a <code>JComponent</code> to host and display one or
 * more visualization trees.  That is, one or more {@link ImageDisplay} top
 * nodes, each representing an image hierarchy.
 * Use the {@link BrowserFactory} to create an object implementing this 
 * interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface Browser
	extends ObservableComponent
{
	
	/** 
	 * Bound property name indicating that a cell is selected. 
	 */
	public static final String CELL_SELECTION_PROPERTY = "cellSelection";
	
	/** 
	 * Bound property name indicating an {@link ImageDisplay} object has been
	 * selected in the visualization tree. 
	 */
	public static final String MOUSE_OVER_PROPERTY = "mouseOver";
	
	/** 
	 * Bound property name indicating an {@link ImageDisplay} object has been
	 * selected in the visualization tree. 
	 */
	public static final String SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY = 
		"selectedDataBrowserNodeDisplay";
	
	/** 
	 * Bound property name indicating {@link ImageDisplay} objects have been
	 * selected in the visualization tree. 
	 */
	public static final String SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY = 
		"selectedDataBrowserNodesDisplay";
	
	/** 
	 * Bound property name indicating an {@link ImageDisplay} object has been
	 * unselected in the visualization tree. 
	 */
	public static final String UNSELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY =
		"unselectedDataBrowserNodeDisplay";
	
	/** 
	 * Bound property name indicating a {@link Thumbnail} has been selected
	 * within an {@link ImageNode}.
	 * The associated property change event is always dispatched <i>after</i>
	 * dispatching the one for the 
	 * {@link #SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY}.  This
	 * latter property will be set to the {@link ImageNode} the 
	 * {@link Thumbnail} belong in.
	 */
	public static final String  THUMB_SELECTED_PROPERTY = "thumbSelected";
	
	/** 
	 * Bound property name indicating a pop-up trigger event occurred at a 
	 * given point within the browser component. 
	 */
	public static final String  POPUP_POINT_PROPERTY = "popupPoint";
	
	/** Bound property name indicating a layout selection. */
	public static final String  LAYOUT_PROPERTY = "layout";
	
	/** Bound property indicating an annotation visualization. */
	public static final String  ANNOTATED_NODE_PROPERTY = "annotatedNode";
	
	/** Bound property indicating a classification visualization. */
	public static final String  CLASSIFIED_NODE_PROPERTY = "classifiedNode";
	
	/** Bound property indicating to magnify the node when roll over. */
	public static final String  ROLL_OVER_PROPERTY = "rollOver";

	/** Bound property indicating to view the specified note. */
	public static final String  VIEW_DISPLAY_PROPERTY = "viewDisplay";
	
	/** Bound property indicating to view the specified note. */
	public static final String  MAIN_VIEW_DISPLAY_PROPERTY = "mainViewDisplay";
	
	/**
	 * Returns the node, if any, that is currently selected in the 
	 * visualization tree.
	 * 
	 * @return The currently selected node or <code>null</code> if no node
	 *          is currently selected.
	 */
	public ImageDisplay getLastSelectedDisplay();
	
	/**
	 * Sets a flag to indicate if a {@link Thumbnail} has been selected
	 * within an {@link ImageNode}.
	 * 
	 * @param selected  Pass <code>true</code> if the currently passed display
	 *                  is an {@link ImageNode} and its {@link Thumbnail} has
	 *                  been selected. Pass <code>false</code> in any other
	 *                  case.
	 * @param node      The selected node.                
	 * @throws IllegalArgumentException If you pass <code>true</code> but the
	 *         currently selected display is <i>not</i> an {@link ImageNode}.
	 */
	public void setThumbSelected(boolean selected, ImageNode node);
	
	/**
	 * Tells if a {@link Thumbnail} has been selected within an 
	 * {@link ImageNode}.
	 * The only case in which this method will return <code>true</code> is
	 * when the currently selected display is an {@link ImageNode} and its
	 * {@link Thumbnail} has been selected.  In particular, the returned
	 * value will <i>always</i> be <code>false</code> if the currently selected
	 * display is <i>not</i> an {@link ImageNode}.
	 * 
	 * @return <code>true</code> if currently selected display is an 
	 *         {@link ImageNode} and its {@link Thumbnail} has been selected;
	 *         <code>false</code> in all other cases.
	 */
	public boolean isThumbSelected();
	
	/**
	 * Sets the point at which the last pop-up trigger event occurred within 
	 * the browser component.
	 * 
	 * @param p The point at which the event occurred, <i>relative</i> to the 
	 *          coordinates of the currently selected display.
	 * @param fireProperty 	Pass <code>true</code> to fire a property, 
	 * 						<code>false</code> otherwise.
	 */
	public void setPopupPoint(Point p, boolean fireProperty);
	
	/**
	 * Returns the point at which the last pop-up trigger event occurred within 
	 * the browser component.
	 * This method may return <code>null</code>, for example if no such an
	 * event has occurred yet or if a thumbnail has been selected &#151; these
	 * two events are mutually exclusive.
	 * 
	 * @return The point at which the event occurred, <i>relative</i> to the 
	 *         coordinates of the currently selected display.
	 */
	public Point getPopupPoint();
	
	/**
	 * Returns all the hierarchy objects that are linked to any of the
	 * {@link ImageNode}s in the visualization trees hosted by the browser.
	 * 
	 * @return A set of <code>Object</code>s.
	 */
	public Set<DataObject> getImages();
	
	/**
	 * Returns all the {@link ImageNode}s in the visualization trees hosted
	 * by the browser.
	 * 
	 * @return A set of {@link ImageNode} objects.
	 */
	public Set getImageNodes();
	
	/**
	 * Returns all the {@link ImageDisplay} objects that are children
	 * of the {@link RootDisplay} node.
	 * 
	 * @return A set of {@link ImageDisplay} objects.
	 */
	public Collection getRootNodes();
	
	/**
	 * Returns the widget that displays all the visualization trees hosted
	 * by the browser.
	 *  
	 * @return The browser widget.
	 */
	public JComponent getUI();
	
	/**
	 * Sets the selected layout.
	 * 
	 * @param layout The layout.
	 */
	public void setSelectedLayout(Layout layout);
	
	/**
	 * Returns <code>true</code> if more than one {@link ImageNode}s are
	 * selected, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMultiSelection();
	
	/**
	 * Returns the collection of selected nodes.
	 * 
	 * @return See above.
	 */
	public Collection<ImageDisplay> getSelectedDisplays();
	
	/**
	 * Returns the collection of the <code>DataObject</code>s hosted by
	 * the selected nodes.
	 * 
	 * @return See above.
	 */
	public Collection<DataObject> getSelectedDataObjects();
	
	/**
	 * Returns <code>true</code> if the image's title bar is visible,
	 * <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	public boolean isTitleBarVisible();
	
	/**
	 * Sets the title bar visible flag.
	 *  
	 * @param b Pass <code>true</code> to show the image's title bar,
	 *          <code>false</code> to hide it.
	 */
	public void setTitleBarVisible(boolean b);
	
	/**
	 * Sets the value of the <code>Roll over</code> flag.
	 * 
	 * @param rollOver  Pass <code>true</code> to zoom the image when the user
	 *                  mouses over an {@link ImageNode}, 
	 *                  <code>false</code> otherwise.
	 */
	public void setRollOver(boolean rollOver);
	
	/**
	 * Returns <code>true</code> if the image is zoomed when the user mouses
	 * over an {@link ImageNode}, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRollOver();
	
	/**
	 * Returns <code>true</code> if data related to a node is displayed
	 * when the user mouses over the node, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMouseOver();
	
	/**
	 * Sets to <code>true</code> if data related to a node is displayed
	 * when the user mouses over the node, to <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	public void setMouseOver(boolean b);
	
	/**
	 * Adds the nodes to the <code>Desktop</code> of the root node according
	 * to the currently selected layout.
	 */
	public void resetChildDisplay();
	
	/**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor The visitor.  Mustn't be <code>null</code>.
     * @see ImageDisplayVisitor
     */
    public void accept(ImageDisplayVisitor visitor);
    
    /**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor   The visitor.  Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected to visit the visualization trees.
     *                  One of the constants defined by
     *                  {@link ImageDisplayVisitor}
     * @see ImageDisplayVisitor
     */
    public void accept(ImageDisplayVisitor visitor, int algoType);
    
    /**
     * Sets the selected nodes.
     * 
     * @param nodes The collection of selected data objects.
     */
	public void setSelectedNodes(List<DataObject> nodes);

	/**
	 * Sets the collection of nodes filtered.
	 * 
	 * @param nodes The collection to set.
	 */
	public void setFilterNodes(Collection<ImageDisplay> nodes);
	
	/** Shows all the nodes. */
	public void showAll();
	
	/** 
	 * Returns the original collection of <code>DataObject</code>s.
	 * 
	 * @return See above.
	 */
	public Set<DataObject> getOriginal();
	
	/** 
	 * Returns the images currently visible.
	 * 
	 * @return See above.
	 */
	public Set<DataObject> getVisibleImages();
	
	/** 
	 * Returns the images currently visible.
	 * 
	 * @return See above.
	 */
	public List<ImageNode> getVisibleImageNodes();
	
	/**
	 * Returns the selected layout.
	 * 
	 * @return See above.
	 */
	public Layout getSelectedLayout();
	
	/**
	 * Returns the collection of selected nodes.
	 * 
	 * @param nodes The selected nodes.
	 */
	public void setNodesSelection(Collection<ImageDisplay> nodes);
	
	/** 
	 * Removes the passed node from the selection.
	 * 
	 * @param node The node to remove from the list.
	 */
	public void removeSelectedDisplay(ImageDisplay node);
	
	/**
	 * Views the passed node.
	 * 
	 * @param node The node to view.
	 * @param internal Pass <code>true</code> to open in the internal viewer,
	 * <code>false</code> otherwise. This only applies for images.
	 */
	public void viewDisplay(ImageDisplay node, boolean internal);
	
	/** 
	 * Sets the passed title in the header of the browser.
	 * 
	 * @param title The value to set.
	 */
	public void setComponentTitle(String title);
	
	/**
	 * Refreshes the collection of passed nodes.
	 * 
	 * @param nodes The nodes to refresh.
	 * @param selected The nodes to select.
	 */
	public void refresh(Collection<ImageDisplay> nodes, 
			List<ImageDisplay> selected);

	/**
	 * Marks the nodes on which a given operation could not be performed
	 * e.g. paste rendering settings.
	 * 
	 * @param type The type of data objects.
	 * @param ids  Collection of object's ids.
	 */
	public void markUnmodifiedNodes(Class type, Collection<Long> ids);

	/**
	 * Sets the node which has to be zoomed.
	 * 
	 * @param node The node to zoom.
	 */
	public void setRollOverNode(RollOverNode node);
	
	/**
	 * Sets the specified <code>node</code> to be the currently selected
	 * node in the visualization tree.
	 * Sets it to <code>null</code> to indicate no node is currently selected.
	 *  
	 * @param node           	The node to become the currently selected node.
	 * 						 	Pass <code>null</code> only when refreshing the
	 * 						 	display.
	 * @param multiSelection	Pass <code>false</code> to indicate that only 
	 * 							one node is selected, <code>true</code> 
	 * 							otherwise.
	 * @param fireProperty		Pass <code>true</code> to fire a property, 
	 * 							<code>false</code> otherwise.
	 */
	public void setSelectedDisplay(ImageDisplay node, boolean multiSelection, 
			boolean fireProperty);
	
	/**
	 * Scrolls to the specified node.
	 * 
	 * @param node The node to scroll to.
	 */
	public void scrollToNode(ImageDisplay node);
	
	/**
	 * Sets the nodes selected via multi-selection.
	 * 
	 * @param nodes The selected nodes. 
	 */
	public void setSelectedDisplays(List<ImageDisplay> nodes);
	
	/**
         * Select the ImageDisplay which is located at the given coordinates
         *
         * @param coords The component coordinates
         * @param multiSel Pass <code>true</code> for adding selection 
         *             to a multiple selection
         */
	public void setSelectedDisplay(Point coords, boolean multiSel);

}
