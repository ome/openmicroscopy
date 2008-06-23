package ui;

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public abstract class AbstractComponent
	implements ObservableComponent
{

	/** Change notification registry for change observers. */
	private Set         changeRegistry;
	
    /** Used for all change notifications coming from this publisher. */
    private ChangeEvent changeEvent;

	public AbstractComponent() {
		changeRegistry = new HashSet();
		changeEvent = new ChangeEvent(this);
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
}
