/*
 * org.openmicroscopy.shoola.util.ui.component.AbstractComponent
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

package org.openmicroscopy.shoola.util.ui.component;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Convenience class from which components can inherit to gain support for
 * change propagation.
 * This class implements the {@link ObservableComponent} and is an abstract
 * publisher that maintains a registry of currently-subscribed observers.
 * A subclass automatically becomes a <i>source bean</i> and uses the
 * {@link #firePropertyChange(String, Object, Object) firePropertyChange} to
 * trigger notification of <i>bound property</i> changes.
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
public abstract class AbstractComponent
    implements ObservableComponent
{

    /** Change notification registry for change observers. */
    private Set         changeRegistry;
    
    /** Used for all change notifications coming from this publisher. */
    private ChangeEvent changeEvent;
    
    /** Change notification registry for bound properties observers. */
    private Map         propsRegistry;
    
    /** 
     * Key used to look up observers in the {@link #propsRegistry} that 
     * registred for all bound properties. 
     */
    private Object      allPropsKey;
    
    
    /**
     * Initializes the notification registries.
     */
    protected AbstractComponent() 
    { 
        changeRegistry = new HashSet();
        changeEvent = new ChangeEvent(this);
        propsRegistry = new HashMap();
        allPropsKey = new Object();
    }
    
    /**
     * Supports reporting any state change.
     * All change listeners will be notified.
     */
    protected void fireStateChange()
    {
        ChangeListener observer;
        Iterator i = changeRegistry.iterator();
        while (i.hasNext()) {
            observer = (ChangeListener) i.next();
            observer.stateChanged(changeEvent);
        }
    }
    
    /**
     * Supports reporting bound property changes.
     * Listeners will be notified only if <code>newValue</code> is not the
     * same as <code>oldValue</code>.  In this case all state change listeners
     * will be notified too, as state has changed.
     * 
     * @param propertyName  The property that has been set.
     * @param oldValue      The previous value of the property.
     * @param newValue      The value that has just been set.
     */
    protected void firePropertyChange(String propertyName, 
                                      Object oldValue, Object newValue) 
    {
        //Make sure we've got an actual state change.
        if (oldValue == null && newValue == null) return;
        if (oldValue != null && newValue != null && oldValue.equals(newValue))
            return;
        
        //Build the union of the all-properties observers and propertyName 
        //observers sets.  This way we avoid repeated notification.
        Set allPropsListeners = (Set) propsRegistry.get(allPropsKey),
            propListeners = (Set) propsRegistry.get(propertyName),
            notificationSet = new HashSet();
        if (allPropsListeners != null) 
            notificationSet.addAll(allPropsListeners);
        if (propListeners != null) notificationSet.addAll(propListeners);
        
        //Do the stateless notification, then just return if there are no 
        //observers for the stateful notification.
        fireStateChange();
        if (notificationSet.size() == 0) return;
        
        //Ok then, we've got observers.  Let's create the event and then
        //dispatch it.
        PropertyChangeEvent event = new PropertyChangeEvent(this, 
                                              propertyName, oldValue, newValue);
        PropertyChangeListener observer;
        Iterator i = notificationSet.iterator();
        while (i.hasNext()) {
            observer = (PropertyChangeListener) i.next();
            observer.propertyChange(event);
        }
    }
    
    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#addChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        changeRegistry.add(observer);
    }
    
    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#removeChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        changeRegistry.remove(observer);
    }
    
    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#addPropertyChangeListener(
     *      PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        Set allPropsListeners = (Set) propsRegistry.get(allPropsKey);
        if (allPropsListeners == null) {
            allPropsListeners = new HashSet();
            propsRegistry.put(allPropsKey, allPropsListeners);
        }
        allPropsListeners.add(observer);
    }

    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#removePropertyChangeListener(
     *      PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        Set allPropsListeners = (Set) propsRegistry.get(allPropsKey);
        if (allPropsListeners != null) allPropsListeners.remove(observer);
    }

    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#addPropertyChangeListener(String,
     *      PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener observer)
    {
        if (propertyName == null) 
            throw new NullPointerException("No property name.");
        if (observer == null) throw new NullPointerException("No observer.");
        Set propListeners = (Set) propsRegistry.get(propertyName);
        if (propListeners == null) {
            propListeners = new HashSet();
            propsRegistry.put(propertyName, propListeners);
        }
        propListeners.add(observer);
    }

    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#removePropertyChangeListener(String, 
     *      PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener observer)
    {
        if (propertyName == null) 
            throw new NullPointerException("No property name.");
        if (observer == null) throw new NullPointerException("No observer.");
        Set propListeners = (Set) propsRegistry.get(propertyName);
        if (propListeners != null) propListeners.remove(observer);
    }

}
