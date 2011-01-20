/*
 * org.openmicroscopy.shoola.env.data.views.MonitorFactory
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
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.util.concur.tasks.ExecMonitor;

/** 
 * A Factory to create {@link ExecMonitor}s to observe the execution of a
 * {@link BatchCallTree}.
 *   
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
public class MonitorFactory
{
    
    /**
     * Returns an implementation of {@link ExecMonitor} that works as an 
     * adapter to notify the specified <code>observer</code> of execution
     * events. 
     * Specifically, the returned adapter will notify the <code>observer</code>
     * of the <code>tree</code>'s execution progress and of the eventual
     * outcome of the computation.
     * 
     * @param tree The computation tree to observe.
     * @param observer The adaptee.
     * @return The adapter, an instance of {@link BatchCallMonitor}.
     */
    public ExecMonitor makeNew(BatchCallTree tree, AgentEventListener observer)
    {
        return new BatchCallMonitor(tree, observer);
    }
    
}
