/*
 * org.openmicroscopy.shoola.env.event.StateChangeEvent
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

package org.openmicroscopy.shoola.env.event;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Abstract class from which agents derive concrete classes to represent static 
 * change notifications.
 * The <code>stateChange</code> field can be used to carry all state-change 
 * informations.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class StateChangeEvent
    extends AgentEvent
{
        
    /** Carries all state-change information. */
    private Object stateChange;
    
    /**
     * Sets the <code>stateChange</code> object.
     * 
     * @param stateChange The object to set.
     */
    public void setStateChange(Object stateChange)
    { 
        this.stateChange = stateChange;
    }
    
    /**
     * Returns the <code>stateChange</code> obejct.
     * 
     * @return See above.
     */
    public Object getStateChange() { return stateChange; }
      
}
