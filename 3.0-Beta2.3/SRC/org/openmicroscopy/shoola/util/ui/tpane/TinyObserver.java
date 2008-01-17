/*
 * org.openmicroscopy.shoola.util.ui.tpane.TinyObserver
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
 */

package org.openmicroscopy.shoola.util.ui.tpane;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines an Observer contract for UI components that make up the 
 * {@link TinyPane}'s View.
 * The View is built by aggregating smaller UI components,
 * like buttons and icons in the title bar. Each of those usually observes one
 * bound property of the {@link TinyPane} and updates itself upon change
 * notifications. So this interface defines a 
 * method that such components implement to {@link #attach() register} 
 * with the {@link TinyPane}, the Model. After adding a sub-component to the 
 * View, this method is calledto allow the component to start observing the 
 * Model. When the component isremoved from the View, the 
 * {@link #detach() detach} method is called so that the component can remove
 * itself from the Model's change notification register. This is critical to
 * avoid memory leaks. In fact, the title bar's components (icon, buttons, etc.)
 * can be replaced multiple times during the life-cycle of a {@link TinyPane} 
 * &#151; every time a different title bar the <code>TitleBar type</code>
 * {@link TinyPane} is specified.
 * Now if those components in the old title bar didn't remove themselves from 
 * the change notification register, they would keep on being reachable from the
 * {@link TinyPane} object and, for this reason, even if logically discarded,
 * they would never be garbage-collected. 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4694 $ $Date: 2006-12-15 17:02:59 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
public interface TinyObserver
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
