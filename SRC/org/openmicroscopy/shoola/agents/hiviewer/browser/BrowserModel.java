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
    
    /** The currently selected node in the visualization tree. */
    private ImageDisplay    selectedDisplay;
    
    /** 
     * Tells if a thumbnail has been selected in the case the 
     * {@link #selectedDisplay} is an {@link ImageNode}. 
     */
    private boolean         thumbSelected;
    
    /** Position of the last pop-up trigger within the browser. */
    private Point           popupPoint;
    
    /** Contains all visualization trees, our View. */
    private RootDisplay     rootDisplay;
    
    /** The index of the selected layout. */
    private int             selectedLayout;
    
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
    }
    
    /** 
     * Returns the set with all the children of the root node. 
     * 
     * @return See above.
     */
    public Set getRootNodes() { return rootDisplay.getChildrenDisplay(); }
    
    /**
     * String-ifies the path from the {@link #selectedDisplay} to the
     * {@link #rootDisplay}.
     * 
     * @return The above described string.
     */
    String currentPathString()
    {
        StringBuffer buf = new StringBuffer();
        String title;
        ImageDisplay parent = selectedDisplay;
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
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplay(ImageDisplay)
     */
    public void setSelectedDisplay(ImageDisplay node)
    {
        thumbSelected = false;
        popupPoint = null;
        Object oldValue = selectedDisplay;
        selectedDisplay = node;
        firePropertyChange(SELECTED_DISPLAY_PROPERTY, oldValue, node);
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getSelectedDisplay()
     */
    public ImageDisplay getSelectedDisplay() { return selectedDisplay; }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setThumbSelected(boolean)
     */
    public void setThumbSelected(boolean selected)
    {
        if (!(selectedDisplay instanceof ImageNode) && selected)
            throw new IllegalArgumentException(
                "Can only select a thumbnail on an ImageNode.");
        popupPoint = null;
        Boolean oldVal = thumbSelected ? Boolean.TRUE : Boolean.FALSE,
                newVal = selected ? Boolean.TRUE : Boolean.FALSE;
        thumbSelected = selected;
        firePropertyChange(THUMB_SELECTED_PROPERTY, oldVal, newVal);
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

}
