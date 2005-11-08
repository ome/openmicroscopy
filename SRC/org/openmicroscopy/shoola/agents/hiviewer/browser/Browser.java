/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.Browser
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
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface provided by the browser component.
 * The browser provides a <code>JComponent</code> to host and display one or
 * more visualization trees.  That is, one or more {@link ImageDisplay} top
 * nodes, each representing an image hierarchy.
 * Use the {@link BrowserFactory} to create an object implementing this 
 * interface.
 * 
 * @see ImageDisplay
 * @see BrowserFactory
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
public interface Browser
    extends ObservableComponent
{

    /** 
     * Bound property name indicating an {@link ImageDisplay} object has been
     * selected in the visualization tree. 
     */
    public static final String SELECTED_DISPLAY_PROPERTY = "selectedDisplay";
    
    /** 
     * Bound property name indicating a {@link Thumbnail} has been selected
     * within an {@link ImageNode}.
     * The associated property change event is always dispatched <i>after</i>
     * dispatching the one for the {@link #SELECTED_DISPLAY_PROPERTY}.  This
     * latter property will be set to the {@link ImageNode} the 
     * {@link Thumbnail} belong in.
     */
    public static final String THUMB_SELECTED_PROPERTY = "thumbSelected";
    
    /** 
     * Bound property name indicating a pop-up trigger event occurred at a 
     * given point within the browser component. 
     */
    public static final String POPUP_POINT_PROPERTY = "popupPoint";

    
    /**
     * Sets the specified <code>node</code> to be the currently selected
     * node in the visualization tree.
     * Set it to <code>null</code> to indicate no node is currently selected.
     *  
     * @param node The node to become the currently selected node.
     */
    public void setSelectedDisplay(ImageDisplay node);
    
    /**
     * Returns the node, if any, that is currently selected in the 
     * visualization tree.
     * 
     * @return The currently selected node or <code>null</code> if no node
     *          is currently selected.
     */
    public ImageDisplay getSelectedDisplay();
    
    /**
     * Sets a flag to indicate if a {@link Thumbnail} has been selected
     * within an {@link ImageNode}.
     * 
     * @param selected Pass <code>true</code> if the currently selected display
     *                 is an {@link ImageNode} and its {@link Thumbnail} has
     *                 been selected.  Pass <code>false</code> in any other
     *                 case.
     * @throws IllegalArgumentException If you pass <code>true</code> but the
     *         currently selected display is <i>not</i> an {@link ImageNode}.
     */
    public void setThumbSelected(boolean selected);
    
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
     *          cooordinates of the currently selected display.
     */
    public void setPopupPoint(Point p);
    
    /**
     * Returns the point at which the last pop-up trigger event occurred within 
     * the browser component.
     * This method may return <code>null</code>, for example if no such an
     * event has occurred yet or if a thumbnail has been selected &#151; these
     * two events are mutually exclusive.
     * 
     * @return The point at which the event occurred, <i>relative</i> to the 
     *         cooordinates of the currently selected display.
     */
    public Point getPopupPoint();
    
    /**
     * Returns all the hierarchy objects that are linked to any of the
     * {@link ImageNode}s in the visualization trees hosted by the browser.
     * 
     * @return A set of <code>Object</code>s.
     */
    public Set getImages();
    
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
     * @return A set of {@link ImageDisplay} objects.
     */
    public Set getRootNodes();
    
    /**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor The visitor.  Mustn't be <code>null</code>.
     * @see ImageDisplayVisitor
     */
    public void accept(ImageDisplayVisitor visitor);
    
    /**
     * Returns the widget that displays all the visualization trees hosted
     * by the browser.
     *  
     * @return The browser widget.
     */
    public JComponent getUI();
    
}
