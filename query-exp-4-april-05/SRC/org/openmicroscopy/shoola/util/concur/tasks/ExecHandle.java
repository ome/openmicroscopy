/*
 * org.openmicroscopy.shoola.util.concur.tasks.ExecHandle
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
 * Allows to cancel the execution of a service.
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
public class ExecHandle
{

    /** Points to the command that encapsulates the service workflow. */
    private ExecCommand   command;
    
    
    /**
     * Creates a new handle.
     * This handle is temporary invalid because it points to no command.
     * 
     * @see #setCommand(ExecCommand)
     */
    ExecHandle() {}
    
    /**
     * Links this handle to the specified service.
     * 
     * @param cmd   The command that encapsulates the service workflow.
     *              Must be a valid reference.
     */
    void setCommand(ExecCommand cmd) 
    {
        if (cmd == null) 
            throw new NullPointerException("No command.");
        command = cmd;
    }
    
    /**
     * Interrupts the service execution.  
     * Whether execution is actually cancelled depends on the state of the
     * computation at the point when the cancellation signal is received.
     */
    public void cancelExecution()
    {
        command.cancel();
    }
    

/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */
    
    ExecCommand getCommand() { return command; }
    
}
