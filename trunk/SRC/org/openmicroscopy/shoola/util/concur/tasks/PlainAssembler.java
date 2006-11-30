/*
 * org.openmicroscopy.shoola.util.concur.tasks.PlainAssembler
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
