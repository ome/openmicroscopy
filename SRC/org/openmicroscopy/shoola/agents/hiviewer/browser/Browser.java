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
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies

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
{

    /** 
     * Bound property name indicating an {@link ImageDisplay} object has been
     * selected in the visualization tree. 
     */
    public static final String SELECTED_DISPLAY_PROPERTY = "selectedDisplay";
    
    /** 
     * Bound property name indicating a click occurred at a given point within
     * the browser component.
     */
    public static final String CLICK_POINT_PROPERTY = "clickPoint";
    
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
     * Returns all the hierarchy objects that are linked to the any of the
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
     * Returns the node, if any, that is currently selected in the 
     * visualization tree.
     * 
     * @return The currently selected node or <code>null</code> if no node
     *          is currently selected.
     */
    public ImageDisplay getSelectedDisplay();
    
    /**
     * Sets the point at which the last click occurred within the browser
     * component.
     * 
     * @param p The point at which the event occurred.
     */
    public void setClickPoint(Point p);
    
    /**
     * Returns the point at which the last click occurred within the browser
     * component.
     * This method may return <code>null</code>, for example if no click has
     * occurred yet.
     * 
     * @return The point at which the event occurred.
     */
    public Point getClickPoint();
    
    /**
     * Sets the point at which the last pop-up trigger event occurred within 
     * the browser component.
     * 
     * @param p The point at which the event occurred.
     */
    public void setPopupPoint(Point p);
    
    /**
     * Returns the point at which the last pop-up trigger event occurred within 
     * the browser component.
     * This method may return <code>null</code>, for example if no such an
     * event has occurred yet.
     * 
     * @return The point at which the event occurred.
     */
    public Point getPopupPoint();
    
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
    
    /**
     * Registers an observer with the browser component.
     * The observer will be notified of every bound property change.
     * 
     * @param observer The observer to register.
     * @throws NullPointerException If <code>observer</code> is 
     *                              <code>null</code>.
     * @see #removePropertyChangeListener(PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener observer);
    
    /**
     * Removes an observer from the change notification list.
     * 
     * @param observer The observer to remove.
     * @throws NullPointerException If <code>observer</code> is 
     *                              <code>null</code>.
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener observer);
    
    /**
     * Registers an observer with the browser component.
     * The observer will be notified of every change to the specified
     * bound property.
     * 
     * @param propertyName One of the property strings defined by this 
     *                      interface.
     * @param observer The observer to register.
     * @throws NullPointerException If <code>propertyName</code> or 
     *                              <code>observer</code> is 
     *                              <code>null</code>.
     * @see #removePropertyChangeListener(String, PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener observer);
    
    /**
     * Removes an observer from the change notification list for the specified
     * bound property.
     * Note that the observer will still receive notification of other property
     * changes if it has registered for other properties.
     * 
     * @param propertyName One of the property strings defined by this 
     *                      interface.
     * @param observer The observer to remove.
     * @throws NullPointerException If <code>propertyName</code> or 
     *                              <code>observer</code> is 
     *                              <code>null</code>.
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener observer);
    
}
