/*
 * org.openmicroscopy.shoola.env.init.FakeInitializer
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

package org.openmicroscopy.shoola.env.init;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Allows to replace container's initialization tasks before the container
 * is started.
 * This is key in testing environments because intialization tasks can be 
 * replaced with tasks that provide service stubs to remove problematic 
 * dependencies on external resources.
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
public class FakeInitializer
    extends Initializer
{
    
    /**
     * Replaces the specified container's intialization tasks
     * with a new one.
     * To be effective, this method has to be called before the 
     * <code>startupInTestMode</code> method of the container is
     * invoked.
     * 
     * @param oldTaskType One of the classes defined in <code>env.init</code>.
     * @param newTaskType The class that will replace the above task during
     *                      the initialization sequence.
     * @return <code>true</code> if the old task can be replaced by the new
     *          one, <code>false</code> otherwise.
     */
    public static boolean replaceInitTask(Class oldTaskType, Class newTaskType)
    {
        if (oldTaskType == null) throw new NullPointerException();
        if (newTaskType == null) throw new NullPointerException();
        boolean replaced = false;
        for (int i = 0; i < initList.size(); ++i) {
            if (oldTaskType.equals(initList.get(i))) {
                initList.set(i, newTaskType);
                replaced = true;
                break;
            }
        }
        return replaced;
    }
    
}
