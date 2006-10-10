/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.BrowserModel
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/** 
 * Implements {@link Browser} to maintain presentation state, thus acting
 * as the Model in MVC.
 *
 * @see BrowserControl
 * @see RootDisplay
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserModel
    extends AbstractComponent
    implements Browser
{

    /** 
     * Flag to control the zoom action when the user mouses over an 
     * {@link ImageNode}. 
     */
    private boolean         rollOver;
    
    /** 
     * Tells if a thumbnail has been selected in the case the 
     * last selected display is an {@link ImageNode}. 
     */
    private boolean         thumbSelected;
    
    /** Position of the last pop-up trigger within the browser. */
    private Point           popupPoint;
    
    /** Contains all visualization trees, our View. */
    private RootDisplay     rootDisplay;
    
    /** The index of the selected layout. */
    private int             selectedLayout;
    
    /**
     * Tells if more than one {@link ImageNode}s are selected.
     * Doesn't really make sense to select other type of data objects.
     */
    private boolean         multiSelection;
    
    /** The selected nodes. */
    private Set             selectedDisplays;
    
    /** Indicates if the image's title bar is visible. */
    private boolean         titleBarVisible;
    
    /** The node on which the mouse was located before exited. */
    private ImageDisplay    rollOverNode;
    
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
        selectedDisplays = new HashSet();
        titleBarVisible = true;
    }
    
    /**
     * Sets the specified <code>node</code> to be the currently selected
     * node in the visualization tree.
     * Sets it to <code>null</code> to indicate no node is currently selected.
     *  
     * @param node           The node to become the currently selected node.
     * @param multiSelection Pass <code>false</code>  to indicate that only one
     *                       node is selected, <code>true</code> otherwise.
     */
    void setSelectedDisplay(ImageDisplay node, boolean multiSelection)
    {
        thumbSelected = false;
        popupPoint = null;
        this.multiSelection = multiSelection;
        Set oldValue = new HashSet(selectedDisplays.size());
        Iterator i = selectedDisplays.iterator();
        while (i.hasNext())
            oldValue.add(i.next());
        
        if (!multiSelection)
            selectedDisplays.removeAll(selectedDisplays);
        selectedDisplays.add(node);
        firePropertyChange(SELECTED_DISPLAY_PROPERTY, oldValue, node);
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
        String title;
        while (parent != null && !(parent instanceof RootDisplay)) {
            title = parent.getTitle();
            if (title == null || title.length() == 0) title = "[..]";
            buf.insert(0, " > ");
            buf.insert(0, title);
            parent = parent.getParentDisplay();
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
     * Sets the node which has to be zoomed.
     * 
     * @param newNode The node to zoom.
     */
    void setRollOverNode(ImageNode newNode)
    {
        ImageDisplay previousNode = rollOverNode;
        rollOverNode = newNode;
        firePropertyChange(ROLL_OVER_PROPERTY, previousNode, newNode);
    }
    
    /** 
     * Returns the set with all the children of the root node. 
     * 
     * @return See above.
     */
    public Set getRootNodes() { return rootDisplay.getChildrenDisplay(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getSelectedDisplays()
     */
    public Set getSelectedDisplays() { return selectedDisplays; }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplay(ImageDisplay)
     */
    public void setSelectedDisplay(ImageDisplay node)
    {
        setSelectedDisplay(node, false);
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLastSelectedDisplay()
     */
    public ImageDisplay getLastSelectedDisplay()
    { 
        Iterator  i = selectedDisplays.iterator();
        int index = 0;
        while (i.hasNext()) {
            if (index == (selectedDisplays.size()-1)) 
                return (ImageDisplay) i.next();
            index++;
        }
        return null;  
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
     * @see Browser#setPopupPoint(java.awt.Point)
     */
    public void setPopupPoint(Point p)
    {
        thumbSelected = false;
        Object oldValue = popupPoint;
        popupPoint = p;
        firePropertyChange(POPUP_POINT_PROPERTY, oldValue, p);
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
    public Set getImages()
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
    public Set getImageNodes()
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
     * @see Browser#setSelectedLayout(int)
     */
    public void setSelectedLayout(int index)
    {
        int oldIndex = selectedLayout;
        switch (index) {
            case LayoutFactory.SQUARY_LAYOUT:
            //case LayoutFactory.TREE_LAYOUT:    
                selectedLayout = index;
                break;
            default:
                selectedLayout = LayoutFactory.SQUARY_LAYOUT;
        }
        firePropertyChange(LAYOUT_PROPERTY, new Integer(oldIndex), 
                        new Integer(selectedLayout));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getSelectedLayout()
     */
    public int getSelectedLayout() { return selectedLayout; }

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
    public void setRollOver(boolean rollOver) { this.rollOver = rollOver; }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#isRollOver()
     */
    public boolean isRollOver() { return rollOver; }
    
}
