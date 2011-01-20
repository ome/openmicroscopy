/*
 * org.openmicroscopy.shoola.util.concur.ControlFlowObserver
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

package org.openmicroscopy.shoola.util.concur;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Provides a testing contract for classes designed to operate in a 
 * multi-threaded environment.
 * Each class in the <code>concur</code> packages should define "check points"
 * in the object life-line at which execution can be manipulated in order to
 * simulate race conditions, resource contemption, etc.  When a check point is
 * reached observers are notified &151; observers implement this interface. 
 * This allows testing code to coordinate multiple threads that are in a given
 * object at the same time.
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
public interface ControlFlowObserver
{

    /**
     * Notifies an observer that a given check point has been reached.
     *
     * @param checkPointID  Identifies the check point.  Normally defined as
     *                      a <code>static</code> constant by the monitored
     *                      class.
     */
    void update(int checkPointID);
    
}
