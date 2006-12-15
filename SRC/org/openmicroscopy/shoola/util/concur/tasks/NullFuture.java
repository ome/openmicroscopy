/*
 * org.openmicroscopy.shoola.util.concur.tasks.NullFuture
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
 * Overrides the methods of {@link Future} to provide a no-op implementation.
 * This way we can use a <code>NullFuture</code> in place of {@link Future}
 * and, at the same time, have it work like an {@link ExecHandle}.
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
class NullFuture
    extends Future
{

    /** Creates a no-op {@link Future}. */
    NullFuture() {}
    
    /** 
     * No-op implementation. 
     * @see Future#setResult(Object)
     */
    void setResult(Object r) {}
    
    /** 
     * No-op implementation. 
     * @see Future#setException(Throwable)
     */
    void setException(Throwable t) {}
    
    /** 
     * No-op implementation. 
     * @see Future#getResult()
     */
    public Object getResult() { return null; }
   
    /** 
     * No-op implementation. 
     * @see Future#getResult(long)
     */
    public Object getResult(long msec) { return null; }
    
}
