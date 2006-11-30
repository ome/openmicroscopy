/*
 * org.openmicroscopy.shoola.util.concur.tasks.SyncProcessor
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
 * A concrete {@link CmdProcessor} that borrows the caller's thread to
 * execute a service.
 * That is, when one of the <code>exec</code> methods is called, no thread
 * is spawned to run the service.  Instead, the service is simply run within
 * the caller's thread.  This class typically useful when you want to test a
 * service's behavior and you need the service to run within the same thread
 * of the test driver.
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
public class SyncProcessor
    extends CmdProcessor
{

    /**
     * Creates a new instance.
     */
    public SyncProcessor() {}
    
    /** 
     * Runs the specified command.
     * 
     * @param cmd The command to run.
     * @see CmdProcessor#doExec(java.lang.Runnable)
     */
    protected void doExec(Runnable cmd)
    {
        cmd.run();
    }

}
