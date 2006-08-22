/*
 * org.openmicroscopy.shoola.env.data.login.BatchLoginService
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

package org.openmicroscopy.shoola.env.data.login;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;

/** 
 * Provides a <code>LoginService</code> that doesn't need to interact with
 * the user to obtain login credentials.
 * This is useful in test mode as we have no interaction with the user and
 * the credentials are specified before hand.
 * Note that although this class gets rid of the login dialog, the UI might
 * still pop up if you don't link the Container to a <code>NullUserNotifier
 * </code>.  (This is because the parent class makes use of that notification
 * service.) 
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
public class BatchLoginService
    extends LoginServiceImpl
{

    /**
     * Overridden to log in with the current user's credentials.
     * This avoids the need to bring up a dialog to let the user enter
     * their credentials.  In fact, in test mode there's no interaction
     * with the user and the credentials are specified before hand. 
     */
    protected void askForCredentials() { login(config.getCredentials()); }
    
    /**
     * Creates a new instance.
     * It is assumed that the Container's configuration has already been read
     * in and that the Event Bus is available.
     * 
     * @param c Reference to the runtime environment.
     *          Mustn't be <code>null</code>.
     */
    public BatchLoginService(Container c) { super(c); }
    
}
