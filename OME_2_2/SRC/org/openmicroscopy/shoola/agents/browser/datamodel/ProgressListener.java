/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.DataLoaderListener
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.datamodel;

/**
 * An interface that specifies a listener to the an incremental process.
 * Classes that implement this interface will receive events as to the progress
 * of a particular event.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface ProgressListener
{
    /**
     * Specifies that execution of a process is about to start, with the
     * specified number of subtasks.
     * @param piecesOfData The number of subtasks
     */
    public void processStarted(int piecesOfData);
    
    /**
     * Specifies that execution of a process has advanced by an incremental
     * step, with the specified information about exactly what happened.  Use
     * the tokens <code>%n</code> and <code>%t</code> in the strings to replace
     * those tokens with the number step and the total number of steps in
     * the error message (this is carried out by ProgressMessageFormatter)
     * 
     * @param info Information about what was loaded.
     */
    public void processAdvanced(String info);
    
    /**
     * Indicates that the process has completed successfully.
     */
    public void processSucceeded(String successMessage);
    
    /**
     * Indicates that the process has terminated with the specified
     * failure notification.
     */
    public void processFailed(String reason);
}
