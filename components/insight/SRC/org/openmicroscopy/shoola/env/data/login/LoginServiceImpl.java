/*
 * org.openmicroscopy.shoola.env.data.login.LoginServiceImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;

import Glacier2.PermissionDeniedException;
import Ice.ConnectionRefusedException;
import Ice.DNSException;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import omero.SecurityViolation;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.NotificationDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Implements the Login Service's logic.
 * This class ignores threading issues altogether and just focuses on providing
 * the service's logic.  The {@link LoginManager} should be used to decorate an
 * instance of this class to obtain a thread-safe service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class LoginServiceImpl
    implements LoginService
{

    /** 
     * Holds one of the state flags defined by the {@link LoginService}
     * interface to indicate the current state of the service.
     */
    private int state;

    /** Reference to the runtime environment. */
    private Container container;

    /** 
     * The timer used to establish a valid link to an <code>OMERO</code>
     * server.
     */
    private Timer timer;

    /** Flag indicating if the attempt to connect has started. */
    private boolean connAttempt;

    /** The index set if an error occurred while trying to connect. */
    private int failureIndex;

    /** Allows to easily access the service's configuration. */
    protected LoginConfig config;

    /**
     * Suspends execution for as many milliseconds as specified by the
     * service's configuration.
     */
    private void pause()
    {
        try {
            Thread.sleep(config.getRetryInterval());
        } catch (InterruptedException e) {}
    }

    /**
     * Attempts to log onto <i>OMERO</i> using the current user's
     * credentials.
     * Failure or success is reported to the Log Service.
     * 
     * @return <code>true</code> on success, <code>false</code> on failure.
     */
    private int attempt()
    {
        UserCredentials uc = config.getCredentials();
        
        try {
        	if (uc == null) 
                throw new DSOutOfServiceException(
                                "No user's credentials have been entered yet.");
            //NOTE: This will never happen if the Splash Screen always blocks
            //waiting for the user's credentials and the login dialog never
            //passes null credentials along.
            
            DataServicesFactory factory = DataServicesFactory.getInstance(
                                                                    container);
            if (factory.isConnected()) return CONNECTED;
            //factory.connect(uc);
            if (timer == null) {
            	timer = new Timer();
            	timer.schedule(new LoginTask(), config.getTimeout());
            }
            factory.connect(uc);
            if (!factory.isCompatible()) 
            	return INCOMPATIBLE;
            if (factory.isConnected() && connAttempt) {
                //Log success.
                LogMessage msg = new LogMessage();
                msg.println("Logged onto OMERO at: "+uc.getHostName());
                msg.println(uc);
                Logger logger = container.getRegistry().getLogger();
                logger.info(this, msg);
                timer.cancel();
                return CONNECTED;
            }
            if (!connAttempt) {
                timer.cancel();
                //Log success.
                LogMessage msg = new LogMessage();
                msg.println("Cannot connect OMERO at: "+uc.getHostName());
                msg.println(uc);
                Logger logger = container.getRegistry().getLogger();
                logger.info(this, msg);
                return TIMEOUT;
            }
            timer.cancel();
            LogMessage msg = new LogMessage();
            msg.println("Cannot connect OMERO at: "+uc.getHostName());
            msg.println(uc);
            Logger logger = container.getRegistry().getLogger();
            logger.info(this, msg);
            return NOT_CONNECTED;
        } catch (Exception exception) {  //Log failure.
            if (exception instanceof DSOutOfServiceException) {
                Throwable cause = exception.getCause();
                if (cause instanceof ConnectionRefusedException) {
                    failureIndex = CONNECTION_INDEX;
                } else if (cause instanceof DNSException) {
                    failureIndex = DNS_INDEX;
                } else if (cause instanceof PermissionDeniedException) {
                    failureIndex = PERMISSION_INDEX;
                } else if (cause instanceof Ice.FileException) {
                    failureIndex = CONFIGURATION_INDEX;
                } else if (cause instanceof DSOutOfServiceException) {
                    if (cause.getCause() instanceof SecurityViolation)
                        failureIndex = ACTIVE_INDEX;
                }
            } else failureIndex = SYSTEM_FAILURE_INDEX;
            LogMessage msg = new LogMessage();
            msg.println("Failed to log onto OMERO.");
            msg.println("Reason: "+exception.getMessage());
            if (uc != null) {
                msg.println("OMERO address: "+uc.getHostName());
                msg.println(uc);
            }
            msg.print(exception);
            Logger logger = container.getRegistry().getLogger();
            logger.debug(this, msg);
        }
        return NOT_CONNECTED;
    }

    /**
     * Brings up a login dialog to let the user enter their credentials.
     * The dialog will then call the <code>login</code> method passing
     * along the new user's credentials.
     */
    protected void askForCredentials()
    {
    }
    //NOTE: This method is protected so that subclasses can get rid of the
    //dependencies on Swing.  This is useful in test mode.
    
    /**
     * Creates a new instance.
     * It is assumed that the Container's configuration has already been read
     * in and that the Event Bus is available.
     * 
     * @param c Reference to the runtime environment.
     *          Mustn't be <code>null</code>.
     */
    public LoginServiceImpl(Container c)
    {
        if (c == null) throw new NullPointerException("No container.");
        config = new LoginConfig(c.getRegistry());
        this.container = c;
        EventBus bus = c.getRegistry().getEventBus();
        bus.register(this, ServiceActivationRequest.class);
        state = IDLE;
        connAttempt = true;
        failureIndex = 0;
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#getState()
     */
    public int getState() { return state; }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#login()
     */
    public void login()
    {
        state = ATTEMPTING_LOGIN;
        int max = config.getMaxRetry();
         while (0 < max--) {
            if (attempt() == CONNECTED) {
                state = IDLE;
                return;
            }
            pause();
        }
        askForCredentials();
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#login(UserCredentials)
     */
    public int login(UserCredentials uc)
    {
        if (uc == null) return NOT_CONNECTED;
        String name = uc.getUserName();
        if (CommonsLangUtils.isBlank(name)) return NOT_CONNECTED;

        state = ATTEMPTING_LOGIN;
        config.setCredentials(uc);
        int succeeded = attempt();
        state = IDLE;
        if (succeeded == CONNECTED)
        	container.getRegistry().bind(LookupNames.USER_CREDENTIALS, uc);
        return succeeded;
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#eventFired(AgentEvent)
     */
    public void eventFired(AgentEvent ae)
    {
        //Can only be a ServiceActivationRequest, as we only registered 
        //for that kind of event.  Check if we're connected and reply.
        ServiceActivationRequest sar = (ServiceActivationRequest) ae;
        boolean connected = false;
        try {
            DataServicesFactory factory = DataServicesFactory.getInstance(
                                                                    container);
            connected = factory.isConnected();
        } catch (DSOutOfServiceException dsose) {} 
        ServiceActivationResponse resp = new ServiceActivationResponse(
                                                                sar, connected); 
        EventBus bus = container.getRegistry().getEventBus();
        bus.post(resp);
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#notifyLoginFailure()
     */
    public void notifyLoginFailure()
    {
        JFrame f = container.getRegistry().getTaskBar().getFrame();
        String text = "";
        switch (failureIndex) {
        case LoginService.DNS_INDEX:
            text = "Please check the server address\nor try again later.";
            break;
        case LoginService.CONNECTION_INDEX:
            text = "Please check the port\nor try again later.";
            break;
        case LoginService.ACTIVE_INDEX:
            text = "Your user account is no longer active.\nPlease" +
                    " contact your administrator.";
            break;
        case LoginService.CONFIGURATION_INDEX:
            text = "Please unset ICE_CONFIG.";
            break;
        case LoginService.SYSTEM_FAILURE_INDEX:
            text = "Error: System Failure.";
            break;
        case LoginService.PERMISSION_INDEX:
        default:
            text = "Please check your user name\nand/or password " +
                    "or try again later.";
        }
        NotificationDialog dialog = new NotificationDialog(
                f, "Login Failure", "Failed to log onto OMERO.\n"+text,
                IconManager.getDefaultErrorIcon());
        dialog.pack();  
        UIUtilities.centerAndShow(dialog);
    }

    /**
     * Implemented as specified by the {@link LoginService} interface.
     * @see LoginService#notifyLoginTimeout()
     */
    public void notifyLoginTimeout()
    {
        JFrame f = container.getRegistry().getTaskBar().getFrame();
        //Need to do it that way to keep focus on login dialog
        NotificationDialog dialog = new NotificationDialog(
                f, "Login Failure", "Failed to log onto OMERO.\n" +
                "The server entered is not responding.\n"+
                "Please check the server address or try again later.",
                IconManager.getDefaultErrorIcon());
        dialog.pack();
        UIUtilities.centerAndShow(dialog);
    }

    /** Helper inner class. */
    class LoginTask 
    extends TimerTask {

        /** 
         * Sets the {@link #connAttempt} flag.
         * @see TimerTask#run()
         */
        public void run() {
            Toolkit.getDefaultToolkit().beep();
            connAttempt = false;
            timer.cancel();

        }
    }

}
