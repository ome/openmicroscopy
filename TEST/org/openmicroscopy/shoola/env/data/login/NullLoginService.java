/*
 * org.openmicroscopy.shoola.env.data.login.NullLoginService
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

package org.openmicroscopy.shoola.env.data.login;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.AgentEvent;

/** 
 * Implements the {@link LoginService} interface to be a Null Object, that is
 * to do nothing.
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
public class NullLoginService
    implements LoginService
{

    /**
     * @see LoginService#getState()
     */
    public int getState() { return 0; }

    /**
     * @see LoginService#login()
     */
    public void login() {}

    /**
     * @see LoginService#login(UserCredentials)
     */
    public int login(UserCredentials uc) { return 0; }

    /**
     * @see LoginService#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent serviceActivationRequest) {}

    /**
     * @see LoginService#notifyLoginFailure()
     */
	public void notifyLoginFailure() {}

    /**
     * @see LoginService#notifyLoginTimeout()
     */
	public void notifyLoginTimeout() {}

    /**
     * @see LoginService#isConnected()
     */
	public boolean isConnected() { return false; }

}
