/*
 * org.openmicroscopy.shoola.util.concur.tasks.UncaughtExcHandler
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
 * Handles an uncaught exception that occurred during the execution of a 
 * service.
 * Concrete {@link CmdProcessor}s allows you to register such handlers so
 * that you can have a chance to catch those exceptions that escaped from
 * the regular exception handling done during the execution of a service.
 * Notice that if you registered an {@link ExecMonitor} with a service, then
 * any uncaught exception occurred during the execution of the service will
 * be delivered to the <code>onAbort</code> method.  So, in this case, the
 * only uncaught exceptions that can come from that service are 
 * {@link RuntimeException}s thrown by the <code>onAbort</code> method itself
 * or by the <code>onCancel</code> method in the case of cancellation.
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
public interface UncaughtExcHandler
{

    /**
     * Called by a concrete {@link CmdProcessor} if an exception goes uncaught
     * during the execution of a service.
     * 
     * @param t The uncaught exception.
     */
    public void handle(Throwable t);
    
}
