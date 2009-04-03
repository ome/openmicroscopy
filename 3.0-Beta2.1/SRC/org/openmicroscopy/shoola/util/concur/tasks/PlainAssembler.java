/*
 * org.openmicroscopy.shoola.util.concur.tasks.PlainAssembler
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

package org.openmicroscopy.shoola.util.concur.tasks;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Collects the result of an {@link Invocation}. 
 * That is, the value returned by the {@link Invocation#call() call} method.
 * Note that the {@link #add(Object) add} method is meant to be invoked only
 * once to pass in the afore mentioned return value.  Every subsequent
 * invocation will override the previously set value.  In other terms, the
 * {@link #add(Object) add} and {@link #assemble() assemble} methods are to
 * be considered, respectively, a setter and a getter. 
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
public class PlainAssembler
    implements ResultAssembler
{
    /** The sole result. */
    private Object   result;
    
    
    /**
     * Creates a new instance.
     */
    public PlainAssembler()
    {
        result = null;
    }

    /**
     * Implemented as specified by interface.
     * Sets the result.
     * @see ResultAssembler#add(Object)
     */
    public void add(Object result)
    {   
        this.result = result;
    }

    /**
     * Implemented as specified by interface.
     * Returns the sole result.  
     * @return See above.
     * @see ResultAssembler#assemble()
     */
    public Object assemble()
    {
        return result;
    }
    
}
