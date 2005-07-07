/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrameObserver
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines an Observer contract for UI components that make up the 
 * {@link TinyFrame}'s View.
 * The {@link TinyFrame}'s View is built by aggregating smaller UI components,
 * like buttons and icons in the title bar.  Each of those usually observes one
 * bound property of the {@link TinyFrame} and updates itself upon change
 * notifications.  So this interface defines a method that such components
 * implement to {@link #attach(TinyFrame) register} with the {@link TinyFrame},
 * the Model.  After adding a sub-component to the View, this method is called
 * to allow the component to start observing the Model.  When the component is
 * removed from the View, the {@link #detach(TinyFrame) detach} method is
 * called so that the component can remove itself from the Model's change
 * notification register.  This is critical to avoid memory leaks.  In fact,
 * the title bar's components (icon, buttons, etc.) can be replaced multiple
 * times during the life-cycle of a {@link TinyFrame} &#151; every time a 
 * different title bar {@link TinyFrame#setTitleBarType(int) type} is specified.
 * Now if those components in the old title bar didn't remove themselves from 
 * the change notification register, they would keep on being reachable from the
 * {@link TinyFrame} object, and, for this reason, even if logically discarded,
 * they woould never be garbage-collected. 
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
public interface TinyFrameObserver
{
    
    /**
     * Registers with the Model for change notification.
     * This method is called just after the UI component is added to the View.
     */
    void attach();
    
    /**
     * Removes this UI component from the Model's change notification register.
     * This method is called just before the UI component is discarded. 
     */
    void detach();
    
}
