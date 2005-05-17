/*
 * org.openmicroscopy.shoola.env.data.login.LoginManager
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
import org.openmicroscopy.shoola.env.event.AgentEvent;

/** 
 * Decorates a {@link LoginService} to add thread-safety.
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
public class LoginManager
    implements LoginService
{

    /** The service's logic. */
    private LoginService    service;
    
    
    /**
     * Creates a new instance to decorate the given Login Service in order to
     * obtain a thread-safe service.
     * 
     * @param service The actual Login Service.  Mustn't be <code>null</code>.
     */
    public LoginManager(LoginService service)
    {
        if (service == null) throw new NullPointerException("No service.");
        this.service = service;
    }
    
    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#getState()
     */
    public synchronized int getState() { return service.getState(); }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#login()
     */
    public synchronized void login()
    {
        if (service.getState() == IDLE) service.login();
        //Else another thread has already hit this method, the Login Service
        //has attempted to connect but failed and brought up the Login dialog.
        //So it's now waiting for the user to enter their credentials.  The
        //Login dialog will call login(UserCredentials) when the credentials
        //have been entered.  So this thread can just return because an attempt
        //is already ongoing.
        //Note that we can assert this because *both* login methods acquire the
        //same lock and the login(UserCredentials) always leaves the state to
        //IDLE upon return.
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#login(UserCredentials)
     */
    public synchronized boolean login(UserCredentials uc) 
    { 
        return service.login(uc); 
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#eventFired(AgentEvent)
     */
    public synchronized void eventFired(AgentEvent serviceActivationRequest)
    {
        service.eventFired(serviceActivationRequest);
    }
    //NOTE: We do need to acquire the lock here, even though this method is
    //only called within the UI thread.  The reason is that we need to make
    //sure that working memories are flushed -- this is a side-effect of
    //acquiring the lock.  In fact, if another thread has called login() and
    //the attempt was successful, then the connected field in the OMEDSGateway
    //will be set to true.  Later on when the UI thread calls this method, we
    //want to be sure that the connected field has been flushed and so is
    //visible to the UI thread.  In fact, the implementation of eventFired()
    //queries (indirectly) the connected field to find out if there's a valid
    //link to OMEDS.
	
}
