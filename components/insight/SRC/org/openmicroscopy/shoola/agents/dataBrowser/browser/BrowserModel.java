/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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


//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

//Third-party libraries

import org.apache.commons.collections.CollectionUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.Layout;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.FilesetVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.NodesFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.ResetNodesVisitor;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.ImageData;
import pojos.WellData;

/** 
 * Implements {@link Browser} to maintain presentation state, thus acting
 * as the Model in MVC.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class BrowserModel
	extends AbstractComponent
	implements Browser
{
	
	/**
	 * Flag indicating to display the data related to a node
	 * when the user mouses over the node.
	 */
	private boolean				mouseOver;
	
	/** 
	 * Flag to control the zoom action when the user mouses over an 
	 * {@link ImageNode}. 
	 */
	private boolean         	rollOver;
	
	/** 
	 * Tells if a thumbnail has been selected in the case the 
	 * last selected display is an {@link ImageNode}. 
	 */
	private boolean         	thumbSelected;
	
	/** Position of the last pop-up trigger within the browser. */
	private Point           	popupPoint;
	
	/** Contains all visualization trees, our View. */
	private RootDisplay     	rootDisplay;
	
	/** The selected layout. */
	private Layout             	selectedLayout;
	
	/**
	 * Tells if more than one {@link ImageNode}s are selected.
	 * Doesn't really make sense to select other type of data objects.
	 */
	private boolean         	multiSelection;
	
	/** The selected nodes. */
	private List<ImageDisplay>	selectedDisplays;
	
	/** Indicates if the image's title bar is visible. */
	private boolean         	titleBarVisible;
	
	/** The node on which the mouse was located before exited. */
	private RollOverNode    	rollOverNode;
	
	/** The collection of original images. */
	private Set<ImageDisplay>	originalNodes;
	
	/**
	 * Adds the children of the passed node to its internal desktop.
	 * This method should be invoked when user switches between layout.
	 * 
	 * @param node The node to handle.
	 */
	private void addToDesktop(ImageDisplay node)
	{
	    if (node instanceof ImageNode) return;
	    JComponent desktop = node.getInternalDesktop();
	    Collection<ImageDisplay> children = node.getChildrenDisplay();
	    if (children == null) return;
	    //desktop.removeAll();
	    Iterator<ImageDisplay> i = children.iterator();
	    ImageDisplay child;
	    while (i.hasNext()) {
	        child = i.next();
	        if (!node.containsImages()) addToDesktop(child);
	        else desktop.add(child);
	    }
	}
	
	/**
	 * Sets the color of the selected and deselected nodes.
	 * 
	 * @param toSelect		The collection of selected nodes.
	 * @param toDeselect	The collection of deselected nodes.
	 */
	private void setNodesColor(Collection<ImageDisplay> toSelect, Collection<ImageDisplay> toDeselect)
    {
		//paint the nodes
        Colors colors = Colors.getInstance();
        Iterator<ImageDisplay> i;
        ImageDisplay node;
        ImageDisplay primary = null;
        if (selectedDisplays.size() > 0) primary = selectedDisplays.get(0);

        List<ImageData> selectedImages = new ArrayList<ImageData>();
        List<ImageData> deselectedImages = new ArrayList<ImageData>();
        Object ho;
        if (toDeselect != null && toDeselect.size() > 0) {
        	i = toDeselect.iterator();
            while (i.hasNext()) {
            	node = i.next();
            	if (node != null) {
            		ho = node.getHierarchyObject();
            		if (ho instanceof ImageData) {
            			deselectedImages.add((ImageData) ho);
            		}
            		if (toSelect != null) {
            			if (!toSelect.contains(node))
            				node.setHighlight(
            						colors.getDeselectedHighLight(node));
            		} else
            			node.setHighlight(colors.getDeselectedHighLight(node));
            	}
            }
        }
        if (toSelect != null && toSelect.size() > 0) {
        	 i = toSelect.iterator();
             while (i.hasNext()) {
     			node = i.next();
     			ho = node.getHierarchyObject();
        		if (ho instanceof ImageData) {
        			selectedImages.add((ImageData) ho);
        		}
     			node.setHighlight(colors.getSelectedHighLight(node,
     					isSameNode(node, primary)));
     		}
        }
        FilesetVisitor visitor = new FilesetVisitor(selectedImages,
        		deselectedImages);
        accept(visitor);
    }
	
	/**
	 * Returns <code>true</code> if the passed nodes are the same, 
	 * <code>false</code> otherwise.
	 * 
	 * @param n1 One of the nodes to handle.
	 * @param n2 One of the nodes to handle.
	 * @return See above.
	 */
	private boolean isSameNode(ImageDisplay n1, ImageDisplay n2)
	{
		if (n1 != null && n2 != null) {
			Object o1 = n1.getHierarchyObject();
			Object o2 = n2.getHierarchyObject();
			if (o1 == null || o2 == null) return false;
			if (!o1.getClass().equals(o2.getClass())) return false;
			if ((o1 instanceof DataObject) && (o2 instanceof DataObject)) {
				long id1 = ((DataObject) o1).getId();
				long id2 = ((DataObject) o2).getId();
				return id1 == id2;
			} else if ((o1 instanceof File) && (o2 instanceof File)) {
				String s1 = ((File) o1).getAbsolutePath();
				String s2 = ((File) o2).getAbsolutePath();
				return s1.equals(s2);
			}
		}
		return false;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view The root display of the visualization trees. Each child node
	 *              is the top node of a visualization tree.
	 *              Mustn't be <code>null</code>.
	 */
	BrowserModel(RootDisplay view)
	{
	    super();
	    if (view == null) throw new NullPointerException("No view.");
	    rootDisplay = view;
	    selectedDisplays = new ArrayList<ImageDisplay>();
	    originalNodes = new HashSet<ImageDisplay>(rootDisplay.getChildrenDisplay());
	    titleBarVisible = true;
	}
	
    /**
     * Sets the title and the color of the selected node.
     * 
     * @param newNode The selected node.
     * @param nodes   The previously selected nodes.
     */
    void onNodeSelected(ImageDisplay newNode, Set<ImageDisplay> nodes)
    {
        if (newNode == null) return;
        rootDisplay.setTitle(currentPathString(newNode));
        //paint the nodes
        List<ImageDisplay> selected = new ArrayList<ImageDisplay>();
        selected.add(newNode);
		if (isMultiSelection() || nodes == null)
			setNodesColor(selected, null); 
		else setNodesColor(selected, nodes);
    }
    
    /**
     * Sets the selected cells.
     * 
     * @param cell The selected cell.
     */
    void setSelectedCell(CellDisplay cell)
    {
    	firePropertyChange(CELL_SELECTION_PROPERTY, null, cell);
    }
	
	/**
	 * String-ifies the path from the specified node to the
	 * {@link #rootDisplay}.
	 * 
	 * @param parent The node to start from.
	 * @return The above described string.
	 */
	String currentPathString(ImageDisplay parent)
	{
	    StringBuffer buf = new StringBuffer();
	    List<String> titleBuf = new ArrayList<String>();
	    while (parent != null && !(parent instanceof RootDisplay)) {
	    	if (parent instanceof CellDisplay) {
	    		int type = ((CellDisplay) parent).getType();
	    		if (type == CellDisplay.TYPE_HORIZONTAL)
	    			titleBuf.add("column: "+parent.getTitle());
	    		else titleBuf.add("row: "+parent.getTitle());
	    	} else if (parent instanceof WellImageSet) {
	    		WellImageSet wiNode = (WellImageSet) parent;
	    		titleBuf.add(wiNode.getTitle());
	    	} else if (parent instanceof WellSampleNode) {
	    		Object o = ((WellSampleNode) parent).getParentObject();
	    		if (o instanceof WellData) {
	    			titleBuf.add(((WellData) o).getPlate().getName());
	    			//if (titleBuf.s() != 0)
	    			//	titleBuf.append(" > ");
	    			titleBuf.add(parent.getTitle());
	    			if (titleBuf.size() == 0) titleBuf.add("[..]");
		    		//if (titleBuf.length() == 0) titleBuf.append("[..]");
	    		}
	    	} else {
	    		//titleBuf.append(parent.getTitle());
	    		titleBuf.add(parent.getTitle());
	    		//if (titleBuf.length() == 0) titleBuf.append("[..]");
	    		//if (parent instanceof ImageSet) buf.insert(0, " > ");
	    	}
	    	//buf.insert(0, titleBuf.toString());
	        parent = parent.getParentDisplay();
	    }
	    int n = titleBuf.size();
	    for (int i = 0; i < n; i++) {
			buf.append(titleBuf.get(n-1-i));
			if (i != (n-1)) buf.append(">");
		}
	    return buf.toString();
	}
	
	/** 
	 * Fires a property whose new value if the passed object.
	 * 
	 * @param propName  The name of the property.
	 * @param node      The new value.
	 */
	void setNodeForProperty(String propName, Object node)
	{
	    firePropertyChange(propName, null, node);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setRollOverNode(RollOverNode)
	 */
	public void setRollOverNode(RollOverNode node)
	{
		RollOverNode previousNode = rollOverNode;
	    rollOverNode = node;
	    firePropertyChange(ROLL_OVER_PROPERTY, previousNode, node);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getRootNodes()
	 */
	public Collection<ImageDisplay> getRootNodes()
	{
	    NodesFinder finder = new NodesFinder();
	    accept(finder);
	    return finder.getFoundNodes();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getSelectedDisplays()
	 */
	public Collection<ImageDisplay> getSelectedDisplays() { return selectedDisplays; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getSelectedDataObjects()
	 */
	public Collection<DataObject> getSelectedDataObjects()
	{ 
		if (selectedDisplays == null) return null;
		Iterator<ImageDisplay> i = selectedDisplays.iterator();
		List<DataObject> nodes = new ArrayList<DataObject>();
		ImageDisplay o;
		Object ho;
		while (i.hasNext()) {
			o = i.next();
			ho = o.getHierarchyObject();
			if (ho instanceof DataObject)
				nodes.add((DataObject) ho);
		}
		return nodes; 
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getLastSelectedDisplay()
	 */
	public ImageDisplay getLastSelectedDisplay()
	{ 
		final int size = selectedDisplays.size();
		return size < 1 ? null : selectedDisplays.get(size - 1);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setThumbSelected(boolean, ImageNode)
	 */
	public void setThumbSelected(boolean selected, ImageNode node)
	{
	    if (node == null)
	        throw new IllegalArgumentException("No node");
	    if (!selected) return;
	    popupPoint = null;
	    thumbSelected = selected;
	    firePropertyChange(THUMB_SELECTED_PROPERTY, null, node);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isThumbSelected()
	 */
	public boolean isThumbSelected() { return thumbSelected; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setPopupPoint(Point, boolean)
	 */
	public void setPopupPoint(Point p, boolean fireProperty)
	{
	    thumbSelected = false;
	    Object oldValue = null;//popupPoint;
	    popupPoint = p;
	    if (fireProperty) firePropertyChange(POPUP_POINT_PROPERTY, oldValue, p);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getPopupPoint()
	 */
	public Point getPopupPoint() { return popupPoint; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getImages()
	 */
	public Set<DataObject> getImages()
	{ 
	    //Note: avoid caching b/c we don't know yet what we are going
	    //to do with updates
	    ImageFinder finder = new ImageFinder();
	    accept(finder, ImageDisplayVisitor.IMAGE_NODE_ONLY);
	    return finder.getImages(); 
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getImageNodes()
	 */
	public Set<ImageDisplay> getImageNodes()
	{ 
	    //Note: avoid caching b/c we don't know yet what we are going
	    //to do with updates
	    ImageFinder finder = new ImageFinder();
	    accept(finder, ImageDisplayVisitor.IMAGE_NODE_ONLY);
	    return finder.getImageNodes(); 
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#accept(ImageDisplayVisitor)
	 */
	public void accept(ImageDisplayVisitor visitor) 
	{
	    rootDisplay.accept(visitor, ImageDisplayVisitor.ALL_NODES);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#accept(ImageDisplayVisitor, int)
	 */
	public void accept(ImageDisplayVisitor visitor, int algoType) 
	{
	    rootDisplay.accept(visitor, algoType);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getUI()
	 */
	public JComponent getUI() { return rootDisplay; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedLayout(Layout)
	 */
	public void setSelectedLayout(Layout layout)
	{
		if (layout == null) return;
		Layout oldLayout = selectedLayout;
	    switch (layout.getIndex()) {
	        case LayoutFactory.SQUARY_LAYOUT:
	        case LayoutFactory.FLAT_LAYOUT:  
	        case LayoutFactory.PLATE_LAYOUT:  
	            selectedLayout = layout;
	            break;
	    }
	    firePropertyChange(LAYOUT_PROPERTY, oldLayout, selectedLayout);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getSelectedLayout()
	 */
	public Layout getSelectedLayout() { return selectedLayout; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isMultiSelection()
	 */
	public boolean isMultiSelection() { return multiSelection; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isTitleBarVisible()
	 */
	public boolean isTitleBarVisible() { return titleBarVisible; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setTitleBarVisible(boolean)
	 */
	public void setTitleBarVisible(boolean b) { titleBarVisible = b; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setRollOver(boolean)
	 */
	public void setRollOver(boolean rollOver)
	{ 
		setRollOverNode(null);
		this.rollOver = rollOver; 
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isRollOver()
	 */
	public boolean isRollOver() { return rollOver; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isMouseOver()
	 */
	public boolean isMouseOver() { return mouseOver; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setMouseOver(boolean)
	 */
	public void setMouseOver(boolean b) { mouseOver = b; }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#resetChildDisplay()
	 */
	public void resetChildDisplay()
	{
	    rootDisplay.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    Collection<ImageDisplay> rootChildren = rootDisplay.getChildrenDisplay();
	    JComponent desktop = rootDisplay.getInternalDesktop();
	    desktop.removeAll();
	    Iterator<ImageDisplay> i;
	    switch (selectedLayout.getIndex()) {
			case LayoutFactory.SQUARY_LAYOUT:
				 i = rootChildren.iterator();
			        ImageDisplay child;
			        while (i.hasNext()) {
			            child = i.next();
			            desktop.add(child);
			            addToDesktop(child);
			        }
				break;
	
			case LayoutFactory.FLAT_LAYOUT:
				 i = getImageNodes().iterator();
			        while (i.hasNext()) 
			            desktop.add(i.next());   
		}
	    rootDisplay.setCursor(
	            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedNodes(List)
	 */
	public void setSelectedNodes(List<DataObject> nodes)
	{
		Set<ImageDisplay> oldValue = null;
		if (selectedDisplays != null)
			oldValue = new HashSet<ImageDisplay>(selectedDisplays);
		if (CollectionUtils.isEmpty(nodes)) {
			if (selectedDisplays != null) selectedDisplays.clear();
			setNodesColor(null, oldValue);
			return;
		}
		NodesFinder finder = new NodesFinder(nodes);
		accept(finder);
		List<ImageDisplay> found = finder.getFoundNodes();
		if (CollectionUtils.isEmpty(found)) {
			setSelectedDisplay(null, false, false);
			//Check again
			Collection<ImageDisplay> selected = getSelectedDisplays();
            if (CollectionUtils.isEmpty(selected)) {
                setNodesColor(null, getRootNodes());
            }
			return;
		}

		thumbSelected = false;
		this.multiSelection = found.size() > 1;
		setSelectedDisplays(found);
		setNodesColor(found, oldValue);
		List<ImageData> images = new ArrayList<ImageData>();
		Iterator<ImageDisplay> i = found.iterator();
		ImageDisplay d;
		while (i.hasNext()) {
			d = i.next();
			if (d.getHierarchyObject() instanceof ImageData)
				images.add((ImageData) d.getHierarchyObject());
		}
		FilesetVisitor visitor = new FilesetVisitor(images, null);
		accept(visitor);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setFilterNodes(Collection)
	 */
	public void setFilterNodes(Collection<ImageDisplay> nodes)
	{
		ResetNodesVisitor visitor = new ResetNodesVisitor(nodes, true);
		rootDisplay.accept(visitor, ImageDisplayVisitor.IMAGE_SET_ONLY);
		final Set<DataObject> visibleDataObjects = getVisibleImages();
		final Collection<DataObject> selectedDataObjects = getSelectedDataObjects();
		final Set<ImageDisplay> nodesWithVisibleSelectedDataObjects = new HashSet<ImageDisplay>();
		for (final ImageDisplay filterNode : nodes) {
			final Object hierarchyObject = filterNode.getHierarchyObject();
			if (visibleDataObjects.contains(hierarchyObject) && selectedDataObjects.contains(hierarchyObject))
				nodesWithVisibleSelectedDataObjects.add(filterNode);
		}
		setNodesSelection(nodesWithVisibleSelectedDataObjects);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#showAll()
	 */
	public void showAll()
	{
		setFilterNodes(getImageNodes());
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getOriginal()
	 */
	public Set<DataObject> getOriginal()
	{
		Set<DataObject> nodes = new HashSet<DataObject>();
		Iterator<ImageDisplay> i = originalNodes.iterator();
		Object ho;
		while (i.hasNext()) {
			ho = i.next().getHierarchyObject();
			if (ho instanceof DataObject)
				nodes.add((DataObject) ho);
		}
		return nodes;
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getVisibleImages()
	 */
	public Set<DataObject> getVisibleImages()
	{
		//Note: avoid caching b/c we don't know yet what we are going
		//to do with updates
	    ImageFinder finder = new ImageFinder();
	    accept(finder, ImageDisplayVisitor.IMAGE_SET_ONLY);
	    return finder.getVisibleImages(); 
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getVisibleImageNodes()
	 */
	public List<ImageNode> getVisibleImageNodes()
	{
		//Note: avoid caching b/c we don't know yet what we are going
		//to do with updates
	    ImageFinder finder = new ImageFinder();
	    accept(finder, ImageDisplayVisitor.ALL_NODES);
	    return new ArrayList<ImageNode>(finder.getVisibleImageNodes());
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setNodesSelection(Collection)
	 */
	public void setNodesSelection(Collection<ImageDisplay> nodes)
	{
		if (nodes == null) return;
		setNodesColor(nodes, getSelectedDisplays());
		final HashSet<ImageDisplay> previouslySelectedDisplays =
		        new HashSet<ImageDisplay>(this.selectedDisplays);
		previouslySelectedDisplays.removeAll(nodes);
		Colors colors = Colors.getInstance();
		for (final ImageDisplay node : previouslySelectedDisplays) {
	        node.setHighlight(colors.getDeselectedHighLight(node));
	        selectedDisplays.remove(node);
		}
		boolean multiSelection = false;
		Set<ImageDisplay> oldValue =
                new HashSet<ImageDisplay>(selectedDisplays);
		for (final ImageDisplay node : nodes) {
			setSelectedDisplay(node, multiSelection, false);
			multiSelection = true;
		}
        firePropertyChange(SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY,
                oldValue, selectedDisplays);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#removeSelectedDisplay(ImageDisplay)
	 */
	public void removeSelectedDisplay(ImageDisplay node)
	{
		if (node == null) return;
		Colors colors = Colors.getInstance();
		node.setHighlight(colors.getDeselectedHighLight(node));
		selectedDisplays.remove(node);
		firePropertyChange(UNSELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY, null, 
				node);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#viewDisplay(ImageDisplay, boolean)
	 */
	public void viewDisplay(ImageDisplay node, boolean internal)
	{
		if (node == null) return;
		if (internal)
			firePropertyChange(MAIN_VIEW_DISPLAY_PROPERTY, null, node);
		else firePropertyChange(VIEW_DISPLAY_PROPERTY, null, node);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setComponentTitle(String)
	 */
	public void setComponentTitle(String title)
	{
		rootDisplay.setTitle(title);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#refresh(Collection, List)
	 */
	public void refresh(Collection<ImageDisplay> nodes, 
			List<ImageDisplay> selected)
	{
		rootDisplay.removeAllChildrenDisplay();
		if (nodes == null) return;
		Iterator<ImageDisplay> i = nodes.iterator();
		while (i.hasNext()) 
			rootDisplay.addChildDisplay(i.next());
		if (selected == null) return;
		boolean b = selected.size() > 1;
		i = selected.iterator();
		while (i.hasNext()) 
			setSelectedDisplay(i.next(), b, true);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#markUnmodifiedNodes(Class, Collection)
	 */
	public void markUnmodifiedNodes(Class type, Collection<Long> ids)
	{
		Iterator<ImageDisplay> i = selectedDisplays.iterator();
		ImageDisplay node;
		Object ho;
		long id;
		Colors colors = Colors.getInstance();
		while (i.hasNext()) {
			node = i.next();
			ho = node.getHierarchyObject();
			if (ho.getClass().equals(type) && ho instanceof DataObject) {
				id = ((DataObject) ho).getId();
				if (ids.contains(id)) {
					node.setHighlight(colors.getUnmodifiedHighLight(node));
				}
			}
		}
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * 
	 * @see Browser#setSelectedDisplay(Point, boolean)
	 */
	public void setSelectedDisplay(Point coords, boolean multiSel) {
		Component c = rootDisplay.getInternalDesktop().getComponentAt(coords);
		if (c != null && c instanceof ImageDisplay) {
			setSelectedDisplay((ImageDisplay) c, multiSel, true);
		}
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedDisplay(ImageDisplay, boolean, boolean)
	 */
	public void setSelectedDisplay(ImageDisplay node, boolean multiSelection,
			boolean fireProperty)
	{
		if (node instanceof CellDisplay) return;
	    thumbSelected = false;
	    //popupPoint = null; //TEST mouse click
	    this.multiSelection = multiSelection;
	    final Set<ImageDisplay> oldValue =
	    		new HashSet<ImageDisplay>(this.selectedDisplays);
	    
	    if (!multiSelection) selectedDisplays.clear();
	    int n = selectedDisplays.size();
	    boolean removed = false;
	    if (node != null) {
	    	if (selectedDisplays.contains(node)) {
	    		selectedDisplays.remove(node);
	    		n = selectedDisplays.size();
	    		removed = true;
	    	} else selectedDisplays.add(node);
	    }
	    if (fireProperty) {
	    	onNodeSelected(node, oldValue);
	    	firePropertyChange(SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY,
	    			oldValue, node);
	    } else {
	    	if (multiSelection) {
	    		Colors colors = Colors.getInstance();
	    		if (removed)
	    			node.setHighlight(colors.getDeselectedHighLight(node));
	    		else 
	    			node.setHighlight(colors.getSelectedHighLight(node,
	    					n == 0));
	    	} else onNodeSelected(node, oldValue);
	    }
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedDisplays(List)
	 */
	public void setSelectedDisplays(List<ImageDisplay> nodes)
	{
		if (CollectionUtils.isEmpty(nodes)) return;
		if (nodes.size() == 1) {
			setSelectedDisplay(nodes.get(0), false, true);
		} else {
			List<ImageData> selected = new ArrayList<ImageData>(nodes.size());
			Iterator<ImageDisplay> i = nodes.iterator();
			ImageDisplay img;
			while (i.hasNext()) {
				img = i.next();
				if (img.getHierarchyObject() instanceof ImageData) {
					selected.add((ImageData) img.getHierarchyObject());
				}
			}
			FilesetVisitor visitor = new FilesetVisitor(selected, null);
			accept(visitor);
			thumbSelected = false;
		    this.multiSelection = true;
		    Set<ImageDisplay> oldValue =
		            new HashSet<ImageDisplay>(selectedDisplays);
		    selectedDisplays = nodes;
		    firePropertyChange(SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY,
	    			oldValue, nodes);
		}
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#scrollToNode(ImageDisplay)
	 */
	public void scrollToNode(ImageDisplay node)
	{
		if (node == null) return;
		JScrollPane pane = rootDisplay.getDeskDecorator();
		Rectangle bounds = node.getBounds();
		Rectangle viewRect = pane.getViewport().getViewRect();
		if (viewRect.contains(bounds)) return;
		JScrollBar hBar = pane.getHorizontalScrollBar();
		if (hBar.isVisible()) {
			int x = bounds.x;
			int max = hBar.getMaximum();
			if (x > max) x = max;
			hBar.setValue(x);
		}
		JScrollBar vBar = pane.getVerticalScrollBar();
		if (vBar.isVisible()) {
			int y = bounds.y;
			int max = vBar.getMaximum();
			if (y > max) y = max;
			vBar.setValue(y);
		}
	}

}
