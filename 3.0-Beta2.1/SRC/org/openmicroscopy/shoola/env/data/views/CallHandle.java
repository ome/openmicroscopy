/*
 * org.openmicroscopy.shoola.env.data.views.CallHandle
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.concur.tasks.ExecHandle;

/** 
 * Handle to an asynchronous call to the data services.
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
public class CallHandle
{

/* NOTE:  A CallHandle is returned to clients by every call to a data service
 * view.  We wrap an ExecHandle with this class so to make clients of a data
 * service view completely independent of the util.concur lib which is used
 * under the hood for async exec.  
 */    
    
    /** Allows this object to work just like an {@link ExecHandle}. */
    private ExecHandle  delegate;
    
    
    /**
     * Creates a new instance.
     * The new instance is configured with the specified handle to an
     * asynchronous call. 
     * 
     * @param delegate The actual handle.  Mustn't be <code>null</code>.
     */
    CallHandle(ExecHandle delegate)
    {
        if (delegate == null) throw new NullPointerException("No delegate.");
        this.delegate = delegate;
    }
    
    /**
     * Interrupts the call execution.  
     * Whether execution is actually cancelled depends on the state of the
     * computation at the point when the cancellation signal is received.
     */
    public void cancel() { delegate.cancelExecution(); }
    
}
