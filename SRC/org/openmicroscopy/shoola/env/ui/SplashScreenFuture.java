/*
 * org.openmicroscopy.shoola.env.ui.SplashScreenFuture
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

package org.openmicroscopy.shoola.env.ui;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A basic Future used within the {@link SplashScreen} component to retrieve
 * <code>UserCredentials</code>.
 * This object can only be used to collect one result and should then be
 * discarded. 
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
class SplashScreenFuture
{
    
    /** The result that this Future will collect. */
    private Object      result;
    
    /** Tells whether or not the result has been set. */
    private boolean     isFilledIn;
    
    /** Creates a new instance. */
    SplashScreenFuture()
    { 
        isFilledIn = false;
    }
    
    /**
     * Sets the result this Future was created to collect.
     * This method is thread-safe and awakes threads that were suspended
     * on {@link #get()}.
     * 
     * @param result  The result.
     */
    synchronized void set(Object result)
    {
        this.result = result;
        isFilledIn = true;
        notify();
    }
    
    /**
     * Waits until the result has been filled in and then returns it.
     * 
     * @return  The result object.
     * @see #set(Object)
     */
    synchronized Object get()
    {
        try {
            while (!isFilledIn) wait(); 
        } catch (InterruptedException ie) {
            //Ignore, not relevant in our case.
        }
        return result;
    }
    
}
