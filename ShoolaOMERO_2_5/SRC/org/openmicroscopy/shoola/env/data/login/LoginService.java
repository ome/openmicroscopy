/*
 * org.openmicroscopy.shoola.env.data.login.LoginService
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
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
 * Defines the high-level functionality required to log onto <i>OMEDS</i>.
 * <p>The Login Service provides an abstraction over the process of logging
 * onto <i>OMEDS</i>.  This service is only used directly by the Container
 * either during initialization or when an invalid link is detected to try and 
 * reestablish it &#151; this could happen because the connection went down,
 * the session expired, or the user has an invalid login.  In fact, during the
 * initialization procedure the user is asked to enter their credentials, which
 * the Initializer then passes along to the Login Service so to establish the
 * initial link to the server.  Then whenever a method in the Data Services
 * raises an exception due to an invalid link, the Data Services throw a <code>
 * DSOutOfServiceException</code> to inform the caller (typically an Agent) and
 * ask the Login Service to reestablish the broken link.  At which point, the
 * Login Service attempts to reconnect to <i>OMEDS</i> using the current user's
 * credentials &#151; that is, the credentials that the user entered during the
 * initialization procedure.  So many attempts are made as specified in the
 * Container's configuration file.  If all attempts fail, then a dialog is
 * brought up on screen to allow the user to reenter their credentials (which
 * now become the current credentials) and a last attempt is made.  If this
 * fails too, then the Login Service gives up and informs the user through an
 * error dialog.</p>
 * <p>Because the Container, through the Login Service, transparently manages
 * the connection to <i>OMEDS</i>, Agents never need to make an explicit call
 * to the Login Service.  However, a mechanism is provided so that Agents can
 * be notified if the link to <i>OMEDS</i> is reestablished.  This allows an
 * Agent to gracefully recover from a <code>DSOutOfServiceException</code>.
 * It works as follows.  The Agent registers with the <code>Event Bus</code>
 * for <code>ServiceActivationResponse</code>s and then catches a <code>
 * DSOutOfServiceException</code> to post a <code>ServiceActivationRequest.
 * </code>  This will be responded by the Login Service with a <code>
 * ServiceActivationResponse</code>, whose <code>isActivationSuccessful</code>
 * method can be used to find out if the link has been reestablished.  If so,
 * the Agent can retry the original call to the Data Services.  Note that if
 * the Agent implements this for more than one Data Services method, then
 * upon receipt of a <code>ServiceActivationResponse</code> some sort of
 * de-multiplexing could be required to find out what was the method that
 * originally raised the <code>DSOutOfServiceException</code> &#151; assuming
 * the Agent wants to retry that call.  Instead of writing endless <code>if-
 * else</code> or <code>switch</code> blocks, you may want to consider the
 * fact that a <code>ServiceActivationResponse</code> works like an Asynchronous
 * Completion Token (ACT).  Here's a code example that illustrates a possible 
 * recovery strategy implemented by a fictitious Agent and how to take advantage
 * of ACTs:</p>
 * <p><code><pre>
 * public class MyAgent
 *     implements Agent, AgentEventListener
 * {   
 *     private Registry    context;
 * 
 *     public void setContext(Registry ctx)
 *     {
 *         this.context = ctx;
 *         
 *         //Register with the Event Bus for ServiceActivationResponses
 *         EventBus bus = ctx.getEventBus();
 *         bus.register(this, ServiceActivationResponse.class);
 *     }
 * 
 *     public void displayProjects()
 *     {
 *         DataManagementService dms = context.getDataManagementService();
 *         try {
 *             List projects = dms.retrieveUserProjects();
 *             
 *             //Display the projects in some UI widget...
 *   
 *         } catch (DSOutOfServiceException e) {
 *             //For some reason our link to OMEDS is not valid at the moment.
 *             //The Container is working under the hood to reestablish a valid
 *             //link, but we want to know when and if the Container succeeds.
 *             //So we're going to post a ServiceActivationRequest.
 *             ServiceActivationRequest req = new ServiceActivationRequest(
 *                                      ServiceActivationRequest.DATA_SERVICES);
 *             req.setSource(this);
 * 
 *             //Because we want to retry the call in the case the Container
 *             //manages to reestablish a valid link, we set a CompletionHanlder
 *             //which will be dispatched when we receive the response.
 *             //(See eventFired below.)
 *             req.setCompletionHandler(new CompletionHandler() {
 *                 public void handle(RequestEvent request, 
 *                                    ResponseEvent response)
 *                 {
 *                     ServiceActivationResponse sar = 
 *                                         (ServiceActivationResponse) response;
 *                     if (sar.isActivationSuccessful()) 
 *                         //We have a valid link to OMEDS.  Retry the call.
 *                         displayProjects();
 *                 }
 *             });
 *             
 *             //Now we can post the request.  The Container will notify us of
 *             //the outcome of its attempts to reestablish a link with a 
 *             //ServiceActivationResponse.  (See eventFired below.)
 *             EventBus bus = context.getEventBus();
 *             bus.post(req);
 * 
 *         } catch (DSAccessException e) {
 *             //Tell the user to retry...
 *         }
 *     }
 * 
 *     public void displayDatasets() 
 *     {
 *         //Similar to displayProjects...
 *     }
 *     
 *     public void eventFired(AgentEvent ae)
 *     {
 *         if (ae instanceof ServiceActivationResponse) {
 *             ServiceActivationResponse sar = (ServiceActivationResponse) ae; 
 *             if (sar.getSource() == this)  //This was our request.
 *                 sar.complete();
 *         }
 *         //The complete method will eventually invoke the CompletionHandler.
 *         //Note how we hanlde *all* cases (displayProjects, displayDatasets, 
 *         //etc.) with a *single* statement.
 *         //Also note that we make sure we only hanlde requests that this
 *         //Agent posted.  In fact, multiple ServiceActivationResponses could
 *         //be travelling on the Event Bus if more than one Agent posted
 *         //ServiceActivationRequests.
 *     }
 * 
 * 
 *  //Other methods...
 * 
 * }  //End of MyAgent.
 * </pre></code></p>
 * <p>The Login Service is thread-safe.  If multiple threads attempt to login
 * at the same time, then only one is allowed to proceed as all the others will
 * have to wait for the outcome of the login attempt.  (Note that because a
 * <code>DataServiceView</code> usually runs asynchronously with respect to the
 * UI, it's possible that two or more concurrent calls to the Data Services fail
 * at approximately the same time, which would result in concurrent calls to the
 * Login Service.)</p>
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
public interface LoginService
    extends AgentEventListener
{

    /**
     * Flag to denote the Idle state.
     * While in this state, the Login Service is waiting for a login request.
     */
    public static final int     IDLE = 0;
    
    /**
     * Flag to denote the Attempting Login state.
     * While in this state, the Login Service is carrying out a login request.
     */
    public static final int     ATTEMPTING_LOGIN = 1;
    
    
    /**
     * Reuturns the current state of the Login Service.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Attempts to login using the current user's credentials.
     * If the user hasn't entered their login credentials yet, then this method
     * just returns without attempting a connection to <i>OMEDS</i>.  
     * Otherwise, the most recently entered user's credentials are used to 
     * attempt establishing a valid link to the server.
     * Upon failure, this method retries for how many times as specified by the
     * Container's configuration.  (The time interaval between each attempt is
     * also specified by the Container's configuration.)  If all attempts fail,
     * then a dialog is brought up on screen to allow the user to reenter their
     * credentials (which now become the current credentials) and a last attempt
     * is made.  If this fails too, then this method gives up and informs the
     * user through an error dialog.
     */
    public void login();
    
    /**
     * Attempts to login using the specified user's credentials.
     * If <code>uc</code> is <code>null</code>, then then this method just
     * returns without attempting a connection to <i>OMEDS</i>.  Otherwise,
     * <code>uc</code> becomes the current user's credentials and an attempt
     * is made to establish a link to the server.  If the attempt fails, the
     * user is informed through an error dialog.
     *  
     * @param uc The new user's credentials.
     * @return <code>true</code> upon success, <code>false</code> in the case
     *         of failure.
     */
    public boolean login(UserCredentials uc);
    
    /**
     * Implemented to receive notification of a <code>ServiceActivationRequest
     * </code> coming from an Agent.
     * A <code>ServiceActivationResponse</code> is posted back to inform the
     * Agent about the current login state.  That is, if there's a valid link
     * to the server.
     * 
     * @param serviceActivationRequest A <code>ServiceActivationRequest</code>
     *                                 posted by an Agent.
     */
    public void eventFired(AgentEvent serviceActivationRequest);
    
}
