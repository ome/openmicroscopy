/*
 * org.openmicroscopy.shoola.agents.hiviewer.ObservableComponent
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the interface that a component, like the <code>HiViewer</code> and 
 * the <code>Browser</code>, usually implement to let other components observe
 * state changes of its properties.
 * <p>This interface requires implementing classes to support both a <i>push</i> 
 * and <i>pull</i> model, based on the <i>JavaBeans</i> and <i>Swing</i> APIs.
 * In the <i>push</i> model, the publisher (that is the object implementing 
 * this interface) sends all changed data when it notifies the subscribers 
 * &#151; or observers, that is <i>listeners</i> in <i>JavaBeans</i> terms.
 * Implementing classes are required to support this model through <i>Java 
 * Beans bound properties</i>.  In the <i>pull</i> model, just a change 
 * notification is sent off and subscribers subsequently query the publisher
 * about the state change.  Implementing classes are required to support this
 * model with <i>Swing</i> light-weight event notifications.</p>
 * <p>Implementing classes are required to avoid dispatching the same event to
 * the same observer more than once.  That is, even if an observer registers
 * twice for a given event type, it only gets notified once per each occurrence
 * of said event type.  This also implies that if an observer registers for
 * all <i>bound</i> properties and for a specific one <code>P</code> at the
 * same time, then every time <code>P</code> changes the observer has to get
 * only <i>one</i> notification.</p>  
 *
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
public interface ObservableComponent
{

    /**
     * Registers an observer with this component.
     * The observer will be notified of <i>every</i> state change.
     * 
     * @param observer The observer to register.
     * @throws NullPointerException If <code>observer</code> is 
     *                              <code>null</code>.
     * @see #removeChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener observer);
    
    /**
     * Removes an observer from the change notification list.
     * 
     * @param observer The observer to remove.
     * @throws NullPointerException If <code>observer</code> is 
     *                              <code>null</code>.
     * @see #addChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener observer);
    
    /**
     * Registers an observer with this component.
     * The observer will be notified of every <i>bound property</i> change.
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
     * Registers an observer with this component.
     * The observer will be notified of every change to the specified
     * <i>bound property</i>.
     * 
     * @param propertyName The name of the property.  This is usually defined
     *                      in a <code>public static</code> field by the
     *                      implementing component.
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
     * <i>bound property</i>.
     * Note that the observer will still receive notification of other property
     * changes if it has registered for other properties.
     * 
     * @param propertyName The name of the property.  This is usually defined
     *                      in a <code>public static</code> field by the
     *                      implementing component.
     * @param observer The observer to remove.
     * @throws NullPointerException If <code>propertyName</code> or 
     *                              <code>observer</code> is 
     *                              <code>null</code>.
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener observer);
    
}
